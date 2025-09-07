package com.nitsoft.ipoallotmentprank;

import static android.content.Context.MODE_PRIVATE;

import androidx.annotation.*;
import androidx.core.content.ContextCompat; // ← for safe color access

import android.os.*;
import android.util.Log;
import android.view.*;
import android.widget.*;
import android.content.*;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;

import android.widget.LinearLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Spinner;
import android.widget.Button;

import android.view.View;
import android.widget.AdapterView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

// Native Ad imports
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.nativead.NativeAdView;

// Reward Ad imports
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.AdError;

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

    // Summary section view
    private TextView textview_summary;

    // Request network url
    private final String requestCompanyUrl = "https://iporesult.cdsc.com.np/result/companyShares/fileUploaded";
    private final String requestResultUrl = "https://iporesult.cdsc.com.np/result/result/check";

    private final HashMap<String, Object> headers = new HashMap<>();
    private ArrayList<HashMap<String, Object>> listmapCompanyDetails = new ArrayList<>();
    private boolean realCompanyLoaded = false;
    private int selectedCompanyIndex = 0;

    // Response variables from company list request
    private String responseCompanyJson = "";
    private String responseUnsolvedCaptchaIdentifier = "";
    private String responseUnsolvedCaptchaBaseUrl = "";

    // Request network
    private RequestNetwork requestCompany;
    private RequestNetwork.RequestListener requestCompanyListener;
    private RequestNetwork requestResult;                        // one-at-a-time sequential
    private RequestNetwork.RequestListener requestResultListener;

    private RequestNetwork requestAntiCaptcha;
    private RequestNetwork.RequestListener requestAntiCaptchaListener;

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

    // --- Anti-Captcha API bits ---
    private static final String ANTICAPTCHA_API_KEY = "84b260cf61e60d0bf15c802b3f8ceb64";
    private static final String ANTICAPTCHA_CREATE_TASK_URL = "https://api.anti-captcha.com/createTask";
    private static final String ANTICAPTCHA_GET_RESULT_URL = "https://api.anti-captcha.com/getTaskResult";
    private static final int CAPTCHA_POLL_INTERVAL_MS = 7000; // 7 seconds

    // --- Simplified Captcha Management ---
    private List<CaptchaData> captchaDataList = new ArrayList<>();

    // --- Native Ad bits ---
    private NativeAd nativeAd;             // keep a reference to destroy later
    private boolean isAdLoaded = false;    // gate for adapter to insert ad row
    private static final int AD_INTERVAL = 3; // insert ad after every 3 items

    // --- Reward Ad bits ---
    private static final String REWARD_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"; // Test ad unit ID
    // private static final String REWARD_AD_UNIT_ID = "ca-app-pub-7184690369277704/4361533678"; // Real ad unit ID
    private RewardedAd rewardedAd;
    private boolean isRewardAdLoaded = false;
    private boolean isRewardGranted = false; // Track if user earned reward
    
    // Anti-spam and failure handling
    private boolean isAdLoading = false;
    private boolean isAdShowing = false;
    private long lastAdClickTime = 0;
    private static final long MIN_CLICK_INTERVAL_MS = 2000; // 2 seconds between clicks
    private static final int MAX_LOAD_RETRIES = 3;
    private int currentLoadRetries = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bulk_result, container, false);
        initializeViews(view);
        initializeListeners();
        onActivityCreate();
        return view;
    }

    @Override
    public void onDestroyView() {
        if (nativeAd != null) nativeAd.destroy();
        nativeAd = null;
        isAdLoaded = false;
        showBannerAd();
        super.onDestroyView();
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

        textview_summary = view.findViewById(R.id.textview_summary);

        sharedPreferencesBoidList = getActivity().getSharedPreferences("boidList", MODE_PRIVATE);

        requestCompany = new RequestNetwork(getActivity());
        requestResult = new RequestNetwork(getActivity());
        requestAntiCaptcha = new RequestNetwork(getActivity());
    }

    private void initializeListeners() {
        // Company list listener
        requestCompanyListener = new RequestNetwork.RequestListener() {
            @Override public void onResponse(String tag, String response, HashMap<String, Object> rh) {
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

        // Final-result-only listener
        requestResultListener = new RequestNetwork.RequestListener() {
            @Override public void onResponse(String tag, String response, HashMap<String, Object> h) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String message = jsonObject.optString("message", "");

                    HashMap<String, Object> row = listmapBoidList.get(currentBoidIndex);

                    // FINAL results only — no intermediate UI updates here.
                    String lower = message.toLowerCase();
                    if (lower.contains("invalid captcha")) {
                        // Silent retry: keep row running & blank.
                        Log.d("AntiCaptcha", "Invalid captcha for index " + currentBoidIndex + " → retrying");
                        setRunningSilent(currentBoidIndex);
                        getNewCaptchaAndRetry(currentBoidIndex);
                        return; // don't advance index
                    } else if (lower.contains("congratulation")) {
                        setFinalResult(currentBoidIndex, message, "green");
                    } else if (lower.contains("sorry")) {
                        setFinalResult(currentBoidIndex, message, "red");
                    } else {
                        // Other terminal server messages
                        setFinalResult(currentBoidIndex, message, "blue");
                    }

                } catch (JSONException e) {
                    setFinalError(currentBoidIndex, "Parse error");
                }

                // Advance AFTER a final state only
                advanceOrFinish();
            }

            @Override public void onErrorResponse(String tag, String message) {
                setFinalError(currentBoidIndex, "Network error");
                advanceOrFinish();
            }
        };

        // Anti-Captcha Listener
        requestAntiCaptchaListener = new RequestNetwork.RequestListener() {
            @Override public void onResponse(String tag, String response, HashMap<String, Object> headers) {
                try {
                    JSONObject jsonResponse = new JSONObject(response);

                    if (tag.startsWith("createTask_")) {
                        String captchaIdentifier = tag.substring("createTask_".length());
                        handleTaskCreationResponse(captchaIdentifier, jsonResponse);

                    } else if (tag.startsWith("checkStatus_")) {
                        String captchaIdentifier = tag.substring("checkStatus_".length());
                        handleTaskStatusResponse(captchaIdentifier, jsonResponse);
                    }

                } catch (Exception e) {
                    Log.e("AntiCaptcha", "Parse error: " + e.getMessage());
                }
            }

            @Override public void onErrorResponse(String tag, String message) {
                if (tag.startsWith("createTask_")) {
                    String captchaIdentifier = tag.substring("createTask_".length());
                    handleTaskCreationError(captchaIdentifier);
                } else if (tag.startsWith("checkStatus_")) {
                    String captchaIdentifier = tag.substring("checkStatus_".length());
                    handleStatusCheckError(captchaIdentifier);
                }
            }
        };

        spinner_result_bulk_select_company.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int i, long l) {
                selectedCompanyIndex = i;
                if (!listmapCompanyDetails.isEmpty()) {
                    currentCompanyId = String.valueOf(listmapCompanyDetails.get(i).get("id"));
                    if (!listmapBoidList.isEmpty()) {
                        for (HashMap<String, Object> row : listmapBoidList) {
                            row.put("message", "");
                            row.put("status", "idle");
                            row.put("color", "white");
                        }
                        adapter.notifyDataSetChanged();
                        updateSummaryCounts();
                        // Ensure button text is correct after company change
                        updateButtonText();
                    }
                }
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        button_result_bulk_check.setOnClickListener(v -> {
            // Anti-spam protection
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastAdClickTime < MIN_CLICK_INTERVAL_MS) {
                Utilities.showSnackbar(parent_linear_bottom_nav, "", "Please wait before clicking again");
                return;
            }
            lastAdClickTime = currentTime;
            
            if (isRunning) return;
            if (isAdLoading || isAdShowing) {
                Utilities.showSnackbar(parent_linear_bottom_nav, "", "Ad is already loading or showing");
                return;
            }
            if (listmapCompanyDetails.isEmpty()) {
                Utilities.showSnackbar(parent_linear_bottom_nav, "", "Select a company first.");
                return;
            }
            if (listmapBoidList.isEmpty()) {
                Utilities.showSnackbar(parent_linear_bottom_nav, "", "No BOIDs to check.");
                return;
            }

            // Show reward ad before starting bulk check
            showRewardAdAndStartBulkCheck();
        });

        // Long press to manually reload ad for testing
        button_result_bulk_check.setOnLongClickListener(v -> {
            if (!isRunning && !isAdLoading && !isAdShowing) {
                Utilities.showSnackbar(parent_linear_bottom_nav, "", "Reloading ad");
                currentLoadRetries = 0; // Reset retry counter
                loadRewardAd();
            } else if (isAdLoading) {
                Utilities.showSnackbar(parent_linear_bottom_nav, "", "Ad is already loading");
            } else if (isAdShowing) {
                Utilities.showSnackbar(parent_linear_bottom_nav, "", "Ad is currently showing");
            }
            return true;
        });
    }

    private void onActivityCreate(){
        userInterface();
        fetchExistingBoidList();
        if (listmapBoidList.isEmpty()) { showEmpty(); } else { showList(); }
        updateSummaryCounts();
        updateButtonText(); // Ensure button text is correct on startup
        loadRewardAd(); // Load reward ad on startup

        if (!realCompanyLoaded) {
            headers.put("Accept", "application/json, text/plain, */*");
            headers.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/138.0.0.0 Safari/537.36");
            headers.put("Referer", "https://iporesult.cdsc.com.np/");
            headers.put("Host", "iporesult.cdsc.com.np");
            headers.put("Accept-Language", "en-US,en;q=0.7");
            requestCompany.setHeaders(headers);
            requestCompany.startRequestNetwork(RequestNetworkController.GET, requestCompanyUrl, "company", requestCompanyListener);
        }
        hideBannerAd();
    }

    // --- UI / Summary helpers ---

    private void updateSummaryCounts() {
        if (textview_summary == null) return;

        int total = listmapBoidList.size();
        int checked = 0;
        int alloted = 0;

        for (HashMap<String, Object> item : listmapBoidList) {
            String status = String.valueOf(item.get("status"));
            if ("done".equals(status) || "error".equals(status)) checked++;

            String message = String.valueOf(item.get("message"));
            // Only count as alloted if status is "done" and message contains congratulations
            if ("done".equals(status) && message != null && message.toLowerCase().contains("congratulation")) {
                alloted++;
            }
        }

        textview_summary.setText(String.format("Total: %d     Checked: %d     Alloted: %d", total, checked, alloted));
    }

    private void userInterface() {
        textview_app_title.setText("IPO Allotment Result");
        textview_app_subtitle.setText("Helps you check multiple results at once!");

        linear_result_bulk_select_company.setVisibility(View.GONE);
        linear_result_bulk_select_company_placement.setVisibility(View.VISIBLE);

        Utilities.setBackground(linear_result_bulk_window, 15, 0, "#333A56", false);
        Utilities.setBackground(linear_result_bulk_select_company, 15, 0, "#FFFFFF", false);
        Utilities.setBackground(linear_result_bulk_select_company_placement, 15, 0, "#FFFFFF", false);
        Utilities.setBackground(button_result_bulk_check, 15, 0, "#4f5b8b", true);
        
        // Set button text to show it's for watching ad
        button_result_bulk_check.setText("Watch Ad to Bulk Check");
    }

    // --- FINAL-ONLY row updates ---

    private void setFinalResult(int index, String message, String colorKey) {
        HashMap<String, Object> row = listmapBoidList.get(index);
        row.put("status", "done");
        row.put("message", message);
        row.put("color", colorKey);
        adapter.notifyItemChanged(index);
        updateSummaryCounts();
    }
    private void setFinalError(int index, String message) {
        HashMap<String, Object> row = listmapBoidList.get(index);
        row.put("status", "error");
        row.put("message", message);
        row.put("color", "red");
        adapter.notifyItemChanged(index);
        updateSummaryCounts();
    }
    private void setRunningSilent(int index) {
        HashMap<String, Object> row = listmapBoidList.get(index);
        row.put("status", "running");
        row.put("message", "");      // ← no interim text
        row.put("color", "white");   // not used while running
        adapter.notifyItemChanged(index);
    }
    private void advanceOrFinish() {
        currentBoidIndex++;
        if (currentBoidIndex < listmapBoidList.size()) {
            handler.postDelayed(this::processNextBoid, REQUEST_DELAY_MS);
        } else {
            isRunning = false;
            button_result_bulk_check.setEnabled(true);
            // Update button text back to original
            updateButtonText();
            Utilities.showSnackbar(parent_linear_bottom_nav, "", "Bulk check finished.");
            updateSummaryCounts();
        }
    }

    // --- Anti-Captcha API Methods ---
    private void createAntiCaptchaTask(CaptchaData captchaData) {
        try {
            captchaData.isProcessing = true;

            HashMap<String, Object> antiCaptchaHeaders = new HashMap<>();
            antiCaptchaHeaders.put("Content-Type", "application/json");
            antiCaptchaHeaders.put("Accept", "application/json");
            requestAntiCaptcha.setHeaders(antiCaptchaHeaders);

            HashMap<String, Object> antiCaptchaParams = new HashMap<>();
            antiCaptchaParams.put("clientKey", ANTICAPTCHA_API_KEY);

            HashMap<String, Object> taskMap = new HashMap<>();
            taskMap.put("type", "ImageToTextTask");
            taskMap.put("body", captchaData.captcha);
            taskMap.put("phrase", false);
            taskMap.put("case", false);
            taskMap.put("numeric", false);
            taskMap.put("math", false);
            taskMap.put("minLength", 4);
            taskMap.put("maxLength", 8);

            antiCaptchaParams.put("task", taskMap);
            requestAntiCaptcha.setParams(antiCaptchaParams, RequestNetworkController.REQUEST_BODY);

            requestAntiCaptcha.startRequestNetwork(RequestNetworkController.POST, ANTICAPTCHA_CREATE_TASK_URL,
                    "createTask_" + captchaData.captchaIdentifier, requestAntiCaptchaListener);

        } catch (Exception e) {
            Log.e("AntiCaptcha", "Create task error: " + e.getMessage());
            captchaData.isProcessing = false;
        }
    }
    private void handleTaskCreationResponse(String captchaIdentifier, JSONObject response) {
        try {
            if (response.getInt("errorId") == 0) {
                String taskId = response.getString("taskId");

                for (CaptchaData captchaData : captchaDataList) {
                    if (captchaData != null && captchaData.captchaIdentifier.equals(captchaIdentifier)) {
                        captchaData.taskId = taskId;
                        captchaData.isProcessing = false;
                        startTaskMonitoring(captchaData);
                        break;
                    }
                }
            } else {
                Log.e("AntiCaptcha", "Create task failed: " + response.optString("errorCode"));
            }
        } catch (Exception e) {
            Log.e("AntiCaptcha", "Create task parse: " + e.getMessage());
        }
    }
    private void startTaskMonitoring(CaptchaData captchaData) {
        checkTaskStatus(captchaData);
    }
    private void checkTaskStatus(CaptchaData captchaData) {
        try {
            HashMap<String, Object> antiCaptchaHeaders = new HashMap<>();
            antiCaptchaHeaders.put("Content-Type", "application/json");
            antiCaptchaHeaders.put("Accept", "application/json");
            requestAntiCaptcha.setHeaders(antiCaptchaHeaders);

            HashMap<String, Object> antiCaptchaParams = new HashMap<>();
            antiCaptchaParams.put("clientKey", ANTICAPTCHA_API_KEY);
            antiCaptchaParams.put("taskId", captchaData.taskId);
            requestAntiCaptcha.setParams(antiCaptchaParams, RequestNetworkController.REQUEST_BODY);

            requestAntiCaptcha.startRequestNetwork(RequestNetworkController.POST, ANTICAPTCHA_GET_RESULT_URL,
                    "checkStatus_" + captchaData.captchaIdentifier, requestAntiCaptchaListener);

        } catch (Exception e) {
            Log.e("AntiCaptcha", "Status check error: " + e.getMessage());
        }
    }
    private void handleTaskStatusResponse(String captchaIdentifier, JSONObject response) {
        try {
            if (response.getInt("errorId") == 0) {
                String status = response.getString("status");

                if ("ready".equals(status)) {
                    JSONObject solution = response.getJSONObject("solution");
                    String solvedText = solution.getString("text");

                    for (int i = 0; i < captchaDataList.size(); i++) {
                        CaptchaData captchaData = captchaDataList.get(i);
                        if (captchaData != null && captchaData.captchaIdentifier.equals(captchaIdentifier)) {
                            captchaData.solvedText = solvedText;
                            captchaData.isReady = true;

                            // NO UI text here (no flicker). If this is current, go check result.
                            if (isRunning && currentBoidIndex == i) {
                                checkBoidResult(captchaData);
                            }
                            break;
                        }
                    }
                } else if ("processing".equals(status)) {
                    for (CaptchaData captchaData : captchaDataList) {
                        if (captchaData != null && captchaData.captchaIdentifier.equals(captchaIdentifier)) {
                            handler.postDelayed(() -> checkTaskStatus(captchaData), CAPTCHA_POLL_INTERVAL_MS);
                            break;
                        }
                    }
                }
            } else {
                Log.e("AntiCaptcha", "Status error: " + response.optString("errorCode"));
            }
        } catch (Exception e) {
            Log.e("AntiCaptcha", "Status parse: " + e.getMessage());
        }
    }
    private void handleTaskCreationError(String captchaIdentifier) {
        for (CaptchaData captchaData : captchaDataList) {
            if (captchaData != null && captchaData.captchaIdentifier.equals(captchaIdentifier)) {
                captchaData.isProcessing = false;
                break;
            }
        }
    }
    private void handleStatusCheckError(String captchaIdentifier) {
        for (CaptchaData captchaData : captchaDataList) {
            if (captchaData != null && captchaData.captchaIdentifier.equals(captchaIdentifier)) {
                handler.postDelayed(() -> checkTaskStatus(captchaData), CAPTCHA_POLL_INTERVAL_MS);
                break;
            }
        }
    }
    private void getNewCaptchaAndRetry(int boidIndex) {
        try {
            RequestNetwork captchaRequest = new RequestNetwork(getActivity());
            captchaRequest.setHeaders(headers);
            HashMap<String, Object> params = new HashMap<>();
            params.put("captchaIdentifier", responseUnsolvedCaptchaIdentifier);
            captchaRequest.setParams(params, RequestNetworkController.REQUEST_BODY);

            String reloadUrl = "https://iporesult.cdsc.com.np/result/captcha/reload/" + responseUnsolvedCaptchaIdentifier;
            captchaRequest.startRequestNetwork(RequestNetworkController.POST, reloadUrl, "retryCaptcha_" + boidIndex, new RequestNetwork.RequestListener() {
                @Override public void onResponse(String tag, String response, HashMap<String, Object> headers) {
                    try {
                        JSONObject json = new JSONObject(response);
                        JSONObject body = json.getJSONObject("body");
                        String newCaptchaBaseUrl = body.getString("captcha");
                        String newCaptchaIdentifier = body.getString("captchaIdentifier");

                        CaptchaData captchaData = captchaDataList.get(boidIndex);
                        captchaData.captcha = newCaptchaBaseUrl;
                        captchaData.captchaIdentifier = newCaptchaIdentifier;
                        captchaData.taskId = null;
                        captchaData.solvedText = null;
                        captchaData.isReady = false;
                        captchaData.isProcessing = false;

                        createAntiCaptchaTask(captchaData);
                    } catch (Exception e) {
                        Log.e("AntiCaptcha", "Retry parse: " + e.getMessage());
                        setFinalError(boidIndex, "Captcha retry failed");
                        advanceOrFinish();
                    }
                }
                @Override public void onErrorResponse(String tag, String message) {
                    Log.e("AntiCaptcha", "Retry captcha error: " + message);
                    setFinalError(boidIndex, "Captcha retry error");
                    advanceOrFinish();
                }
            });

        } catch (Exception e) {
            Log.e("AntiCaptcha", "Retry request error: " + e.getMessage());
            setFinalError(boidIndex, "Captcha retry error");
            advanceOrFinish();
        }
    }

    private void fetchExistingBoidList() {
        if (sharedPreferencesBoidList.getString("boidList", "").isEmpty() || sharedPreferencesBoidList.getString("boidList", "").equals("[]")){
            showEmpty();
        } else {
            showList();
            String jsonBoidList = sharedPreferencesBoidList.getString("boidList", "");
            listmapBoidList = new Gson().fromJson(jsonBoidList, new TypeToken<ArrayList<HashMap<String, Object>>>(){}.getType());

            adapter = new BulkResultBoidListAdapter(listmapBoidList);
            recycler_result_bulk_boids.setLayoutManager(new LinearLayoutManager(getContext()));
            recycler_result_bulk_boids.setAdapter(adapter);

            if (isAdLoaded && adapter != null) adapter.setAdLoaded(true);

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
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    private void fetchAdditionalCaptchas() {
        int totalBoids = listmapBoidList.size();
        int captchasNeeded = totalBoids - 1; // already have first

        for (int i = 0; i < captchasNeeded; i++) {
            int boidIndex = i + 1;
            String boid = String.valueOf(listmapBoidList.get(boidIndex).get("boid"));
            getNewCaptchaForBulkCheck(boidIndex, boid);
        }
    }
    private void getNewCaptchaForBulkCheck(int boidIndex, String boid) {
        try {
            RequestNetwork captchaRequest = new RequestNetwork(getActivity());
            captchaRequest.setHeaders(headers);
            HashMap<String, Object> params = new HashMap<>();
            params.put("captchaIdentifier", responseUnsolvedCaptchaIdentifier);
            captchaRequest.setParams(params, RequestNetworkController.REQUEST_BODY);

            String reloadUrl = "https://iporesult.cdsc.com.np/result/captcha/reload/" + responseUnsolvedCaptchaIdentifier;
            captchaRequest.startRequestNetwork(RequestNetworkController.POST, reloadUrl, "bulkCaptcha_" + boidIndex, new RequestNetwork.RequestListener() {
                @Override public void onResponse(String tag, String response, HashMap<String, Object> headers) {
                    try {
                        JSONObject json = new JSONObject(response);
                        JSONObject body = json.getJSONObject("body");
                        String newCaptchaBaseUrl = body.getString("captcha");
                        String newCaptchaIdentifier = body.getString("captchaIdentifier");

                        CaptchaData captchaData = new CaptchaData(boid, newCaptchaBaseUrl, newCaptchaIdentifier);
                        captchaDataList.set(boidIndex, captchaData);
                        createAntiCaptchaTask(captchaData);

                        if (boidIndex == listmapBoidList.size() - 1) {
                            startProcessingAllBoids();
                        }
                    } catch (Exception e) {
                        Log.e("AntiCaptcha", "Bulk captcha parse: " + e.getMessage());
                        setFinalError(boidIndex, "Captcha fetch failed");
                    }
                }
                @Override public void onErrorResponse(String tag, String message) {
                    Log.e("AntiCaptcha", "Bulk captcha error: " + message);
                    setFinalError(boidIndex, "Captcha fetch error");
                }
            });

        } catch (Exception e) {
            Log.e("AntiCaptcha", "Bulk captcha request error: " + e.getMessage());
            setFinalError(boidIndex, "Captcha fetch error");
        }
    }
    private void startProcessingAllBoids() {
        // Ensure all indices filled
        for (int i = 0; i < captchaDataList.size(); i++) {
            if (captchaDataList.get(i) == null) {
                handler.postDelayed(this::startProcessingAllBoids, 1000);
                return;
            }
        }
        isRunning = true;
        currentBoidIndex = 0;
        
        // Update button text to show processing status
        updateButtonText();
        
        // Now that processing is starting, show progress bar for first BOID
        setRunningSilent(currentBoidIndex);
        
        processNextBoid();
    }
    private void processNextBoid() {
        if (currentBoidIndex >= listmapBoidList.size()) {
            isRunning = false;
            button_result_bulk_check.setEnabled(true);
            Utilities.showSnackbar(parent_linear_bottom_nav, "", "Bulk check finished.");
            return;
        }

        CaptchaData captchaData = captchaDataList.get(currentBoidIndex);
        if (captchaData == null) {
            // Don't show progress bar for BOIDs without captcha data yet
            handler.postDelayed(this::processNextBoid, 1000);
            return;
        }

        // Only show progress bar when we're actually processing this BOID
        setRunningSilent(currentBoidIndex);

        if (!captchaData.isReady || captchaData.solvedText == null) {
            if (captchaData.taskId != null) {
                handler.postDelayed(() -> checkTaskStatus(captchaData), CAPTCHA_POLL_INTERVAL_MS);
            } else {
                handler.postDelayed(this::processNextBoid, 2000);
            }
            return;
        }

        checkBoidResult(captchaData);
    }
    private void checkBoidResult(CaptchaData captchaData) {
        try {
            requestResult.setHeaders(headers);
            HashMap<String, Object> params = new HashMap<>();
            params.put("captchaIdentifier", captchaData.captchaIdentifier);
            params.put("userCaptcha", captchaData.solvedText);
            params.put("companyShareId", currentCompanyId);
            params.put("boid", captchaData.boid);
            requestResult.setParams(params, RequestNetworkController.REQUEST_BODY);

            requestResult.startRequestNetwork(RequestNetworkController.POST, requestResultUrl, "result_" + currentBoidIndex, requestResultListener);
        } catch (Exception e) {
            setFinalError(currentBoidIndex, "Result request error");
            advanceOrFinish();
        }
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
            // Always reset to idle state to prevent progress bars from showing
            r.put("status", "idle");     // idle | running | done | error
            r.put("message", "");
            r.put("color", "white");
            decorated.add(r);
        }
        listmapBoidList.clear();
        listmapBoidList.addAll(decorated);
        updateSummaryCounts();
    }
    private void hideBannerAd() {
        try {
            if (getActivity() != null) {
                View bannerAd = getActivity().findViewById(R.id.adview1);
                if (bannerAd != null) bannerAd.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            Log.d("BulkResult", "Could not hide banner ad: " + e.getMessage());
        }
    }
    private void showBannerAd() {
        try {
            if (getActivity() != null) {
                View bannerAd = getActivity().findViewById(R.id.adview1);
                if (bannerAd != null) bannerAd.setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            Log.d("BulkResult", "Could not show banner ad: " + e.getMessage());
        }
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
        private static final int VIEW_TYPE_ITEM = 0;
        private static final int VIEW_TYPE_AD = 1;
        private boolean isAdLoaded = false;

        BulkResultBoidListAdapter(ArrayList<HashMap<String, Object>> listmapBoidList) { this.listmapBoidList = listmapBoidList; }

        @Override
        public int getItemViewType(int position) {
            return VIEW_TYPE_ITEM; // ads disabled
        }

        private int mapToDataIndex(int adapterPos) { return adapterPos; }

        @NonNull
        @Override
        public BulkResultBoidListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.boid_listview, parent, false);
            return new BulkResultBoidListAdapter.ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull BulkResultBoidListAdapter.ViewHolder holder, int position) {
            holder.itemView.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;

            int dataIndex = mapToDataIndex(position);
            if (dataIndex < 0 || dataIndex >= listmapBoidList.size()) return;

            View view = holder.itemView;
            LinearLayout linear_item = view.findViewById(R.id.linear_item);
            TextView textview_boid_count = view.findViewById(R.id.textview_boid_count);
            ProgressBar bulk_result_progress_bar =  view.findViewById(R.id.bulk_result_progress_bar);
            TextView textview_name = view.findViewById(R.id.textview_name);
            TextView textview_boid = view.findViewById(R.id.textview_boid);
            TextView textview_detail = view.findViewById(R.id.textview_detail);
            ImageView imageview_actions = view.findViewById(R.id.imageview_actions);

            // Defaults
            bulk_result_progress_bar.setVisibility(View.GONE);
            textview_boid_count.setVisibility(View.VISIBLE);
            textview_boid_count.setText(String.valueOf(dataIndex + 1));
            imageview_actions.setImageResource(R.drawable.ic_more_vert_white);

            HashMap<String, Object> map = listmapBoidList.get(dataIndex);
            textview_name.setText(String.valueOf(map.get("name")));

            // Mask BOID
            String boid = String.valueOf(map.get("boid"));
            textview_boid.setText(maskBoid(boid));

            String status = String.valueOf(map.get("status"));
            String msg = String.valueOf(map.get("message"));

            // Only show final results
            if ("done".equals(status) || "error".equals(status)) {
                textview_detail.setVisibility(View.VISIBLE);
                textview_detail.setText(msg);
                bulk_result_progress_bar.setVisibility(View.GONE);
            } else if ("idle".equals(status)){
                // Idle status
                textview_detail.setVisibility(View.GONE);
                textview_detail.setText("");
                bulk_result_progress_bar.setVisibility(View.GONE);
            }else {
                // running / idle — no text, show spinner
                textview_detail.setVisibility(View.GONE);
                textview_detail.setText("");
                bulk_result_progress_bar.setVisibility(View.VISIBLE);
            }

            // Color mapping — green/red/blue/orange; default white
            String colorKey = String.valueOf(map.get("color"));
            int colorInt;
            switch (colorKey) {
                case "green":
                    colorInt = ContextCompat.getColor(requireContext(), R.color.result_green);
                    break;
                case "red":
                    colorInt = ContextCompat.getColor(requireContext(), R.color.result_red);
                    break;
                case "blue":
                    colorInt = ContextCompat.getColor(requireContext(), android.R.color.holo_blue_light);
                    break;
                case "orange":
                    colorInt = ContextCompat.getColor(requireContext(), android.R.color.holo_orange_light);
                    break;
                default:
                    colorInt = ContextCompat.getColor(requireContext(), android.R.color.white);
            }
            textview_detail.setTextColor(colorInt);

            imageview_actions.setVisibility(View.GONE);
            Utilities.setBackground(linear_item, 15, 0, "#333A56", true);
        }

        @Override
        public int getItemCount() { return listmapBoidList.size(); }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public ViewHolder(@NonNull View itemView) { super(itemView); }
        }

        public void setAdLoaded(boolean adLoaded) { this.isAdLoaded = adLoaded; }
    }

    private String maskBoid(String boid) {
        if (boid == null || boid.length() < 7) return boid;
        int length = boid.length();
        String prefix = boid.substring(0, 3);
        String suffix = boid.substring(length - 2);
        int maskLength = length - 5;
        StringBuilder masked = new StringBuilder(prefix);
        for (int i = 0; i < maskLength; i++) masked.append("X");
        masked.append(suffix);
        return masked.toString();
    }

    private static class CaptchaData {
        String boid;
        String captcha;           // base64 image
        String captchaIdentifier;
        String taskId;            // Anti-Captcha task ID
        String solvedText;        // Anti-Captcha output
        boolean isReady;          // Captcha solved and ready
        boolean isProcessing;     // Currently being processed

        CaptchaData(String boid, String captcha, String captchaIdentifier) {
            this.boid = boid;
            this.captcha = captcha;
            this.captchaIdentifier = captchaIdentifier;
            this.isReady = false;
            this.isProcessing = false;
        }
    }

    // ===== Reward Ad Methods =====
    
    /**
     * Load reward ad with retry logic and failure handling
     */
    private void loadRewardAd() {
        if (isAdLoading) {
            Log.d("RewardAd", "Ad already loading, skipping...");
            return;
        }
        
        try {
            isAdLoading = true;
            Log.d("RewardAd", "Starting to load reward ad... (Attempt " + (currentLoadRetries + 1) + "/" + MAX_LOAD_RETRIES + ")");
            Log.d("RewardAd", "Ad Unit ID: " + REWARD_AD_UNIT_ID);
            
            AdRequest adRequest = new AdRequest.Builder().build();
            RewardedAd.load(getActivity(), REWARD_AD_UNIT_ID, adRequest, new RewardedAdLoadCallback() {
                @Override
                public void onAdFailedToLoad(LoadAdError loadAdError) {
                    isAdLoading = false;
                    Log.e("RewardAd", "Reward ad failed to load: " + loadAdError.getMessage());
                    Log.e("RewardAd", "Error Code: " + loadAdError.getCode());
                    Log.e("RewardAd", "Error Domain: " + loadAdError.getDomain());
                    
                    isRewardAdLoaded = false;
                    rewardedAd = null;
                    
                    // Handle different error types
                    String userMessage = getAdErrorMessage(loadAdError.getCode());
                    
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            Utilities.showSnackbar(parent_linear_bottom_nav, "", userMessage);
                        });
                    }
                    
                    // Retry logic for recoverable errors
                    if (shouldRetryAdLoad(loadAdError.getCode()) && currentLoadRetries < MAX_LOAD_RETRIES) {
                        currentLoadRetries++;
                        Log.d("RewardAd", "Retrying ad load in 5 seconds... (Attempt " + currentLoadRetries + "/" + MAX_LOAD_RETRIES + ")");
                        handler.postDelayed(() -> loadRewardAd(), 5000);
                    } else {
                        // Max retries reached or non-recoverable error
                        currentLoadRetries = 0;
                        Log.d("RewardAd", "Max retries reached or non-recoverable error");
                    }
                }

                @Override
                public void onAdLoaded(RewardedAd ad) {
                    isAdLoading = false;
                    currentLoadRetries = 0; // Reset retry counter on success
                    Log.d("RewardAd", "Reward ad loaded successfully!");
                    Log.d("RewardAd", "Ad object: " + (ad != null ? "Valid" : "Null"));
                    rewardedAd = ad;
                    isRewardAdLoaded = true;
                    setupRewardAdCallbacks();
                }
            });
        } catch (Exception e) {
            isAdLoading = false;
            Log.e("RewardAd", "Exception while loading reward ad: " + e.getMessage());
            e.printStackTrace();
            isRewardAdLoaded = false;
            rewardedAd = null;
            
            // Retry on exception if under limit
            if (currentLoadRetries < MAX_LOAD_RETRIES) {
                currentLoadRetries++;
                handler.postDelayed(() -> loadRewardAd(), 5000);
            }
        }
    }
    
    /**
     * Get user-friendly error message based on ad error code
     */
    private String getAdErrorMessage(int errorCode) {
        switch (errorCode) {
            case 0: // INTERNAL_ERROR
                return "Ad service error, please try again";
            case 1: // INVALID_REQUEST
                return "Ad request invalid, please restart app";
            case 2: // NETWORK_ERROR
                return "Network error, check your connection";
            case 3: // NO_FILL
                return "No ads available right now";
            case 4: // TIMEOUT
                return "Ad request timed out, please try again";
            default:
                return "Ad not available, please try again";
        }
    }
    
    /**
     * Determine if ad load error is recoverable and should be retried
     */
    private boolean shouldRetryAdLoad(int errorCode) {
        // Retry on network errors, timeouts, and internal errors
        // Don't retry on invalid requests or no fill
        return errorCode == 0 || errorCode == 2 || errorCode == 4;
    }
    
    /**
     * Setup reward ad callbacks
     */
    private void setupRewardAdCallbacks() {
        if (rewardedAd == null) return;
        
        rewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
            @Override
            public void onAdDismissedFullScreenContent() {
                isAdShowing = false;
                Log.d("RewardAd", "Reward ad was dismissed");
                rewardedAd = null;
                isRewardAdLoaded = false;
                
                // Only reset if user didn't earn reward (interrupted ad)
                if (!isRewardGranted) {
                    Log.d("RewardAd", "User interrupted ad - resetting bulk check");
                    resetBulkCheckState();
                } else {
                    Log.d("RewardAd", "User completed ad - starting bulk check");
                    // Reward was granted, start bulk check now
                    startBulkCheckAfterReward();
                }
                
                // Reset reward flag for next use
                isRewardGranted = false;
                
                // Reload ad for next use
                loadRewardAd();
            }

            @Override
            public void onAdFailedToShowFullScreenContent(AdError adError) {
                isAdShowing = false;
                Log.d("RewardAd", "Reward ad failed to show: " + adError.getMessage());
                rewardedAd = null;
                isRewardAdLoaded = false;
                
                // Ad failed to show - reset everything
                resetBulkCheckState();
                
                // Reload ad for next use
                loadRewardAd();
            }

            @Override
            public void onAdShowedFullScreenContent() {
                isAdShowing = true;
                Log.d("RewardAd", "Reward ad showed fullscreen content");
            }
        });
    }
    
    /**
     * Show reward ad and then start bulk check if successful
     */
    private void showRewardAdAndStartBulkCheck() {
        Log.d("RewardAd", "showRewardAdAndStartBulkCheck called");
        Log.d("RewardAd", "rewardedAd: " + (rewardedAd != null ? "Valid" : "Null"));
        Log.d("RewardAd", "isRewardAdLoaded: " + isRewardAdLoaded);
        
        if (rewardedAd != null && isRewardAdLoaded && !isAdShowing) {
            Log.d("RewardAd", "Showing reward ad...");
            try {
                // Show the reward ad
                rewardedAd.show(getActivity(), new OnUserEarnedRewardListener() {
                    @Override
                    public void onUserEarnedReward(RewardItem rewardItem) {
                        Log.d("RewardAd", "User earned reward: " + rewardItem.getAmount() + " " + rewardItem.getType());
                        // Mark that user earned reward - don't start bulk check yet
                        // Wait for onAdDismissedFullScreenContent to start it
                        isRewardGranted = true;
                    }
                });
            } catch (Exception e) {
                Log.e("RewardAd", "Exception showing reward ad: " + e.getMessage());
                e.printStackTrace();
                // Fallback to direct bulk check
                startBulkCheckAfterReward();
            }
        } else {
            Log.d("RewardAd", "Reward ad not ready, loading...");
            // If ad not ready, load ad silently
            loadRewardAd();
            
            // Wait a bit and try again
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                Log.d("RewardAd", "Retrying after delay...");
                Log.d("RewardAd", "rewardedAd: " + (rewardedAd != null ? "Valid" : "Null"));
                Log.d("RewardAd", "isRewardAdLoaded: " + isRewardAdLoaded);
                
                if (rewardedAd != null && isRewardAdLoaded && !isAdShowing) {
                    showRewardAdAndStartBulkCheck();
                } else {
                    // If still not ready, proceed without ad (fallback)
                    Log.d("RewardAd", "Ad still not ready, proceeding without ad");
                    Utilities.showSnackbar(parent_linear_bottom_nav, "", "Ad not available, proceeding with bulk check...");
                    startBulkCheckAfterReward();
                }
            }, 3000); // Wait 3 seconds
        }
    }
    
    /**
     * Start bulk check after user watched reward ad
     */
    private void startBulkCheckAfterReward() {
        // Prepare rows: RUNNING & BLANK (no intermediate messages)
        captchaDataList.clear();
        int total = listmapBoidList.size();
        for (int i = 0; i < total; i++) captchaDataList.add(null);

        button_result_bulk_check.setEnabled(false);
        isRunning = false; // will flip true after all captchas fetched

        // Don't show progress bars yet - wait until actual processing starts
        // for (int i = 0; i < total; i++) setRunningSilent(i); // REMOVED

        // Place first captcha from initial payload
        CaptchaData initialCaptcha = new CaptchaData(
                String.valueOf(listmapBoidList.get(0).get("boid")),
                responseUnsolvedCaptchaBaseUrl,
                responseUnsolvedCaptchaIdentifier
        );
        captchaDataList.set(0, initialCaptcha);
        createAntiCaptchaTask(initialCaptcha);

        // Fetch rest
        fetchAdditionalCaptchas();

        updateSummaryCounts();
    }
    
    /**
     * Reset bulk check state when user cancels or ad fails
     */
    private void resetBulkCheckState() {
        // Reset all BOIDs to idle state
        for (HashMap<String, Object> row : listmapBoidList) {
            row.put("status", "idle");
            row.put("message", "");
            row.put("color", "white");
        }
        
        // Clear captcha data
        captchaDataList.clear();
        
        // Reset running state
        isRunning = false;
        currentBoidIndex = 0;
        
        // Reset ad states
        isAdShowing = false;
        isAdLoading = false;
        
        // Re-enable button and update text
        button_result_bulk_check.setEnabled(true);
        updateButtonText();
        
        // Update UI
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
        updateSummaryCounts();
        
        // Show user-friendly message
        Utilities.showSnackbar(parent_linear_bottom_nav, "", "Bulk check cancelled");
    }
    
    /**
     * Update button text based on current status
     */
    private void updateButtonText() {
        if (isRunning) {
            button_result_bulk_check.setText("Checking Result...");
        } else {
            button_result_bulk_check.setText("Watch Ad to Bulk Check");
        }
    }
}
