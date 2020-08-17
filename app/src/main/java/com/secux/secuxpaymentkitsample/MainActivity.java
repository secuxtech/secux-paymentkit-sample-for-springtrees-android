package com.secux.secuxpaymentkitsample;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;

import com.secuxtech.paymentkit.SecuXAccountManager;
import com.secuxtech.paymentkit.SecuXPaymentManager;
import com.secuxtech.paymentkit.SecuXServerRequestHandler;
import com.secuxtech.paymentkit.SecuXStoreInfo;

import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {



    final private String mAccountName = "secuxdemo";
    final private String mAccountPwd = "secuxdemo168";

    final private String mTestQRCode = "{\"amount\":\"1\", \"coinType\":\"DCT:SPC\", \"nonce\":\"281aedbc\", \"deviceIDhash\":\"4afff62e0b314266d9e1b3a48158d56134331a9f\"}";

    private SecuXPaymentManager mPaymentManager = new SecuXPaymentManager();
    private SecuXAccountManager mAccountManager = new SecuXAccountManager();

    private Context mContext = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android M Permission check

            if (this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }
    }

    public void onClickTestWithHardcodeQRCodeButton(View v){

        new Thread(new Runnable() {
            @Override
            public void run() {
                final Pair<Integer, String> ret = doPromotionVerify(mTestQRCode, "Test0001");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        final AlertDialog alertDialog = new AlertDialog.Builder(mContext).create();
                        alertDialog.setTitle("Do promotion activity result");

                        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                alertDialog.dismiss();
                            }
                        });


                        if (ret.first == 0){
                            alertDialog.setMessage("Successfully!");
                        }else{
                            alertDialog.setMessage("Failed! " + ret.second);
                        }

                        alertDialog.show();
                    }
                });

            }
        }).start();


    }

    public void onClickTestWithRealQRCodeButton(View v){

    }


    public boolean login(String name, String pwd){
        Pair<Integer, String> ret = mAccountManager.loginMerchantAccount(name, pwd);
        if (ret.first == SecuXServerRequestHandler.SecuXRequestOK) {
            return true;
        }

        Log.i("", "Login merchant account failed! error:" + ret.second);
        return false;
    }

    public Pair<Integer, String> doPromotionVerify(String devQRCodeInfo, String transID){

        String nonce = "";
        String amount = "";
        String token = "";
        String coin = "";
        String devIDHash = "";

        try {
            JSONObject infoJson = new JSONObject(devQRCodeInfo);
            amount = infoJson.optString("amount");
            devIDHash = infoJson.getString("deviceIDhash");
            nonce = infoJson.getString("nonce");
            if (nonce.length()!=8){
                return new Pair<>(-1, "The QRCode has invalid nonce!");
            }

            String coinTokenInfo = infoJson.optString("coinType");
            String[] itemArr = coinTokenInfo.split(":");
            if (itemArr.length != 2){
                return new Pair<>(-1, "The QRCode has invalid promotion code!");
            }
            coin = itemArr[0];
            token = itemArr[1];


        }catch (Exception e){
            e.printStackTrace();
            return new Pair<>(-1, "Invalid QRCode infor.");
        }


        if (!login(this.mAccountName, this.mAccountPwd)){
            return new Pair<>(-2, "Loin failed!");
        }

        Pair<Pair<Integer, String>, SecuXStoreInfo> storeInfo = mPaymentManager.getStoreInfo("4afff62e0b314266d9e1b3a48158d56134331a9f");
        if (storeInfo.first.first != SecuXServerRequestHandler.SecuXRequestOK){
            return new Pair<>(-3, "Get store info. from server failed! error: " + storeInfo.first.second);
        }

        Pair<Integer, String> verifyRet = mPaymentManager.doActivity(this, this.mAccountName, storeInfo.second.mDevID,
                                                                        coin, token, transID, amount, nonce);
        if (verifyRet.first == SecuXServerRequestHandler.SecuXRequestUnauthorized){
            if (!login(this.mAccountName, this.mAccountPwd)){
                return new Pair<>(-2, "Loin failed!");
            }
            verifyRet = mPaymentManager.doActivity(this, "secuxdemo", storeInfo.second.mDevID,
                    "DCT", "SPC", transID, "1", "9a7dc748");
        }

        if (verifyRet.first != SecuXServerRequestHandler.SecuXRequestOK){
            return new Pair<>(-4, "Verify the promotion data to P22 failed! error:" + verifyRet.second);
        }

        return new Pair<>(0, "");
    }
}