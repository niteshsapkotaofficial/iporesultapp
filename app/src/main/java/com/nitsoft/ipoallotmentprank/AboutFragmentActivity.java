package com.nitsoft.ipoallotmentprank;

import androidx.annotation.*;

import android.view.*;
import android.view.View.*;
import android.widget.*;
import android.content.*;
import android.text.*;

import java.util.HashMap;
import java.util.ArrayList;
import android.widget.ScrollView;
import android.widget.LinearLayout;
import de.hdodenhof.circleimageview.*;
import android.widget.TextView;
import android.widget.ImageView;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.content.Intent;
import android.net.Uri;

import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ChildEventListener;
import android.app.AlertDialog;

import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.ads.AdRequest;

import com.google.android.gms.ads.AdListener;
import java.util.Timer;
import java.util.Calendar;

import android.view.View;
import android.graphics.Typeface;

//import com.suke.widget.*;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


public class AboutFragmentActivity extends Fragment {

	// ===== your existing fields (kept) =====
	private FirebaseDatabase _firebase = FirebaseDatabase.getInstance();
	private double Like_Count = 0;
	private double Dislike_Count = 0;
	private HashMap<String, Object> Map_Like = new HashMap<>();
	private HashMap<String, Object> Map_Dislike = new HashMap<>();
	private String a = "";
	private String b = "";

	private ArrayList<HashMap<String, Object>> likesmap = new ArrayList<>();
	private ArrayList<HashMap<String, Object>> dislikemap = new ArrayList<>();

	private SharedPreferences sharedPreferencesWatchlist;
	private SharedPreferences React;
	private SharedPreferences PushNotification;
	private SharedPreferences Animation;
	private SharedPreferences Pin;

	private Intent intent = new Intent();
	private DatabaseReference Like = _firebase.getReference("Like");
	private ChildEventListener _Like_child_listener;
	private DatabaseReference Dislike = _firebase.getReference("Dislike");
	private ChildEventListener _Dislike_child_listener;
	private AlertDialog.Builder dialog;
	private FirebaseAuth Fauth;
	private AlertDialog.Builder pricvacypin_dialog;

	// ads
	private InterstitialAd inter_ad;

	// ===== RecyclerView =====
	private RecyclerView rvAbout;

	// simple item model
	private static final int TYPE_SECTION = 0;
	private static final int TYPE_OPTION  = 1;

	// option ids
	private static final String ID_PROFILE       = "profile";
	private static final String ID_PRIVACY_PIN   = "privacy_pin";
	private static final String ID_ERASE         = "erase_all";
	private static final String ID_TELEGRAM      = "telegram";
	private static final String ID_PRIVACY_POLICY= "privacy_policy";
	private static final String ID_REPORT        = "report";
	private static final String ID_SHARE         = "share";
	private static final String ID_RATE          = "rate";
	private static final String ID_EXIT          = "exit";

	private ArrayList<HashMap<String, Object>> list = new ArrayList<>();

	@NonNull
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View _view = inflater.inflate(R.layout.fragment_about, container, false);
		initialize(savedInstanceState, _view);
		com.google.firebase.FirebaseApp.initializeApp(getContext());
		onActivityCreate();
		return _view;
	}
	private void initialize(Bundle _savedInstanceState, View view) {
		// prefs / services
		sharedPreferencesWatchlist = requireContext().getSharedPreferences("watchlistSymbol", Context.MODE_PRIVATE);
		React = requireContext().getSharedPreferences("React", Activity.MODE_PRIVATE);
		PushNotification = requireContext().getSharedPreferences("Notification", Activity.MODE_PRIVATE);
		Animation = requireContext().getSharedPreferences("Animation", Activity.MODE_PRIVATE);
		Pin = requireContext().getSharedPreferences("Pin", Activity.MODE_PRIVATE);
		dialog = new AlertDialog.Builder(requireContext());
		pricvacypin_dialog = new AlertDialog.Builder(requireContext());
		Fauth = FirebaseAuth.getInstance();

		// recycler
		rvAbout = view.findViewById(R.id.rvAbout);
		rvAbout.setLayoutManager(new LinearLayoutManager(getContext()));

		// firebase like/dislike (kept, simplified)
		_Like_child_listener = new ChildEventListener() {
			@Override public void onChildAdded(DataSnapshot s, String p) { pullLike(s); }
			@Override public void onChildChanged(DataSnapshot s, String p) { pullLike(s); }
			@Override public void onChildMoved(DataSnapshot s, String p) {}
			@Override public void onChildRemoved(DataSnapshot s) {}
			@Override public void onCancelled(DatabaseError e) {}
			private void pullLike(DataSnapshot snap){
				Like.addListenerForSingleValueEvent(new ValueEventListener() {
					@Override public void onDataChange(DataSnapshot ds) {
						likesmap.clear();
						try {
							GenericTypeIndicator<HashMap<String, Object>> ind = new GenericTypeIndicator<HashMap<String, Object>>(){};
							for (DataSnapshot d: ds.getChildren()) {
								HashMap<String, Object> m = d.getValue(ind);
								if (m!=null) likesmap.add(m);
							}
						} catch(Exception ignored){}
						HashMap v = snap.getValue(new GenericTypeIndicator<HashMap<String,Object>>(){});
						if (v!=null && v.get("Likes")!=null) Like_Count = Double.parseDouble(v.get("Likes").toString());
					}
					@Override public void onCancelled(DatabaseError e) {}
				});
			}
		};
		Like.addChildEventListener(_Like_child_listener);

		_Dislike_child_listener = new ChildEventListener() {
			@Override public void onChildAdded(DataSnapshot s, String p) { pullDislike(s); }
			@Override public void onChildChanged(DataSnapshot s, String p) { pullDislike(s); }
			@Override public void onChildMoved(DataSnapshot s, String p) {}
			@Override public void onChildRemoved(DataSnapshot s) {}
			@Override public void onCancelled(DatabaseError e) {}
			private void pullDislike(DataSnapshot snap){
				Dislike.addListenerForSingleValueEvent(new ValueEventListener() {
					@Override public void onDataChange(DataSnapshot ds) {
						dislikemap.clear();
						try {
							GenericTypeIndicator<HashMap<String, Object>> ind = new GenericTypeIndicator<HashMap<String, Object>>(){};
							for (DataSnapshot d: ds.getChildren()) {
								HashMap<String, Object> m = d.getValue(ind);
								if (m!=null) dislikemap.add(m);
							}
						} catch(Exception ignored){}
						HashMap v = snap.getValue(new GenericTypeIndicator<HashMap<String,Object>>(){});
						if (v!=null && v.get("Dislikes")!=null) Dislike_Count = Double.parseDouble(v.get("Dislikes").toString());
					}
					@Override public void onCancelled(DatabaseError e) {}
				});
			}
		};
		Dislike.addChildEventListener(_Dislike_child_listener);
	}

	private void onActivityCreate() {
		initializeLogic();
		buildList(); // fills "list"
		rvAbout.setAdapter(new AboutAdapter());
	}
	private void initializeLogic() {
		a = "An amazing app to check ipo allotment result with bulk check feature.\n\nThis app contains many more feature like saving boid, bulk check, alerts, fun result and many more.\n\nDownload Link: https://play.google.com/store/apps/details?id=com.nitsoft.ipoallotmentprank\n\nWe've been always waiting for your feedback.  :-)";
		b = a;
	}

	// ===== simple data builder using HashMap like your style =====
	private void buildList() {
		list.clear();

//		addOption(ID_PROFILE, "Developer profile", 0xFFFFFFFF, R.drawable.profile);

		addSection("General");
		addOption(ID_ERASE, "Erase all data", 0xFFb71c1c, R.drawable.logout);

		addSection("Community");
		addOption(ID_TELEGRAM, "Telegram channel", 0xFFFFFFFF, R.drawable.ic_telegram_white);

		addSection("Application");
		addOption(ID_PRIVACY_POLICY, "Privacy policy", 0xFFFF5722, R.drawable.privcay_policy);
		addOption(ID_REPORT, "Report a problem", 0xFF009688, R.drawable.report);
		addOption(ID_SHARE, "Share app", 0xFF3f51b5, R.drawable.share);
		addOption(ID_RATE, "Rate app", 0xFFF44336, R.drawable.rating);
		addOption(ID_EXIT, "Exit", 0xFF9c27b0, R.drawable.exit);
	}
	private void addSection(String title){
		HashMap<String, Object> m = new HashMap<>();
		m.put("type", TYPE_SECTION);
		m.put("title", title);
		list.add(m);
	}
	private void addOption(String id, String title, int bgColor, int iconRes){
		HashMap<String, Object> m = new HashMap<>();
		m.put("type", TYPE_OPTION);
		m.put("id", id);
		m.put("title", title);
		m.put("bg", bgColor);
		m.put("icon", iconRes);
		list.add(m);
	}

	// ===== RecyclerView Adapter kept super simple, inner class =====
	private class AboutAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

		private final Typeface quicksandBold;

		AboutAdapter() {
			// cache once
			quicksandBold = Typeface.createFromAsset(requireContext().getAssets(), "fonts/quicksand.ttf");
			setHasStableIds(true);
		}

		@Override
		public int getItemViewType(int position) {
			return (int) list.get(position).get("type");
		}

		@Override
		public long getItemId(int position) {
			// stable ids help RecyclerView
			HashMap<String, Object> m = list.get(position);
			if ((int) m.get("type") == TYPE_SECTION) {
				return ("sec:" + m.get("title")).hashCode();
			} else {
				return ("opt:" + m.get("id")).hashCode();
			}
		}

		@NonNull @Override
		public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
			LayoutInflater inf = LayoutInflater.from(parent.getContext());
			if (viewType == TYPE_SECTION) {
				View v = inf.inflate(R.layout.item_about_section, parent, false);
				return new VHSection(v);
			} else {
				View v = inf.inflate(R.layout.item_about_option, parent, false);
				return new VHOption(v);
			}
		}

		@Override
		public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
			HashMap<String, Object> m = list.get(position);

			if (holder instanceof VHSection) {
				VHSection vh = (VHSection) holder;
				vh.tv.setText(String.valueOf(m.get("title")));
				vh.tv.setTypeface(quicksandBold, Typeface.BOLD);

			} else if (holder instanceof VHOption) {
				VHOption vh = (VHOption) holder;
				vh.title.setText(String.valueOf(m.get("title")));
				vh.title.setTypeface(quicksandBold, Typeface.NORMAL);
				vh.icon.setCircleBackgroundColor((int) m.get("bg"));
				vh.icon.setImageResource((int) m.get("icon"));
				vh.itemView.setOnClickListener(v -> onOptionClick(String.valueOf(m.get("id"))));
			}
		}

		@Override public int getItemCount() { return list.size(); }

		class VHSection extends RecyclerView.ViewHolder {
			TextView tv;
			VHSection(@NonNull View itemView) { super(itemView); tv = itemView.findViewById(R.id.tvSectionTitle); }
		}

		class VHOption extends RecyclerView.ViewHolder {
			CircleImageView icon; TextView title; ImageView arrow;
			VHOption(@NonNull View itemView) {
				super(itemView);
				icon = itemView.findViewById(R.id.imgOptionIcon);
				title = itemView.findViewById(R.id.tvOptionTitle);
				arrow = itemView.findViewById(R.id.imgArrow);
			}
		}
	}

	// ===== click routing kept simple =====
	private void onOptionClick(String id) {
		switch (id) {
			case ID_PROFILE:
				_contact_us();
				break;

			case ID_PRIVACY_PIN:
				// open your privacy pin flow here
				Toast.makeText(getContext(), "Privacy pin tapped", Toast.LENGTH_SHORT).show();
				break;

			case ID_ERASE:
				clearData();
				break;

			case ID_TELEGRAM:
				intent.setAction(Intent.ACTION_VIEW);
				intent.setData(Uri.parse("https://tinyurl.com/iporesulttohsmtelegram"));
				startActivity(intent);
				break;

			case ID_PRIVACY_POLICY:
				intent.setAction(Intent.ACTION_VIEW);
				intent.setData(Uri.parse("https://sites.google.com/view/ipo-result/privacy-policy"));
				startActivity(intent);
				break;

			case ID_REPORT:
				_report();
				break;

			case ID_SHARE: {
				Intent i = new Intent(Intent.ACTION_SEND);
				i.setType("text/plain");
				i.putExtra(Intent.EXTRA_SUBJECT, b);
				i.putExtra(Intent.EXTRA_TEXT, a);
				startActivity(Intent.createChooser(i, "Share IPO result using"));
				break;
			}

			case ID_RATE:
				intent.setAction(Intent.ACTION_VIEW);
				intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=com.nitsoft.ipoallotmentprank"));
				startActivity(intent);
				break;

			case ID_EXIT:
				Utilities.negativeDialog(getContext(), "Exit", "Are you sure you want to close this application?", "Close",
						new View.OnClickListener() { @Override public void onClick(View v) { requireActivity().finishAffinity(); }});
				break;
		}
	}

	// ===== your existing helpers (unchanged) =====
	public void _contact_us () {
		final AlertDialog dialog1 = new AlertDialog.Builder(getActivity()).create();
		View inflate = getLayoutInflater().inflate(R.layout.custom2,null);
		if (dialog1.getWindow()!=null) dialog1.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
		dialog1.setView(inflate);
		TextView t1 = inflate.findViewById(R.id.t1);
		TextView t2 = inflate.findViewById(R.id.t2);
		LinearLayout b2 = inflate.findViewById(R.id.b2);
		ImageView i1 = inflate.findViewById(R.id.i1);
		ImageView i2 = inflate.findViewById(R.id.i2);
		ImageView i3 = inflate.findViewById(R.id.i3);
		ImageView i4 = inflate.findViewById(R.id.i4);
		ImageView i5 = inflate.findViewById(R.id.i5);
		final ImageView i6 = inflate.findViewById(R.id.i6);
		final ImageView i7 = inflate.findViewById(R.id.i7);
		LinearLayout bg = inflate.findViewById(R.id.bg);
		LinearLayout ll = inflate.findViewById(R.id.ll);
		LinearLayout l5 = inflate.findViewById(R.id.l5);
		TextView t3 = inflate.findViewById(R.id.t3);
		final TextView t4  = inflate.findViewById(R.id.t4);
		final TextView t5 = inflate.findViewById(R.id.t5);

		if (React.getString("React", "").equals("Like")) i6.setImageResource(R.drawable.like_filled);
		else if (React.getString("React", "").equals("Dislike")) i7.setImageResource(R.drawable.like_filled);

		t4.setText(String.valueOf((long)(Like_Count)));
		t5.setText(String.valueOf((long)(Dislike_Count)));
		i1.setImageResource(R.drawable.profile);
		i2.setImageResource(R.drawable.instagram);
		i3.setImageResource(R.drawable.facebook);
		i4.setImageResource(R.drawable.gmail);
		i5.setImageResource(R.drawable.close);
		t1.setText("Nitesh Sapkota");
		t2.setText("Ui Ux Designer and Developer");
		t3.setText("Check my social media");
		Utilities.setBackground(bg, 25, 0, "#273238", false);
		Utilities.setBackground(ll, 0, 0, "#518333", false);
		Utilities.setBackground(i1, 360, 0, "#273238", false);
		Utilities.cornerRadiusWithStroke(i5 ,"#273238", "#273238", 0, 100, 100, 100, 100, false);
		Utilities.cornerRadiusWithStroke(l5 ,"#273238", "#273238", 0, 100, 100, 100, 100, false);
		Utilities.cornerRadiusWithStroke(i7 ,"#273238", "#273238", 0, 100, 100, 100, 100, false);
		Utilities.cornerRadiusWithStroke(l5 ,"#273238", "#273238", 0, 0, 0, 25, 25, false);
		Utilities.cornerRadiusWithStroke(b2 ,"#273238", "#273238", 0, 0, 0, 25, 25, false);

		i5.setOnClickListener(v -> dialog1.dismiss());
		i2.setOnClickListener(v -> {
			intent.setAction(Intent.ACTION_VIEW);
			intent.setData(Uri.parse("https://instagram.com/niteshsapkotaofficial"));
			startActivity(intent);
		});
		i3.setOnClickListener(v -> {
			intent.setAction(Intent.ACTION_VIEW);
			intent.setData(Uri.parse("https://facebook.com/niteshsapkotaofficial"));
			startActivity(intent);
		});
		i4.setOnClickListener(v -> {
			intent.setAction(Intent.ACTION_VIEW);
			intent.setData(Uri.parse("mailto:niteshsapkotaofficial@gmail.com"));
			startActivity(intent);
		});

		i6.setOnClickListener(v -> {
			if (React.getString("React", "").equals("Like")) {
				React.edit().putString("React", "").apply();
				i6.setImageResource(R.drawable.like);
				i7.setImageResource(R.drawable.like);
				t4.setText(String.valueOf((long)(Like_Count - 1)));
				Map_Like.put("Likes", String.valueOf((long)(Like_Count - 1)));
				Like.child("Like").updateChildren(Map_Like);
			} else if (React.getString("React", "").equals("Dislike")) {
				React.edit().putString("React", "Like").apply();
				i6.setImageResource(R.drawable.like_filled);
				i7.setImageResource(R.drawable.like);
				t4.setText(String.valueOf((long)(Like_Count + 1)));
				t5.setText(String.valueOf((long)(Dislike_Count - 1)));
				Map_Like.put("Likes", String.valueOf((long)(Like_Count + 1)));
				Like.child("Like").updateChildren(Map_Like);
				Map_Dislike.put("Dislikes", String.valueOf((long)(Dislike_Count - 1)));
				Dislike.child("Dislike").updateChildren(Map_Dislike);
			} else {
				React.edit().putString("React", "Like").apply();
				i6.setImageResource(R.drawable.like_filled);
				i7.setImageResource(R.drawable.like);
				t4.setText(String.valueOf((long)(Like_Count + 1)));
				Map_Like.put("Likes", String.valueOf((long)(Like_Count + 1)));
				Like.child("Like").updateChildren(Map_Like);
			}
		});

		i7.setOnClickListener(v -> {
			if (React.getString("React", "").equals("Dislike")) {
				React.edit().putString("React", "").apply();
				i6.setImageResource(R.drawable.like);
				i7.setImageResource(R.drawable.like);
				t5.setText(String.valueOf((long)(Dislike_Count - 1)));
				Map_Dislike.put("Dislikes", String.valueOf((long)(Dislike_Count - 1)));
				Dislike.child("Dislike").updateChildren(Map_Dislike);
			} else if (React.getString("React", "").equals("Like")) {
				React.edit().putString("React", "Dislike").apply();
				i6.setImageResource(R.drawable.like);
				i7.setImageResource(R.drawable.like_filled);
				t5.setText(String.valueOf((long)(Dislike_Count + 1)));
				t4.setText(String.valueOf((long)(Like_Count - 1)));
				Map_Dislike.put("Dislikes", String.valueOf((long)(Dislike_Count + 1)));
				Dislike.child("Dislike").updateChildren(Map_Dislike);
				Map_Like.put("Likes", String.valueOf((long)(Like_Count - 1)));
				Like.child("Like").updateChildren(Map_Like);

				AdRequest adRequest = new AdRequest.Builder().build();
				InterstitialAd.load(getActivity(),"ca-app-pub-7184690369277704/4653608727", adRequest,
						new InterstitialAdLoadCallback() {
							@Override public void onAdLoaded(@NonNull InterstitialAd ad) { ad.show(getActivity()); }
							@Override public void onAdFailedToLoad(@NonNull LoadAdError e) {}
						});
			} else {
				React.edit().putString("React", "Dislike").apply();
				i6.setImageResource(R.drawable.like);
				i7.setImageResource(R.drawable.like_filled);
				t5.setText(String.valueOf((long)(Dislike_Count + 1)));
				Map_Dislike.put("Dislikes", String.valueOf((long)(Dislike_Count + 1)));
				Dislike.child("Dislike").updateChildren(Map_Dislike);

				AdRequest adRequest = new AdRequest.Builder().build();
				InterstitialAd.load(getActivity(),"ca-app-pub-7184690369277704/4653608727", adRequest,
						new InterstitialAdLoadCallback() {
							@Override public void onAdLoaded(@NonNull InterstitialAd ad) { ad.show(getActivity()); }
							@Override public void onAdFailedToLoad(@NonNull LoadAdError e) {}
						});
			}
		});

		dialog1.setCancelable(false);
		dialog1.show();
	}
	public void clearData () {
		Utilities.negativeDialog(getContext(), "Erase Data", "Are you sure want to delete all the saved data?\n This will delete the following: \n - Watchlist", "Delete",
				new View.OnClickListener() {
					@Override public void onClick(View view) {
						sharedPreferencesWatchlist.edit().putString("watchlistSymbol", "").apply();
					}
				});
	}
	public void _report () {
		final AlertDialog dialog1 = new AlertDialog.Builder(getActivity()).create();
		View inflate = getLayoutInflater().inflate(R.layout.report,null);
		if (dialog1.getWindow()!=null) dialog1.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
		dialog1.setView(inflate);

		TextView t1 = inflate.findViewById(R.id.t1);
		TextView t3 = inflate.findViewById(R.id.t3);
		final TextView tv = inflate.findViewById(R.id.tv_count);
		final EditText e1 = inflate.findViewById(R.id.edittext_report);
		LinearLayout ls = inflate.findViewById(R.id.linear_stroke);
		LinearLayout bt = inflate.findViewById(R.id.bt_submit);
		ImageView i1 = inflate.findViewById(R.id.i1);
		LinearLayout bg = inflate.findViewById(R.id.bg);
		LinearLayout bg1 = inflate.findViewById(R.id.bg1);

		t1.setTypeface(Typeface.createFromAsset(getContext().getAssets(),"fonts/sansation_regular.ttf"), Typeface.NORMAL);
		t3.setTypeface(Typeface.createFromAsset(getContext().getAssets(),"fonts/sansation_regular.ttf"), Typeface.NORMAL);
		i1.setImageResource(R.drawable.ic_clear_white);
		t1.setText("Report");
		e1.setFilters(new InputFilter[]{new InputFilter.LengthFilter(225)});
		e1.setHint("Write your problem here...");
		e1.setHintTextColor(0xFF009688);
		e1.addTextChangedListener(new TextWatcher() {
			@Override public void onTextChanged(CharSequence s, int a, int b, int c) { tv.setText(e1.length()+"/225"); }
			@Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
			@Override public void afterTextChanged(Editable s) {}
		});
		t3.setText("Submit");
		Utilities.setBackground(bg, 10, 0, "#FFFFFF", false);
		Utilities.setBackground(bg1, 10, 0, "#FFFFFF", false);
		Utilities.cornerRadiusWithStroke(bt, "#009688", "#FFFFFF", 0, 15, 15, 15, 15, false);
		Utilities.cornerRadiusWithStroke(ls, "#ffffff", "#009688", 2, 10, 10, 10, 10, false);
		Utilities.btnRipple(i1);

		bt.setOnClickListener(v -> {
			SketchwareUtil.showMessage(getContext(), "Report Generated");
			if (e1.getText().toString().length() < 25) {
				SketchwareUtil.showMessage(getContext(), "Must contain atleast 25 letters");
			} else {
				Intent selectorIntent = new Intent(Intent.ACTION_SENDTO);
				selectorIntent.setData(Uri.parse("mailto:"));
				final Intent emailIntent = new Intent(Intent.ACTION_SEND);
				emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"nitsofts@gmail.com"});
				emailIntent.putExtra(Intent.EXTRA_SUBJECT, "IPO Result App Error");
				emailIntent.putExtra(Intent.EXTRA_TEXT, e1.getText().toString());
				emailIntent.setSelector(selectorIntent);
				startActivity(Intent.createChooser(emailIntent, "Send Report Via Email"));
				dialog1.dismiss();
			}
		});
		i1.setOnClickListener(v -> dialog1.dismiss());
		dialog1.setCancelable(false);
		dialog1.show();
	}
}
