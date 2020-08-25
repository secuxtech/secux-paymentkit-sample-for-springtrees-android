package com.secux.secuxpaymentkitsample;



import android.Manifest;
import android.content.Intent;
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






public class MainActivity extends BaseActivity{


    final private String mAccountName = "secuxdemo";
    final private String mAccountPwd = "secuxdemo168";

    private SecuXPaymentManager mPaymentManager = new SecuXPaymentManager();
    private SecuXAccountManager mAccountManager = new SecuXAccountManager();

    PromotionDetailsDialog mPromotionDetailsDlg = new PromotionDetailsDialog();

    SecuXQRCodeParser mQRCodeParser = null;
    SecuXStoreInfo mStoreInfo = null;

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

                        doPromotionVerify(qrcode);

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


    public void doPromotionVerify(String devQRCodeInfo){


        try{
            mQRCodeParser = new SecuXQRCodeParser(devQRCodeInfo);
        }catch (Exception e){
            showAlertInMain("Unsupported QRCode!", "", true);
            return;
        }

        if (!login(this.mAccountName, this.mAccountPwd)){
            showAlertInMain("Login failed!", "", true);
            return;
        }

        final Pair<Pair<Integer, String>, SecuXStoreInfo> storeInfo = mPaymentManager.getStoreInfo(mQRCodeParser.mDevIDHash);
        if (storeInfo.first.first != SecuXServerRequestHandler.SecuXRequestOK){
            showAlertInMain("Get store info. failed!", "", true);
            return;
        }

        mStoreInfo = storeInfo.second;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                hideProgress();
                SecuXStoreInfo.SecuXPromotion promotion = storeInfo.second.getPromotionDetails("test");
                mPromotionDetailsDlg.showDialog(mContext, storeInfo.second, promotion);
            }
        });


    }


    public void confirmPromotion(SecuXQRCodeParser qrCodeParser, SecuXStoreInfo storeInfo, String transID){
        Pair<Integer, String> verifyRet = mPaymentManager.doActivity(this, this.mAccountName, storeInfo.mDevID,
                qrCodeParser.mCoin, qrCodeParser.mToken, transID,
                qrCodeParser.mAmount, qrCodeParser.mNonce);

        if (verifyRet.first == SecuXServerRequestHandler.SecuXRequestUnauthorized){
            if (!login(this.mAccountName, this.mAccountPwd)){
                showAlertInMain("Login failed!", "", true);
                return;
            }
            verifyRet = mPaymentManager.doActivity(this, this.mAccountName, storeInfo.mDevID,
                    qrCodeParser.mCoin, qrCodeParser.mToken, transID,
                    qrCodeParser.mAmount, qrCodeParser.mNonce);
        }

        if (verifyRet.first != SecuXServerRequestHandler.SecuXRequestOK){
            showAlertInMain("Verify the promotion data to P22 failed!", "error: " + verifyRet.second, true);
        }else{
            showAlertInMain("Verify the promotion data to P22 successfully!", "", true);
        }
    }

    public void onCancelButtonClick(View v){
        mPromotionDetailsDlg.dismiss();
    }

    public void onConfirmButtonClick(View v){
        mPromotionDetailsDlg.dismiss();

        showProgress("Confirm...");
        new Thread(new Runnable() {
            @Override
            public void run() {
                confirmPromotion(mQRCodeParser, mStoreInfo, "TestTran00001");
            }
        }).start();
    }

}