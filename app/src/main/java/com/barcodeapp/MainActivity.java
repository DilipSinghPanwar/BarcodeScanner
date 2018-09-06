package com.barcodeapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import static com.barcodeapp.CheckConnection.isNetworkConnected;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    public static final int BARCODE_REQUEST_CODE = 999;
    private Button mBtnScanBarcode, mBtnGetValues;
    private TextView mTvBarcodeValue, mTvBarcodeServerValue;
    public static String mScanValues = "";
    int TIME_OUT_MAX = 1000 * 60;
    int MAX_RETRIES = 4;
    private ProgressDialog mDialog;
    private String setValuesAPI = "http://votivephp.in/epco/rahultest/insert.php?data=";
    private String getValuesAPI = "http://votivephp.in/epco/rahultest/fetch.php";
    private String encodedUrl;
    private StringRequest mStrRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        mBtnScanBarcode = findViewById(R.id.btnScanBarcode);
        mBtnScanBarcode.setOnClickListener(this);
        mBtnGetValues = findViewById(R.id.btnGetValues);
        mBtnGetValues.setOnClickListener(this);
        mTvBarcodeValue = findViewById(R.id.tvBarcodeValue);
        mTvBarcodeServerValue = findViewById(R.id.tvBarcodeServerValue);
        mTvBarcodeServerValue.setVisibility(View.GONE);
        mDialog = new ProgressDialog(this);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            if (extras.containsKey("mBagsScanValues")) {
                mScanValues = intent.getStringExtra("mBagsScanValues" + "");
                if (!mScanValues.equalsIgnoreCase("")) {
                    mTvBarcodeValue.setText("Scan result: \n\n" + mScanValues);
                    mTvBarcodeValue.setVisibility(View.VISIBLE);
                    mSendBarcode(this);
                }
            }
        } else {
            mTvBarcodeValue.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnScanBarcode:
                startActivityForResult(new Intent(MainActivity.this, ScanBarcodeActivity.class), BARCODE_REQUEST_CODE);
                overridePendingTransition(R.anim.activity_left, R.anim.activity_right);
                finish();
                break;
            case R.id.btnGetValues:
                mGetBarcode(MainActivity.this);
                break;
        }
    }

    public void mSendBarcode(final Context mContext) {
        if (isNetworkConnected(mContext)) {
            mDialog.setMessage("Please wait....");
            mDialog.show();
            Log.e(TAG, "mSendBarcode: >>" + setValuesAPI + mScanValues);

            try {
                String encodedQueryString = URLEncoder.encode(mScanValues, "UTF-8");

                encodedUrl = setValuesAPI + "?" + encodedQueryString;

                Log.e(TAG, "encodedUrl: >>" + encodedUrl);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            mStrRequest = new StringRequest(Request.Method.POST, encodedUrl,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Log.e(TAG, "onResponse: >>" + response);
                            if (mDialog.isShowing()) {
                                mDialog.dismiss();
                            }
                            JSONObject jsonObject = null;
                            try {
                                jsonObject = new JSONObject(response);
                                String message = jsonObject.optString("message");
                                Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e(TAG, "onErrorResponse: >>" + error.getMessage());
                            if (mDialog.isShowing()) {
                                mDialog.dismiss();
                            }
                            Toast.makeText(mContext, "Error Occur!", Toast.LENGTH_SHORT).show();
                        }
                    }) {
            };
            mStrRequest.setTag(TAG);
            ApplicationController.getInstance().addToRequestQueue(mStrRequest);
            mStrRequest.setRetryPolicy(new DefaultRetryPolicy(TIME_OUT_MAX,
                    MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        } else {
            Toast.makeText(mContext, "No Internet Connection!", Toast.LENGTH_SHORT).show();
        }
    }

    public void mGetBarcode(final Context mContext) {
        if (isNetworkConnected(mContext)) {
            mDialog.setMessage("Please wait....");
            mDialog.show();
            StringRequest mStrRequest = new StringRequest(Request.Method.POST, getValuesAPI,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Log.e(TAG, "onResponse: >>" + response);
                            mTvBarcodeServerValue.setVisibility(View.VISIBLE);
                            if (mDialog.isShowing()) {
                                mDialog.dismiss();
                            }
                            JSONObject jsonObject = null;
                            try {
                                jsonObject = new JSONObject(response);
                                String result = jsonObject.optString("result");
                                mTvBarcodeServerValue.setText(result);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e(TAG, "onErrorResponse: >>" + error.getMessage());
                            if (mDialog.isShowing()) {
                                mDialog.dismiss();
                            }
                            Toast.makeText(mContext, "Error Occur!", Toast.LENGTH_SHORT).show();
                        }
                    }) {
            };
            mStrRequest.setTag(TAG);
            ApplicationController.getInstance().addToRequestQueue(mStrRequest);
            mStrRequest.setRetryPolicy(new DefaultRetryPolicy(TIME_OUT_MAX,
                    MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        } else {
            Toast.makeText(mContext, "No Internet Connection!", Toast.LENGTH_SHORT).show();
        }
    }
}
