package com.example.myapplication;

import android.app.AlertDialog;
import android.app.Application;
import android.content.DialogInterface;

import com.onesignal.OneSignal;
import android.util.Log;

public class MyApplication extends Application {

    private static final String ONESIGNAL_APP_ID = "88463ba9-fa9e-4f78-9543-559239f6275a";

    @Override
    public void onCreate() {
        super.onCreate();

        OneSignal.setRequiresUserPrivacyConsent(true);

        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE);
        OneSignal.initWithContext(this);
        OneSignal.setAppId(ONESIGNAL_APP_ID);
        // OneSignal.provideUserConsent(true);
    }

    private void showPrivacyConsentDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Dozvola za notifikacije")
                .setMessage("Naša aplikacija koristi push notifikacije za obaveštavanje o novostima. Da li dozvoljavate primanje notifikacija?")
                .setCancelable(false)
                .setPositiveButton("Prihvatam", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Korisnik je dao pristanak
                        OneSignal.provideUserConsent(true);
                    }
                })
                .setNegativeButton("Ne hvala", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Korisnik nije dao pristanak
                        OneSignal.provideUserConsent(false);
                    }
                })
                .show();
    }

}