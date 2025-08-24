package com.nitsoft.ipoallotmentprank;

import androidx.annotation.*;
import android.app.*;
import android.os.*;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.*;
import android.content.*;
import android.graphics.*;
import android.graphics.drawable.*;

import java.text.*;
import java.util.HashMap;
import java.util.ArrayList;

import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.ImageView;
import android.graphics.Typeface;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

//import com.suke.widget.*;

import androidx.fragment.app.Fragment;

public class WatchlistFragmentActivity extends  Fragment  {

	private SharedPreferences sharedPreferencesWatchlist;

	// Declare Views Here
	private TextView textview_temp;

	private TextView textview_app_title;
	private TextView textview_app_subtitle;

	private LinearLayout linear_card_menu;
	private LinearLayout linear_watchlist_main;
	private LinearLayout linear_cond_main;
	private ImageView imageview_cond_img;
	private TextView textview_cond_title;
	private TextView textview_cond_subtitle;
	private Button button_cond_reload;
	private LinearLayout linear_watchlist;
	private LinearLayout linear_watchlist_item_main;
	private TextView textview_watchlist_item_symbol;
	private TextView textview_watchlist_item_ltp;
	private TextView textview_watchlist_item_pclose;
	private TextView textview_watchlist_item_pchange;
	private TextView textview_watchlist_item_qty;
	private ImageView imageview_watchlist_item_indicator;
	private ListView listview_shimmer;
	private ListView listview_watchlist;

	// Variables Here
	String internetErrorMessage = "";

	private RequestNetwork requestCompanyData;
	private RequestNetwork.RequestListener requestCompanyDataListener;
	private RequestNetwork requestCompanySymbol;
	private RequestNetwork.RequestListener requestCompanySymbolListener;

	private ArrayList<HashMap <String, Object>> listmapRequestSymbol = new ArrayList<>();
	private ArrayList<HashMap <String, Object>> listmapSavedSymbol = new ArrayList<>();
	private ArrayList<HashMap<String, Object>> listmapLiveWatchList = new ArrayList<>();
	private ArrayList<HashMap<String, Object>> listmapShimmer = new ArrayList<>();

	@NonNull @Override public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.watchlist_fragment, container, false);
		initialize(savedInstanceState, view);
		com.google.firebase.FirebaseApp.initializeApp(getContext());
		onActivityCreate();
		return view;
	}
	@Override public void onResume() {
//		checkInternetAndRequestSymbol();
		super.onResume();
	}
	private void initialize(Bundle savedInstanceState, View view) {

		textview_temp = (TextView) view.findViewById(R.id.textview_temp);

		textview_app_title = (TextView) getActivity().findViewById(R.id.textview_app_title);
		textview_app_subtitle = (TextView) getActivity().findViewById(R.id.textview_app_subtitle);
		linear_card_menu = (LinearLayout) getActivity().findViewById(R.id.linear_card_menu);

		linear_watchlist_main = (LinearLayout) view.findViewById(R.id.linear_watchlist_main);
//		linear_cond_main = (LinearLayout) view.findViewById(R.id.linear_cond_main);
//		imageview_cond_img = (ImageView) view.findViewById(R.id.imageview_cond_img);
//		textview_cond_title = (TextView) view.findViewById(R.id.textview_cond_title);
//		textview_cond_subtitle = (TextView) view.findViewById(R.id.textview_cond_subtitle);
//		button_cond_reload = (Button) view.findViewById(R.id.button_cond_reload);
		linear_watchlist = (LinearLayout) view.findViewById(R.id.linear_watchlist);
		linear_watchlist_item_main = (LinearLayout) view.findViewById(R.id.linear_watchlist_item_main);
		textview_watchlist_item_symbol = (TextView) view.findViewById(R.id.textview_watchlist_item_symbol);
		textview_watchlist_item_ltp = (TextView) view.findViewById(R.id.textview_watchlist_item_ltp);
		textview_watchlist_item_pclose = (TextView) view.findViewById(R.id.textview_watchlist_item_pclose);
		textview_watchlist_item_pchange = (TextView) view.findViewById(R.id.textview_watchlist_item_pchange);
		textview_watchlist_item_qty = (TextView) view.findViewById(R.id.textview_watchlist_item_qty);
		imageview_watchlist_item_indicator = (ImageView) view.findViewById(R.id.imageview_watchlist_item_indicator);
		listview_shimmer = (ListView) view.findViewById(R.id.listview_shimmer);
		listview_watchlist = (ListView) view.findViewById(R.id.listview_watchlist);

		requestCompanyData = new RequestNetwork((Activity)getContext());
		requestCompanySymbol = new RequestNetwork((Activity)getContext());

		sharedPreferencesWatchlist = getContext().getSharedPreferences("watchlistSymbol", Context.MODE_PRIVATE);

		requestCompanyDataListener = new RequestNetwork.RequestListener() {
			@Override
			public void onResponse(String tag, String response, HashMap<String, Object> responseHeaders) {

				listview_shimmer.setVisibility(View.GONE);
				linear_cond_main.setVisibility(View.GONE);
				linear_watchlist.setVisibility(View.VISIBLE);

				listmapLiveWatchList = new Gson().fromJson(response, new TypeToken<ArrayList<HashMap<String, Object>>>(){}.getType());
				listview_watchlist.setAdapter(new Listview_companyAdapter(listmapLiveWatchList));
				((BaseAdapter)listview_watchlist.getAdapter()).notifyDataSetChanged();
				Utilities.hideProgressDialog();														// Hiding Adding To Watchlist Dialog
			}

			@Override
			public void onErrorResponse(String tag, String message) {
				condViewVisibility(true, "Error Occured", "There was an error fetching data. Please try again later.", R.drawable.il_error, false);
				Utilities.hideProgressDialog();														// Hiding Adding To Watchlist Dialog
			}
		};
		requestCompanySymbolListener = new RequestNetwork.RequestListener() {
			@Override
			public void onResponse(String tag, String response, HashMap<String, Object> responseHeaders) {
				// Company Symbol Fetch Successful - Response Here
				listmapRequestSymbol = new Gson().fromJson(response, new TypeToken<ArrayList<HashMap<String, Object>>>(){}.getType());
			}

			@Override
			public void onErrorResponse(String tag, String message) {
				// Company Symbol Fetch Failed - Error Handling Here
				if(!SketchwareUtil.isConnected(getContext())) {
					internetErrorMessage = "nointernet";
					condViewVisibility(true, "No Internet Connection", "No internet connection detected. Please check your connection and try again.", R.drawable.il_error, true);
				} else if (message.equals("timeout")){
					internetErrorMessage = message;
					condViewVisibility(true, "Connection Timeout", "An error occurred while processing your request. Please try again later. We apologize for the inconvenience.", R.drawable.il_error, true);
				} else {
					condViewVisibility(true, "Error Occured", "There was an error fetching data. Please try again later.", R.drawable.il_error, false);

				}
			}
		};

		button_cond_reload.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				// Check if the error is no-internet or connection timeout
				if(internetErrorMessage.equals("nointernet")){
					// No Internet Connection - Show No Internet Connection Illustration and Recheck if available now
					condViewVisibility(true,"No Internet Connection", "No internet connection detected. Please check your connection and try again.", R.drawable.il_error, false);
					requestCompanySymbol.startRequestNetwork(RequestNetworkController.GET, "https://hsmapi.com/api/watchlist/get_companies_symbol?stocks=watchlist", "internetRefresh", requestCompanySymbolListener);
					checkInternetAndRequestSymbol();
				} else if(internetErrorMessage.equals("timeout")){
					// Connection Timeout - Show No Internet Connection Illustration and Try Feching Data Symbol Again
					requestCompanySymbol.startRequestNetwork(RequestNetworkController.GET, "https://hsmapi.com/api/watchlist/get_companies_symbol?stocks=watchlist", "timeoutRefresh", requestCompanySymbolListener);
				}
			}
		});
		linear_card_menu.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				// Checking If The Symbol List Has Been Loaded Or Not
				if(listmapRequestSymbol.isEmpty()) {
					// The Data Hasn't been Loaded Yet
					Toast.makeText(getContext(), "Loading Data", Toast.LENGTH_SHORT).show();
				} else {
					// Showing Search Dialog Along with Company Name and Symbol Here
					showSearchableDialog();
				}
			}
		});
	}


	private void onActivityCreate(){
		userInterface();
		initializeLogic();
	}
	private void initializeLogic() {
		for(int i = 0; i < (int)(25); i++) {
			{
				HashMap<String, Object> _item = new HashMap<>();
				_item.put("iporesult", "no. one app");
				listmapShimmer.add(_item);
			}
		}
		listview_shimmer.setAdapter(new Listview_shimmerAdapter(listmapShimmer));
		((BaseAdapter)listview_shimmer.getAdapter()).notifyDataSetChanged();
		Utilities.disableScrolling(listview_shimmer);

	}
	private void userInterface() {

		Utilities.hide_scrollbar(listview_shimmer);
		Utilities.hide_scrollbar(listview_watchlist);

		Utilities.cornerRadiusWithStroke(linear_watchlist_item_main, "#383f66", "#383f66", 0, 20, 20, 20, 20, false);
		Utilities.cornerRadiusWithStroke(linear_watchlist, "#383f66", "#383f66", 0, 20, 20, 20, 20, false);
		Utilities.setBackground(button_cond_reload, 15, 0,"#383F66", true);

		// Custom Fonts if Needed
		Utilities.setFont(getContext(), textview_cond_title, "quicksand.ttf", Typeface.BOLD);
		Utilities.setFont(getContext(), textview_cond_subtitle, "josefinsans_regular.ttf", Typeface.NORMAL);
		Utilities.setFont(getContext(), textview_watchlist_item_symbol, "sansation_regular.ttf", Typeface.BOLD);
		Utilities.setFont(getContext(), textview_watchlist_item_ltp, "sansation_regular.ttf", Typeface.BOLD);
		Utilities.setFont(getContext(), textview_watchlist_item_pclose, "sansation_regular.ttf", Typeface.BOLD);
		Utilities.setFont(getContext(), textview_watchlist_item_pchange, "sansation_regular.ttf", Typeface.BOLD);
		Utilities.setFont(getContext(), textview_watchlist_item_qty, "sansation_regular.ttf", Typeface.BOLD);

	}
	private void condViewVisibility( boolean condVisibility, String title, String subtitle, int image, boolean btnVisibility){
		if(condVisibility){
			linear_watchlist.setVisibility(View.GONE);
			linear_cond_main.setVisibility(View.VISIBLE);
			imageview_cond_img.setImageResource(image);
			textview_cond_title.setText(title);
			textview_cond_subtitle.setText(subtitle);
			if(btnVisibility){
				button_cond_reload.setVisibility(View.VISIBLE);
			} else {
				button_cond_reload.setVisibility(View.GONE);
			}
		} else {
			linear_cond_main.setVisibility(View.GONE);
			linear_watchlist.setVisibility(View.VISIBLE);
		}
	}

	private void checkInternetAndRequestSymbol(){
		// Check if device is connected to internet on Activity Create
		if(!SketchwareUtil.isConnected(getContext())){
			// Internet is not connected - Show No Internet Connection Illustration
			internetErrorMessage = "nointernet";
			condViewVisibility(
					true,
					"No Internet Connection",
					"No internet connection detected. Please check your connection and try again.",
					 R.drawable.il_error,
					true
			);

		} else {
			// Internet is connected - Fetch Data
			requestCompanySymbol.startRequestNetwork(RequestNetworkController.GET, "https://hsmapi.com/api/watchlist/get_companies_symbol?stocks=watchlist", "companySymbol", requestCompanySymbolListener);
			condViewVisibility(false,"", "", R.drawable.il_empty,false);
			checkAndRequestSymbolData();
		}
	}
	private void checkAndRequestSymbolData(){
		// Checking if there is any data in watchlist
		if (sharedPreferencesWatchlist.getString("watchlistSymbol", "").toString().equals("")) {
			// No Data In Watchlist - Show Empty Illustration
			condViewVisibility(true,
					"No Data Found",
					"Add stocks to your watchlist by clicking on '+' icon at top-right corner of the screen",
					 R.drawable.il_empty,
					false);

			textview_temp.setText("Empty");

		} else {

			textview_temp.setText(sharedPreferencesWatchlist.getString("watchlistSymbol", "").toString());
			// Data In Watchlist - Fetch Data
			listmapSavedSymbol = new Gson().fromJson(sharedPreferencesWatchlist.getString("watchlistSymbol", ""), new TypeToken<ArrayList<HashMap<String, Object>>>(){}.getType());
			// User Has Some Existing Symbols In Watchlist - Fetch Their Data
			StringBuilder stockParams = new StringBuilder();                                        // StringBuilder to efficiently create the stockParams string
			// Loop through the list and append the symbol to stockParams with a comma
			for (int i = 0; i < listmapSavedSymbol.size(); i++) {
				String symbol = listmapSavedSymbol.get(i).get("symbol").toString();                 // Get the symbol from the current HashMap
				stockParams.append(symbol);                                                         // Append symbol to stockParams, followed by a comma if it's not the last element
				if (i < listmapSavedSymbol.size() - 1) {
					stockParams.append(",");                                                        // Add a comma if it's not the last symbol in the list
				}
			}
			String stockParamsString = stockParams.toString();                                      // Convert the StringBuilder to String
			String requestUrl = "https://hsmapi.com/api/watchlist/get_companies_data?stocks=" + stockParamsString;

			requestCompanyData.startRequestNetwork(RequestNetworkController.GET, requestUrl, "companyData", requestCompanyDataListener);
		}
	}
	
	public class Listview_shimmerAdapter extends BaseAdapter {
		ArrayList<HashMap<String, Object>> _data;
		public Listview_shimmerAdapter(ArrayList<HashMap<String, Object>> _arr) {
			_data = _arr;
		}

		@Override
		public int getCount() {
			return _data.size();
		}

		@Override
		public HashMap<String, Object> getItem(int _index) {
			return _data.get(_index);
		}

		@Override
		public long getItemId(int _index) {
			return _index;
		}
		@Override
		public View getView(final int position, View _v, ViewGroup _container) {
			LayoutInflater _inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View view = _v;
			if (view == null) {
				view = _inflater.inflate(R.layout.watchlist_item_shimmer, null);
			}

			final LinearLayout linear_item = (LinearLayout) view.findViewById(R.id.linear_item);
			final com.facebook.shimmer.ShimmerFrameLayout linear1 = (com.facebook.shimmer.ShimmerFrameLayout) view.findViewById(R.id.linear1);
			final com.facebook.shimmer.ShimmerFrameLayout linear2 = (com.facebook.shimmer.ShimmerFrameLayout) view.findViewById(R.id.linear2);
			final com.facebook.shimmer.ShimmerFrameLayout linear3 = (com.facebook.shimmer.ShimmerFrameLayout) view.findViewById(R.id.linear3);
			final com.facebook.shimmer.ShimmerFrameLayout linear4 = (com.facebook.shimmer.ShimmerFrameLayout) view.findViewById(R.id.linear4);
			final com.facebook.shimmer.ShimmerFrameLayout linear5 = (com.facebook.shimmer.ShimmerFrameLayout) view.findViewById(R.id.linear5);
			final com.facebook.shimmer.ShimmerFrameLayout linear6 = (com.facebook.shimmer.ShimmerFrameLayout) view.findViewById(R.id.linear6);

			linear1.setBackground(new GradientDrawable() { public GradientDrawable getIns(int a, int b) { this.setCornerRadius(a); this.setColor(b); return this; } }.getIns((int)20, 0xFFE0E0E0));
			linear2.setBackground(new GradientDrawable() { public GradientDrawable getIns(int a, int b) { this.setCornerRadius(a); this.setColor(b); return this; } }.getIns((int)20, 0xFFE0E0E0));
			linear3.setBackground(new GradientDrawable() { public GradientDrawable getIns(int a, int b) { this.setCornerRadius(a); this.setColor(b); return this; } }.getIns((int)20, 0xFFE0E0E0));
			linear4.setBackground(new GradientDrawable() { public GradientDrawable getIns(int a, int b) { this.setCornerRadius(a); this.setColor(b); return this; } }.getIns((int)20, 0xFFE0E0E0));
			linear5.setBackground(new GradientDrawable() { public GradientDrawable getIns(int a, int b) { this.setCornerRadius(a); this.setColor(b); return this; } }.getIns((int)20, 0xFFE0E0E0));
			linear6.setBackground(new GradientDrawable() { public GradientDrawable getIns(int a, int b) { this.setCornerRadius(a); this.setColor(b); return this; } }.getIns((int)20, 0xFFE0E0E0));

			return view;
		}
	}
	public class Listview_companyAdapter extends BaseAdapter {
		ArrayList<HashMap<String, Object>> _data;
		public Listview_companyAdapter(ArrayList<HashMap<String, Object>> _arr) {
			_data = _arr;
		}

		@Override
		public int getCount() {
			return _data.size();
		}

		@Override
		public HashMap<String, Object> getItem(int _index) {
			return _data.get(_index);
		}

		@Override
		public long getItemId(int _index) {
			return _index;
		}
		@Override
		public View getView(final int position, View v, ViewGroup container) {
			LayoutInflater _inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View view = v;
			if (view == null) {
				view = _inflater.inflate(R.layout.watchlist_item, null);
			}

			final LinearLayout linear_watchlist_item_main = (LinearLayout) view.findViewById(R.id.linear_watchlist_item_main);
			final TextView textview_watchlist_item_symbol = (TextView) view.findViewById(R.id.textview_watchlist_item_symbol);
			final TextView textview_watchlist_item_ltp = (TextView) view.findViewById(R.id.textview_watchlist_item_ltp);
			final TextView textview_watchlist_item_pclose = (TextView) view.findViewById(R.id.textview_watchlist_item_pclose);
			final TextView textview_watchlist_item_pchange = (TextView) view.findViewById(R.id.textview_watchlist_item_pchange);
			final TextView textview_watchlist_item_qty = (TextView) view.findViewById(R.id.textview_watchlist_item_qty);
			final ImageView imageview_watchlist_item_indicator = (ImageView) view.findViewById(R.id.imageview_watchlist_item_indicator);


			if (listmapLiveWatchList.get((int)position).containsKey("symbol")) {
				textview_watchlist_item_symbol.setText(listmapLiveWatchList.get((int)position).get("symbol").toString());
			}
			if (listmapLiveWatchList.get((int)position).containsKey("close")) {
				textview_watchlist_item_ltp.setText(listmapLiveWatchList.get((int)position).get("close").toString());
			}
			if (listmapLiveWatchList.get((int)position).containsKey("open")) {
				textview_watchlist_item_pclose.setText(listmapLiveWatchList.get((int)position).get("open").toString());
			}
			if (listmapLiveWatchList.get((int)position).containsKey("percentage_change")) {
				textview_watchlist_item_pchange.setText(new DecimalFormat("0.01").format(Double.parseDouble(listmapLiveWatchList.get((int) position).get("percentage_change").toString())).concat(" %"));
			}
			if (listmapLiveWatchList.get((int)position).containsKey("curr_volume")) {
				textview_watchlist_item_qty.setText(listmapLiveWatchList.get((int)position).get("curr_volume").toString());
			}

			// Fonts
			Utilities.setFont(getContext(), textview_watchlist_item_symbol, "sansation_regular.ttf", Typeface.BOLD);
			Utilities.setFont(getContext(), textview_watchlist_item_ltp, "sansation_regular.ttf", Typeface.BOLD);
			Utilities.setFont(getContext(), textview_watchlist_item_pclose, "sansation_regular.ttf", Typeface.BOLD);
			Utilities.setFont(getContext(), textview_watchlist_item_pchange, "sansation_regular.ttf", Typeface.BOLD);
			Utilities.setFont(getContext(), textview_watchlist_item_qty, "sansation_regular.ttf", Typeface.BOLD);

			Utilities.cornerRadiusWithStroke(linear_watchlist_item_main, "#383F66", "#383F66", 0, 20, 20, 20, 20, true);

			if (textview_watchlist_item_pchange.getText().toString().contains("-")) {
				imageview_watchlist_item_indicator.setRotation((float)(180));
				imageview_watchlist_item_indicator.setImageResource(R.drawable.indicator_1);
				imageview_watchlist_item_indicator.setColorFilter(0xFFF44336, PorterDuff.Mode.MULTIPLY);
			}
			if (!textview_watchlist_item_pchange.getText().toString().contains("-") && textview_watchlist_item_pchange.getText().toString().equals("0.00")) {
				imageview_watchlist_item_indicator.setRotation((float)(90));
				imageview_watchlist_item_indicator.setImageResource(R.drawable.equals);
				imageview_watchlist_item_indicator.setColorFilter(0xFFFFFFFF, PorterDuff.Mode.MULTIPLY);
			}
			else {
				if (!textview_watchlist_item_pchange.getText().toString().contains("-")) {
					imageview_watchlist_item_indicator.setRotation((float)(0));
					imageview_watchlist_item_indicator.setImageResource(R.drawable.indicator_1);
					imageview_watchlist_item_indicator.setColorFilter(0xFF8BC34A, PorterDuff.Mode.MULTIPLY);
				}
			}
			// Company Long Click
			linear_watchlist_item_main.setOnLongClickListener(new View.OnLongClickListener() {
				@Override
				public boolean onLongClick(View view) {
					// Show Dialog That Lets Users To Delete The Existing Company From Watchlist
					String symbolToDelete = listmapLiveWatchList.get((int) position).get("symbol").toString();

					Utilities.negativeDialog(getContext(), "Delete Company",
							"Are you sure you want to delete " + symbolToDelete + " from your watchlist?", "Delete", new View.OnClickListener() {

								@Override
								public void onClick(View view) {
									// Remove the symbol from listmapSavedSymbol based on the symbol
									for (int i = 0; i < listmapSavedSymbol.size(); i++) {
										if (listmapSavedSymbol.get(i).get("symbol").toString().equals(symbolToDelete)) {
											listmapSavedSymbol.remove(i);
											break;  // Exit the loop once the symbol is found and removed
										}
									}

									// Remove the symbol from listmapLiveWatchList based on the symbol
									for (int i = 0; i < listmapLiveWatchList.size(); i++) {
										if (listmapLiveWatchList.get(i).get("symbol").toString().equals(symbolToDelete)) {
											listmapLiveWatchList.remove(i);
											break;  // Exit the loop once the symbol is found and removed
										}
									}

									// Save the updated list back to SharedPreferences
									sharedPreferencesWatchlist.edit().putString("watchlistSymbol", new Gson().toJson(listmapSavedSymbol)).apply();

									// Updating Watchlist on ListView
									listview_watchlist.setAdapter(new Listview_companyAdapter(listmapLiveWatchList));
									((BaseAdapter) listview_watchlist.getAdapter()).notifyDataSetChanged();

									// Checking If All Items Are Deleted
									if (listmapSavedSymbol.isEmpty() && listmapLiveWatchList.isEmpty()) {
										condViewVisibility(true, "No Data Found", "Add stocks to your watchlist by clicking on '+' icon at top-right corner of the screen", R.drawable.il_empty, false);
										sharedPreferencesWatchlist.edit().putString("watchlistSymbol", "").apply();
									}
									textview_temp.setText(sharedPreferencesWatchlist.getString("watchlistSymbol", "").toString());
								}
							});
					return false;
				}
			});

			return view;
		}
	}

	// Search Dialog - Symbol
	private void showSearchableDialog() {
		// Create a custom dialog
		final Dialog dialog = new Dialog(getContext());
		dialog.setContentView(R.layout.searchable_spinner);

		// Get references to the EditText and ListView
		EditText editTextSearch = dialog.findViewById(R.id.edittext_search);
		ListView listViewItems = dialog.findViewById(R.id.listview_items);

		// Initialize the adapter
		final searchListviewAdapter adapter = new searchListviewAdapter(listmapRequestSymbol);
		listViewItems.setAdapter(adapter);

		// Add a text change listener to the EditText to filter the ListView
		editTextSearch.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// Call filterList method when text is changed
				adapter.filterList(s.toString());
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});

		// Set an OnItemClickListener to get the symbol when an item is clicked
		// On Item Clicked
		listViewItems.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// Retrieve existing list from SharedPreferences (if it exists)
				String existingSymbolsJson = sharedPreferencesWatchlist.getString("watchlistSymbol", "");
				listmapSavedSymbol = new Gson().fromJson(existingSymbolsJson, new TypeToken<ArrayList<HashMap<String, Object>>>(){}.getType());

				// If the list is null (e.g., first time usage), initialize it as an empty list
				if (listmapSavedSymbol == null) { listmapSavedSymbol = new ArrayList<>();}

				// Get the selected company's symbol from the filtered list
				HashMap<String, Object> hashmapSelectedSymbol = new HashMap<>();
				hashmapSelectedSymbol.put("symbol", listmapRequestSymbol.get(position).get("symbol").toString());

				// Add the selected symbol to the list (if it's not already there)
				if (!listmapSavedSymbol.contains(hashmapSelectedSymbol)) {
					listmapSavedSymbol.add(hashmapSelectedSymbol);
				}

				// Convert the updated listmap to JSON and save it to SharedPreferences
				sharedPreferencesWatchlist.edit().putString("watchlistSymbol", new Gson().toJson(listmapSavedSymbol)).apply();
				Utilities.showProgressDialog(getContext(), "Adding" + " " + listmapRequestSymbol.get(position).get("symbol").toString() + " to Watchlist...");
				checkAndRequestSymbolData();

				adapter.resetFilter();						// Reset Search Filter When An Item Has Been Selected
				dialog.dismiss();							// Dismiss Search Dialog
			}
		});
		// Show the dialog
		dialog.show();
	}
	// Search ListView Adapter
	public class searchListviewAdapter extends BaseAdapter {
		ArrayList<HashMap<String, Object>> listmapCompanySymbol;
		ArrayList<HashMap<String, Object>> originalList;

		// Constructor
		public searchListviewAdapter(ArrayList<HashMap<String, Object>> listmapCompanySymbol) {
			this.listmapCompanySymbol = listmapCompanySymbol;
			this.originalList = new ArrayList<>(listmapCompanySymbol); // Store the original list
		}

		@Override
		public int getCount() {
			return listmapCompanySymbol.size();
		}

		@Override
		public Object getItem(int i) {
			return null;
		}

		@Override
		public long getItemId(int i) {
			return 0;
		}

		@Override
		public View getView(int position, View view, ViewGroup viewGroup) {
			view = View.inflate(getContext(), R.layout.custom_select_company_spinner, null);
			TextView textview_company_name = (TextView) view.findViewById(R.id.textview_spinner_select_company_custom);

			if (listmapCompanySymbol.get(position).containsKey("symbol") && listmapCompanySymbol.get(position).containsKey("name")) {
				textview_company_name.setText("(" + listmapCompanySymbol.get(position).get("symbol").toString() + ") " + listmapCompanySymbol.get(position).get("name").toString());
			}
			return view;
		}

		// Method to filter the list based on search input
		public void filterList(String query) {
			listmapCompanySymbol.clear();
			if (query.isEmpty()) {
				listmapCompanySymbol.addAll(originalList);  // If query is empty, show all items
			} else {
				for (HashMap<String, Object> company : originalList) {
					// Check if the symbol or name contains the query (case-insensitive)
					if (company.get("symbol").toString().toLowerCase().contains(query.toLowerCase()) ||
							company.get("name").toString().toLowerCase().contains(query.toLowerCase())) {
						listmapCompanySymbol.add(company);
					}
				}
			}
			notifyDataSetChanged();  // Notify the adapter that the data has changed
		}
		// Method to reset the filtered list to original
		public void resetFilter() {
			listmapCompanySymbol.clear();
			listmapCompanySymbol.addAll(originalList); // Restore the original list
			notifyDataSetChanged(); // Refresh the list view
		}
	}
}