package com.barcodeapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseArray;
import android.widget.Toast;

import com.google.android.gms.vision.barcode.Barcode;

import java.util.List;

import info.androidhive.barcode.BarcodeReader;

/**
 * Created by ADMIN on 27-Mar-18.
 */

public class ScanBarcodeActivity extends AppCompatActivity implements BarcodeReader.BarcodeReaderListener {

    private static final String TAG = ScanBarcodeActivity.class.getSimpleName();
    private BarcodeReader barcodeReader;
    private String mBagsScanValues;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanbarcode);
        barcodeReader = (BarcodeReader) getSupportFragmentManager().findFragmentById(R.id.barcode_fragment);
    }

    @Override
    public void onScanned(final Barcode barcode) {
        barcodeReader.playBeep();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mBagsScanValues = barcode.displayValue;
                Log.e(TAG, "mBagsScanValues: " + mBagsScanValues);
                Intent intent = new Intent(ScanBarcodeActivity.this,MainActivity.class);
                intent.putExtra("mBagsScanValues",mBagsScanValues);
                startActivity(intent);
                overridePendingTransition(R.anim.activity_back_in, R.anim.activity_back_out);
                finish();
            }
        });
    }

    @Override
    public void onScannedMultiple(List<Barcode> barcodes) {
        Log.e(TAG, "onScannedMultiple: " + barcodes.size());
        String codes = "";
        for (Barcode barcode : barcodes) {
            codes += barcode.displayValue + ", ";
        }
        final String finalCodes = codes;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                Toast.makeText(getApplicationContext(), "Barcodes: " + finalCodes, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onBitmapScanned(SparseArray<Barcode> sparseArray) {
    }

    @Override
    public void onScanError(String s) {
    }

    @Override
    public void onCameraPermissionDenied() {
        Toast.makeText(getApplicationContext(), "Camera permission denied!", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(ScanBarcodeActivity.this,MainActivity.class));
        overridePendingTransition(R.anim.activity_back_in, R.anim.activity_back_out);
        finish();
    }
}