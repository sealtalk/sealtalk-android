package io.rong.callkit;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.bailingcloud.bailingvideo.engine.binstack.util.FinLog;
import com.bailingcloud.bailingvideo.engine.view.BlinkVideoView;

/**
 * Created by Administrator on 2017/3/30.
 */

public class ContainerLayout extends RelativeLayout {
    private Context context;
    private static boolean isNeedFillScrren = true;
    SurfaceView currentView;
    public ContainerLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    public void addView(final SurfaceView videoView) {
        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        this.screenWidth = wm.getDefaultDisplay().getWidth();
        ;
        this.screenHeight = wm.getDefaultDisplay().getHeight();
        ;
        FinLog.d("---xx-- add view " + videoView.toString() + " Height: " + ((BlinkVideoView) videoView).rotatedFrameHeight + " Width: " + ((BlinkVideoView) videoView).rotatedFrameWidth);
        super.addView(videoView, getBigContainerParams((BlinkVideoView) videoView));
        currentView = videoView;
        ((BlinkVideoView) videoView).setOnSizeChangedListener(new BlinkVideoView.OnSizeChangedListener() {
            @Override
            public void onChanged(BlinkVideoView.Size size) {
                try {
                    ContainerLayout.this.removeAllViews();
                    FinLog.d("---xx-- change view " + videoView.toString() + " Height: " + ((BlinkVideoView) videoView).rotatedFrameHeight + " Width: " + ((BlinkVideoView) videoView).rotatedFrameWidth);
                    ContainerLayout.this.addView(videoView, getBigContainerParams((BlinkVideoView) videoView));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }


    @NonNull
    private LayoutParams getBigContainerParams(BlinkVideoView videoView) {
        LayoutParams layoutParams = null;
        if (!isNeedFillScrren) {
            if (screenHeight > screenWidth) { //V
                int layoutParamsHeight = (videoView.rotatedFrameHeight == 0 || videoView.rotatedFrameWidth == 0) ? ViewGroup.LayoutParams.WRAP_CONTENT : screenWidth * videoView.rotatedFrameHeight / videoView.rotatedFrameWidth;
                layoutParams = new LayoutParams(screenWidth, layoutParamsHeight);
            } else {
                int layoutParamsWidth = (videoView.rotatedFrameHeight == 0 || videoView.rotatedFrameHeight == 0) ? ViewGroup.LayoutParams.WRAP_CONTENT : (screenWidth * videoView.rotatedFrameWidth / videoView.rotatedFrameHeight > screenWidth ? screenWidth : screenHeight * videoView.rotatedFrameWidth / videoView.rotatedFrameHeight);
                layoutParams = new LayoutParams(layoutParamsWidth, screenHeight);
            }
        } else {
            layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        }
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        return layoutParams;
    }

    public void setIsNeedFillScrren(boolean isNeed) {
        isNeedFillScrren = isNeed;
    }

    @Override
    public void removeAllViews() {
        if (currentView != null)
            ((BlinkVideoView) currentView).setOnSizeChangedListener(null);
        super.removeAllViews();
    }

    private int screenWidth;
    private int screenHeight;

}
