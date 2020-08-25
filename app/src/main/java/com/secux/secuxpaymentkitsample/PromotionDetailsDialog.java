package com.secux.secuxpaymentkitsample;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.secuxtech.paymentkit.SecuXStoreInfo;


public class PromotionDetailsDialog {

    private AlertDialog mAlertDialog;


    public void showDialog(Context context, SecuXStoreInfo storeInfo, SecuXStoreInfo.SecuXPromotion promotion) {

        mAlertDialog = new AlertDialog.Builder(context).create();
        View loadView = LayoutInflater.from(context).inflate(R.layout.dialog_promotion_detials, null);

        mAlertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mAlertDialog.setView(loadView, 0, 0, 0, 0);
        mAlertDialog.setCanceledOnTouchOutside(false);

        TextView tvType = loadView.findViewById(R.id.textview_promotion_type);
        tvType.setText(promotion.mType);

        TextView tvStoreName = loadView.findViewById(R.id.textview_promotion_store_name);
        tvStoreName.setText(storeInfo.mName);

        ImageView ivStoreImg = loadView.findViewById(R.id.imageView_storelogo);
        ivStoreImg.setImageBitmap(storeInfo.mLogo);

        TextView tvPromotionName = loadView.findViewById(R.id.textview_promotion_name);
        tvPromotionName.setText(promotion.mName);

        TextView tvPromotionDesc = loadView.findViewById(R.id.textview_promotion_desc);
        tvPromotionDesc.setText(promotion.mDesc);

        ImageView ivPromotionImg = loadView.findViewById(R.id.imageView_promotion_img);
        ivPromotionImg.setImageBitmap(promotion.mImg);

        mAlertDialog.show();
    }


    public void dismiss() {
        if (mAlertDialog != null && mAlertDialog.isShowing()) {
            mAlertDialog.dismiss();
            mAlertDialog = null;
        }
    }
}
