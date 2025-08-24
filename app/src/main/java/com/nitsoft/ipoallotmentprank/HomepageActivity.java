package com.nitsoft.ipoallotmentprank;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.view.*;
import android.widget.*;
import android.content.*;
import android.graphics.*;
import android.text.*;

import java.util.HashMap;
import java.util.ArrayList;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ImageView;

import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager.widget.PagerAdapter;

//import com.google.android.ads.mediationtestsuite.MediationTestSuite;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.AdRequest;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.DialogInterface;

import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import android.app.Activity;
import android.content.SharedPreferences;
import java.util.Calendar;

import android.os.Bundle;
import android.content.Intent;

import java.util.Timer;

import android.view.View;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import android.graphics.Typeface;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
//import com.suke.widget.*;

import androidx.recyclerview.widget.*;
import androidx.recyclerview.widget.RecyclerView;

public class HomepageActivity extends  AppCompatActivity  {


	private HashMap<String, Object> Map = new HashMap<>();
	private double random = 0;
	private String result = "";
	private String Edittext = "";
	private double difference = 0;
	private double saved_time = 0;
	private HashMap<String, Object> Map_Boid = new HashMap<>();
	private String Homepage;
	private String Name_Edit = "";
	private String Boid_Edit = "";
	private  RecyclerView recyclerview1;
	private  TextView l1;
	private double position;
	private HashMap<String, Object>UpdatifyMap = new HashMap<>();
	private double homepage_menubtn_adnumber = 0;

	private SharedPreferences sharedPreferencesBoidList;
	private ArrayList<HashMap<String, Object>> listmapBoidList = new ArrayList<>();

	
	private LinearLayout main_linear;
	private LinearLayout linear_bottomnav;
	private LinearLayout linear_tophead;
	private LinearLayout linear3;
	private LinearLayout linear7;
	private LinearLayout linear8;
	private TextView textview_app_title;
	private TextView textview_app_subtitle;
	private LinearLayout linear_card_menu;
	private ImageView imageview_menu;
	private ViewPager viewpager1;
	private LinearLayout linear9;
	private AdView adview1;
	private LinearLayout parent_linear_bottom_nav;
	private LinearLayout linear_bottom_nav;
	private LinearLayout parent_linear_home;
	private LinearLayout parent_linear_result;
	private LinearLayout parent_linear_boid;
	private LinearLayout parent_linear_news;
	private LinearLayout parent_linear_about;
	private ImageView imageview_home;
	private TextView textview_home;
	private ImageView imageview_fake;
	private TextView textview_fake;
	private LinearLayout circular_parent_linear_plus_btn;
	private TextView textview_boid;
	private ImageView imageview_plus;
	private ImageView imageview_more;
	private TextView textview_news;
	private ImageView imageview_about;
	private TextView textview_about;
	
	private ObjectAnimator refresh_anim = new ObjectAnimator();
	private AlertDialog.Builder boid_dialog;
	private FirebaseAuth Auth;
	private ChildEventListener _Result_child_listener;
	private SharedPreferences PushNotification;
	private SharedPreferences Animation;
	private AlertDialog.Builder delete_dialog;
	private Calendar cal_duration = Calendar.getInstance();
	private Intent intent = new Intent();
	private AlertDialog.Builder fake_info_dialog;
	private SharedPreferences First;
	private RequestNetwork Req;
	private RequestNetwork.RequestListener _Req_request_listener;
	@SuppressLint("MissingPermission")
	@Override
	protected void onCreate(Bundle _savedInstanceState) {
		super.onCreate(_savedInstanceState);
		setContentView(R.layout.homepage);

		MobileAds.initialize(this, new OnInitializationCompleteListener() {
			@Override
			public void onInitializationComplete(InitializationStatus initializationStatus) {
			}
		});
		initialize(_savedInstanceState);
		com.google.firebase.FirebaseApp.initializeApp(this);
		initializeLogic();


		AdRequest adRequest = new AdRequest.Builder().build();
		AdView banner_ad_homepage = findViewById(R.id.adview1);
		banner_ad_homepage.loadAd(adRequest);

		banner_ad_homepage.setAdListener(new AdListener() {
			@Override
			public void onAdLoaded() {
				// Code to be executed when an ad finishes loading.
				banner_ad_homepage.setVisibility(View.VISIBLE);
			}

			@Override
			public void onAdFailedToLoad(LoadAdError adError) {
				// Code to be executed when an ad fails to load
			}

			@Override
			public void onAdOpened() {
				// Code to be executed when an ad opens an overlay that
				// covers the screen.
			}

			@Override
			public void onAdClicked() {
				// Code to be executed when the user clicks on an ad.
			}

			@Override
			public void onAdClosed() {
				// Code to be executed when the user is about to return
				// to the app after tapping on an ad.
			}
		});

//		MediationTestSuite.launch(HomepageActivity.this);

	}
	private void initialize(Bundle _savedInstanceState) {

		main_linear = (LinearLayout) findViewById(R.id.main_linear);
		linear_bottomnav = (LinearLayout) findViewById(R.id.linear_bottomnav);
		linear_tophead = (LinearLayout) findViewById(R.id.linear_tophead);
		linear3 = (LinearLayout) findViewById(R.id.linear3);
		linear7 = (LinearLayout) findViewById(R.id.linear7);
		linear8 = (LinearLayout) findViewById(R.id.linear8);
		textview_app_title = (TextView) findViewById(R.id.textview_app_title);
		textview_app_subtitle = (TextView) findViewById(R.id.textview_app_subtitle);
		linear_card_menu = (LinearLayout) findViewById(R.id.linear_card_menu);
		imageview_menu = (ImageView) findViewById(R.id.imageview_menu);
		viewpager1 = (ViewPager) findViewById(R.id.viewpager1);
		linear9 = (LinearLayout) findViewById(R.id.linear9);
		adview1 = (AdView) findViewById(R.id.adview1);
		parent_linear_bottom_nav = (LinearLayout) findViewById(R.id.parent_linear_bottom_nav);
		linear_bottom_nav = (LinearLayout) findViewById(R.id.linear_bottom_nav);
		parent_linear_home = (LinearLayout) findViewById(R.id.parent_linear_home);
		parent_linear_result = (LinearLayout) findViewById(R.id.parent_linear_result);
		parent_linear_boid = (LinearLayout) findViewById(R.id.parent_linear_boid);
		parent_linear_news = (LinearLayout) findViewById(R.id.parent_linear_news);
		parent_linear_about = (LinearLayout) findViewById(R.id.parent_linear_about);
		imageview_home = (ImageView) findViewById(R.id.imageview_home);
		textview_home = (TextView) findViewById(R.id.textview_home);
		imageview_fake = (ImageView) findViewById(R.id.imageview_fake);
		textview_fake = (TextView) findViewById(R.id.textview_fake);
		circular_parent_linear_plus_btn = (LinearLayout) findViewById(R.id.circular_parent_linear_plus_btn);
		textview_boid = (TextView) findViewById(R.id.textview_boid);
		imageview_plus = (ImageView) findViewById(R.id.imageview_plus);
		imageview_more = (ImageView) findViewById(R.id.imageview_more);
		textview_news = (TextView) findViewById(R.id.textview_news);
		imageview_about = (ImageView) findViewById(R.id.imageview_about);
		textview_about = (TextView) findViewById(R.id.textview_about);
		boid_dialog = new AlertDialog.Builder(this);
		Auth = FirebaseAuth.getInstance();
		PushNotification = getSharedPreferences("Notification", Activity.MODE_PRIVATE);
		Animation = getSharedPreferences("Animation", Activity.MODE_PRIVATE);
		delete_dialog = new AlertDialog.Builder(this);
		fake_info_dialog = new AlertDialog.Builder(this);
		First = getSharedPreferences("first", Activity.MODE_PRIVATE);
		Req = new RequestNetwork(this);

		linear_card_menu.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				random++;
				if (viewpager1.getCurrentItem() == 0) {
					// Show dialog with whats new message
					Drawable icon = ContextCompat.getDrawable(getApplicationContext(), Utilities.whatsnewIcon);
					Utilities.alertDialog(HomepageActivity.this,
							Utilities.whatsnewTitle,
							icon,
							Utilities.whatsnewMessage,
							Utilities.whatsnewButton,
							null,
							null,
							null);
				}
				if (viewpager1.getCurrentItem() == 1) {
					// Show fake result  disclaimer dialog from utilities class - alertDialog
					Utilities.alertDialog(
							HomepageActivity.this,
							"Fake Result",
							null,
							"These results are purely for entertainment, don’t take them too seriously. " +
									"Feel free to share and laugh with your friends! Works with any random boid number.\n\n" +
									"Note: Long click on result button or select a company to change the fake result and get a new one.",
							null,
							"Okay",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialogInterface, int i) {
									// Positive button action here
									// Refresh the result and show a toast message
									Toast.makeText(HomepageActivity.this, "Result refreshed", Toast.LENGTH_SHORT).show();
								}
							},
							null
					);
				}
				if (viewpager1.getCurrentItem() == 2) {
					boid_dialog.setTitle("Saving BOID Number");
					boid_dialog.setMessage("This feature allows users to locally save their boid numbers, enabling quick access to check multiple results at one click. It is particularly beneficial for checking multiple results. Users can easily modify or delete their saved data as needed.");
					boid_dialog.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface _dialog, int _which) {

						}
					});
					boid_dialog.create().show();
				}
				if (viewpager1.getCurrentItem() == 3) {
					boid_dialog.setTitle("Boid Manager");
					boid_dialog.setMessage("Here you can copy, edit or delete your boid numbers.");
					boid_dialog.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface _dialog, int _which) {

						}
					});
					boid_dialog.create().show();
				}
			}
		});

		viewpager1.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			@Override
			public void onPageScrolled(int _position, float _positionOffset, int _positionOffsetPixels) {
				refresh_anim.cancel();
			}

			@Override
			public void onPageSelected(int _position) {
				if (_position == 0) {
					btmnavHomeClickUi();
					textview_app_title.setText("IPO Allotment Result");
					textview_app_subtitle.setText("Real single result, one at a time!");
					textview_app_subtitle.setVisibility(View.VISIBLE);
					imageview_menu.setImageResource(R.drawable.menu);
					linear_card_menu.setVisibility(View.VISIBLE);
					viewpager1.setCurrentItem((int) viewpager1.getCurrentItem());
					imageview_menu.setRotation((float) (0));
					((PagerAdapter) viewpager1.getAdapter()).notifyDataSetChanged();
					textview_app_title.setVisibility(View.VISIBLE);
					adview1.setVisibility(View.VISIBLE);
				}
				if (_position == 1) {
					btmnavFakeClickUi();
					textview_app_title.setText("Bulk IPO Result");
					textview_app_subtitle.setText("Click once then sit back and relax!");
					textview_app_subtitle.setVisibility(View.VISIBLE);
					imageview_menu.setImageResource(R.drawable.icons_2);
					linear_card_menu.setVisibility(View.VISIBLE);
					viewpager1.setCurrentItem((int) viewpager1.getCurrentItem());
					imageview_menu.setRotation((float) (0));
					((PagerAdapter) viewpager1.getAdapter()).notifyDataSetChanged();
					textview_app_title.setVisibility(View.VISIBLE);
					adview1.setVisibility(View.VISIBLE);
				}
				if (_position == 2) {
					btmnavBoidClickUi();
					textview_app_title.setText("Add Boid numbers");
					textview_app_subtitle.setText("Save boid numbers for quick result check!");
					textview_app_subtitle.setVisibility(View.VISIBLE);
					imageview_menu.setImageResource(R.drawable.icons_1);
					linear_card_menu.setVisibility(View.VISIBLE);
					viewpager1.setCurrentItem((int) viewpager1.getCurrentItem());
					imageview_menu.setRotation((float) (0));
					((PagerAdapter) viewpager1.getAdapter()).notifyDataSetChanged();
					textview_app_title.setVisibility(View.VISIBLE);
					adview1.setVisibility(View.VISIBLE);
				}
				if (_position == 3) {
					btmnavNewsClickUi();
					textview_app_title.setText("Boid Manager");
					textview_app_subtitle.setText("Copy, edit and delete your boids!");
					textview_app_subtitle.setVisibility(View.VISIBLE);
					linear_card_menu.setVisibility(View.VISIBLE);
					viewpager1.setCurrentItem((int) viewpager1.getCurrentItem());
					imageview_menu.setRotation((float) (0));
					((PagerAdapter) viewpager1.getAdapter()).notifyDataSetChanged();
					adview1.setVisibility(View.GONE);
					imageview_menu.setImageResource(R.drawable.icons_1);
				}
				if (_position == 4) {
					btmnavAboutClickUi();
					textview_app_title.setText("About");
					textview_app_subtitle.setText("Know more about us!");
					textview_app_subtitle.setVisibility(View.GONE);
					linear_card_menu.setVisibility(View.GONE);
					viewpager1.setCurrentItem((int) viewpager1.getCurrentItem());
					imageview_menu.setRotation((float) (0));
					((PagerAdapter) viewpager1.getAdapter()).notifyDataSetChanged();
					adview1.setVisibility(View.VISIBLE);
				}

			}

			@Override
			public void onPageScrollStateChanged(int _scrollState) {

			}
		});
		_Req_request_listener = new RequestNetwork.RequestListener() {
			@Override
			public void onResponse(String _param1, String _param2, HashMap<String, Object> _param3) {
				final String _tag = _param1;
				final String _response = _param2;
				final HashMap<String, Object> _responseHeaders = _param3;
				textview_app_subtitle.setText("Your result is ready!");
			}

			@Override
			public void onErrorResponse(String _param1, String _param2) {
				final String _tag = _param1;
				final String _message = _param2;
				textview_app_subtitle.setText("Server is busy!");
			}
		};

		// BOTTOM NAV BUTTONS CLICK LISTENERS
		parent_linear_home.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				viewpager1.setCurrentItem((int) 0);
				_setBackground(parent_linear_home, 20, 0, "#555b7b", true);
				btmnavHomeClickUi();
			}
		});
		parent_linear_result.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				viewpager1.setCurrentItem((int) 1);
				btmnavFakeClickUi();
			}
		});
		parent_linear_boid.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				viewpager1.setCurrentItem((int) 2);
				btmnavBoidClickUi();
			}
		});
		parent_linear_news.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				viewpager1.setCurrentItem((int) 3);
				btmnavNewsClickUi();
			}
		});
		parent_linear_about.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				viewpager1.setCurrentItem((int) 4);
				btmnavAboutClickUi();
			}
		});
	};

	public void btmnavHomeClickUi () {
		_setBackground(parent_linear_home, 20, 0, "#555b7b", true);
		imageview_home.setScaleX((float)(1.0d + 0.2d));
		imageview_home.setScaleY((float)(1.0d + 0.2d));
		textview_home.setTextSize((int)13);
		_setBackground(parent_linear_result, 0, 0, "#323957", false);
		imageview_fake.setScaleX((float)(1.0d));
		imageview_fake.setScaleY((float)(1.0d));
		textview_fake.setTextSize((int)12);
		_setBackground(parent_linear_boid, 0, 0, "#323957", false);
		imageview_plus.setScaleX((float)(1.0d));
		imageview_plus.setScaleY((float)(1.0d));
		textview_boid.setTextSize((int)12);
		_setBackground(parent_linear_news, 0, 0, "#323957", false);
		imageview_more.setScaleX((float)(1.0d));
		imageview_more.setScaleY((float)(1.0d));
		textview_news.setTextSize((int)12);
		_setBackground(parent_linear_about, 0, 0, "#323957", false);
		imageview_about.setScaleX((float)(1.0d));
		imageview_about.setScaleY((float)(1.0d));
		textview_about.setTextSize((int)12);
	}
	public void btmnavFakeClickUi () {
		_setBackground(parent_linear_home, 0, 0, "#323957", false);
		imageview_home.setScaleX((float)(1.0d));
		imageview_home.setScaleY((float)(1.0d));
		textview_home.setTextSize((int)12);
		_setBackground(parent_linear_result, 20, 0, "#555b7b", true);
		imageview_fake.setScaleX((float)(1.0d + 0.2d));
		imageview_fake.setScaleY((float)(1.0d + 0.2d));
		textview_fake.setTextSize((int)13);
		_setBackground(parent_linear_boid, 0, 0, "#323957", false);
		imageview_plus.setScaleX((float)(1.0d));
		imageview_plus.setScaleY((float)(1.0d));
		textview_boid.setTextSize((int)12);
		_setBackground(parent_linear_news, 0, 0, "#323957", false);
		imageview_more.setScaleX((float)(1.0d));
		imageview_more.setScaleY((float)(1.0d));
		textview_news.setTextSize((int)12);
		_setBackground(parent_linear_about, 0, 0, "#323957", false);
		imageview_about.setScaleX((float)(1.0d));
		imageview_about.setScaleY((float)(1.0d));
		textview_about.setTextSize((int)12);
	}
	public void btmnavBoidClickUi (){
		_setBackground(parent_linear_home, 0, 0, "#323957", false);
		imageview_home.setScaleX((float)(1.0d));
		imageview_home.setScaleY((float)(1.0d));
		textview_home.setTextSize((int)12);
		_setBackground(parent_linear_result, 0, 0, "#323957", false);
		imageview_fake.setScaleX((float)(1.0d));
		imageview_fake.setScaleY((float)(1.0d));
		textview_fake.setTextSize((int)12);
		_setBackground(parent_linear_boid, 360, 0, "#555b7b", true);
		imageview_plus.setScaleX((float)(1.0d + 0.2d));
		imageview_plus.setScaleY((float)(1.0d + 0.2d));
		textview_boid.setTextSize((int)13);
		_setBackground(parent_linear_news, 0, 0, "#323957", false);
		imageview_more.setScaleX((float)(1.0d));
		imageview_more.setScaleY((float)(1.0d));
		textview_news.setTextSize((int)12);
		_setBackground(parent_linear_about, 0, 0, "#323957", false);
		imageview_about.setScaleX((float)(1.0d));
		imageview_about.setScaleY((float)(1.0d));
		textview_about.setTextSize((int)12);
	}
	public void btmnavNewsClickUi () {
		_setBackground(parent_linear_home, 0, 0, "#323957", false);
		imageview_home.setScaleX((float)(1.0d));
		imageview_home.setScaleY((float)(1.0d));
		textview_home.setTextSize((int)12);
		_setBackground(parent_linear_result, 0, 0, "#323957", false);
		imageview_fake.setScaleX((float)(1.0d));
		imageview_fake.setScaleY((float)(1.0d));
		textview_fake.setTextSize((int)12);
		_setBackground(parent_linear_boid, 0, 0, "#323957", false);
		imageview_plus.setScaleX((float)(1.0d));
		imageview_plus.setScaleY((float)(1.0d));
		textview_boid.setTextSize((int)12);
		_setBackground(parent_linear_news, 20, 0, "#555b7b", true);
		imageview_more.setScaleX((float)(1.0d + 0.2d));
		imageview_more.setScaleY((float)(1.0d + 0.2d));
		textview_news.setTextSize((int)13);
		_setBackground(parent_linear_about, 0, 0, "#323957", false);
		imageview_about.setScaleX((float)(1.0d));
		imageview_about.setScaleY((float)(1.0d));
		textview_about.setTextSize((int)12);
	}
	public void btmnavAboutClickUi () {
		_setBackground(parent_linear_home, 0, 0, "#323957", false);
		imageview_home.setScaleX((float)(1.0d));
		imageview_home.setScaleY((float)(1.0d));
		textview_home.setTextSize((int)12);
		_setBackground(parent_linear_result, 0, 0, "#323957", false);
		imageview_fake.setScaleX((float)(1.0d));
		imageview_fake.setScaleY((float)(1.0d));
		textview_fake.setTextSize((int)12);
		_setBackground(parent_linear_boid, 0, 0, "#323957", false);
		imageview_plus.setScaleX((float)(1.0d));
		imageview_plus.setScaleY((float)(1.0d));
		textview_boid.setTextSize((int)12);
		_setBackground(parent_linear_news, 0, 0, "#323957", false);
		imageview_more.setScaleX((float)(1.0d));
		imageview_more.setScaleY((float)(1.0d));
		textview_news.setTextSize((int)12);
		_setBackground(parent_linear_about, 20, 0, "#555b7b", true);
		imageview_about.setScaleX((float)(1.0d + 0.2d));
		imageview_about.setScaleY((float)(1.0d + 0.2d));
		textview_about.setTextSize((int)13);
	}
	
	private void initializeLogic() {
		viewpager1.setAdapter(new MyFragmentAdapter(getApplicationContext(), getSupportFragmentManager(), 5));
		viewpager1.setCurrentItem(0);
		_Font();
		_UI();
		viewpager1.setPageTransformer(true, new BackgroundToForegroundTransformer());

		((androidx.viewpager.widget.PagerAdapter) viewpager1.getAdapter()).notifyDataSetChanged();

		// Show "What's New" dialog on first open after update from utilities class - alertDialog
		if (First.getString("New", "").equals("")) {
			Drawable icon = ContextCompat.getDrawable(getApplicationContext(), Utilities.whatsnewIcon);
			Utilities.alertDialog(HomepageActivity.this,
					Utilities.whatsnewTitle,
					icon,
					Utilities.whatsnewMessage,
					Utilities.whatsnewButton,
					null,
					null,
					null);

			First.edit().putString("New", "false").commit();		// SET MEMORY TO NOT-NEW LAUNCH
		}

		adview1.setVisibility(View.VISIBLE);
	}
	@Override
	protected void onActivityResult(int _requestCode, int _resultCode, Intent _data) {
		
		super.onActivityResult(_requestCode, _resultCode, _data);

	}
	public class MyFragmentAdapter extends FragmentStatePagerAdapter {
		Context context;
		int tabCount;
		public MyFragmentAdapter(Context context, FragmentManager fm, int tabCount) {
			super(fm);
			this.context = context;
			this.tabCount = tabCount;
		}
		@Override
		public int getCount(){
			return tabCount;
		}
		
		@Override
		public CharSequence getPageTitle(int _position) {
			return null;
		}
		
		@Override
		public Fragment getItem(int _position) {
			if (_position == 0) {
				return new HomeFragmentActivity();
			}
			if (_position == 1) {
				return new BulkResultFragmentActivity();
			}
			if (_position == 2) {
				return new SaveBoidFragmentActivity();
			}
			if (_position == 3) {
				return new BoidListFragmentActivity();
			}
			if (_position == 4) {
				return new AboutFragmentActivity();
			}
			return null;
		}
		
	}
	public void _Font () {
		textview_app_title.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/quicksand.ttf"), Typeface.BOLD);
		textview_app_subtitle.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/josefinsans_regular.ttf"), Typeface.NORMAL);
	}
	public void _UI () {
		Window w = this.getWindow();w.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);w.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS); w.setStatusBarColor(Color.parseColor("#222945"));
		_setBackground(linear_card_menu, 20, 0, "#323957", true);
		//BottomNav
		_setBackground(linear_bottom_nav, 25, 0, "#323957", false);
		//Selected Item while displaying the activity
		_setBackground(parent_linear_home, 20, 0, "#555b7b", false);
		imageview_home.setScaleX((float)(imageview_home.getScaleX() + 0.2d));
		imageview_home.setScaleY((float)(imageview_home.getScaleY() + 0.2d));
		textview_home.setTextSize((int)12.5d);
		_setBackground(circular_parent_linear_plus_btn, 360, 0, "#555b7b", false);
	}

	public void _setBackground (final View _view, final double _radius, final double _shadow, final String _color, final boolean _ripple) {
		if (_ripple) {
			android.graphics.drawable.GradientDrawable gd = new android.graphics.drawable.GradientDrawable();
			gd.setColor(Color.parseColor(_color));
			gd.setCornerRadius((int)_radius);
			_view.setElevation((int)_shadow);
			android.content.res.ColorStateList clrb = new android.content.res.ColorStateList(new int[][]{new int[]{}}, new int[]{Color.parseColor("#9e9e9e")});
			android.graphics.drawable.RippleDrawable ripdrb = new android.graphics.drawable.RippleDrawable(clrb , gd, null);
			_view.setClickable(true);
			_view.setBackground(ripdrb);
		}
		else {
			android.graphics.drawable.GradientDrawable gd = new android.graphics.drawable.GradientDrawable();
			gd.setColor(Color.parseColor(_color));
			gd.setCornerRadius((int)_radius);
			_view.setBackground(gd);
			_view.setElevation((int)_shadow);
		}
	}

	public class Recyclerview1Adapter extends RecyclerView.Adapter<Recyclerview1Adapter.ViewHolder> {
		ArrayList<HashMap<String, Object>> _data;
		public Recyclerview1Adapter(ArrayList<HashMap<String, Object>> _arr) {
			_data = _arr;
		}

		@Override
		public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			LayoutInflater _inflater = (LayoutInflater)getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View _v = _inflater.inflate(R.layout.boid_listview, null);
			RecyclerView.LayoutParams _lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			_v.setLayoutParams(_lp);
			return new ViewHolder(_v);
		}

		@Override
		public void onBindViewHolder(ViewHolder _holder, @SuppressLint("RecyclerView") final int _position) {
			View _view = _holder.itemView;

			final LinearLayout linear8 = (LinearLayout) _view.findViewById(R.id.linear8);
			final LinearLayout linear2 = (LinearLayout) _view.findViewById(R.id.linear2);
			final LinearLayout linear6 = (LinearLayout) _view.findViewById(R.id.linear6);
			final TextView textview_name = (TextView) _view.findViewById(R.id.textview_name);
			final TextView textview_boid = (TextView) _view.findViewById(R.id.textview_boid);
			final TextView textview_fake = (TextView) _view.findViewById(R.id.textview_fake);
			final ImageView imageview_actions = (ImageView) _view.findViewById(R.id.imageview_actions);

			if (_data.get((int)_position).containsKey("Name")) {
				textview_name.setText(_data.get((int)_position).get("Name").toString());
			}
			if (_data.get((int)_position).containsKey("Boid")) {
				textview_boid.setText(_data.get((int)_position).get("Boid").toString());
			}
			else {
				textview_fake.setText("Added at unknown time.");
			}
			textview_name.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/quicksand.ttf"), Typeface.NORMAL);
			textview_fake.setTextSize((int)10);
			textview_boid.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/quicksand.ttf"), Typeface.NORMAL);
			textview_fake.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/quicksand.ttf"), Typeface.NORMAL);
			_setBackground(linear8, 14, 0, "#555b7b", false);
		}

		@Override
		public int getItemCount() {
			return _data.size();
		}

		public class ViewHolder extends RecyclerView.ViewHolder{
			public ViewHolder(View v){
				super(v);
			}
		}
	}


	public static abstract class BaseTransformer implements androidx.viewpager.widget.ViewPager.PageTransformer {
			protected abstract void onTransform(View view, float position);
			@Override
			public void transformPage(View view, float position) {
					onPreTransform(view, position);
					onTransform(view, position);
					onPostTransform(view, position);
			}
			protected boolean hideOffscreenPages() {
					return true;
			}
			protected boolean isPagingEnabled() {
					return false;
			}
			protected void onPreTransform(View view, float position) {
					final float width = view.getWidth();
					view.setRotationX(0);
					view.setRotationY(0);
					view.setRotation(0);
					view.setScaleX(1);
					view.setScaleY(1);
					view.setPivotX(0);
					view.setPivotY(0);
					view.setTranslationY(0);
					view.setTranslationX(isPagingEnabled() ? 0f : -width * position);
					if (hideOffscreenPages()) {
							view.setAlpha(position <= -1f || position >= 1f ? 0f : 1f);
					} else {
							view.setAlpha(1f);
					}
			}
			protected void onPostTransform(View view, float position) {
			}
	}
	public static class BackgroundToForegroundTransformer extends BaseTransformer {
			@Override
			protected void onTransform(View view, float position) {
					final float height = view.getHeight();
					final float width = view.getWidth();
					final float scale = min(position < 0 ? 1f : Math.abs(1f - position), 0.5f);
					view.setScaleX(scale);
					view.setScaleY(scale);
					view.setPivotX(width * 0.5f);
					view.setPivotY(height * 0.5f);
					view.setTranslationX(position < 0 ? width * position : -width * position * 0.25f);
			}
			private static final float min(float val, float min) {
					return val < min ? min : val;
			}
	}
	public static class DefaultTransformer extends BaseTransformer {
			@Override protected void onTransform(View view, float position) {}
			@Override public boolean isPagingEnabled() {
					return true;
			}
	}
	public static class ForegroundToBackgroundTransformer extends BaseTransformer {
			@Override
			protected void onTransform(View view, float position) {
					final float height = view.getHeight();
					final float width = view.getWidth();
					final float scale = min(position > 0 ? 1f : Math.abs(1f + position), 0.5f);
					view.setScaleX(scale);
					view.setScaleY(scale);
					view.setPivotX(width * 0.5f);
					view.setPivotY(height * 0.5f);
					view.setTranslationX(position > 0 ? width * position : -width * position * 0.25f);
			}
			private static final float min(float val, float min) {
					return val < min ? min : val;
			}
	}


	public void _ICC (final ImageView _img, final String _c1, final String _c2) {
		_img.setImageTintList(new android.content.res.ColorStateList(new int[][] {{-android.R.attr.state_pressed},{android.R.attr.state_pressed}},new int[]{Color.parseColor(_c1), Color.parseColor(_c2)}));
	}
	public void _radius_4 (final String _color1, final String _color2, final double _str, final double _n1, final double _n2, final double _n3, final double _n4, final View _view) {
		android.graphics.drawable.GradientDrawable gd = new android.graphics.drawable.GradientDrawable();
		
		gd.setColor(Color.parseColor(_color1));
		
		gd.setStroke((int)_str, Color.parseColor(_color2));
		
		gd.setCornerRadii(new float[]{(int)_n1,(int)_n1,(int)_n2,(int)_n2,(int)_n3,(int)_n3,(int)_n4,(int)_n4});
		
		_view.setBackground(gd);
	}

}
