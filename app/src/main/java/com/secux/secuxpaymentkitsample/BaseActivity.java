package com.secux.secuxpaymentkitsample;

import android.content.Context;
import android.content.DialogInterface;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class BaseActivity  extends AppCompatActivity {

    protected Context mContext = this;

    private CommonProgressDialog mProgressDlg = new CommonProgressDialog();

    public void showAlert(String title, String msg){
        final AlertDialog alertDialog = new AlertDialog.Builder(mContext).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(msg);
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                alertDialog.dismiss();
            }
        });

        alertDialog.show();
    }

    public void showAlertInMain(final String title, final String msg){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showAlert(title, msg);
            }
        });
    }

    public void showAlertInMain(final String title, final String msg, final boolean closeProgressDlg){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (closeProgressDlg){
                    mProgressDlg.dismiss();
                }

                showAlert(title, msg);
            }
        });
    }

    public void showProgress(String info){
        mProgressDlg.showProgressDialog(mContext, info);

    }

    public void showProgressInMain(String info){
        final String msgInfo = info;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mProgressDlg.showProgressDialog(mContext, msgInfo);
            }
        });
    }

    public void hideProgressInMain(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mProgressDlg.dismiss();
            }
        });
    }
}
