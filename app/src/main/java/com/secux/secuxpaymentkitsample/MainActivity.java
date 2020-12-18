package com.secux.secuxpaymentkitsample;



import android.Manifest;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.util.Pair;
import android.view.View;

import com.secuxtech.paymentdevicekit.PaymentPeripheralManager;
import com.secuxtech.paymentdevicekit.SecuXPaymentUtility;
import com.secuxtech.paymentkit.SecuXAccountManager;
import com.secuxtech.paymentkit.SecuXPaymentManager;
import com.secuxtech.paymentkit.SecuXServerRequestHandler;
import com.secuxtech.paymentkit.SecuXStoreInfo;

import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;

import static com.secuxtech.paymentdevicekit.PaymentPeripheralManagerV1.SecuX_Peripheral_Operation_OK;



public class MainActivity extends BaseActivity{


    private SecuXPaymentManager mPaymentManager = new SecuXPaymentManager();
    private SecuXAccountManager mAccountManager = new SecuXAccountManager();
    private PaymentPeripheralManager mPaymentPeripheralManager = new PaymentPeripheralManager(mContext, 10, -80, 30);

    private PromotionDetailsDialog mPromotionDetailsDlg = new PromotionDetailsDialog();
    private PaymentDetailsDialog mPaymentDetailsDlg = new PaymentDetailsDialog();
    private RefillDetailsDialog mRefillDetailsDlg = new RefillDetailsDialog();

    private SecuXQRCodeParser mQRCodeParser = null;
    private SecuXStoreInfo mStoreInfo = null;

    private String mDevIVKey = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAccountManager.setBaseServer("https://pmsweb-sandbox.secuxtech.com");
        checkBLESetting();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK && data!=null){
            if (requestCode == 0x01){
                final String qrcode = data.getExtras().getString("qrcode");

                try{
                    mQRCodeParser = new SecuXQRCodeParser(qrcode);
                }catch (Exception e){
                    showAlertInMain("Unsupported QRCode!", "", true);
                    return;
                }
                mDevIVKey = "";
                showProgress("processing...");

                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        String accName = "sttest";
                        String accPwd = "sttest168";
                        if (!login(accName, accPwd)){
                            showAlertInMain("Login failed!", "", true);
                            return;
                        }

                        final Pair<Pair<Integer, String>, SecuXStoreInfo> storeInfo = mPaymentManager.getStoreInfo(mQRCodeParser.mDevIDHash);
                        if (storeInfo.first.first != SecuXServerRequestHandler.SecuXRequestOK){
                            showAlertInMain("Get store info. failed!", "", true);
                            return;
                        }
                        mStoreInfo = storeInfo.second;

                        byte[] code = SecuXPaymentUtility.hexStringToData(mQRCodeParser.mNonce);
                        Pair<Integer, String> ret = mPaymentPeripheralManager.doGetIVKey(code, mContext, 10, mStoreInfo.mDevID, -80, 30);
                        if (ret.first != SecuX_Peripheral_Operation_OK){
                            showAlertInMain("Get device ivKey failed!", "", true);
                            return;
                        }
                        mDevIVKey = ret.second;

                        if (mQRCodeParser.mAmount.length()>0) {
                            if (mQRCodeParser.mCoin.compareTo("$") == 0) {
                                showPromotionDetails();
                            }else{
                                showPaymentDetails();
                            }
                        }else if (mQRCodeParser.mRefill.length()>0) {
                            showRefillDetails();
                        }
                    }
                }).start();

            }
        }
    }


    public void onClickScanQRCodeButton(View v){
        if (!checkBLESetting()) {
            return;
        }

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


    public void showPromotionDetails(){

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                hideProgress();
                SecuXStoreInfo.SecuXPromotion promotion = mStoreInfo.getPromotionDetails(mQRCodeParser.mToken);
                if (promotion != null) {
                    mPromotionDetailsDlg.showDialog(mContext, mStoreInfo, promotion);
                }else{
                    showAlert("Unsupported promotion!", "");
                }
            }
        });

    }

    public void showPaymentDetails(){

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                hideProgress();
                mPaymentDetailsDlg.showDialog(mContext, mStoreInfo, mQRCodeParser.mCoin, mQRCodeParser.mToken, mQRCodeParser.mAmount);
            }
        });

    }

    public void showRefillDetails(){

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                hideProgress();
                mRefillDetailsDlg.showDialog(mContext, mStoreInfo, mQRCodeParser.mCoin, mQRCodeParser.mToken, mQRCodeParser.mRefill);
            }
        });

    }

    public void confirmOperation(SecuXStoreInfo storeInfo, String transID, String coin, String token, String amount, String nonce, String type){

        String accName = "springtreesoperator";
        String accPwd = "springtrees";

        if (!login(accName, accPwd)){
            mPaymentPeripheralManager.requestDisconnect();
            showAlertInMain("Login failed!", "", true);
            return;
        }

        Pair<Integer, String> genEncDataRet = mPaymentManager.generateEncryptedData(mDevIVKey, accName, storeInfo.mDevID,
                coin, token, transID, amount, type);

        if (genEncDataRet.first != SecuXServerRequestHandler.SecuXRequestOK){
            mPaymentPeripheralManager.requestDisconnect();
            showAlertInMain("Generate encrypted data failed!", "error: " + genEncDataRet.second, true);
            return;
        }

        String encryptedStr = genEncDataRet.second;
        final byte[] encryptedData = Base64.decode(encryptedStr, Base64.DEFAULT);

        android.util.Pair<Integer, String> verifyRet = mPaymentPeripheralManager.doPaymentVerification(encryptedData);
        if (verifyRet.first == SecuX_Peripheral_Operation_OK){
            showAlertInMain("Verify the operation data to P22 successfully!", "", true);
        }else{
            mPaymentPeripheralManager.requestDisconnect();
            showAlertInMain("Verify the operation data to P22 failed!", verifyRet.second, true);
        }

    }

    public void cancelOperation(){
        this.mPaymentPeripheralManager.requestDisconnect();
    }


    public void onCancelPromotionButtonClick(View v){
        mPromotionDetailsDlg.dismiss();
        cancelOperation();
    }

    public void onConfirmPromotionButtonClick(View v){
        mPromotionDetailsDlg.dismiss();

        showProgress("Confirm...");
        new Thread(new Runnable() {
            @Override
            public void run() {
                confirmOperation(mStoreInfo, "Promotion00001", mQRCodeParser.mCoin, mQRCodeParser.mToken, mQRCodeParser.mAmount, mQRCodeParser.mNonce, "promotion");
            }
        }).start();
    }


    public void onCancelRefillButtonClick(View v){
        mRefillDetailsDlg.dismiss();
        cancelOperation();
    }

    public void onConfirmRefillButtonClick(View v){
        mRefillDetailsDlg.dismiss();

        showProgress("Confirm...");
        new Thread(new Runnable() {
            @Override
            public void run() {
                confirmOperation(mStoreInfo, "Refill00001", mQRCodeParser.mCoin, mQRCodeParser.mToken, mQRCodeParser.mRefill, mQRCodeParser.mNonce, "refill");
            }
        }).start();
    }

    public void onCancelPaymentButtonClick(View v){
        mPaymentDetailsDlg.dismiss();
        cancelOperation();
    }

    public void onConfirmPaymentButtonClick(View v){
        mPaymentDetailsDlg.dismiss();

        showProgress("Confirm...");
        new Thread(new Runnable() {
            @Override
            public void run() {
                confirmOperation(mStoreInfo, "Payment00001", mQRCodeParser.mCoin, mQRCodeParser.mToken, mQRCodeParser.mAmount, mQRCodeParser.mNonce, "payment");
            }
        }).start();
    }
}