package com.secux.secuxpaymentkitsample;

import android.util.Pair;

import org.json.JSONObject;

public class SecuXQRCodeParser {

    public String mQRCodeStr   = "";
    public String mAmount      = "";
    public String mCoin        = "";
    public String mToken       = "";
    public String mNonce       = "";
    public String mDevIDHash   = "";

    public SecuXQRCodeParser(String p22QRCode) throws Exception{

        try {
            JSONObject infoJson = new JSONObject(p22QRCode);
            String amount = infoJson.getString("amount");
            String devIDHash = infoJson.getString("deviceIDhash");
            String nonce = infoJson.getString("nonce");
            if (nonce.length()!=8){
                throw new Exception("The QRCode has invalid nonce!");
            }

            String coinTokenInfo = infoJson.optString("coinType");
            String[] itemArr = coinTokenInfo.split(":");
            if (itemArr.length != 2 || itemArr[0].length()==0 || itemArr[1].length()==0){
                throw new Exception("The QRCode has invalid promotion code!");
            }

            this.mCoin = itemArr[0];
            this.mToken = itemArr[1];
            this.mAmount = amount;
            this.mNonce = nonce;
            this.mDevIDHash = devIDHash;

        }catch (Exception e){
            e.printStackTrace();
            throw new Exception("Invalid QRCode infor.");
        }

        mQRCodeStr = p22QRCode;
    }


}
