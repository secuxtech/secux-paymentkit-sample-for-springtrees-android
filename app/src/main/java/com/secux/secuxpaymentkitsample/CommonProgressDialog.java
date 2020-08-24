package com.secux.secuxpaymentkitsample;


import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;


public class CommonProgressDialog {
    private AlertDialog mAlertDialog;

    public void showProgressDialog(Context context) {

        mAlertDialog = new AlertDialog.Builder(context, R.style.CustomProgressDialog).create();
        View loadView = LayoutInflater.from(context).inflate(R.layout.dialog_common_progress, null);
        mAlertDialog.setView(loadView, 0, 0, 0, 0);
        mAlertDialog.setCanceledOnTouchOutside(false);

        mAlertDialog.show();
    }

    public void showProgressDialog(Context context, String tip) {
        if (TextUtils.isEmpty(tip)) {
            tip = "";
        }

        mAlertDialog = new AlertDialog.Builder(context, R.style.CustomProgressDialog).create();
        View loadView = LayoutInflater.from(context).inflate(R.layout.dialog_common_progress, null);
        mAlertDialog.setView(loadView, 0, 0, 0, 0);
        mAlertDialog.setCanceledOnTouchOutside(false);

        TextView tvTip = loadView.findViewById(R.id.tvTip);
        tvTip.setText(tip);

        mAlertDialog.show();
    }

    public void setProgressTip(String tip){
        TextView tvTip = mAlertDialog.findViewById(R.id.tvTip);
        tvTip.setText(tip);
    }


    public boolean isProgressVisible(){
        if (mAlertDialog == null) {
            return false;
        }
        return true;
    }

    public void dismiss() {
        if (mAlertDialog != null && mAlertDialog.isShowing()) {
            mAlertDialog.dismiss();
            mAlertDialog = null;
        }
    }
}
