package com.nitsoft.ipoallotmentprank;


import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

import java.util.Locale;

public class Utilities {

    // Global String Variables
    public static String whatsnewTitle = "IPO Result App";
    public static String whatsnewMessage = "Enjoy checking result!";
    public static String whatsnewButton = "Okay";
    public static int whatsnewIcon = R.drawable.app_icon;

    private static AlertDialog progressDialog;
    private static AlertDialog.Builder alert_dialog;

    // Function for adding background radius, shadow, color and ripple effect
    public static void setBackground(final View _view, final double _radius, final double _shadow, final String _color, final boolean _ripple) {
        if (_ripple) {
            android.graphics.drawable.GradientDrawable gd = new android.graphics.drawable.GradientDrawable();
            gd.setColor(Color.parseColor(_color));
            gd.setCornerRadius((int) _radius);
            _view.setElevation((int) _shadow);
            android.content.res.ColorStateList clrb = new android.content.res.ColorStateList(new int[][]{new int[]{}}, new int[]{Color.parseColor("#4D4A4A4A")});
            android.graphics.drawable.RippleDrawable ripdrb = new android.graphics.drawable.RippleDrawable(clrb, gd, null);
            _view.setClickable(true);
            _view.setBackground(ripdrb);
        } else {
            android.graphics.drawable.GradientDrawable gd = new android.graphics.drawable.GradientDrawable();
            gd.setColor(Color.parseColor(_color));
            gd.setCornerRadius((int) _radius);
            _view.setBackground(gd);
            _view.setElevation((int) _shadow);
        }
    }

    public static void cornerRadiusWithStroke(final View view, final String bgColor, final String strokeColor, final double strokeWidth, final double c1, final double c2, final double c3, final double c4, final boolean ripple) {
        android.graphics.drawable.GradientDrawable gd = new android.graphics.drawable.GradientDrawable();

        gd.setColor(Color.parseColor(bgColor));
        gd.setStroke((int) strokeWidth, Color.parseColor(strokeColor));
        gd.setCornerRadii(new float[]{(int) c1, (int) c1, (int) c2, (int) c2, (int) c3, (int) c3, (int) c4, (int) c4});

        if (ripple) {
            android.content.res.ColorStateList clrb = new android.content.res.ColorStateList(new int[][]{new int[]{}}, new int[]{Color.parseColor("#555b7b")});
            android.graphics.drawable.RippleDrawable ripdrb = new android.graphics.drawable.RippleDrawable(clrb, gd, null);
            view.setClickable(true);
            view.setBackground(ripdrb);
        } else {
            view.setBackground(gd);
        }
    }

    public static void createRippleEffect(Context context, View view) {
        int[] attrs = new int[]{android.R.attr.selectableItemBackgroundBorderless};
        TypedArray typedArray = context.obtainStyledAttributes(attrs);
        int backgroundResource = typedArray.getResourceId(0, 0);
        typedArray.recycle();
        view.setBackgroundResource(backgroundResource);
        view.setClickable(true);
    }

    public static void btnRipple(final View _v) {

        android.graphics.drawable.RippleDrawable ripdr = new android.graphics.drawable.RippleDrawable(new android.content.res.ColorStateList(new int[][]{new int[]{}}, new int[]{Color.parseColor("#BDBDBD")}), new android.graphics.drawable.ColorDrawable(Color.WHITE), null);
        _v.setBackground(ripdr);

    }

//    public static void showMaterialSnackbar(final String title, final String action, final boolean flag, final View view) {
//        Snackbar snackbar = Snackbar.make(view, title, Snackbar.LENGTH_LONG);
//        TextView tv = (snackbar.getView()).findViewById(com.google.android.material.R.id.snackbar_text);
//        if (flag) {
//            snackbar.setAction(action, new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                }
//            });
//        }
//        snackbar.setDuration(3000);
//
//        snackbar.show();
//    }

    public static void hide_scrollbar(final View view) {
        view.setVerticalScrollBarEnabled(false);
    }

    public static void copyToClipboard(Context context, String text) {
        // Get the ClipboardManager instance
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);

        // Check Android version and handle accordingly
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) { // API level 11
            ClipData clip = ClipData.newPlainText("label", text);
            clipboard.setPrimaryClip(clip);
        } else {
            // For devices below Honeycomb, use the older method
            android.text.ClipboardManager oldClipboard = (android.text.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            oldClipboard.setText(text);
        }

    }

    public static void negativeDialog(Context context, String title, String message, String buttonText, View.OnClickListener buttonAction) {
        AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        View view = LayoutInflater.from(context).inflate(R.layout.negative_dialog, null);
        alertDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        alertDialog.setView(view);

        TextView textview_title = view.findViewById(R.id.t1);
        TextView textview_message = view.findViewById(R.id.t2);
        TextView textview_button = view.findViewById(R.id.t3);
        LinearLayout button_action = view.findViewById(R.id.bt);
        ImageView imageview_cancel = view.findViewById(R.id.i1);
        LinearLayout linear_title = view.findViewById(R.id.bg);
        LinearLayout linear_body = view.findViewById(R.id.bg1);

        textview_title.setTypeface(Typeface.createFromAsset(context.getAssets(), "fonts/sansation_regular.ttf"), Typeface.NORMAL);
        textview_message.setTypeface(Typeface.createFromAsset(context.getAssets(), "fonts/sansation_regular.ttf"), Typeface.NORMAL);
        textview_button.setTypeface(Typeface.createFromAsset(context.getAssets(), "fonts/sansation_regular.ttf"), Typeface.NORMAL);
        imageview_cancel.setImageResource(R.drawable.ic_clear_white);
        imageview_cancel.setColorFilter(0xFFB71C1C);

        textview_title.setText(title);
        textview_message.setText(message);
        textview_button.setText(buttonText);

        setBackground(linear_title, 10, 0, "#FFFFFF", false);
        setBackground(linear_body, 10, 0, "#FFFFFF", false);
        cornerRadiusWithStroke(button_action, "#B71C1C", "#000000", 0, 0, 0, 9, 9, false);

        // Set the button action
        button_action.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Dismiss the dialog first
                alertDialog.dismiss();

                // If a custom action is provided, execute it
                if (buttonAction != null) {
                    buttonAction.onClick(v);
                }
            }
        });


        // Set the cancel button action
        imageview_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

        alertDialog.setCancelable(false);
        alertDialog.show();
    }

    public static void setFont(Context context, TextView textView, String fontName, int style) {
        Typeface typeface = Typeface.createFromAsset(context.getAssets(), "fonts/" + fontName);
        textView.setTypeface(Typeface.create(typeface, style));
    }

    // Method to show the progress dialog with a message
    public static void showProgressDialog(Context context, String message) {
        if (progressDialog != null && progressDialog.isShowing()) {
            return; // Avoid showing multiple dialogs
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(false);

        // Inflate the progress bar
        ProgressBar progressBar = new ProgressBar(context);
        progressBar.setIndeterminate(false);
        progressBar.setVisibility(ProgressBar.VISIBLE);

        // Add the message TextView
        TextView messageView = new TextView(context);
        messageView.setText(message);
        messageView.setPadding(30, 20, 30, 0);  // Add padding to the text

        // Add ProgressBar and message to the dialog
        builder.setView(progressBar)
                .setMessage(message)
                .setCancelable(false); // Dialog won't be dismissable by tapping outside

        // Create and show the dialog
        progressDialog = builder.create();
        progressDialog.show();
    }

    // Method to hide the progress dialog
    public static void hideProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    // Disable touch events for scrolling listview
    public static void disableScrolling(ListView listview) {
        listview.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // Consume the touch event to prevent scrolling
                return true;
            }
        });
    }

    // Snackbar with Custom Title
    public static void showSnackbar(View view, String title, String message) {
        if (view == null) return;
        String formattedMessage = title + message;
        Snackbar snackbar = Snackbar.make(view, formattedMessage, 5000);

        // Customize Snackbar if needed (optional)
        snackbar.setAction("OK", v -> snackbar.dismiss());
        snackbar.setActionTextColor(Color.WHITE);
        snackbar.show();
    }

    public static DeviceInfo getDeviceInfo(Context context) {
        DeviceInfo deviceInfo = new DeviceInfo();

        // Get device information
        deviceInfo.model = Build.MODEL; // Device model
        deviceInfo.manufacturer = Build.MANUFACTURER; // Device manufacturer
        deviceInfo.sdkVersion = Build.VERSION.SDK_INT; // Android SDK version
        deviceInfo.releaseVersion = Build.VERSION.RELEASE; // Android OS release version

        // Get Android ID (a unique identifier for the app on the device)
        deviceInfo.androidId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);

        // Get screen size and density
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(displayMetrics);
        deviceInfo.width = displayMetrics.widthPixels; // Screen width in pixels
        deviceInfo.height = displayMetrics.heightPixels; // Screen height in pixels
        deviceInfo.density = displayMetrics.density; // Screen density

        // Get battery information
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, ifilter);
        int level = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) : -1;
        int scale = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1) : -1;
        deviceInfo.batteryPct = (level / (float) scale) * 100; // Battery percentage

        // Get network information
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm != null ? cm.getActiveNetworkInfo() : null;
        deviceInfo.isConnected = activeNetwork != null && activeNetwork.isConnected(); // Network connectivity status
        deviceInfo.networkType = activeNetwork != null ? activeNetwork.getTypeName() : "No connection"; // Network type

        // Get storage information
        StatFs statFs = new StatFs(Environment.getDataDirectory().getPath());
        deviceInfo.totalBytes = (long) statFs.getBlockCount() * statFs.getBlockSize(); // Total internal storage
        deviceInfo.availableBytes = (long) statFs.getAvailableBlocks() * statFs.getBlockSize(); // Available internal storage

        // Get locale and language information
        Locale locale = Locale.getDefault();
        deviceInfo.language = locale.getLanguage(); // Device language
        deviceInfo.country = locale.getCountry(); // Device country

        // Get CPU information
        deviceInfo.numberOfCores = Runtime.getRuntime().availableProcessors(); // Number of CPU cores
        deviceInfo.cpuArchitecture = Build.SUPPORTED_ABIS[0]; // CPU architecture

        return deviceInfo;
    }

    public static class DeviceInfo {
        public String model;
        public String manufacturer;
        public int sdkVersion;
        public String releaseVersion;
        public String imei;
        public String androidId;
        public int width;
        public int height;
        public float density;
        public float batteryPct;
        public boolean isConnected;
        public String networkType;
        public long totalBytes;
        public long availableBytes;
        public String language;
        public String country;
        public int numberOfCores;
        public String cpuArchitecture;
    }

    // Method to show edittext length count on addTextChanged
    public static void setBoidLengthWatcher(final EditText editText, final TextView counterText, final TextView resultText, final Button button) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int length = s.length();
                counterText.setText(length + "/16");

                if (length < 16) {
                    resultText.setText("");
                    resultText.setVisibility(View.INVISIBLE);
                }

                button.setEnabled(length == 16);
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });
    }
    // Alert Dialog with Custom Icon, Title, Message, Positive Button and Negative Button with Button Actions
    public static void alertDialog(Context context, String title, Drawable icon, String message, String positiveButtonText, String negativeButtonText, DialogInterface.OnClickListener onClickListenerPositive, DialogInterface.OnClickListener onClickListenerNegative) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        dialogBuilder.setTitle(title);

        if (icon != null) {
            dialogBuilder.setIcon(icon);
        }

        dialogBuilder.setMessage(message);
        dialogBuilder.setPositiveButton(positiveButtonText,
                onClickListenerPositive != null ? onClickListenerPositive : (dialog, which) -> dialog.dismiss());

        dialogBuilder.setNegativeButton(negativeButtonText,
                onClickListenerNegative != null ? onClickListenerNegative : (dialog, which) -> dialog.dismiss());

        dialogBuilder.setCancelable(true);

        // Get dialog instance first
        AlertDialog dialog = dialogBuilder.create();
        dialog.show();

        // Disable all-caps on buttons
        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
        if (positiveButton != null) positiveButton.setAllCaps(false);
        if (negativeButton != null) negativeButton.setAllCaps(false);
    }

}
