package com.nitsoft.ipoallotmentprank;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.HashMap;


public class SaveBoidFragmentActivity extends Fragment {

    private TextView textview_app_title;
    private TextView textview_app_subtitle;
    private LinearLayout parent_linear_bottom_nav;

    private ScrollView vscroll_main;
    private LinearLayout linear_parent;
    private LinearLayout linear_window;
    private LinearLayout linear_enter_name;
    private EditText edittext_name;
    private LinearLayout linear_enter_boid;
    private EditText edittext_boid;
    private TextView textview_boid_count;
    private LinearLayout linear_button;
    private Button button_save_boid;
    private TextView textview_temp;
    private LinearLayout linear_icon_plus;
    private ImageView imageview_icon_plus;

    private SharedPreferences sharedPreferencesBoidList;
    private ArrayList<HashMap<String, Object>> listmapBoidList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_save_boid, container, false);
        initialize(savedInstanceState, view);
        onActivityCreate();
        return view;
    }

    private void initialize(Bundle _savedInstanceState, View _view) {
        textview_app_title = (TextView) getActivity().findViewById(R.id.textview_app_title);
        textview_app_subtitle = (TextView) getActivity().findViewById(R.id.textview_app_subtitle);
        parent_linear_bottom_nav = (LinearLayout) getActivity().findViewById(R.id.parent_linear_bottom_nav);

        vscroll_main = (ScrollView) _view.findViewById(R.id.vscroll_main);
        linear_parent = (LinearLayout) _view.findViewById(R.id.linear_parent);
        linear_window = (LinearLayout) _view.findViewById(R.id.linear_window);
        linear_enter_name = (LinearLayout) _view.findViewById(R.id.linear_enter_name);
        edittext_name = (EditText) _view.findViewById(R.id.edittext_name);
        linear_enter_boid = (LinearLayout) _view.findViewById(R.id.linear_enter_boid);
        edittext_boid = (EditText) _view.findViewById(R.id.edittext_boid);
        textview_boid_count = (TextView) _view.findViewById(R.id.textview_boid_count);
        linear_button = (LinearLayout) _view.findViewById(R.id.linear_button);
        button_save_boid = (Button) _view.findViewById(R.id.button_save_boid);
        textview_temp = (TextView) _view.findViewById(R.id.textview_temp);
        linear_icon_plus = (LinearLayout) _view.findViewById(R.id.linear_icon_plus);
        imageview_icon_plus = (ImageView) _view.findViewById(R.id.imageview_icon_plus);

        sharedPreferencesBoidList = getActivity().getSharedPreferences("boidList", MODE_PRIVATE);

        // Edittext boid text change listener
        Utilities.setBoidLengthWatcher(edittext_boid, textview_boid_count, textview_temp, button_save_boid);
        // Save Boid Button Click
        button_save_boid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Check if name and boid are not empty
                if (edittext_name.getText().toString().isEmpty()) {
                    Utilities.showSnackbar(parent_linear_bottom_nav, "", "Name cannot be empty" );
                } else if (edittext_boid.getText().toString().length() < 16) {
                    Utilities.showSnackbar(parent_linear_bottom_nav, "", "Boid length should be 16" );
                } else if(!edittext_boid.getText().toString().startsWith("130")){
                    // Check if boid starts with 130
                    Utilities.showSnackbar(parent_linear_bottom_nav, "", "Boid should start with 130" );
                } else {
                    // Save name and boid to SharedPreferences as a json string
                    HashMap mapBoidList = new HashMap();
                    mapBoidList.put("name", edittext_name.getText().toString());
                    mapBoidList.put("boid", edittext_boid.getText().toString());
                    mapBoidList.put("timestamp", System.currentTimeMillis());
                    listmapBoidList.add(mapBoidList);

                    String jsonBoidList = new Gson().toJson(listmapBoidList);
                    SharedPreferences.Editor editor = sharedPreferencesBoidList.edit();
                    editor.putString("boidList", jsonBoidList);
                    editor.apply();

                    // Notify Boids fragment to reload instantly
                    Bundle result = new Bundle();
                    result.putBoolean("updated", true);
                    getParentFragmentManager().setFragmentResult("boid_updated", result);

                    Utilities.showSnackbar(parent_linear_bottom_nav, "", "Boid saved successfully");

                    // Clear edittext fields
                    edittext_name.setText("");
                    edittext_boid.setText("");
                }
            }
        });
    }

    private void onActivityCreate(){
        userInterface();
        fetchExistingBoidList();
    }
    private void userInterface(){
        textview_app_title.setText("Save Boid Number");
        textview_app_subtitle.setText("Helps you check multiple results at once!");

        Utilities.setBackground(linear_window, 20, 0, "#323957", false);
        Utilities.setBackground(linear_icon_plus, 360, 0, "#555B7B", false);
        Utilities.setBackground(linear_enter_name, 15, 0, "#FFFFFF", false);
        Utilities.setBackground(linear_enter_boid, 15, 0, "#FFFFFF", false);
        Utilities.setBackground(button_save_boid, 15, 0, "#FFFFFF", true);
    }

    private void fetchExistingBoidList() {
        // Check if there are existing boid entries in SharedPreferences
        if(!sharedPreferencesBoidList.getString("boidList", "").isEmpty()){
            // There are existing boid entries, fetch them into listmapBoidList
            String jsonBoidList = sharedPreferencesBoidList.getString("boidList", "");
            listmapBoidList = new Gson().fromJson(jsonBoidList, new TypeToken<ArrayList<HashMap<String, Object>>>(){}.getType());
        }
    }
}