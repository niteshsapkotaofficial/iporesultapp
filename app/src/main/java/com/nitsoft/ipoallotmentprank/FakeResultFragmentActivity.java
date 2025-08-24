package com.nitsoft.ipoallotmentprank;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.URLUtil;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Objects;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;


import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ChildEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class FakeResultFragmentActivity extends  Fragment  {

    private LinearLayout parent_linear_bottom_nav;
    private ScrollView scrollview_fake_result;
    private LinearLayout linear_result_single;
    private LinearLayout linear_result_single_window;
    private ImageView imageview_result_single_logo;
    private TextView textview_result_single_prompt;
    private LinearLayout linear_result_single_select_company;
    private Spinner spinner_result_single_select_company;
    private LinearLayout linear_result_single_select_company_placement;
    private TextView textview_result_single_select_company_placement;
    private LinearLayout linear_result_single_enter_boid;
    private EditText edittext_result_single_boid;
    private TextView textview_result_single_boid_count;
    private Button button_result_single_view_result;
    private TextView textview_result_single_result;
    private TextView textview_result_single_selected_company;

    // Variables
    String fakeCompanyURL = "https://niteshsapkotaofficial.github.io/iporesultapp/fakecompany.json";
    String fakeResultURL = "https://niteshsapkotaofficial.github.io/iporesultapp/fakeresult.json";
    String randomFakeResult = "";
    ArrayList<HashMap<String, Object>> listmapFakeCompany = new ArrayList<>();
    ArrayList<HashMap<String, Object>> listmapFakeResult = new ArrayList<>();

    // Request network
    private RequestNetwork requestFakeCompany;
    private RequestNetwork requestFakeResult;
    private RequestNetwork.RequestListener requestFakeCompanyListener;
    private RequestNetwork.RequestListener requestFakeResultListener;


    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater _inflater, @Nullable ViewGroup _container, @Nullable Bundle _savedInstanceState) {
        View view = _inflater.inflate(R.layout.fragment_fake_result, _container, false);
        initialize(_savedInstanceState, view);
        onActivityCreate();
        return view;
    }
    private void initialize(Bundle _savedInstanceState, View _view) {

        parent_linear_bottom_nav = (LinearLayout) getActivity().findViewById(R.id.parent_linear_bottom_nav);
        scrollview_fake_result = (ScrollView) _view.findViewById(R.id.scrollview_fake_result);
        linear_result_single = (LinearLayout) _view.findViewById(R.id.linear_result_single);
        linear_result_single_window = (LinearLayout) _view.findViewById(R.id.linear_result_single_window);
        imageview_result_single_logo = (ImageView) _view.findViewById(R.id.imageview_result_single_logo);
        textview_result_single_prompt = (TextView) _view.findViewById(R.id.textview_result_single_prompt);
        linear_result_single_select_company = (LinearLayout) _view.findViewById(R.id.linear_result_single_select_company);
        spinner_result_single_select_company = (Spinner) _view.findViewById(R.id.spinner_result_single_select_company);
        linear_result_single_select_company_placement = (LinearLayout) _view.findViewById(R.id.linear_result_single_select_company_placement);
        textview_result_single_select_company_placement = (TextView) _view.findViewById(R.id.textview_result_single_select_company_placement);
        linear_result_single_enter_boid = (LinearLayout) _view.findViewById(R.id.linear_result_single_enter_boid);
        edittext_result_single_boid = (EditText) _view.findViewById(R.id.edittext_result_single_boid);
        textview_result_single_boid_count = (TextView) _view.findViewById(R.id.textview_result_single_boid_count);
        button_result_single_view_result = (Button) _view.findViewById(R.id.button_result_single_view_result);
        textview_result_single_result = (TextView) _view.findViewById(R.id.textview_result_single_result);
        textview_result_single_selected_company = (TextView) _view.findViewById(R.id.textview_result_single_selected_company);

        requestFakeCompany = new RequestNetwork(getActivity());
        requestFakeResult = new RequestNetwork(getActivity());

        // Request listener for fake company
        requestFakeCompanyListener = new RequestNetwork.RequestListener() {
            @Override
            public void onResponse(String tag, String response, HashMap<String, Object> responseHeaders) {
                // Fake company response received
                linear_result_single_select_company.setVisibility(View.VISIBLE);                  // Making company area visible after company gets loaded
                linear_result_single_select_company_placement.setVisibility(View.GONE);      // Making placement area invisible after company gets loaded

                // Set company list from response into the listmap and set the adapter
                listmapFakeCompany = new Gson().fromJson(response, new TypeToken<ArrayList<HashMap<String, Object>>>() {}.getType());
                Collections.reverse(listmapFakeCompany);
                spinner_result_single_select_company.setAdapter(new spinnerSelectCompanyAdapter(listmapFakeCompany));
            }
            @Override
            public void onErrorResponse(String tag, String message) {
                // Fake company response error
                linear_result_single_select_company.setVisibility(View.GONE);                                     // Making company area invisible after company fails to load
                linear_result_single_select_company_placement.setVisibility(View.VISIBLE);                        // Making placement area visible with error message after company fails to load
                textview_result_single_select_company_placement.setText(R.string.load_error);            // Setting error message after company fails to load
            }
        };
        // Request listener for fake result
        requestFakeResultListener = new RequestNetwork.RequestListener() {
            @Override
            public void onResponse(String tag, String response, HashMap<String, Object> responseHeaders) {
                // Fake result response received
                listmapFakeResult = new Gson().fromJson(response, new TypeToken<ArrayList<HashMap<String, Object>>>() {}.getType());
                int randomFakeResultPosition = new Random().nextInt(listmapFakeResult.size());
                randomFakeResult = Objects.requireNonNull(listmapFakeResult.get(randomFakeResultPosition).get("result")).toString();
            }

            @Override
            public void onErrorResponse(String tag, String message) {
                // Fake result response error
                textview_result_single_result.setVisibility(View.VISIBLE);                                        // Making result text visible after result fails to load to show error message
                textview_result_single_result.setText("Failed Loading, Try Again!");                              // Setting error message after result fails to load
            }
        };
        // Spinner on item selected listener
        spinner_result_single_select_company.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                // Refresh result if company is changed in spinner
                textview_result_single_result.setVisibility(View.INVISIBLE);                                           // Making result text visible after result button pressed
                if(!listmapFakeResult.isEmpty()){
                    int randomFakeResultPosition = new Random().nextInt(listmapFakeResult.size());
                    randomFakeResult = Objects.requireNonNull(listmapFakeResult.get(randomFakeResultPosition).get("result")).toString();
                }
                // Hide keyboard when result button is long pressed
                InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                View focusedView = requireActivity().getCurrentFocus();
                if (focusedView != null) {
                    imm.hideSoftInputFromWindow(focusedView.getWindowToken(), 0);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        // Edittext boid text change listener
        Utilities.setBoidLengthWatcher(edittext_result_single_boid, textview_result_single_boid_count, textview_result_single_result, button_result_single_view_result);
        // Result button click listener
        button_result_single_view_result.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Pick random fake result from the fake company list and set the result text
                // Check if boid is entered and is 16 digits
                if(!edittext_result_single_boid.getText().toString().isEmpty() && edittext_result_single_boid.getText().toString().length() == 16){
                    if(!listmapFakeResult.isEmpty()){
                        textview_result_single_result.setVisibility(View.VISIBLE);
                        textview_result_single_result.setText(randomFakeResult);
                        // Hide keyboard when result button is clicked
                        InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }
                }
            }
        });
        // Result button long click listener
        button_result_single_view_result.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                // Refresh result when result button is long pressed
                textview_result_single_result.setVisibility(View.INVISIBLE);                                           // Making result text visible after result button pressed
                if(!listmapFakeResult.isEmpty()){
                    int randomFakeResultPosition = new Random().nextInt(listmapFakeResult.size());
                    randomFakeResult = Objects.requireNonNull(listmapFakeResult.get(randomFakeResultPosition).get("result")).toString();
                    Utilities.showSnackbar(parent_linear_bottom_nav, "", "Result refreshed!");
                }
                // Hide keyboard when result button is long pressed
                InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                return true;
            }
        });
    }

    private void onActivityCreate(){
        userInterface();                        // User interface setup

        // Start request network for fake company and fake result on activity create
        requestFakeCompany.startRequestNetwork(RequestNetworkController.GET, fakeCompanyURL, "fakeCompany", requestFakeCompanyListener);
        requestFakeResult.startRequestNetwork(RequestNetworkController.GET, fakeResultURL, "fakeResult", requestFakeResultListener);
    }
    private void userInterface (){

        textview_result_single_prompt.setText("Fun Results By IPO Result App");
        // Default visibility settings when activity is created
        linear_result_single_select_company_placement.setVisibility(View.VISIBLE);		// Making placement area visible until company gets loaded
        linear_result_single_select_company.setVisibility(View.GONE);				        // Making company area invisible until company gets loaded
        textview_result_single_result.setVisibility(View.INVISIBLE);				        // Making result text invisible

        // Set background properties
        Utilities.setBackground(linear_result_single_window, 20, 0, "#323957", false);
        Utilities.setBackground(linear_result_single_select_company, 15, 0, "#FFFFFF", false);
        Utilities.setBackground(linear_result_single_select_company_placement, 15, 0, "#FFFFFF", false);
        Utilities.setBackground(linear_result_single_enter_boid, 15, 0, "#FFFFFF", false);
        Utilities.setBackground(button_result_single_view_result, 15, 0, "#4f5b8b", true);
    }

    public class spinnerSelectCompanyAdapter extends BaseAdapter{

        ArrayList<HashMap<String, Object>> companyList;
        public spinnerSelectCompanyAdapter(ArrayList<HashMap<String, Object>> companyList){
            this.companyList = companyList;
        }

        @Override
        public int getCount() {
            return companyList.size();
        }

        @Override
        public Object getItem(int index) {
            return companyList.get(index);
        }

        @Override
        public long getItemId(int index) {
            return index;
        }

        @Override
        public View getView(int position, View view, ViewGroup viewGroup) {
            if(view == null){
                view = getLayoutInflater().inflate(R.layout.custom_select_company_spinner, null);
            }
            TextView textview_spinner_select_company_custom = view.findViewById(R.id.textview_spinner_select_company_custom);
            if(listmapFakeCompany.get((int)position).containsKey("companyName")){
                textview_spinner_select_company_custom.setText(Objects.requireNonNull(companyList.get(position).get("companyName")).toString());
            }
            return view;
        }
    }

}