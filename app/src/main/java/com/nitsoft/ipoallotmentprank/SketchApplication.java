package com.nitsoft.ipoallotmentprank;

import android.app.Application;
import com.google.android.gms.ads.MobileAds;

public class SketchApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        
        // Initialize AdMob
        MobileAds.initialize(this, initializationStatus -> {
            // AdMob initialization completed
        });
    }
}
