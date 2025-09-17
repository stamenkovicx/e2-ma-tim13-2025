package com.example.myapplication.presentation.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class QRScannerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Pokretanje skenera odmah po kreiranju aktivnosti
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setPrompt("Skenirajte QR kod prijatelja");
        integrator.setOrientationLocked(false);
        integrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        if(result != null) {
            if(result.getContents() == null) {
                // Korisnik je otkazao skeniranje
                Toast.makeText(this, "Scan canceled", Toast.LENGTH_LONG).show();
            } else {
                // Skeniranje je uspjelo, dobijamo ID korisnika iz QR koda
                String scannedUserId = result.getContents();

                // Vracanje rezultata u prethodnu aktivnost (npr. FriendsActivity)
                Intent returnIntent = new Intent();
                returnIntent.putExtra("scannedUserId", scannedUserId);
                setResult(Activity.RESULT_OK, returnIntent);
            }
        }
        finish();
    }
}