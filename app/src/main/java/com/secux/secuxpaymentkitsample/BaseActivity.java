package com.secux.secuxpaymentkitsample;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.provider.Settings;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;

public class BaseActivity  extends AppCompatActivity implements EasyPermissions.PermissionCallbacks{

    protected Context mContext = this;

    private CommonProgressDialog mProgressDlg = new CommonProgressDialog();

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> list) {
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> list) {
        showAlert("No location permission!", "Bluetooth error! Operation abort.");
    }


    protected boolean checkBLESetting(){

        LocationManager lm = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
        }

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
        }

        if (!gps_enabled && !network_enabled) {

            new AlertDialog.Builder(mContext)
                    .setMessage("APP needs to use phone's bluetooth. PLease turn on the location setting!")
                    .setPositiveButton("Setting", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                            mContext.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        }
                    }).show();

            return false;
        }

        String[]  permsLocation = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
        if (EasyPermissions.hasPermissions(this, permsLocation )) {


        } else {

            EasyPermissions.requestPermissions(this,"APP needs to use phone's bluetooth. PLease authorize the location permission!", 1, permsLocation);
            return false;
        }


        return true;
    }


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

    public void hideProgress(){
        mProgressDlg.dismiss();
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
