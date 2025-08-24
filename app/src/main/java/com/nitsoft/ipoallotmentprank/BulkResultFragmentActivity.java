package com.nitsoft.ipoallotmentprank;

import static android.content.Context.MODE_PRIVATE;

import androidx.annotation.*;

import android.os.*;
import android.util.Log;
import android.view.*;
import android.widget.*;
import android.content.*;

import java.util.HashMap;
import java.util.ArrayList;

import android.widget.LinearLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Spinner;
import android.widget.Button;

import android.view.View;
import android.widget.AdapterView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
//import com.suke.widget.*;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.firebase.database.*;

public class BulkResultFragmentActivity extends Fragment {

    private TextView textview_app_title;
    private TextView textview_app_subtitle;
    private LinearLayout parent_linear_bottom_nav;

    private LinearLayout linear_result_bulk;
    private LinearLayout linear_result_bulk_window;
    private ImageView imageview_result_bulk_logo;
    private TextView textview_result_bulk_prompt;
    private LinearLayout linear_result_bulk_select_company;
    private Spinner spinner_result_bulk_select_company;
    private LinearLayout linear_result_bulk_select_company_placement;
    private TextView textview_result_bulk_select_company_placement;
    private Button button_result_bulk_check;
    private LinearLayout linear_empty_bulk_boids;
    private TextView textview_empty_title;
    private TextView textview_empty_boids;
    private RecyclerView recycler_result_bulk_boids;

    // Request network url
    private final String requestCompanyUrl = "https://iporesult.cdsc.com.np/result/companyShares/fileUploaded";
    private final String requestResultUrl = "https://iporesult.cdsc.com.np/result/result/check";
    private final String requestCaptchaReloadUrl = "https://iporesult.cdsc.com.np/result/captcha/reload/";
    private final String requestBackendUrl = "https://api.niteshsapkota.com/api/v1/request-captcha-process";

    private final HashMap<String, Object> headers = new HashMap<>();
    private ArrayList<HashMap<String, Object>> listmapCompanyDetails = new ArrayList<>();
    private boolean realCompanyLoaded = false;
    private int selectedCompanyIndex = 0;


    // Firebase database references
    private DatabaseReference freshCaptchaListReferecnce;
    private DatabaseReference holdCaptchaListReferecnce;
    private DatabaseReference usedCaptchaListReference;

    // Response variables from company list request
    private String responseCompanyJson = "";
    private String responseUnsolvedCaptchaIdentifier = "";
    private String responseUnsolvedCaptchaBaseUrl = "";

    // Firebase database captcha variables
    private String dbCaptchaIdentifier = "";
    private String dbCaptchaPushKey = "";
    private String dbCaptchaSavedBy = "";
    private String dbCaptchaSavedTimestamp = "";
    private String dbCaptchaUsage = "";
    private String dbCaptchaUsedBy = "";
    private String dbCaptchaUsedTimestamp = "";
    private String dbCaptchaValue = "";

    // Request network
    private RequestNetwork requestCompany;
    private RequestNetwork.RequestListener requestCompanyListener;
    private RequestNetwork requestResult;                        // one-at-a-time sequential
    private RequestNetwork.RequestListener requestResultListener;
    private RequestNetwork requestCaptchaReload;
    private RequestNetwork.RequestListener requestCaptchaReloadListener;
    private RequestNetwork requestBackend;
    private RequestNetwork.RequestListener requestBackendListener;

    private SharedPreferences sharedPreferencesBoidList;
    private ArrayList<HashMap<String, Object>> listmapBoidList = new ArrayList<>();

    // Adapters
    private BulkResultBoidListAdapter adapter;

    // Bulk run state
    private final Handler handler = new Handler(Looper.getMainLooper());
    private static final long REQUEST_DELAY_MS = 1500; // tweak as needed
    private boolean isRunning = false;
    private int currentBoidIndex = 0;

    private Boolean companyChanged = false;

    // Current params for result
    private String currentCompanyId = "";
    private String currentBoid = "";
    private String currentCaptchaValue = "";
    private String currentCaptchaIdentifier = "";
    private String currentCaptchaPushKey = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bulk_result, container, false);
        com.google.firebase.FirebaseApp.initializeApp(getContext());
        initializeViews(view);
        initializeListeners();
        onActivityCreate();
        return view;
    }
    private void initializeViews(View view) {
        textview_app_title = requireActivity().findViewById(R.id.textview_app_title);
        textview_app_subtitle = requireActivity().findViewById(R.id.textview_app_subtitle);
        parent_linear_bottom_nav = requireActivity().findViewById(R.id.parent_linear_bottom_nav);

        linear_result_bulk = view.findViewById(R.id.linear_result_bulk);
        linear_result_bulk_window = view.findViewById(R.id.linear_result_bulk_window);
        imageview_result_bulk_logo = view.findViewById(R.id.imageview_result_bulk_logo);
        textview_result_bulk_prompt = view.findViewById(R.id.textview_result_bulk_prompt);
        linear_result_bulk_select_company = view.findViewById(R.id.linear_result_bulk_select_company);
        spinner_result_bulk_select_company = view.findViewById(R.id.spinner_result_bulk_select_company);
        linear_result_bulk_select_company_placement = view.findViewById(R.id.linear_result_bulk_select_company_placement);
        textview_result_bulk_select_company_placement = view.findViewById(R.id.textview_result_bulk_select_company_placement);
        button_result_bulk_check = view.findViewById(R.id.button_result_bulk_check);
        linear_empty_bulk_boids = view.findViewById(R.id.linear_empty_bulk_boids);
        textview_empty_title = view.findViewById(R.id.textview_empty_title);
        textview_empty_boids = view.findViewById(R.id.textview_empty_boids);
        recycler_result_bulk_boids = view.findViewById(R.id.recycler_result_bulk_boids);

        sharedPreferencesBoidList = getActivity().getSharedPreferences("boidList", MODE_PRIVATE);
        freshCaptchaListReferecnce = FirebaseDatabase.getInstance().getReference("freshCaptchaList");
        holdCaptchaListReferecnce = FirebaseDatabase.getInstance().getReference("holdCaptchaList");
        usedCaptchaListReference = FirebaseDatabase.getInstance().getReference("usedCaptchaList");

        requestCompany = new RequestNetwork(getActivity());
        requestResult = new RequestNetwork(getActivity());
        requestCaptchaReload = new RequestNetwork(getActivity());
        requestBackend = new RequestNetwork(getActivity());
    }
    private void initializeListeners() {
        // Company list listener
        requestCompanyListener = new RequestNetwork.RequestListener() {
            @Override public void onResponse(String tag, String response, HashMap<String, Object> rh) {
                // Company list has been loaded
                realCompanyLoaded = true;
                responseCompanyJson = response;
                parseCompanyListResponse();
                linear_result_bulk_select_company_placement.setVisibility(View.GONE);
                linear_result_bulk_select_company.setVisibility(View.VISIBLE);
                spinner_result_bulk_select_company.setAdapter(new CompanyAdapter(listmapCompanyDetails));
            }
            @Override public void onErrorResponse(String tag, String msg) {
                Utilities.showSnackbar(parent_linear_bottom_nav, "", "Failed to load companies.");
            }
        };
        // single result listener (used sequentially)
        requestResultListener = new RequestNetwork.RequestListener() {
            @Override public void onResponse(String tag, String response, HashMap<String, Object> h) {

                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String message = jsonObject.getString("message");

                    HashMap<String, Object> row = listmapBoidList.get(currentBoidIndex);
                    row.put("status", "done");
                    row.put("message", message);
                    row.put("color", message.toLowerCase().contains("allot") ? "green" : "red");
                    adapter.notifyItemChanged(currentBoidIndex);

                } catch ( JSONException e) {
                    e.printStackTrace();

                    HashMap<String, Object> row = listmapBoidList.get(currentBoidIndex);
                    row.put("status", "done");
                    row.put("message", e);
                    row.put("color", "red");
                    adapter.notifyItemChanged(currentBoidIndex);
                }

                // rotate captcha for next calls
                requestToCaptchaReload();
                moveCaptchaFromHoldToUsed();
                fetchFreshCaptchaFromDatabase();
                currentBoidIndex++;
                if (currentBoidIndex < listmapBoidList.size()) {
                    handler.postDelayed(() -> checkBulkResult(), REQUEST_DELAY_MS);
                } else {
                    isRunning = false;
                }
            }

            @Override public void onErrorResponse(String tag, String message) {
                HashMap<String, Object> row = listmapBoidList.get(currentBoidIndex);
                row.put("status","error");
                row.put("message","2");
                row.put("color","red");
                adapter.notifyItemChanged(currentBoidIndex);

                // rotate captcha for next calls
                requestToCaptchaReload();
                moveCaptchaFromHoldToUsed();
                fetchFreshCaptchaFromDatabase();
                currentBoidIndex++;
                if (currentBoidIndex < listmapBoidList.size()) {
                    handler.postDelayed(() -> checkBulkResult(), REQUEST_DELAY_MS);
                } else {
                    isRunning = false;
                }
            }
        };

        requestCaptchaReloadListener = new RequestNetwork.RequestListener() {
            @Override public void onResponse(String tag, String response, HashMap<String, Object> headers) {
                try {
                    JSONObject json = new JSONObject(response);
                    JSONObject body = json.getJSONObject("body");
                    responseUnsolvedCaptchaBaseUrl = body.getString("captcha");
                    responseUnsolvedCaptchaIdentifier = body.getString("captchaIdentifier");

                    // Also send the captcha for processing to the backend
                    sendCaptchaToBackendForProcessing();
                } catch (Exception ignore) {}
            }
            @Override public void onErrorResponse(String tag, String message) {}
        };

        requestBackendListener = new RequestNetwork.RequestListener() {
            @Override public void onResponse(String tag, String response, HashMap<String, Object> headers) {}
            @Override public void onErrorResponse(String tag, String message) {}
        };

        spinner_result_bulk_select_company.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int i, long l) {
                selectedCompanyIndex = i;
                if (!listmapCompanyDetails.isEmpty()) {
                    currentCompanyId = String.valueOf(listmapCompanyDetails.get(i).get("id"));

                    // 1) Clear previous results so UI knows to hide
                    if (!listmapBoidList.isEmpty()){
                        for (HashMap<String, Object> row : listmapBoidList) {
                            row.put("message", "");     // or remove("message")
                            row.put("status", "");      // optional
                        }

                        // 2) Refresh list
                        adapter.notifyDataSetChanged();
                    }
                }
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });


        button_result_bulk_check.setOnClickListener(v -> {
            if (isRunning) return;
            if (listmapCompanyDetails.isEmpty()) {
                Utilities.showSnackbar(parent_linear_bottom_nav, "", "Select a company first.");
                return;
            }
            if (listmapBoidList.isEmpty()) {
                Utilities.showSnackbar(parent_linear_bottom_nav, "", "No BOIDs to check.");
                return;
            }
            checkBulkResult();
            button_result_bulk_check.setEnabled(false);
        });
    }

    private void onActivityCreate(){
        userInterface();
        fetchFreshCaptchaFromDatabase();
        fetchExistingBoidList();
        if (listmapBoidList.isEmpty()) { showEmpty(); } else { showList(); }
        // Check if real company is loaded or not yet, if not then fetch it
        if (!realCompanyLoaded) {
            // Real headers (same as single)
            headers.put("Accept", "application/json, text/plain, */*");
            headers.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/138.0.0.0 Safari/537.36");
            headers.put("Referer", "https://iporesult.cdsc.com.np/");
            headers.put("Host", "iporesult.cdsc.com.np");
            headers.put("Accept-Language", "en-US,en;q=0.7");
            requestCompany.setHeaders(headers);
            requestCompany.startRequestNetwork(RequestNetworkController.GET, requestCompanyUrl, "company", requestCompanyListener);
        }
    }
    private void userInterface() {
        textview_app_title.setText("IPO Allotment Result");
        textview_app_subtitle.setText("Helps you check multiple results at once!");

        // Default visibility settings when activity is created
        linear_result_bulk_select_company.setVisibility(View.GONE);
        linear_result_bulk_select_company_placement.setVisibility(View.VISIBLE);

        // Set background properties
        Utilities.setBackground(linear_result_bulk_window, 15, 0, "#333A56", false);
        Utilities.setBackground(linear_result_bulk_select_company, 15, 0, "#FFFFFF", false);
        Utilities.setBackground(linear_result_bulk_select_company_placement, 15, 0, "#FFFFFF", false);
        Utilities.setBackground(button_result_bulk_check, 15, 0, "#4f5b8b", true);
    }
    private void checkBulkResult() {
        if (listmapBoidList == null || listmapBoidList.isEmpty()) return;

        if (currentBoidIndex >= listmapBoidList.size()) {
            isRunning = false; // finished
            button_result_bulk_check.setEnabled(true);
            Utilities.showSnackbar(parent_linear_bottom_nav, "", "Bulk check finished.");
            return;
        }
        isRunning = true;
        HashMap<String, Object> row = listmapBoidList.get(currentBoidIndex);

        // UI: mark running
        row.put("status", "running");
        adapter.notifyItemChanged(currentBoidIndex);

        // Build request
        requestResult.setHeaders(headers);
        HashMap<String, Object> params = new HashMap<>();
        params.put("captchaIdentifier", currentCaptchaIdentifier);
        params.put("userCaptcha", currentCaptchaValue);
        params.put("companyShareId", currentCompanyId);
        params.put("boid", String.valueOf(row.get("boid")));
        requestResult.setParams(params, RequestNetworkController.REQUEST_BODY);

        // Fire request with listener bound to this idx
        requestResult.startRequestNetwork(RequestNetworkController.POST, requestResultUrl, "result_" + currentBoidIndex, requestResultListener);
    }

    private void fetchFreshCaptchaFromDatabase() {
        Query fetchOne = freshCaptchaListReferecnce.limitToFirst(1); // only 1 captcha

        fetchOne.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String key = snapshot.getKey();

                    // Fetch all of its attributes from database and put them into variables
                    dbCaptchaIdentifier = snapshot.child("captchaIdentifier").getValue(String.class);
                    dbCaptchaPushKey = snapshot.child("captchaPushKey").getValue(String.class);
                    dbCaptchaSavedBy = snapshot.child("captchaSavedBy").getValue(String.class);
                    dbCaptchaSavedTimestamp = snapshot.child("captchaSavedTimestamp").getValue(String.class);
                    dbCaptchaUsage = snapshot.child("captchaUsage").getValue(String.class);
                    dbCaptchaUsedBy = snapshot.child("captchaUsedBy").getValue(String.class);
                    dbCaptchaUsedTimestamp = snapshot.child("captchaUsedTimestamp").getValue(String.class);
                    dbCaptchaValue = snapshot.child("captchaValue").getValue(String.class);

                    // Also put that freshCaptcha into 'holdCaptchaList' until its used
                    holdCaptchaListReferecnce.child(dbCaptchaPushKey).setValue(snapshot.getValue());
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("captchaIdentifier", dbCaptchaIdentifier);
                    map.put("captchaPushKey", dbCaptchaPushKey);
                    map.put("captchaSavedBy", dbCaptchaSavedBy);
                    map.put("captchaSavedTimestamp", dbCaptchaSavedTimestamp);
                    map.put("captchaUsage", dbCaptchaUsage);
                    map.put("captchaUsedBy", dbCaptchaUsedBy);
                    map.put("captchaHoldTimestamp", new java.text.SimpleDateFormat("HH:mm:ss, dd MMM yyyy").format(new java.util.Date()));
                    map.put("captchaUsedTimestamp", dbCaptchaUsedTimestamp);
                    map.put("captchaValue", dbCaptchaValue);
                    holdCaptchaListReferecnce.child(dbCaptchaPushKey).setValue(map);

                    // Instantly remove that captcha from 'freshCaptchaList'
                    freshCaptchaListReferecnce.child(key).removeValue();

                    currentCaptchaValue = dbCaptchaValue;
                    currentCaptchaIdentifier = dbCaptchaIdentifier;
                    currentCaptchaPushKey = dbCaptchaPushKey;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("Firebase", "Error fetching captcha: " + databaseError.getMessage());
            }
        });

    }
    private void fetchExistingBoidList() {
        // Check if there are existing boid entries in SharedPreferences
        if(sharedPreferencesBoidList.getString("boidList", "").isEmpty() || sharedPreferencesBoidList.getString("boidList", "").equals("[]")){
            // There are no existing boid entries, show no data view
            showEmpty();

        } else if(!sharedPreferencesBoidList.getString("boidList", "").isEmpty()){
            // There are existing boid entries, fetch them into listmapBoidList
            showList();
            String jsonBoidList = sharedPreferencesBoidList.getString("boidList", "");
            listmapBoidList = new Gson().fromJson(jsonBoidList, new TypeToken<ArrayList<HashMap<String, Object>>>(){}.getType());

            // Set up the RecyclerView adapter with the listmapBoidList
            adapter = new BulkResultBoidListAdapter(listmapBoidList);
            recycler_result_bulk_boids.setLayoutManager(new LinearLayoutManager(getContext()));
            recycler_result_bulk_boids.setAdapter(adapter);
            adapter.notifyDataSetChanged();


        }
    }
    private void parseCompanyListResponse() {
        try {
            JSONObject responseObject = new JSONObject(responseCompanyJson);
            if (responseObject.getBoolean("success")) {
                JSONObject body = responseObject.getJSONObject("body");

                JSONObject captchaData = body.getJSONObject("captchaData");
                responseUnsolvedCaptchaIdentifier = captchaData.getString("captchaIdentifier");
                responseUnsolvedCaptchaBaseUrl = captchaData.getString("captcha");

                // Clear previous data to avoid duplicates
                listmapCompanyDetails.clear();

                JSONArray companyShareList = body.getJSONArray("companyShareList");
                for (int i = 0; i < companyShareList.length(); i++) {
                    JSONObject company = companyShareList.getJSONObject(i);

                    HashMap<String, Object> map = new HashMap<>();
                    map.put("name", company.getString("name"));
                    map.put("id", company.getInt("id"));
                    map.put("scrip", company.getString("scrip"));
                    map.put("isFileUploaded", company.getBoolean("isFileUploaded"));
                    listmapCompanyDetails.add(map);
                }

                sendCaptchaToBackendForProcessing();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    private void sendCaptchaToBackendForProcessing(){
        HashMap<String, Object> headers = new HashMap<>();
        headers.put("X-API-KEY", "E8822B1A637C1965A88DC18CAA9D8");
        requestBackend.setHeaders(headers);
        HashMap<String, Object> params = new HashMap<>();
        params.put("captchaIdentifier", responseUnsolvedCaptchaIdentifier);
        params.put("captcha", responseUnsolvedCaptchaBaseUrl);

        // For attribution, we use current row’s BOID when available
        if (currentBoid != null && currentBoid.length() == 16 && currentBoid.startsWith("130")) {
            params.put("captchaSavedBy", currentBoid);
        } else {
            params.put("captchaSavedBy", "bulkDefault");
        }
        requestBackend.setParams(params, RequestNetworkController.REQUEST_BODY);
        requestBackend.startRequestNetwork(RequestNetworkController.POST, requestBackendUrl, "backendBulk", requestBackendListener);
    }

    private void showEmpty() {
        linear_empty_bulk_boids.setVisibility(View.VISIBLE);
        recycler_result_bulk_boids.setVisibility(View.GONE);
    }
    private void showList() {
        linear_empty_bulk_boids.setVisibility(View.GONE);
        recycler_result_bulk_boids.setVisibility(View.VISIBLE);

        ArrayList<HashMap<String, Object>> decorated = new ArrayList<>();
        for (HashMap<String, Object> m : listmapBoidList) {
            HashMap<String, Object> r = new HashMap<>(m);
            r.put("status", "idle");     // idle | running | done | error
            r.put("message", "");
            r.put("color", "white");
            decorated.add(r);
        }
        listmapBoidList.clear();
        listmapBoidList.addAll(decorated);
    }


    private void requestToCaptchaReload() {
        requestCaptchaReload.setHeaders(headers);
        HashMap<String, Object> params = new HashMap<>();
        params.put("captchaIdentifier", responseUnsolvedCaptchaIdentifier);
        requestCaptchaReload.setParams(params, RequestNetworkController.REQUEST_BODY);
        requestCaptchaReload.startRequestNetwork(RequestNetworkController.POST, requestCaptchaReloadUrl + responseUnsolvedCaptchaIdentifier, "captchaReloadBulk", requestCaptchaReloadListener);
    }
    private void moveCaptchaFromHoldToUsed() {
        String usedCaptchaPushKey = freshCaptchaListReferecnce.push().getKey();
        HashMap<String, Object> usedCaptchaEntry = new HashMap<>();
        usedCaptchaEntry.put("captchaIdentifier", dbCaptchaIdentifier);
        usedCaptchaEntry.put("captchaPushKey", usedCaptchaPushKey);
        usedCaptchaEntry.put("captchaSavedBy", dbCaptchaSavedBy);
        usedCaptchaEntry.put("captchaSavedTimestamp", dbCaptchaSavedTimestamp);
        usedCaptchaEntry.put("captchaUsage", "true");
        // we record BOID that consumed this captcha
        String boid = (currentBoidIndex < listmapBoidList.size()) ? String.valueOf(listmapBoidList.get(currentBoidIndex).get("boid")) : "";
        usedCaptchaEntry.put("captchaUsedBy", boid);
        usedCaptchaEntry.put("captchaUsedTimestamp", new java.text.SimpleDateFormat("HH:mm:ss, dd MMM yyyy").format(new java.util.Date()));
        usedCaptchaEntry.put("captchaValue", dbCaptchaValue);

        usedCaptchaListReference.child(usedCaptchaPushKey).setValue(usedCaptchaEntry);
        holdCaptchaListReferecnce.child(dbCaptchaPushKey).removeValue();
    }

    // --- Adapters ---
    private class CompanyAdapter extends BaseAdapter {
        private final ArrayList<HashMap<String, Object>> list;
        CompanyAdapter(ArrayList<HashMap<String, Object>> l) { list = l; }
        @Override public int getCount() { return list.size(); }
        @Override public Object getItem(int position) { return list.get(position); }
        @Override public long getItemId(int position) { return position; }
        @Override public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) convertView = getLayoutInflater().inflate(R.layout.custom_select_company_spinner, parent, false);
            TextView t = convertView.findViewById(R.id.textview_spinner_select_company_custom);
            t.setText(String.valueOf(list.get(position).get("name")));
            return convertView;
        }
    }
    private class BulkResultBoidListAdapter extends RecyclerView.Adapter<BulkResultBoidListAdapter.ViewHolder> {

        private ArrayList<HashMap<String, Object>> listmapBoidList;
        BulkResultBoidListAdapter(ArrayList<HashMap<String, Object>> listmapBoidList) { this.listmapBoidList = listmapBoidList; }

        @NonNull
        @Override
        public BulkResultBoidListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.boid_listview, parent, false);
            RecyclerView.LayoutParams layoutParams = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            view.setLayoutParams(layoutParams);
            return new BulkResultBoidListAdapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull BulkResultBoidListAdapter.ViewHolder holder, int position) {
            View view = holder.itemView;
            LinearLayout linear_item = view.findViewById(R.id.linear_item);
            TextView textview_boid_count = view.findViewById(R.id.textview_boid_count);
            LinearLayout linear_boid_details = view.findViewById(R.id.linear_boid_details);
            ProgressBar bulk_result_progress_bar =  view.findViewById(R.id.bulk_result_progress_bar);
            TextView textview_name = view.findViewById(R.id.textview_name);
            TextView textview_boid = view.findViewById(R.id.textview_boid);
            TextView textview_detail = view.findViewById(R.id.textview_detail);
            LinearLayout linear_actions = view.findViewById(R.id.linear_actions);
            ImageView imageview_actions = view.findViewById(R.id.imageview_actions);

            // Setting default UI
            bulk_result_progress_bar.setVisibility(View.VISIBLE);
            textview_boid_count.setVisibility(View.GONE);
            imageview_actions.setImageResource(R.drawable.ic_more_vert_white);
            if (companyChanged) {
                textview_detail.setVisibility(View.GONE);
                companyChanged = false;
            }

            // Result logic
            HashMap<String, Object> map = listmapBoidList.get(position);
            textview_name.setText(String.valueOf(map.get("name")));
            textview_boid.setText(String.valueOf(map.get("boid")));
            String status = String.valueOf(map.get("status"));
            String msg = String.valueOf(map.get("message"));
            textview_detail.setText(msg);

            if (msg.isEmpty()) {
                textview_detail.setVisibility(View.GONE);
                textview_detail.setText(""); // avoid showing stale text
            } else {
                textview_detail.setVisibility(View.VISIBLE);
                textview_detail.setText(msg);
            }

            switch (status) {
                case "running":
                    bulk_result_progress_bar.setVisibility(View.VISIBLE);
                    textview_detail.setVisibility(View.GONE);
                    break;
                case "done":
                    textview_boid_count.setText(String.valueOf(position + 1));
                    textview_boid_count.setVisibility(View.VISIBLE);
                    bulk_result_progress_bar.setVisibility(View.GONE);
                    textview_detail.setVisibility(View.VISIBLE);
                    break;
                case "error":
                    textview_boid_count.setText("!");
                    textview_boid_count.setVisibility(View.VISIBLE);
                    bulk_result_progress_bar.setVisibility(View.GONE);
                    textview_detail.setVisibility(View.VISIBLE);
                    break;
                default:
                    textview_boid_count.setVisibility(View.VISIBLE);
                    textview_boid_count.setText(String.valueOf(position + 1));
                    bulk_result_progress_bar.setVisibility(View.GONE);
                    textview_detail.setVisibility(View.GONE);
            }

            // colour
            String color = String.valueOf(map.get("color"));
            int colorInt;
            if ("green".equals(color)) colorInt = getResources().getColor(R.color.result_green);
            else if ("red".equals(color)) colorInt = getResources().getColor(R.color.result_red);
            else colorInt = getResources().getColor(android.R.color.white);
            textview_detail.setTextColor(colorInt);

            // User Interface
            imageview_actions.setVisibility(View.GONE);
            Utilities.setBackground(linear_item, 15, 0, "#333A56", true);
        }

        @Override
        public int getItemCount() {
            return listmapBoidList.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
            }
        }
    }
}