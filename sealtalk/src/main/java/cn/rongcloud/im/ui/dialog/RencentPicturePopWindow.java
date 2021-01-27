package cn.rongcloud.im.ui.dialog;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupWindow;

import cn.rongcloud.im.R;
import cn.rongcloud.im.common.IntentExtra;
import cn.rongcloud.im.ui.activity.ImagePreviewActivity;

public class RencentPicturePopWindow extends PopupWindow implements View.OnClickListener {

    private View contentView;
    private ImageView ivPicture;
    private Activity activity;
    public static final int REQUEST_PICTURE = 0x8901;
    private String mUri;

    @SuppressLint("InflateParams")
    public RencentPicturePopWindow(final Activity context) {
        activity = context;
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        contentView = inflater.inflate(R.layout.recent_picture_popup, null);
        contentView.setOnClickListener(this);
        ivPicture = contentView.findViewById(R.id.iv_picture);
        this.setContentView(contentView);
        this.setWidth(dp2px(100));
        this.setHeight(dp2px(143));
        this.setFocusable(true);
        this.setOutsideTouchable(true);
        this.update();
        ColorDrawable dw = new ColorDrawable(0000000000);
        this.setBackgroundDrawable(dw);
        this.setAnimationStyle(R.style.AnimationMainTitleMore);
    }
    public void setIvPicture(String uri){
        mUri = uri;
        ivPicture.setScaleType(ImageView.ScaleType.CENTER_CROP);
        ivPicture.setImageURI(Uri.parse(uri));
    }
    /**
     * 显示popupWindow
     *
     */
    public void showPopupWindow(int h) {
        showAtLocation(contentView, Gravity.BOTTOM|Gravity.RIGHT,dp2px(8),h+dp2px(4));
    }

    private int dp2px(int dp) {
        float density = activity.getResources().getDisplayMetrics().density;
        return (int) (dp * density + 0.5f);
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(activity, ImagePreviewActivity.class);
        intent.putExtra(IntentExtra.URL, mUri);
        intent.putExtra(IntentExtra.IMAGE_PREVIEW_TYPE, ImagePreviewActivity.FROM_RECENT_PICTURE);
        activity.startActivityForResult(intent, REQUEST_PICTURE);
        dismiss();
    }
}
