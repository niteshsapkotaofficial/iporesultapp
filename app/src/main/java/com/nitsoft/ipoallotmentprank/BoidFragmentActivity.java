package com.nitsoft.ipoallotmentprank;

import androidx.annotation.*;
import android.app.*;
import android.os.*;
import android.view.*;
import android.view.View.*;
import android.widget.*;
import android.content.*;

import java.util.regex.*;

import org.json.*;
import java.util.HashMap;
import java.util.ArrayList;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Button;
import android.widget.ImageView;

import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ChildEventListener;

import java.util.Calendar;

import com.google.android.gms.ads.AdRequest;

import com.google.android.gms.ads.AdListener;

import java.util.TimerTask;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.graphics.Typeface;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
//import com.suke.widget.*;

import androidx.fragment.app.Fragment;


public class BoidFragmentActivity extends  Fragment  { 
	

	private FirebaseDatabase _firebase = FirebaseDatabase.getInstance();
	
	private HashMap<String, Object> Map_Boid = new HashMap<>();
	private String uid = "";
	private String subchild_time = "";
	private String key = "";
	private double fragboid_ad = 0;
	private String Key = "";
	private HashMap<String, Object> Inter_ad_Map = new HashMap<>();

	private ArrayList<String> boid_string = new ArrayList<>();
	private ArrayList<HashMap<String, Object>> listmap = new ArrayList<>();

	private ArrayList<HashMap<String, Object>> listmapCapital = new ArrayList<>();
	private ArrayList<HashMap<String, Object>> listmapBank = new ArrayList<>();
	private ArrayList<HashMap<String, Object>> listmapOwnDetail = new ArrayList<>();
	private ArrayList<HashMap<String, Object>> listmapAccounts = new ArrayList<>();

	// Layouts
	private TextView textview_app_title;
	private TextView textview_app_subtitle;
	private ScrollView vscroll_main;
	private LinearLayout linear_main;
	private LinearLayout linear_save_detail_dialog;
	private LinearLayout linear_select_capital;
	private Spinner spinner_capital;
	private TextView textview_capital_loading;
	private ImageView imageview_search_capital;
	private LinearLayout linear_enter_username;
	private EditText edittext_username;
	private TextView textview_username_count;
	private LinearLayout linear_enter_password;
	private EditText edittext_password;
	private ImageView imageview_show_password;
	private LinearLayout linear_select_bank;
	private Spinner spinner_bank;
	private ImageView imageview_bank_search;
	private LinearLayout linear_enter_crn;
	private EditText edittext_crn;
	private ImageView imageview_show_crn;
	private LinearLayout linear_enter_txnpin;
	private EditText edittext_txnpin;
	private ImageView imageview_show_txnpin;

	private TextView textview_temp;

	private LinearLayout linear_button_login;
	private Button button_login;

	// Adapters
	private SpinnerCapitalAdapter spinnerCapitalAdapter;
	private SpinnerBankAdapter spinnerBankAdapter;

	// All Auth Related Variables
	private String meroshareAuthKeyHeader = "";
	private boolean isAuthenticated = false;
	private String clientID ="";
	private String meroshareUsername = "";
	private String merosharePassword = "";
	
	// Variables
	private SharedPreferences sharedPreferencesOwnDetail;

	private RequestNetwork requestCapital;
	private RequestNetwork.RequestListener requestListenerCapital;
	private RequestNetwork requestMeroshareAuth;
	private RequestNetwork.RequestListener requestListenerMeroshareAuth;
	private RequestNetwork requestBankRequest;
	private RequestNetwork.RequestListener requestListenerBankRequest;
	private RequestNetwork requestOwnDetail;
	private RequestNetwork.RequestListener requestListenerOwnDetail;

	private Calendar Cal = Calendar.getInstance();
	private Calendar cal = Calendar.getInstance();
	private InterstitialAd inter_ad;
	private AdListener _inter_ad_ad_listener;
	private TimerTask interad_onfailed;
	private DatabaseReference Ad_Open = _firebase.getReference("Ad Open");
	private ChildEventListener _Ad_Open_child_listener;
	@NonNull
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.boid_fragment, container, false);
		initialize(savedInstanceState, view);
		com.google.firebase.FirebaseApp.initializeApp(getContext());
		onActivityCreate();
		return view;
	}
	private void initialize(Bundle _savedInstanceState, View view) {

		textview_app_title = (TextView) getActivity().findViewById(R.id.textview_app_title);
		textview_app_subtitle = (TextView) getActivity().findViewById(R.id.textview_app_subtitle);

		vscroll_main = (ScrollView) view.findViewById(R.id.vscroll_main);
		linear_main = (LinearLayout) view.findViewById(R.id.linear_main);
		linear_save_detail_dialog = (LinearLayout) view.findViewById(R.id.linear_save_detail_dialog);
		// First Input Field
		linear_select_capital = (LinearLayout) view.findViewById(R.id.linear_select_capital);
		spinner_capital = (Spinner) view.findViewById(R.id.spinner_capital);
		textview_capital_loading = (TextView) view.findViewById(R.id.textview_capital_loading);
		imageview_search_capital = (ImageView) view.findViewById(R.id.imageview_search_capital);
		linear_enter_username = (LinearLayout) view.findViewById(R.id.linear_enter_username);
		edittext_username = (EditText) view.findViewById(R.id.edittext_username);
		textview_username_count = (TextView) view.findViewById(R.id.textview_username_count);
		linear_enter_password = (LinearLayout) view.findViewById(R.id.linear_enter_password);
		edittext_password = (EditText) view.findViewById(R.id.edittext_password);
		imageview_show_password = (ImageView) view.findViewById(R.id.imageview_show_password);
		// Second Input Field
		linear_select_bank = (LinearLayout) view.findViewById(R.id.linear_select_bank);
		spinner_bank = (Spinner) view.findViewById(R.id.spinner_bank);
		imageview_bank_search = (ImageView) view.findViewById(R.id.imageview_bank_search);
		linear_enter_crn = (LinearLayout) view.findViewById(R.id.linear_enter_crn);
		edittext_crn = (EditText) view.findViewById(R.id.edittext_crn);
		imageview_show_crn = (ImageView) view.findViewById(R.id.imageview_show_crn);
		linear_enter_txnpin = (LinearLayout) view.findViewById(R.id.linear_enter_txnpin);
		edittext_txnpin = (EditText) view.findViewById(R.id.edittext_txn_pin);
		imageview_show_txnpin = (ImageView) view.findViewById(R.id.imageview_show_txnpin);
		linear_button_login = (LinearLayout) view.findViewById(R.id.linear_button_login);
		button_login = (Button) view.findViewById(R.id.button_login);
		sharedPreferencesOwnDetail = getContext().getSharedPreferences("ownDetail", Context.MODE_PRIVATE);

		textview_temp = (TextView) view.findViewById(R.id.textview_temp);

		requestCapital = new RequestNetwork(getActivity());
		requestMeroshareAuth = new RequestNetwork(getActivity());
		requestBankRequest = new RequestNetwork(getActivity());
		requestOwnDetail = new RequestNetwork(getActivity());

		requestListeners();

		// Capital Spinner Click Here
		spinner_capital.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_UP) {
					showSearchableSpinnerDialog();
				}
				return true; // Return true to indicate that the event was handled
			}
		});
		// Capital Spinner On Item Selected
		spinner_capital.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> param1, View param2, int param3, long param4) {
				final int position = param3;
			}

			@Override
			public void onNothingSelected(AdapterView<?> _param1) {

			}
		});
		// Password Visibility Here
		imageview_show_password.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (edittext_password.getTransformationMethod()
						instanceof android.text.method.PasswordTransformationMethod) {
					edittext_password.setTransformationMethod(null); 						// Show password
					imageview_show_password.setImageResource(R.drawable.ic_hide_password);	// Change to hide password icon
				} else {
					edittext_password.setTransformationMethod(new
							android.text.method.PasswordTransformationMethod()); 			// Hide password
					imageview_show_password.setImageResource(R.drawable.ic_show_password);	// Change to show password icon
				}
				edittext_password.setSelection(edittext_password.getText().length());		// Move cursor to the end of the text
			}
		});
		// CRN Number Visibility Here
		imageview_show_crn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (edittext_crn.getTransformationMethod()
						instanceof android.text.method.PasswordTransformationMethod) {
					edittext_crn.setTransformationMethod(null); 						// Show password
					imageview_show_crn.setImageResource(R.drawable.ic_hide_password);	// Change to hide password icon
				} else {
					edittext_crn.setTransformationMethod(new
							android.text.method.PasswordTransformationMethod()); 			// Hide password
					imageview_show_crn.setImageResource(R.drawable.ic_show_password);	// Change to show password icon
				}
				edittext_crn.setSelection(edittext_crn.getText().length());		// Move cursor to the end of the text
			}
		});
		// Transaction Pin Visibility Here
		imageview_show_txnpin.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (edittext_txnpin.getTransformationMethod()
						instanceof android.text.method.PasswordTransformationMethod) {
					edittext_txnpin.setTransformationMethod(null); 						// Show password
					imageview_show_txnpin.setImageResource(R.drawable.ic_hide_password);	// Change to hide password icon
				} else {
					edittext_txnpin.setTransformationMethod(new
							android.text.method.PasswordTransformationMethod()); 			// Hide password
					imageview_show_txnpin.setImageResource(R.drawable.ic_show_password);	// Change to show password icon
				}
				edittext_txnpin.setSelection(edittext_txnpin.getText().length());		// Move cursor to the end of the text
			}
		});
		// Username Edittext Counter Here
		edittext_username.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence param1, int param2, int param3, int param4) {
				final String charSeq = param1.toString();
				textview_username_count.setText(String.valueOf((long)(edittext_username.getText().toString().length())).concat("/8"));			// Set Username Count while typing
			}
			
			@Override
			public void beforeTextChanged(CharSequence param1, int param2, int param3, int param4) {
				
			}
			
			@Override
			public void afterTextChanged(Editable param1) {
				
			}
		});

		// Login Button Click Here
		button_login.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				// Check if Authentication is Completed
				if (!isAuthenticated) {
					// Authentication is not completed - Next Part is Logging In
					// Check if Username and Password are entered and Valid
					if (edittext_username.getText().toString().length() < 8) {
						Toast.makeText(getContext(), "Please Enter Valid Username", Toast.LENGTH_LONG).show();		// Check if 8 characters are entered in Username
					} else if (edittext_password.getText().toString().isEmpty()) {
						Toast.makeText(getContext(), "Please Enter Password", Toast.LENGTH_LONG).show();			// Check if Password is entered
					} else if (!listmapAccounts.isEmpty()){
						// There are Accounts in the List, so check existence
						if(!listmapAccounts.contains(edittext_username.getText().toString())){
							uiForIsAuthFalse();
							Utilities.negativeDialog(getContext(), "Error", "You have already saved this user's detail", "Save Another", new OnClickListener() {
								@Override
								public void onClick(View view) {
								}
							});

						}
					} else {
						// Proceed to Login with Capital, Username and Password Here
						HashMap<String, Object> auth_payload = new HashMap<>();																			// Create Request Payload for Meroshare Auth
						int id = ((Number) listmapCapital.get(spinner_capital.getSelectedItemPosition()).get("id")).intValue();
						auth_payload.put("clientId", id);																								// Set Client ID to Selected Capital Code
						auth_payload.put("username", edittext_username.getText().toString());															// Set Username to Entered Username
						auth_payload.put("password", edittext_password.getText().toString());															// Set Password to Entered Password

						// Request MeroShare Auth Here with Client ID, Username and Password
						requestMeroshareAuth.setParams(auth_payload, RequestNetworkController.REQUEST_BODY);											// Set Request Payload and Type to Request Body
						requestMeroshareAuth.startRequestNetwork(
								RequestNetworkController.POST, "https://webbackend.cdsc.com.np/api/meroShare/auth/", "requestMeroshareAuth", requestListenerMeroshareAuth
						);
						Utilities.showProgressDialog(getContext(), "Checking Details");
					}
				} else {
					// Authentication is completed - Next Part is Saving Details
					// Check if CRN Number and Transaction Pin are entered
					if (edittext_crn.getText().toString().isEmpty()) {
						// CRN Number is not entered
						Toast.makeText(getContext(), "Please Enter CRN Number", Toast.LENGTH_LONG).show();
					} else if (edittext_txnpin.length() < 4) {
						// Transaction Pin is not entered
						Toast.makeText(getContext(), "Please Enter Valid Transaction Pin", Toast.LENGTH_LONG).show();
					} else{
						// Proceed to Saving Details Here
						HashMap<String, Object> userMeroshareLoginDetails = listmapOwnDetail.get(listmapOwnDetail.size() - 1);
						userMeroshareLoginDetails.put("clientId", ((Number) listmapCapital.get(spinner_capital.getSelectedItemPosition()).get("id")).intValue());
						userMeroshareLoginDetails.put("username", edittext_username.getText().toString());
						userMeroshareLoginDetails.put("password", edittext_password.getText().toString());
						userMeroshareLoginDetails.put("bank", listmapBank.get(spinner_bank.getSelectedItemPosition()).get("name").toString());
						userMeroshareLoginDetails.put("crn", edittext_crn.getText().toString());
						userMeroshareLoginDetails.put("txnPin", edittext_txnpin.getText().toString());

						listmapOwnDetail.add(userMeroshareLoginDetails);

						String listmapOwnDetailJson = new Gson().toJson(listmapOwnDetail);
						sharedPreferencesOwnDetail.edit().putString("ownDetail", listmapOwnDetailJson).apply();
						textview_temp.setText(listmapOwnDetailJson);

						// Showing "Saving Details" Progress Bar
						Utilities.showProgressDialog(getContext(), "Saving Details");

						// Making UI Changes After 2 Seconds of Saving Details
						Handler handler = new Handler();
						handler.postDelayed(new Runnable() {
							@Override
							public void run() {
								Utilities.hideProgressDialog();
								Utilities.showSnackbar(button_login, "Details Saved", "");

								uiForIsAuthFalse();
							}
						}, 2000);

					}
				}

				// Handling Ad Visibility
				fragboid_ad++;
				if (fragboid_ad == 3) {
					// Interstitial Ad Here
					AdRequest adRequest = new AdRequest.Builder().build();
					InterstitialAd.load(getActivity(),"ca-app-pub-7184690369277704/4653608727", adRequest,
							new InterstitialAdLoadCallback() {
								@Override
								public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
									// The mInterstitialAd reference will be null until
									// an ad is loaded.
									InterstitialAd mInterstitialAd = interstitialAd;
									mInterstitialAd.show(getActivity());

								}

								@Override
								public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
									// Handle the error

								}
							});

				}
				else {
					if (fragboid_ad == 6) {
						// Interstitial Ad Here

						AdRequest adRequest = new AdRequest.Builder().build();
						InterstitialAd.load(getActivity(),"ca-app-pub-7184690369277704/4653608727", adRequest,
								new InterstitialAdLoadCallback() {
									@Override
									public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
										// The mInterstitialAd reference will be null until
										// an ad is loaded.
										InterstitialAd mInterstitialAd = interstitialAd;
										mInterstitialAd.show(getActivity());

									}

									@Override
									public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
										// Handle the error

									}
								});

						fragboid_ad = 0;
					}
				}
			}
		});

		};

	private void onActivityCreate(){
		userInterface();
		initializeLogic();
	}
	private void requestListeners(){
		// Request Capital Listener Here
		requestListenerCapital = new RequestNetwork.RequestListener() {
			@Override
			public void onResponse(String tag, String response, HashMap<String, Object> responseHeaders) {
				// Fetch Successful - Response Here
				listmapCapital = new Gson().fromJson(response, new TypeToken<ArrayList<HashMap<String, Object>>>(){}.getType());
				spinnerCapitalAdapter = new SpinnerCapitalAdapter(getContext(), listmapCapital);
				spinner_capital.setAdapter(spinnerCapitalAdapter);
				// Hiding Capital Loading Text & Showing Spinner
				textview_capital_loading.setVisibility(View.GONE);
				spinner_capital.setVisibility(View.VISIBLE);

			}

			@Override
			public void onErrorResponse(String tag, String message) {
				// Fetch Failed - Error Handling Here
				Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
			}
		};
		// Request Meroshare Auth Listener Here
		requestListenerMeroshareAuth = new RequestNetwork.RequestListener() {
			@Override
			public void onResponse(String tag, String response, HashMap<String, Object> responseHeaders) {
				// Fetch Successful - Response Here
				// Extracting Error Message from Response
				String userMessage = extractErrorMeroshareAuthMessage(response);
				textview_temp.setText(response);
				// Handling 401 - Unauthorized Error Here, Caused when username or password is incorrect
				if (response.contains("401")) {
					Utilities.negativeDialog(getContext(), "Error", userMessage, "Retry", new OnClickListener() {
						@Override
						public void onClick(View view) {
						}
					});
					// Hide Progress Bar
					Utilities.hideProgressDialog();
				} else if (response.contains("200") && response.contains("Log in successful.")){
					// Successful Login with Status Code 200 and Message "Log in successful."
					// At First, Retrieve "Authorization" header value
					if (responseHeaders != null && responseHeaders.containsKey("Authorization")) {
						meroshareAuthKeyHeader = (String) responseHeaders.get("Authorization");

						// Create Auth Header For The Bank Request
						HashMap<String, Object> headers = new HashMap<>();
						headers.put("Authorization", meroshareAuthKeyHeader);

						// Request For "BANK REQUEST" Upon Successful Login
						requestBankRequest.setHeaders(headers); // If this is the appropriate method to set headers
						requestBankRequest.startRequestNetwork(RequestNetworkController.GET,"https://webbackend.cdsc.com.np/api/meroShare/bank/","bankRequest", requestListenerBankRequest						);

						// Request For "OWN DETAIL" Upon Successful Login
						requestOwnDetail.setHeaders(headers);
						requestOwnDetail.startRequestNetwork(RequestNetworkController.GET, "https://webbackend.cdsc.com.np/api/meroShare/ownDetail/", "ownDetail", requestListenerOwnDetail);

					}
				} else {
					// Handling 200 Status Code, Successful Login but Expired Account
					try {
						// Parse the JSON string into a JSONObject
						JSONObject jsonObject = new JSONObject(response);
						// Extract the "message" field into a String
						String message = jsonObject.optString("message", "No message provided");
						Utilities.negativeDialog(getContext(), "Account Expired", message, "Got It", new OnClickListener() {
							@Override
							public void onClick(View view) {
							}
						});
						// Hide Progress Bar
						Utilities.hideProgressDialog();
					} catch (JSONException e) {
						// If there's an error parsing the JSON, handle it here by showing an error message
						if (!userMessage.equals("")){
							// Hide Progress Bar
							Utilities.hideProgressDialog();
							// Show Error Dialog
							Utilities.negativeDialog(getContext(), "Error", userMessage, "Retry", new OnClickListener() {
								@Override
								public void onClick(View view) {

								}
							});
						} else {
							// Hide Progress Bar
							Utilities.hideProgressDialog();
							// Show Error Dialog
							Utilities.negativeDialog(getContext(), "Error", "Something Went Wrong", "Retry", new OnClickListener() {
								@Override
								public void onClick(View view) {

								}
							});
						}
					}
				}
			}

			@Override
			public void onErrorResponse(String tag, String message) {
				// Fetch Failed - Error Handling Here
				Toast.makeText(getContext(), "Error Response: " + message, Toast.LENGTH_LONG).show();
			}
		};
		// Request Bank Request Listener Here
		requestListenerBankRequest = new RequestNetwork.RequestListener() {
			@Override
			public void onResponse(String tag, String response, HashMap<String, Object> responseHeaders) {
				// Bank Request Successful - Response Here

				// Hiding Capital, Username and Password Input Field After Authentication Gets Completed and Bank Request is Successful
				linear_select_capital.setVisibility(View.GONE);
				linear_enter_username.setVisibility(View.GONE);
				linear_enter_password.setVisibility(View.GONE);

				// Showing Bank, CRN Number and Transaction Pin Input Field After Authentication Gets Completed and Bank Request is Successful
				linear_select_bank.setVisibility(View.VISIBLE);
				linear_enter_crn.setVisibility(View.VISIBLE);
				linear_enter_txnpin.setVisibility(View.VISIBLE);

				// Saving Bank List and Showing it in Spinner
				listmapBank = new Gson().fromJson(response, new TypeToken<ArrayList<HashMap<String, Object>>>(){}.getType());
				spinnerBankAdapter = new SpinnerBankAdapter(getContext(), listmapBank);
				spinner_bank.setAdapter(spinnerBankAdapter);

				// Changing Text Of Button From "Login" to "Save"
				button_login.setText("Save");
			}

			@Override
			public void onErrorResponse(String tag, String message) {
				// Bank Request Failed - Error Handling Here
				Toast.makeText(getContext(), "Bank Request Failed Loading", Toast.LENGTH_LONG).show();
				Utilities.hideProgressDialog();
			}
		};
		// Request Own Detail Listener Here
		requestListenerOwnDetail = new RequestNetwork.RequestListener() {
			@Override
			public void onResponse(String tag, String response, HashMap<String, Object> responseHeaders) {
				// Changing isAuthenticated to True
				isAuthenticated = true;

				// Own Detail Request Successful - Response Here
				Utilities.hideProgressDialog();
				String jsonString = "[" + response + "]";  // Make sure the response is in an array format

				// Parse the incoming JSON response into an ArrayList of HashMaps
				ArrayList<HashMap<String, Object>> responseData = new Gson().fromJson(jsonString, new TypeToken<ArrayList<HashMap<String, Object>>>(){}.getType());

				// Check if listmapOwnDetail is initialized, if not, initialize it
				if (listmapOwnDetail == null) {
					listmapOwnDetail = new Gson().fromJson(sharedPreferencesOwnDetail.getString("ownDetail", "[]"), new TypeToken<ArrayList<HashMap<String, Object>>>(){}.getType());
				}

				// Iterate through responseData to check for duplicates
				for (HashMap<String, Object> newDetail : responseData) {
					boolean isDuplicate = false;
					for (HashMap<String, Object> existingDetail : listmapOwnDetail) {
						if (existingDetail.get("username").equals(newDetail.get("username"))) {
							// If a duplicate is found, break and set isDuplicate to true
							isDuplicate = true;
							// Changing isAuthenticated to false to reinitiate new login if needed
							uiForIsAuthFalse();
							Utilities.negativeDialog(getContext(), "Error", "You have already saved this user's account", "Save Another", new OnClickListener() {
								@Override
								public void onClick(View view) {
								}
							});
							break;
						}
					}
					// If no duplicate was found, add the newDetail to listmapOwnDetail
					if (!isDuplicate) {
						listmapOwnDetail.add(newDetail);
					}
				}

				// Optionally update the UI or storage with the new listmapOwnDetail
				String updatedJson = new Gson().toJson(listmapOwnDetail);
				textview_temp.setText(updatedJson);  // Assuming there's a TextView to display the JSON

			}



			@Override
			public void onErrorResponse(String tag, String message) {
				// Own Detail Request Failed - Error Handling Here
				Toast.makeText(getContext(), "Own Detail Failed Loading", Toast.LENGTH_LONG).show();

				// Hiding Progress Bar
				Utilities.hideProgressDialog();

			}
		};
	}
	private void userInterface(){

		Utilities.hide_scrollbar(vscroll_main);														// Hide Scrollbar
		Utilities.createRippleEffect(getContext(), imageview_show_password);						// Create Ripple Effect on Show/Hide Password Icon
		Utilities.createRippleEffect(getContext(), imageview_search_capital);						// Create Ripple Effect on Search Capital Button
		Utilities.createRippleEffect(getContext(), imageview_bank_search);							// Create Ripple Effect on Search Bank Button
		Utilities.createRippleEffect(getContext(), imageview_show_crn);								// Create Ripple Effect on Show/Hide CRN Number Icon
		Utilities.createRippleEffect(getContext(), imageview_show_txnpin);							// Create Ripple Effect on Show/Hide Transaction Pin Icon

		// Hiding Bank, CRN Number and Transaction Pin Input Field Before Authentication gets Completed
		linear_select_bank.setVisibility(View.GONE);
		linear_enter_crn.setVisibility(View.GONE);
		linear_enter_txnpin.setVisibility(View.GONE);
		// Showing Capital Loading Dummy Text Until Actual List Gets Loaded In Spinner From Meroshare's Backend
		textview_capital_loading.setVisibility(View.VISIBLE);
		spinner_capital.setVisibility(View.GONE);

		button_login.setText("Login");							// Set Login Button Text Before Authentication Gets Completed

		// Set Background Properties
		Utilities.setBackground(linear_save_detail_dialog, 15, 8, "#333a56", false);
		Utilities.setBackground(linear_select_capital, 15, 0, "#ffffff", false);
		Utilities.setBackground(linear_enter_username, 15, 0, "#FFFFFF", false);
		Utilities.setBackground(linear_enter_password, 15, 0, "#FFFFFF", false);
		Utilities.setBackground(linear_select_bank, 15, 0, "#ffffff", false);
		Utilities.setBackground(linear_enter_crn, 15, 0, "#FFFFFF", false);
		Utilities.setBackground(linear_enter_txnpin, 15, 0, "#FFFFFF", false);
		Utilities.setBackground(button_login, 15, 0, "#ffffff", true);

		// Font Property _ TEMP
		edittext_username.setTypeface(Typeface.createFromAsset(getContext().getAssets(),"fonts/quicksand.ttf"), Typeface.NORMAL);
		edittext_password.setTypeface(Typeface.createFromAsset(getContext().getAssets(),"fonts/quicksand.ttf"), Typeface.NORMAL);
		textview_username_count.setTypeface(Typeface.createFromAsset(getContext().getAssets(),"fonts/quicksand.ttf"), Typeface.NORMAL);
		button_login.setTypeface(Typeface.createFromAsset(getContext().getAssets(),"fonts/opensans_semibold.ttf"), Typeface.NORMAL);
	}
	private void initializeLogic() {
		fragboid_ad = 0;

		// At first, Request Capital from Meroshare's Backend and Show it in Spinner
		requestCapital.startRequestNetwork(RequestNetworkController.GET, "https://webbackend.cdsc.com.np/api/meroShare/capital/", "requestCapital", requestListenerCapital);
		// Check if there are some accounts saved already, if yes get into "listmapAccounts"
		listmapAccounts = new Gson().fromJson(sharedPreferencesOwnDetail.getString("ownDetail", "[]"), new TypeToken<ArrayList<HashMap<String, Object>>>(){}.getType());

	}
	
	@Override
	public void onActivityResult(int _requestCode, int _resultCode, Intent _data) {
		
		super.onActivityResult(_requestCode, _resultCode, _data);
		
		switch (_requestCode) {
			
			default:
			break;
		}
	}


	public class SpinnerCapitalAdapter extends BaseAdapter {
		private ArrayList<HashMap<String, Object>> originalListmap; // Original data list
		private ArrayList<HashMap<String, Object>> filteredListmap; // Filtered data list
		private LayoutInflater inflater;

		public SpinnerCapitalAdapter(Context context, ArrayList<HashMap<String, Object>> listmapCapital) {
			this.originalListmap = new ArrayList<>(listmapCapital); // Copy original data
			this.filteredListmap = new ArrayList<>(listmapCapital); // Initialize with all items
			this.inflater = LayoutInflater.from(context);
		}

		@Override
		public int getCount() {
			return filteredListmap.size();
		}

		@Override
		public HashMap<String, Object> getItem(int index) {
			return filteredListmap.get(index);
		}

		@Override
		public long getItemId(int index) {
			return index;
		}

		@Override
		public View getView(final int position, View v, ViewGroup container) {
			View view = v;
			if (view == null) {
				view = inflater.inflate(R.layout.custom_select_company_spinner, null);
			}

			TextView textview_company_name = (TextView) view.findViewById(R.id.textview_spinner_select_company_custom);

			if (filteredListmap.get(position).containsKey("name")) {
				textview_company_name.setText(filteredListmap.get(position).get("name").toString().concat(" (").concat(filteredListmap.get(position).get("code").toString()).concat(")"));
			}
			return view;
		}

		// Method to filter the data based on the search input
		public void filter(String query) {
			filteredListmap.clear(); // Clear the filtered list
			if (query.isEmpty()) {
				filteredListmap.addAll(originalListmap); // If query is empty, show all items
			} else {
				for (HashMap<String, Object> item : originalListmap) {
					if (item.get("name").toString().toLowerCase().contains(query.toLowerCase()) || item.get("code").toString().toLowerCase().contains(query.toLowerCase())) {
						filteredListmap.add(item); // Add matching items
					}
				}
			}
			notifyDataSetChanged(); // Notify the adapter to refresh the list view
		}
		// Method to reset the filtered list to original
		public void resetFilter() {
			filteredListmap.clear();
			filteredListmap.addAll(originalListmap); // Restore the original list
			notifyDataSetChanged(); // Refresh the list view
		}
	}
	public class SpinnerBankAdapter extends BaseAdapter {

		private ArrayList<HashMap<String, Object>> listmapBank;

		public SpinnerBankAdapter(Context context, ArrayList<HashMap<String, Object>> listmapBank) {
			this.listmapBank = listmapBank;
		}

		@Override
		public int getCount() {
			return listmapBank.size();
		}

		@Override
		public Object getItem(int i) {
			return i;
		}

		@Override
		public long getItemId(int i) {
			return i;
		}

		@Override
		public View getView(int position, View view, ViewGroup viewGroup) {
			view = View.inflate(getContext(), R.layout.custom_select_company_spinner, null);

			TextView textview_company_name = (TextView) view.findViewById(R.id.textview_spinner_select_company_custom);

			if (listmapBank.get(position).containsKey("name")) {
				textview_company_name.setText(listmapBank.get(position).get("name").toString());
			}

			return view;
		}
	}


	// Showing Error Message from Meroshare Auth when Username or Password is incorrect
	private String extractErrorMeroshareAuthMessage(String xmlResponse) {
		String regex = "<message>(.*?)</message>";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(xmlResponse);
		return matcher.find() ? matcher.group(1) : "Unknown error";
	}
	private void showSearchableSpinnerDialog() {
		// Create a custom dialog
		final Dialog dialog = new Dialog(getContext());
		dialog.setContentView(R.layout.searchable_spinner);

		// Get references to the EditText and ListView
		EditText editTextSearch = dialog.findViewById(R.id.edittext_search);
		ListView listViewItems = dialog.findViewById(R.id.listview_items);

		// Initialize your adapter and set it to the ListView
		final SpinnerCapitalAdapter adapter = new SpinnerCapitalAdapter(getContext(), listmapCapital);
		listViewItems.setAdapter(adapter);

		// Reset the adapter to show all items
		adapter.resetFilter();

		// Set an item click listener for the ListView
		listViewItems.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// Get the selected item from the filtered list
				HashMap<String, Object> selectedItem = adapter.getItem(position);

				// Find the original position of the selected item to set in the spinner
				int originalPosition = adapter.originalListmap.indexOf(selectedItem);

				// Set the selected item to the spinner
				if (originalPosition >= 0) {
					spinner_capital.setSelection(originalPosition);
				}

				// Dismiss the dialog
				dialog.dismiss();
			}
		});

		// Add a text change listener to the EditText to filter the ListView
		editTextSearch.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				adapter.filter(s.toString());
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});

		// Show the dialog
		dialog.show();
	}
	private void uiForIsAuthFalse(){
		// Changing isAuthenticated to false to reinitiate new login if needed
		isAuthenticated = false;

		// Showing Capital, Username and Password Input Field After Authentication Gets Completed and Bank Request is Successful
		linear_select_capital.setVisibility(View.VISIBLE);
		linear_enter_username.setVisibility(View.VISIBLE);
		linear_enter_password.setVisibility(View.VISIBLE);

		// Showing Bank, CRN Number and Transaction Pin Input Field After Authentication Gets Completed and Bank Request is Successful
		linear_select_bank.setVisibility(View.GONE);
		linear_enter_crn.setVisibility(View.GONE);
		linear_enter_txnpin.setVisibility(View.GONE);

		// Clearing Spinner, Edittext and Textview
		spinner_capital.setSelection(0);
		spinner_bank.setSelection(0);
		edittext_username.setText("");
		edittext_password.setText("");
		edittext_crn.setText("");
		edittext_txnpin.setText("");
		textview_username_count.setText("0/8");

		// Changing Button Text to Login
		button_login.setText("Login");
	}

}
