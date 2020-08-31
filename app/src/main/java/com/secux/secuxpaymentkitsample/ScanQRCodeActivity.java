package com.secux.secuxpaymentkitsample;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;

import java.util.List;

import cn.bingoogolapple.qrcode.core.QRCodeView;
import cn.bingoogolapple.qrcode.zxing.ZXingView;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class ScanQRCodeActivity extends BaseActivity implements QRCodeView.Delegate,EasyPermissions.PermissionCallbacks  {

    private static final int REQUEST_CODE_QRCODE_PERMISSIONS = 1;
    private ZXingView mZXingView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_qrcode);

        mZXingView = findViewById(R.id.zxingview);
        mZXingView.setDelegate(this);
    }

    @Override
    protected void onStart() {
        super.onStart();

        requestCodeQRCodePermissions();

    }

    @Override
    protected void onStop() {
        mZXingView.stopCamera();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        mZXingView.onDestroy();
        super.onDestroy();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        mZXingView.startCamera();
        mZXingView.startSpotAndShowRect();
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {

    }

    @AfterPermissionGranted(REQUEST_CODE_QRCODE_PERMISSIONS)
    private void requestCodeQRCodePermissions() {
        String[] perms = {Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE};
        if (!EasyPermissions.hasPermissions(this, perms)) {
            EasyPermissions.requestPermissions(this, "APP needs to use camera!", REQUEST_CODE_QRCODE_PERMISSIONS, perms);
        }
    }


    @Override
    public void onScanQRCodeSuccess(String result) {
        //showAlert(result, "");

        Intent intent = new Intent();
        intent.putExtra("qrcode", result);
        this.setResult(RESULT_OK, intent);
        this.finish();

    }

    @Override
    public void onCameraAmbientBrightnessChanged(boolean isDark) {

    }

    @Override
    public void onScanQRCodeOpenCameraError() {

    }
}