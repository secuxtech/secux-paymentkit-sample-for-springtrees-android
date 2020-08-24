package com.secux.secuxpaymentkitsample;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Toast;


import com.secuxtech.paymentkit.SecuXAccountManager;
import com.secuxtech.paymentkit.SecuXPaymentManager;
import com.secuxtech.paymentkit.SecuXServerRequestHandler;
import com.secuxtech.paymentkit.SecuXStoreInfo;

import org.json.JSONObject;




public class MainActivity extends BaseActivity{


    final private String mAccountName = "secuxdemo";
    final private String mAccountPwd = "secuxdemo168";


    private SecuXPaymentManager mPaymentManager = new SecuXPaymentManager();
    private SecuXAccountManager mAccountManager = new SecuXAccountManager();



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


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK && data!=null){
            if (requestCode == 0x01){
                final String qrcode = data.getExtras().getString("qrcode");

                showProgress("processing...");
                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        doPromotionVerify(qrcode, "TransTest0001");

                    }
                }).start();

            }

        }
    }

    public void onClickScanQRCodeButton(View v){
        Intent newIntent = new Intent(mContext, ScanQRCodeActivity.class);
        //startActivity(newIntent);

        startActivityForResult(newIntent, 0x01);


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

        SecuXQRCodeParser qrCodeParser = null;
        try{
            qrCodeParser = new SecuXQRCodeParser(devQRCodeInfo);
        }catch (Exception e){
            showAlertInMain("Unsupported QRCode!", "", true);
            return new Pair<>(-1, "Invalid QRCode");
        }

        if (!login(this.mAccountName, this.mAccountPwd)){
            showAlertInMain("Login failed!", "", true);
            return new Pair<>(-2, "Loin failed!");
        }

        final Pair<Pair<Integer, String>, SecuXStoreInfo> storeInfo = mPaymentManager.getStoreInfo(qrCodeParser.mDevIDHash);
        if (storeInfo.first.first != SecuXServerRequestHandler.SecuXRequestOK){
            showAlertInMain("Get store info. failed!", "", true);
            return new Pair<>(-3, "Get store info. from server failed! error: " + storeInfo.first.second);
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                SecuXStoreInfo.SecuXPromotion promotion = storeInfo.second.getPromotionDetails("test");
                PromotionDetailsDialog detailsDlg = new PromotionDetailsDialog();
                detailsDlg.showProgressDialog(mContext, storeInfo.second, promotion);
            }
        });


        /*

        Pair<Integer, String> verifyRet = mPaymentManager.doActivity(this, this.mAccountName, storeInfo.second.mDevID,
                                                                        qrCodeParser.mCoin, qrCodeParser.mToken, transID,
                                                                        qrCodeParser.mAmount, qrCodeParser.mNonce);
        if (verifyRet.first == SecuXServerRequestHandler.SecuXRequestUnauthorized){
            if (!login(this.mAccountName, this.mAccountPwd)){
                showAlertInMain("Login failed!", "", true);
                return new Pair<>(-2, "Login failed!");
            }
            verifyRet = mPaymentManager.doActivity(this, this.mAccountName, storeInfo.second.mDevID,
                                                    qrCodeParser.mCoin, qrCodeParser.mToken, transID,
                                                    qrCodeParser.mAmount, qrCodeParser.mNonce);
        }

        if (verifyRet.first != SecuXServerRequestHandler.SecuXRequestOK){
            showAlertInMain("Verify the promotion data to P22 failed!", "error: " + verifyRet.second, true);
            return new Pair<>(-4, "Verify the promotion data to P22 failed! error:" + verifyRet.second);
        }else{
            showAlertInMain("Verify the promotion data to P22 successfully!", "", true);
        }
        */
        return new Pair<>(0, "");

    }


}