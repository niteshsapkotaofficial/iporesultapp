package com.nitsoft.ipoallotmentprank;

import static android.content.ContentValues.TAG;

import androidx.annotation.*;
import android.app.*;
import android.os.*;
import android.util.Log;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import android.content.*;
import android.text.*;
import android.webkit.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

import android.widget.ScrollView;
import android.widget.LinearLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Spinner;
import android.widget.EditText;
import android.widget.Button;

import com.bumptech.glide.Glide;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.AdRequest;

import com.google.android.gms.ads.AdListener;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.graphics.Typeface;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
//import com.suke.widget.*;

import androidx.fragment.app.Fragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.internal.Util;

import com.google.firebase.database.*;


public class HomeFragmentActivity extends Fragment {

	// ===== UI REFS (existing) =====
	private TextView textview_app_title;
	private TextView textview_app_subtitle;
	private LinearLayout parent_linear_bottom_nav;

	private LinearLayout linear_switch_result_tab;
	private LinearLayout linear_real_check_tab;
	private TextView textview_real_check_tab;
	private LinearLayout linear_indicator_real_check_tab;
	private LinearLayout linear_fake_check_tab;
	private TextView textview_fake_check_tab;
	private LinearLayout linear_indicator_fake_check_tab;

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

	// ===== Firebase (existing) =====
	private DatabaseReference freshCaptchaListReferecnce;
	private DatabaseReference holdCaptchaListReferecnce;
	private DatabaseReference usedCaptchaListReference;

	// ===== Real endpoints/vars (existing) =====
	String requestCompanyUrl = "https://iporesult.cdsc.com.np/result/companyShares/fileUploaded";
	String requestCaptchaReloadUrl = "https://iporesult.cdsc.com.np/result/captcha/reload/";
	String requestResultUrl = "https://iporesult.cdsc.com.np/result/result/check";
	String requestBackendUrl = "https://api.niteshsapkota.com/api/v1/request-captcha-process";
	String responseCompanyJson = "";
	String responseUnsolvedCaptchaIdentifier = "";
	String responseUnsolvedCaptchaBaseUrl = "";

	// Firebase database captcha variables
	String dbCaptchaIdentifier = "";
	String dbCaptchaPushKey = "";
	String dbCaptchaSavedBy = "";
	String dbCaptchaSavedTimestamp = "";
	String dbCaptchaUsage = "";
	String dbCaptchaUsedBy = "";
	String dbCaptchaUsedTimestamp = "";
	String dbCaptchaValue = "";

	// Result checking vars
	String resultReadyCompanyId = "";
	String resultReadyUserBoid = "";
	String resultReadyCaptchaIdentifier = "";
	String resultReadyCaptchaValue = "";
	String resultReadyCaptchaPushKey = "";

	// Toggle
	String resultType = "real"; // "real" | "fake"

	private HashMap<String, Object> headers = new HashMap<>();
	private ArrayList<HashMap<String, Object>> listmapCompanyDetails = new ArrayList<>();

	// Requests (real)
	private RequestNetwork requestCompany;
	private RequestNetwork.RequestListener requestCompanyListener;
	private RequestNetwork requestCaptchaReload;
	private RequestNetwork.RequestListener requestCaptchaReloadListener;
	private RequestNetwork requestResult;
	private RequestNetwork.RequestListener requestResultListener;
	private RequestNetwork requestBackend;
	private RequestNetwork.RequestListener requestBackendListener;

	// ===== Fake flow (new) =====
	private final String fakeCompanyURL = "https://niteshsapkotaofficial.github.io/iporesultapp/fakecompany.json";
	private final String fakeResultURL  = "https://niteshsapkotaofficial.github.io/iporesultapp/fakeresult.json";
	private ArrayList<HashMap<String, Object>> listmapFakeCompany = new ArrayList<>();
	private ArrayList<HashMap<String, Object>> listmapFakeResult = new ArrayList<>();
	private RequestNetwork requestFakeCompany;
	private RequestNetwork requestFakeResult;
	private RequestNetwork.RequestListener requestFakeCompanyListener;
	private RequestNetwork.RequestListener requestFakeResultListener;
	private String randomFakeResult = "";

	// ===== Cache controls (new) =====
	private boolean realLoaded = false;
	private boolean fakeLoaded = false;
	private int realSelectedIndex = 0;
	private int fakeSelectedIndex = 0;

	@NonNull
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_home, container, false);
		initialize(savedInstanceState, view);
		com.google.firebase.FirebaseApp.initializeApp(getContext());
		onActivityCreate();
		return view;
	}

	private void initialize(Bundle savedInstanceState, View view) {
		// ===== findViewById (existing) =====
		textview_app_title = getActivity().findViewById(R.id.textview_app_title);
		textview_app_subtitle = getActivity().findViewById(R.id.textview_app_subtitle);
		parent_linear_bottom_nav = getActivity().findViewById(R.id.parent_linear_bottom_nav);

		linear_switch_result_tab = view.findViewById(R.id.linear_switch_result_tab);
		linear_real_check_tab = view.findViewById(R.id.linear_real_check_tab);
		textview_real_check_tab = view.findViewById(R.id.textview_real_check_tab);
		linear_indicator_real_check_tab = view.findViewById(R.id.linear_indicator_real_check_tab);
		linear_fake_check_tab = view.findViewById(R.id.linear_fake_check_tab);
		textview_fake_check_tab = view.findViewById(R.id.textview_fake_check_tab);
		linear_indicator_fake_check_tab = view.findViewById(R.id.linear_indicator_fake_check_tab);

		linear_result_single = view.findViewById(R.id.linear_result_single);
		linear_result_single_window = view.findViewById(R.id.linear_result_single_window);
		imageview_result_single_logo = view.findViewById(R.id.imageview_result_single_logo);
		textview_result_single_prompt = view.findViewById(R.id.textview_result_single_prompt);
		linear_result_single_select_company = view.findViewById(R.id.linear_result_single_select_company);
		spinner_result_single_select_company = view.findViewById(R.id.spinner_result_single_select_company);
		linear_result_single_select_company_placement = view.findViewById(R.id.linear_result_single_select_company_placement);
		textview_result_single_select_company_placement = view.findViewById(R.id.textview_result_single_select_company_placement);
		linear_result_single_enter_boid = view.findViewById(R.id.linear_result_single_enter_boid);
		edittext_result_single_boid = view.findViewById(R.id.edittext_result_single_boid);
		textview_result_single_boid_count = view.findViewById(R.id.textview_result_single_boid_count);
		button_result_single_view_result = view.findViewById(R.id.button_result_single_view_result);
		textview_result_single_result = view.findViewById(R.id.textview_result_single_result);
		textview_result_single_selected_company = view.findViewById(R.id.textview_result_single_selected_company);

		// Firebase refs
		freshCaptchaListReferecnce = FirebaseDatabase.getInstance().getReference("freshCaptchaList");
		holdCaptchaListReferecnce = FirebaseDatabase.getInstance().getReference("holdCaptchaList");
		usedCaptchaListReference = FirebaseDatabase.getInstance().getReference("usedCaptchaList");

		// Requests
		requestCompany = new RequestNetwork(getActivity());
		requestCaptchaReload = new RequestNetwork(getActivity());
		requestResult = new RequestNetwork(getActivity());
		requestBackend = new RequestNetwork(getActivity());
		requestFakeCompany = new RequestNetwork(getActivity());
		requestFakeResult = new RequestNetwork(getActivity());

		// Boid watcher
		Utilities.setBoidLengthWatcher(edittext_result_single_boid, textview_result_single_boid_count, textview_result_single_result, button_result_single_view_result);

		// ===== REAL listeners (unchanged) =====
		requestCompanyListener = new RequestNetwork.RequestListener() {
			@Override
			public void onResponse(String tag, String response, HashMap<String, Object> responseHeaders) {
				responseCompanyJson = response;
				parseCompanyListResponse();
				spinner_result_single_select_company.setAdapter(new spinnerSelectCompanyAdapter(listmapCompanyDetails, /*isFake=*/false));
				linear_result_single_select_company.setVisibility(View.VISIBLE);
				linear_result_single_select_company_placement.setVisibility(View.GONE);
				realLoaded = true; // cache flag
				// restore selection if any
				spinner_result_single_select_company.setSelection(Math.min(realSelectedIndex, Math.max(0, listmapCompanyDetails.size()-1)));
			}
			@Override
			public void onErrorResponse(String tag, String message) {
				linear_result_single_select_company_placement.setVisibility(View.VISIBLE);
				textview_result_single_select_company_placement.setText(R.string.load_error);
			}
		};
		requestCaptchaReloadListener = new RequestNetwork.RequestListener() {
			@Override
			public void onResponse(String tag, String response, HashMap<String, Object> responseHeaders) {
				try {
					JSONObject jsonObject = new JSONObject(response);
					JSONObject body = jsonObject.getJSONObject("body");
					responseUnsolvedCaptchaBaseUrl = body.getString("captcha");
					responseUnsolvedCaptchaIdentifier = body.getString("captchaIdentifier");

					// Also send the captcha for processing to the backend
					sendCaptchaToBackendForProcessing();
				} catch (JSONException e) { e.printStackTrace(); }
			}
			@Override public void onErrorResponse(String tag, String message) {}
		};
		requestResultListener = new RequestNetwork.RequestListener() {
			@Override
			public void onResponse(String tag, String response, HashMap<String, Object> responseHeaders) {
				// Restore edittext and button
				edittext_result_single_boid.setText("");
				button_result_single_view_result.setEnabled(true);

				try {
					JSONObject jsonObject = new JSONObject(response);
					String message = jsonObject.getString("message");

					requestToCaptchaReload();

					textview_result_single_result.setVisibility(View.VISIBLE);
					textview_result_single_result.setText(message);
					// Green if "Congratulation", else Red
					if (message.contains("Congratulation")) {
						textview_result_single_result.setTextColor(getResources().getColor(R.color.result_green));
					} else {
						textview_result_single_result.setTextColor(getResources().getColor(R.color.result_red));
					}

					button_result_single_view_result.setText("View Result");
					button_result_single_view_result.setEnabled(true);
				} catch (JSONException e) {
					e.printStackTrace();
					Utilities.showSnackbar(parent_linear_bottom_nav, "", "Something went wrong while parsing response.");
				}

				// Now that the result is shown, move captcha and fetch new
				moveCaptchaFromHoldToUsed();
				fetchFreshCaptchaFromDatabase();
			}

			@Override
			public void onErrorResponse(String tag, String message) {
				Utilities.showSnackbar(parent_linear_bottom_nav, "", "Something went wrong, please restart app");
			}
		};
		requestBackendListener = new RequestNetwork.RequestListener() {
			@Override public void onResponse(String tag, String response, HashMap<String, Object> responseHeaders) {}
			@Override public void onErrorResponse(String tag, String message) {}
		};

		// ===== FAKE listeners =====
		requestFakeCompanyListener = new RequestNetwork.RequestListener() {
			@Override
			public void onResponse(String tag, String response, HashMap<String, Object> headers) {
				linear_result_single_select_company.setVisibility(View.VISIBLE);
				linear_result_single_select_company_placement.setVisibility(View.GONE);

				listmapFakeCompany = new Gson().fromJson(
						response, new TypeToken<ArrayList<HashMap<String, Object>>>(){}.getType()
				);
				Collections.reverse(listmapFakeCompany);
				spinner_result_single_select_company.setAdapter(new spinnerSelectCompanyAdapter(listmapFakeCompany, /*isFake=*/true));

				// mark loaded + restore selection
				fakeLoaded = true;
				spinner_result_single_select_company.setSelection(Math.min(fakeSelectedIndex, Math.max(0, listmapFakeCompany.size()-1)));
			}
			@Override
			public void onErrorResponse(String tag, String message) {
				linear_result_single_select_company.setVisibility(View.GONE);
				linear_result_single_select_company_placement.setVisibility(View.VISIBLE);
				textview_result_single_select_company_placement.setText(R.string.load_error);
			}
		};
		requestFakeResultListener = new RequestNetwork.RequestListener() {
			@Override
			public void onResponse(String tag, String response, HashMap<String, Object> headers) {
				listmapFakeResult = new Gson().fromJson(
						response, new TypeToken<ArrayList<HashMap<String, Object>>>(){}.getType()
				);
				if (!listmapFakeResult.isEmpty()) {
					int rnd = new Random().nextInt(listmapFakeResult.size());
					randomFakeResult = String.valueOf(listmapFakeResult.get(rnd).get("result"));
				}
			}
			@Override
			public void onErrorResponse(String tag, String message) {
				textview_result_single_result.setVisibility(View.VISIBLE);
				textview_result_single_result.setText("Failed Loading, Try Again!");
			}
		};

		// ===== Spinner selection (remember index per mode) =====
		spinner_result_single_select_company.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override public void onItemSelected(AdapterView<?> adapterView, View v, int i, long l) {
				if ("real".equals(resultType)) {
					realSelectedIndex = i;
					resultReadyCompanyId = Objects.requireNonNull(listmapCompanyDetails.get(i).get("id")).toString();
				} else {
					fakeSelectedIndex = i;
					textview_result_single_result.setVisibility(View.INVISIBLE);
					if(!listmapFakeResult.isEmpty()){
						int rnd = new Random().nextInt(listmapFakeResult.size());
						randomFakeResult = Objects.requireNonNull(listmapFakeResult.get(rnd).get("result")).toString();
					}
				}
				hideKeyboard();
			}
			@Override public void onNothingSelected(AdapterView<?> adapterView) {}
		});

		// ===== Tabs =====
		linear_real_check_tab.setOnClickListener(v -> switchToReal());
		linear_fake_check_tab.setOnClickListener(v -> switchToFake());

		// ===== Button =====
		button_result_single_view_result.setOnClickListener(v -> onViewResultClick());
		button_result_single_view_result.setOnLongClickListener(v -> {
			if ("fake".equals(resultType) && !listmapFakeResult.isEmpty()) {
				textview_result_single_result.setVisibility(View.INVISIBLE);
				int rnd = new Random().nextInt(listmapFakeResult.size());
				randomFakeResult = String.valueOf(listmapFakeResult.get(rnd).get("result"));
				Utilities.showSnackbar(parent_linear_bottom_nav, "", "Result refreshed!");
			}
			hideKeyboard();
			return true;
		});
	}

	private void onActivityCreate() {
		userInterface();
		fetchFreshCaptchaFromDatabase();
		headers.put("Accept", "application/json, text/plain, */*");
		headers.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/138.0.0.0 Safari/537.36");
		headers.put("Referer", "https://iporesult.cdsc.com.np/");
		headers.put("Host", "iporesult.cdsc.com.np");
		headers.put("Accept-Language", "en-US,en;q=0.7");

		requestCompany.setHeaders(headers);
		requestCompany.startRequestNetwork(RequestNetworkController.GET, requestCompanyUrl, "company", requestCompanyListener);
	}
	private void userInterface() {
		textview_app_title.setText("IPO Allotment Result");
		textview_app_subtitle.setText("Single result, one at a time!");
		textview_result_single_prompt.setText("Result Sourced From Original Owner");

		linear_indicator_real_check_tab.setVisibility(View.VISIBLE);
		linear_indicator_fake_check_tab.setVisibility(View.GONE);
		textview_result_single_result.setVisibility(View.INVISIBLE);
		linear_result_single_window.setVisibility(View.VISIBLE);
		linear_result_single_select_company.setVisibility(View.GONE);

		Utilities.setBackground(linear_real_check_tab, 20, 0, "#323957", true);
		Utilities.setBackground(linear_fake_check_tab, 20, 0, "#323957", true);
		Utilities.setBackground(linear_indicator_real_check_tab, 10, 0, "#FFFFFF", false);
		Utilities.setBackground(linear_indicator_fake_check_tab, 10, 0, "#FFFFFF", false);
		Utilities.setBackground(linear_result_single_window, 20, 0, "#323957", false);
		Utilities.setBackground(linear_result_single_enter_boid, 15, 0, "#FFFFFF", false);
		Utilities.setBackground(button_result_single_view_result, 15, 0, "#4f5b8b", true);
		Utilities.setBackground(linear_result_single_select_company, 15, 0, "#FFFFFF", false);
		Utilities.setBackground(linear_result_single_select_company_placement, 15, 0, "#FFFFFF", false);

		textview_real_check_tab.setTypeface(Typeface.createFromAsset(getContext().getAssets(), "fonts/opensans_semibold.ttf"), Typeface.BOLD);
		textview_fake_check_tab.setTypeface(Typeface.createFromAsset(getContext().getAssets(), "fonts/opensans_semibold.ttf"), Typeface.BOLD);
		textview_result_single_prompt.setTypeface(Typeface.createFromAsset(getContext().getAssets(), "fonts/josefinsans_regular.ttf"), Typeface.NORMAL);
		edittext_result_single_boid.setTypeface(Typeface.createFromAsset(getContext().getAssets(), "fonts/quicksand.ttf"), Typeface.NORMAL);
		button_result_single_view_result.setTypeface(Typeface.createFromAsset(getContext().getAssets(), "fonts/quicksand.ttf"), Typeface.NORMAL);
		textview_result_single_result.setTypeface(Typeface.createFromAsset(getContext().getAssets(), "fonts/quicksand.ttf"), Typeface.NORMAL);
	}

	// ===== Tab switch with cache =====
	private void switchToReal() {
		resultType = "real";
		resetResultUI();
		textview_result_single_prompt.setText("Result Sourced From Original Owner");
		textview_app_title.setText("IPO Allotment Result");
		textview_app_subtitle.setText("Real single result, one at a time!");
		linear_indicator_real_check_tab.setVisibility(View.VISIBLE);
		linear_indicator_fake_check_tab.setVisibility(View.GONE);

		if (!realLoaded) {
			linear_result_single_select_company.setVisibility(View.GONE);
			linear_result_single_select_company_placement.setVisibility(View.VISIBLE);
			textview_result_single_select_company_placement.setText("Loading companies...");
			requestCompany.setHeaders(headers);
			requestCompany.startRequestNetwork(RequestNetworkController.GET, requestCompanyUrl, "company", requestCompanyListener);
		} else {
			spinner_result_single_select_company.setAdapter(new spinnerSelectCompanyAdapter(listmapCompanyDetails, /*isFake=*/false));
			spinner_result_single_select_company.setSelection(Math.min(realSelectedIndex, Math.max(0, listmapCompanyDetails.size()-1)));
			linear_result_single_select_company_placement.setVisibility(View.GONE);
			linear_result_single_select_company.setVisibility(View.VISIBLE);
		}
	}
	private void switchToFake() {
		resultType = "fake";
		resetResultUI();
		textview_result_single_prompt.setText("Fun Results By IPO Result App");
		textview_app_title.setText("IPO Allotment Result");
		textview_app_subtitle.setText("Fake & funny results for fun!");
		linear_indicator_fake_check_tab.setVisibility(View.VISIBLE);
		linear_indicator_real_check_tab.setVisibility(View.GONE);

		if (!fakeLoaded) {
			linear_result_single_select_company.setVisibility(View.GONE);
			linear_result_single_select_company_placement.setVisibility(View.VISIBLE);
			textview_result_single_select_company_placement.setText("Loading companies...");
			requestFakeCompany.startRequestNetwork(RequestNetworkController.GET, fakeCompanyURL, "fakeCompany", requestFakeCompanyListener);
			requestFakeResult.startRequestNetwork(RequestNetworkController.GET, fakeResultURL, "fakeResult", requestFakeResultListener);
		} else {
			spinner_result_single_select_company.setAdapter(new spinnerSelectCompanyAdapter(listmapFakeCompany, /*isFake=*/true));
			spinner_result_single_select_company.setSelection(Math.min(fakeSelectedIndex, Math.max(0, listmapFakeCompany.size()-1)));
			linear_result_single_select_company_placement.setVisibility(View.GONE);
			linear_result_single_select_company.setVisibility(View.VISIBLE);
		}
	}
	private void resetResultUI() {
		textview_result_single_result.setVisibility(View.INVISIBLE);
		textview_result_single_result.setText("");
		textview_result_single_result.setTextColor(getResources().getColor(android.R.color.white));
		button_result_single_view_result.setText("View Result");
		button_result_single_view_result.setEnabled(true);
	}


	// ===== Button click (branches) =====
	private void onViewResultClick() {
		resultReadyUserBoid = edittext_result_single_boid.getText().toString();

		if ("real".equals(resultType)) {
			if (resultReadyCompanyId.equals("")) {
				Utilities.showSnackbar(parent_linear_bottom_nav, "", "Please select a company.");
				return;
			} else if ((resultReadyUserBoid.length() != 16) || !resultReadyUserBoid.startsWith("130")) {
				Utilities.showSnackbar(parent_linear_bottom_nav, "", "Please enter a valid BOID.");
				return;
			} else if (resultReadyCaptchaValue == null || resultReadyCaptchaValue.equals("")) {
				Utilities.showSnackbar(parent_linear_bottom_nav, "", "Captcha error, restart app.");
				return;
			}

			HashMap<String, Object> params = new HashMap<>();
			params.put("boid", resultReadyUserBoid);
			params.put("companyShareId", resultReadyCompanyId);
			params.put("userCaptcha", resultReadyCaptchaValue);
			params.put("captchaIdentifier", resultReadyCaptchaIdentifier);

			requestResult.setHeaders(headers);
			requestResult.setParams(params, RequestNetworkController.REQUEST_BODY);
			requestResult.startRequestNetwork(RequestNetworkController.POST, requestResultUrl, "result", requestResultListener);

			hideKeyboard();
			button_result_single_view_result.setText("Checking Result...");
			button_result_single_view_result.setEnabled(false);

		} else { // FAKE
			if (resultReadyUserBoid.isEmpty() || resultReadyUserBoid.length() != 16) {
				Utilities.showSnackbar(parent_linear_bottom_nav, "", "Please enter a valid BOID.");
				return;
			}
			if (listmapFakeResult.isEmpty()) {
				textview_result_single_result.setVisibility(View.VISIBLE);
				textview_result_single_result.setText("Failed Loading, Try Again!");
				return;
			}
			textview_result_single_result.setVisibility(View.VISIBLE);
			textview_result_single_result.setText(randomFakeResult);
			textview_result_single_result.setTextColor(getResources().getColor(R.color.result_yellow));
			hideKeyboard();
		}
	}
	private void hideKeyboard() {
		InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
		View focusedView = requireActivity().getCurrentFocus();
		if (focusedView != null) {
			imm.hideSoftInputFromWindow(focusedView.getWindowToken(), 0);
		}
	}

	// ======== Captcha Handling ========
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
				// Also send the captcha for processing to the backend
				sendCaptchaToBackendForProcessing();
			} else {
				// Optional: show error message from responseObject.getString("message")
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
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

					resultReadyCaptchaValue = dbCaptchaValue;
					resultReadyCaptchaIdentifier = dbCaptchaIdentifier;
					resultReadyCaptchaPushKey = dbCaptchaPushKey;
				}
			}

			@Override
			public void onCancelled(DatabaseError databaseError) {
				Log.e("Firebase", "Error fetching captcha: " + databaseError.getMessage());
			}
		});

	}
	private void moveCaptchaFromHoldToUsed() {
		// Assign new usedCaptchaList pushkey
		String usedCaptchaPushKey = freshCaptchaListReferecnce.push().getKey();
		// Add captcha to usedCaptchaList
		HashMap<String, Object> usedCaptchaEntry = new HashMap<>();
		usedCaptchaEntry.put("captchaIdentifier", dbCaptchaIdentifier);
		usedCaptchaEntry.put("captchaPushKey", usedCaptchaPushKey);
		usedCaptchaEntry.put("captchaSavedBy", dbCaptchaSavedBy);
		usedCaptchaEntry.put("captchaSavedTimestamp", dbCaptchaSavedTimestamp);
		usedCaptchaEntry.put("captchaUsage", "true");
		usedCaptchaEntry.put("captchaUsedBy", resultReadyUserBoid);
		usedCaptchaEntry.put("captchaUsedTimestamp", new java.text.SimpleDateFormat("HH:mm:ss, dd MMM yyyy").format(new java.util.Date()));
		usedCaptchaEntry.put("captchaValue", dbCaptchaValue);

		usedCaptchaListReference.child(usedCaptchaPushKey).setValue(usedCaptchaEntry);
		holdCaptchaListReferecnce.child(dbCaptchaPushKey).removeValue();
	}
	private void requestToCaptchaReload() {
		requestCaptchaReload.setHeaders(headers);
		HashMap<String, Object> params = new HashMap<>();
		params.put("captchaIdentifier", responseUnsolvedCaptchaIdentifier);
		requestCaptchaReload.setParams(params, RequestNetworkController.REQUEST_BODY);
		requestCaptchaReload.startRequestNetwork(RequestNetworkController.POST, requestCaptchaReloadUrl + responseUnsolvedCaptchaIdentifier, "captchaReload", requestCaptchaReloadListener);
	}

	private void sendCaptchaToBackendForProcessing(){
		HashMap<String, Object> headers = new HashMap<>();
		headers.put("X-API-KEY", "E8822B1A637C1965A88DC18CAA9D8");
		requestBackend.setHeaders(headers);
		HashMap<String, Object> params = new HashMap<>();
		params.put("captchaIdentifier", responseUnsolvedCaptchaIdentifier);
		params.put("captcha", responseUnsolvedCaptchaBaseUrl);
		if (resultReadyUserBoid.isEmpty()){
			params.put("captchaSavedBy", "default");
		} else if (resultReadyUserBoid.length() == 16 && resultReadyUserBoid.startsWith("130")){
			params.put("captchaSavedBy", resultReadyUserBoid);
		} else {
			params.put("captchaSavedBy", "default");
		}
		requestBackend.setParams(params, RequestNetworkController.REQUEST_BODY);
		requestBackend.startRequestNetwork(RequestNetworkController.POST, requestBackendUrl, "backend", requestBackendListener);
	}

	// ===== Unified spinner adapter =====
	public class spinnerSelectCompanyAdapter extends BaseAdapter {
		ArrayList<HashMap<String, Object>> companyList;
		boolean isFake;
		public spinnerSelectCompanyAdapter(ArrayList<HashMap<String, Object>> companyList, boolean isFake) {
			this.companyList = companyList; this.isFake = isFake;
		}
		@Override public int getCount() { return companyList.size(); }
		@Override public Object getItem(int index) { return companyList.get(index); }
		@Override public long getItemId(int index) { return index; }
		@Override
		public View getView(int position, View view, ViewGroup parent) {
			if (view == null) view = getLayoutInflater().inflate(R.layout.custom_select_company_spinner, parent, false);
			TextView t = view.findViewById(R.id.textview_spinner_select_company_custom);
			if (isFake) {
				if (companyList.get(position).containsKey("companyName")) {
					t.setText(String.valueOf(companyList.get(position).get("companyName")));
				}
			} else {
				if (companyList.get(position).containsKey("name")) {
					t.setText(String.valueOf(companyList.get(position).get("name")));
				}
			}
			return view;
		}
	}
}