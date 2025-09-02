package com.nitsoft.ipoallotmentprank;

import static android.content.Context.MODE_PRIVATE;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.google.android.gms.ads.nativead.MediaView;

public class BoidListFragmentActivity extends Fragment {

    private LinearLayout parent_linear_bottom_nav;
    private TextView textview_app_title;
    private TextView textview_app_subtitle;
    private LinearLayout linear_nodata_main;
    private ImageView imageview_illustration;
    private TextView textview_nodata_title;
    private TextView textview_nodata_subtitle;
    private Button button_nodata_action;
    private RecyclerView recyclerview_boid_list;

    private String editName = "";
    private String editBoid = "";
    private double editPosition;

    private SharedPreferences sharedPreferencesBoidList;
    private ArrayList<HashMap<String, Object>> listmapBoidList = new ArrayList<>();

    // --- Native Ad bits ---
    private NativeAd nativeAd;             // keep a reference to destroy later
    private boolean isAdLoaded = false;    // gate for adapter to insert ad row
    private static final int AD_INTERVAL = 2; // insert ad after every 2 items

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_boid_list_activity, container, false);
        initialize(savedInstanceState, view);
        onActivityCreate();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchExistingBoidList(); // refresh list when returning
    }

    @Override
    public void onDestroyView() {
        if (nativeAd != null) nativeAd.destroy();
        nativeAd = null;
        isAdLoaded = false;
        super.onDestroyView();
    }

    private void initialize(Bundle savedInstanceState, View view) {
        parent_linear_bottom_nav = view.findViewById(R.id.parent_linear_bottom_nav);
        textview_app_title = requireActivity().findViewById(R.id.textview_app_title);
        textview_app_subtitle = requireActivity().findViewById(R.id.textview_app_subtitle);
        linear_nodata_main = view.findViewById(R.id.linear_nodata_main);
        imageview_illustration = view.findViewById(R.id.imageview_illustration);
        textview_nodata_title = view.findViewById(R.id.textview_nodata_title);
        textview_nodata_subtitle = view.findViewById(R.id.textview_nodata_subtitle);
        button_nodata_action = view.findViewById(R.id.button_nodata_action);
        recyclerview_boid_list = view.findViewById(R.id.recyclerview_boid_list);

        // LayoutManager set once
        recyclerview_boid_list.setLayoutManager(new LinearLayoutManager(getContext()));

        sharedPreferencesBoidList = requireActivity().getSharedPreferences("boidList", MODE_PRIVATE);
    }

    private void onActivityCreate() {
        userInterface();

        // load ad once
        loadNativeAd();

        // refresh list immediately on “boid_updated”
        getParentFragmentManager().setFragmentResultListener("boid_updated", this, (requestKey, bundle) -> {
            fetchExistingBoidList();
        });

        // first load
        fetchExistingBoidList();
    }

    private void userInterface(){
        textview_app_title.setText("Boid Manager");
        textview_app_subtitle.setText("Manage your saved boid at one place!");
    }

    private void fetchExistingBoidList() {
        String json = sharedPreferencesBoidList.getString("boidList", "");
        boolean empty = (json == null || json.isEmpty() || "[]".equals(json));

        if (empty) {
            linear_nodata_main.setVisibility(View.VISIBLE);
            textview_nodata_title.setText("Oops! Nothing's Here");
            textview_nodata_subtitle.setText("Please save some boid numbers to see it here");
            button_nodata_action.setVisibility(View.GONE);

            // still set an adapter so list is stable
            listmapBoidList = new ArrayList<>();
            BoidListAdapter adapter = new BoidListAdapter();
            recyclerview_boid_list.setAdapter(adapter);
        } else {
            linear_nodata_main.setVisibility(View.GONE);
            listmapBoidList = new Gson().fromJson(json,
                    new TypeToken<ArrayList<HashMap<String, Object>>>(){}.getType());

            if (recyclerview_boid_list.getAdapter() == null) {
                recyclerview_boid_list.setAdapter(new BoidListAdapter());
            } else {
                recyclerview_boid_list.getAdapter().notifyDataSetChanged();
            }
            
            // Refresh ad if list changed significantly
            if (listmapBoidList.size() > 0 && listmapBoidList.size() % AD_INTERVAL == 0) {
                loadNativeAd();
            }
        }
    }

    // ---------------- Native Ad Helpers ----------------

    private void loadNativeAd() {
        MobileAds.initialize(requireContext(), initStatus -> {});
        
        // Build ad request with production settings
        AdRequest.Builder adRequestBuilder = new AdRequest.Builder();
        
        AdLoader adLoader = new AdLoader.Builder(requireContext(),
                "ca-app-pub-7184690369277704/9303315304")
                .forNativeAd(ad -> {
                    if (nativeAd != null) nativeAd.destroy();
                    nativeAd = ad;
                    isAdLoaded = true;
                    if (recyclerview_boid_list.getAdapter() != null) {
                        recyclerview_boid_list.getAdapter().notifyDataSetChanged();
                    }
                })
                .withAdListener(new AdListener() {
                    @Override public void onAdFailedToLoad(@NonNull LoadAdError adError) {
                        isAdLoaded = false;
                        // Log error for debugging (remove in production if needed)
                        Log.d("BoidListAd", "Ad failed to load: " + adError.getMessage());
                    }
                    
                    @Override public void onAdLoaded() {
                        Log.d("BoidListAd", "Ad loaded successfully");
                    }
                })
                .build();

        adLoader.loadAd(adRequestBuilder.build());
    }

    private void scheduleAdRefresh() {
        // Refresh ad every 5 minutes to ensure fresh content
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            if (isAdded() && nativeAd != null) {
                loadNativeAd();
            }
        }, 5 * 60 * 1000); // 5 minutes
    }

    private void bindNativeAd(NativeAd ad, NativeAdView adView) {
        TextView headline = adView.findViewById(R.id.ad_headline);
        TextView body = adView.findViewById(R.id.ad_body);
        TextView advertiser = adView.findViewById(R.id.ad_advertiser);
        ImageView icon = adView.findViewById(R.id.ad_app_icon);
//        MediaView mediaView = adView.findViewById(R.id.ad_media);
//        Button cta = adView.findViewById(R.id.ad_call_to_action);

        adView.setHeadlineView(headline);
        adView.setBodyView(body);
        adView.setAdvertiserView(advertiser);
        adView.setIconView(icon);
//        adView.setMediaView(mediaView);
//        adView.setCallToActionView(cta);

        headline.setText(ad.getHeadline());

        if (ad.getBody() != null) {
            body.setText(ad.getBody());
            body.setVisibility(View.VISIBLE);
        } else body.setVisibility(View.GONE);

        if (ad.getAdvertiser() != null) {
            advertiser.setText(ad.getAdvertiser());
            advertiser.setVisibility(View.VISIBLE);
        } else advertiser.setVisibility(View.GONE);

        if (ad.getIcon() != null) {
            icon.setImageDrawable(ad.getIcon().getDrawable());
            icon.setVisibility(View.VISIBLE);
        } else icon.setVisibility(View.GONE);

//        if (ad.getMediaContent() != null) {
//            mediaView.setMediaContent(ad.getMediaContent());
//            mediaView.setVisibility(View.VISIBLE);
//        } else mediaView.setVisibility(View.GONE);

        adView.setNativeAd(ad);
    }

    // ---------------- RecyclerView Adapter ----------------

    private class BoidListAdapter extends RecyclerView.Adapter<BoidListAdapter.ViewHolder> {

        private static final int VIEW_TYPE_ITEM = 0;
        private static final int VIEW_TYPE_AD = 1;

        @Override
        public int getItemViewType(int position) {
            if (isAdLoaded && shouldShowAdAtPosition(position)) {
                return VIEW_TYPE_AD;
            }
            return VIEW_TYPE_ITEM;
        }

        @Override
        public int getItemCount() {
            int base = listmapBoidList.size();
            return base + (isAdLoaded ? getAdCount() : 0);
        }

        private int getAdCount() {
            if (!isAdLoaded) return 0;
            // Show ad after every 2 items, so for n items, we show floor(n/2) ads
            return listmapBoidList.size() / AD_INTERVAL;
        }

        private boolean shouldShowAdAtPosition(int position) {
            if (!isAdLoaded) return false;
            
            // Super simple: show ad at specific positions
            // Position 2: AD (after 2 items)
            // Position 5: AD (after 4 items) 
            // Position 8: AD (after 6 items)
            // Position 11: AD (after 8 items)
            // etc.
            
            // Formula: position = 2 + (3 * adIndex) where adIndex starts at 0
            // So positions: 2, 5, 8, 11, 14, 17, 20, 23, 26, 29, 32, 35, 38, 41, 44, 47, 50, 53, 56, 59, 62, 65, 68, 71, 74, 77, 80, 83, 86, 89, 92, 95, 98
            
            return (position - 2) % 3 == 0 && position >= 2;
        }

        private int mapToDataIndex(int adapterPos) {
            if (!isAdLoaded) return adapterPos;
            
            // Count how many ads appear before this position
            int adCount = 0;
            for (int i = 0; i < adapterPos; i++) {
                if (shouldShowAdAtPosition(i)) {
                    adCount++;
                }
            }
            
            // If this position is an ad, return -1
            if (shouldShowAdAtPosition(adapterPos)) {
                return -1; // ad row
            }
            
            // Adjust for ads that appear before this position
            return adapterPos - adCount;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == VIEW_TYPE_AD) {
                View v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.native_small, parent, false);
                return new ViewHolder(v);
            } else {
                View v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.boid_listview, parent, false);
                return new ViewHolder(v);
            }
        }

        @Override
        public void onBindViewHolder(@NonNull BoidListAdapter.ViewHolder holder, int position) {
            // Set recyclerview to wrap content
            holder.itemView.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
            if (getItemViewType(position) == VIEW_TYPE_AD) {
                if (nativeAd != null) {
                    NativeAdView adView = (NativeAdView) holder.itemView;
                    bindNativeAd(nativeAd, adView);
                    // Apply consistent styling to match BOID items with subtle border
                    Utilities.cornerRadiusWithStroke(adView, "#555B7B", "#6B7A99", 1, 30, 30, 30, 30, true);
                    // Add elevation to match BOID items
                    adView.setElevation(2f);
                }
                return;
            }

            int dataIndex = mapToDataIndex(position);
            if (dataIndex < 0 || dataIndex >= listmapBoidList.size()) return;

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

            bulk_result_progress_bar.setVisibility(View.GONE);
            imageview_actions.setImageResource(R.drawable.ic_more_vert_white);

            textview_name.setText(String.valueOf(listmapBoidList.get(dataIndex).get("name")));
            textview_boid.setText(String.valueOf(listmapBoidList.get(dataIndex).get("boid")));
            textview_boid_count.setText(String.valueOf(dataIndex + 1));

            // timestamp -> date
            double timestampDouble = Double.parseDouble(String.valueOf(listmapBoidList.get(dataIndex).get("timestamp")));
            long timestamp = (long) timestampDouble;
            Date date = new Date(timestamp);
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss a, dd MMM yyyy", Locale.getDefault());
            String formattedDate = sdf.format(date);
            textview_detail.setText("Added on: " + formattedDate);

            Utilities.setBackground(linear_item, 30, 0, "#555B7B", true);

            linear_item.setOnLongClickListener(v -> {
                linear_actions.callOnClick();
                return true;
            });

            linear_actions.setOnClickListener(v -> {
                int currentPosition = holder.getAdapterPosition();
                int currentDataIndex = mapToDataIndex(currentPosition);
                if (currentDataIndex == -1) return; // ad row

                List<DialogOptions.OptionItem> opts = Arrays.asList(
                        new DialogOptions.OptionItem(1, "Copy", R.drawable.ic_content_copy_white),
                        new DialogOptions.OptionItem(2, "Edit", R.drawable.delete_edit_2),
                        new DialogOptions.OptionItem(3, "Delete", R.drawable.ic_delete_white)
                );

                DialogOptions.show(getContext(), opts, item -> {
                    switch (item.id) {
                        case 1: {
                            ClipboardManager clipboard = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
                            ClipData clip = ClipData.newPlainText("Boid", String.valueOf(listmapBoidList.get(currentDataIndex).get("boid")));
                            clipboard.setPrimaryClip(clip);
                            Utilities.showSnackbar(parent_linear_bottom_nav, "", "Boid number copied to clipboard");
                            break;
                        }
                        case 2: {
                            editName = String.valueOf(listmapBoidList.get(currentDataIndex).get("name"));
                            editBoid = String.valueOf(listmapBoidList.get(currentDataIndex).get("boid"));
                            editPosition = currentDataIndex;
                            editBoid();
                            break;
                        }
                        case 3: {
                            Utilities.alertDialog(
                                    getContext(),
                                    "Delete Boid",
                                    null,
                                    "Are you sure want to delete the following boid number? \n\n" +
                                            "Name: " + listmapBoidList.get(currentDataIndex).get("name") + "\n" +
                                            "Boid: " + listmapBoidList.get(currentDataIndex).get("boid") + "\n\n" +
                                            "Note: You cannot undo this action, and have to manually add it back again.",
                                    "Delete",
                                    "Cancel",
                                    (dialogInterface, i) -> {
                                        listmapBoidList.remove(currentDataIndex);
                                        SharedPreferences.Editor editor = sharedPreferencesBoidList.edit();
                                        editor.putString("boidList", new Gson().toJson(listmapBoidList));
                                        editor.apply();
                                        notifyDataSetChanged();
                                        Utilities.showSnackbar(parent_linear_bottom_nav, "", "Boid deleted successfully");
                                    },
                                    null
                            );
                            break;
                        }
                    }
                });
            });
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
            }
        }
    }

    // ---------------- Edit Dialog ----------------

    public void editBoid () {
        final AlertDialog dialog1 = new AlertDialog.Builder(getContext()).create();
        View inflate = getLayoutInflater().inflate(R.layout.edit_boid,null);
        dialog1.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog1.setView(inflate);
        TextView t1 = (TextView) inflate.findViewById(R.id.t1);
        final TextView t2 = (TextView) inflate.findViewById(R.id.t2);
        final TextView t3 = (TextView) inflate.findViewById(R.id.t3);
        final EditText e1 = (EditText) inflate.findViewById(R.id.e1);
        final EditText e2 = (EditText) inflate.findViewById(R.id.e2);
        LinearLayout l1 = (LinearLayout) inflate.findViewById(R.id.l1);
        LinearLayout l2 = (LinearLayout) inflate.findViewById(R.id.l2);
        LinearLayout b1 = (LinearLayout) inflate.findViewById(R.id.b1);
        ImageView i1 = (ImageView) inflate.findViewById(R.id.i1);
        LinearLayout bg = (LinearLayout) inflate.findViewById(R.id.bg);
        LinearLayout bg1 = (LinearLayout) inflate.findViewById(R.id.bg1);
        t1.setTypeface(Typeface.createFromAsset(getContext().getAssets(),"fonts/sansation_regular.ttf"), Typeface.NORMAL);
        t2.setTypeface(Typeface.createFromAsset(getContext().getAssets(),"fonts/sansation_regular.ttf"), Typeface.NORMAL);
        t3.setTypeface(Typeface.createFromAsset(getContext().getAssets(),"fonts/sansation_regular.ttf"), Typeface.NORMAL);
        i1.setImageResource(R.drawable.ic_clear_white);
        t1.setText("Edit Boid");
        e2.setFilters(new InputFilter[]{new InputFilter.LengthFilter(16)});
        e1.setHint("Enter name");
        e1.setHintTextColor(0xFF607D8B);
        e2.setHint("16-Digit Boid");
        e2.setHintTextColor(0xFF607D8B);
        e2.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence _param1, int _param2, int _param3, int _param4) {
                t2.setText(String.valueOf((long)(e2.getText().toString().length())).concat("/16"));
            }
            @Override public void beforeTextChanged(CharSequence _s, int a, int b, int c) {}
            @Override public void afterTextChanged(Editable _s) {}
        });
        t3.setText("Save changes");

        // FIX: prefill correct fields
        e1.setText(editName);
        e2.setText(editBoid);

        // UI
        Utilities.cornerRadiusWithStroke(bg, "#ffffff", "#607D8B", 0, 10, 10, 0, 0, false);
        Utilities.cornerRadiusWithStroke(bg1, "#ffffff", "#607D8B", 0, 0, 0, 10, 10, false);
        Utilities.cornerRadiusWithStroke(b1, "#607D8B", "#FFFFFF", 0, 15, 15, 15, 15, true);
        Utilities.cornerRadiusWithStroke(l1, "#ffffff", "#607D8B", 2, 10, 10, 10, 10, false);
        Utilities.cornerRadiusWithStroke(l2, "#ffffff", "#607D8B", 2, 10, 10, 10, 10, false);

        b1.setOnClickListener(v -> {
            if (e1.getText().toString().trim().isEmpty()) {
                Utilities.showSnackbar(parent_linear_bottom_nav, "", "Invalid name");
            } else if (e2.getText().toString().length() < 16) {
                Utilities.showSnackbar(parent_linear_bottom_nav, "", "Invalid Boid");
            } else if (!e2.getText().toString().substring(0, 3).contains("130")) {
                Utilities.showSnackbar(parent_linear_bottom_nav, "", "Invalid Boid");
            } else {
                // Save name and boid to SharedPreferences as a json string
                HashMap<String, Object> mapBoidList = new HashMap<>();
                mapBoidList.put("name", e1.getText().toString());
                mapBoidList.put("boid", e2.getText().toString());
                mapBoidList.put("timestamp", System.currentTimeMillis());

                // replace edited item
                listmapBoidList.set((int) editPosition, mapBoidList);

                String jsonBoidList = new Gson().toJson(listmapBoidList);
                SharedPreferences.Editor editor = sharedPreferencesBoidList.edit();
                editor.putString("boidList", jsonBoidList);
                editor.apply();

                // Notify Boids fragment to reload instantly
                Bundle result = new Bundle();
                result.putBoolean("updated", true);
                getParentFragmentManager().setFragmentResult("boid_updated", result);

                // DO NOT remove the edited row (bug fix)
                Utilities.showSnackbar(parent_linear_bottom_nav, "", "Boid edited successfully");
                dialog1.dismiss();
            }
        });

        i1.setOnClickListener(v -> dialog1.dismiss());
        dialog1.setCancelable(false);
        dialog1.show();
    }
}
