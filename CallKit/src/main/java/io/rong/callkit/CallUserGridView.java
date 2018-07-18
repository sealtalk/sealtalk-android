package io.rong.callkit;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import io.rong.callkit.util.ICallScrollView;
import io.rong.imkit.widget.AsyncImageView;
import io.rong.imlib.model.Group;
import io.rong.imlib.model.UserInfo;

/**
 * Created by weiqinxiao on 16/3/25.
 * coming 横向显示
 * 多人语音_被叫
 */
public class CallUserGridView extends HorizontalScrollView implements ICallScrollView {
    private Context context;
    private boolean enableTitle;
    private LinearLayout linearLayout;

    private static int CHILDREN_PER_LINE = 5;
    private final static int CHILDREN_SPACE = 13;

    private int portraitSize;
    private boolean isHorizontal=true;

    public CallUserGridView(Context context) {
        super(context);
        init(context);
    }

    public CallUserGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.CallUserGridView);
        isHorizontal=a.getBoolean(R.styleable.CallUserGridView_CallGridViewOrientation,true);
        CHILDREN_PER_LINE=a.getInteger(R.styleable.CallUserGridView_CallGridViewChildrenPerLine,4);
        init(context);
        a.recycle();
    }

    private void init(Context context) {
        this.context = context;
        linearLayout = new LinearLayout(context);
        linearLayout.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
//        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        addView(linearLayout);
    }

    public int dip2pix(int dipValue) {
        float scale = getResources().getDisplayMetrics().density;
        return (int)(dipValue * scale + 0.5f);
    }

    public int getScreenWidth() {
        return getResources().getDisplayMetrics().widthPixels;
    }

    public void setChildPortraitSize(int size) {
        portraitSize = size;
    }

    public void enableShowState(boolean enable) {
        enableTitle = enable;
    }

    public void addChild(String childId, UserInfo userInfo) {
        addChild(childId, userInfo, null);
    }

    public void addChild(String childId, UserInfo userInfo, String state) {
        int containerCount = linearLayout.getChildCount();
        LinearLayout lastContainer = null;
        int i;
        for (i = 0; i < containerCount; i++) {
            LinearLayout container = (LinearLayout)linearLayout.getChildAt(i);
            if (container.getChildCount() < CHILDREN_PER_LINE) {
                lastContainer = container;
                break;
            }
        }
        if (lastContainer == null) {
            lastContainer = new LinearLayout(context);
            lastContainer.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            lastContainer.setGravity(Gravity.CENTER_HORIZONTAL);
            lastContainer.setPadding(0, dip2pix(CHILDREN_SPACE), 0, 0);
            linearLayout.addView(lastContainer);
        }

        LinearLayout child = (LinearLayout)LayoutInflater.from(context).inflate(R.layout.rc_voip_user_info, null);
        child.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        if(containerCount==0){
            child.setPadding(dip2pix(15), 0, dip2pix(CHILDREN_SPACE), 0);
        }else{
            child.setPadding(0, 0, dip2pix(CHILDREN_SPACE), 0);
        }
        child.setTag(childId);
        if (portraitSize > 0) {
            child.findViewById(R.id.rc_user_portrait_layout).setLayoutParams(new LinearLayout.LayoutParams(portraitSize, portraitSize));
        }
        AsyncImageView imageView = (AsyncImageView)child.findViewById(R.id.rc_user_portrait);
        TextView name = (TextView)child.findViewById(R.id.rc_user_name);
        name.setVisibility(enableTitle ? VISIBLE : GONE);
        TextView stateV = (TextView)child.findViewById(R.id.rc_voip_member_state);
        stateV.setVisibility(enableTitle ? VISIBLE : GONE);
        if (state != null) {
            stateV.setText(state);
        } else {
            stateV.setVisibility(GONE);
        }

        if (userInfo != null) {
            imageView.setAvatar(userInfo.getPortraitUri());
            name.setText(userInfo.getName() == null ? userInfo.getUserId() : userInfo.getName());
        } else {
            name.setText(childId);
        }
        lastContainer.addView(child);
    }


    @Override
    public void setScrollViewOverScrollMode(int mode) {
        this.setOverScrollMode(mode);
    }

    public void removeChild(String childId) {
        int containerCount = linearLayout.getChildCount();

        LinearLayout lastContainer = null;
        List<LinearLayout> containerList = new ArrayList<>();
        for (int i = 0; i < containerCount; i++) {
            LinearLayout container = (LinearLayout) linearLayout.getChildAt(i);
            containerList.add(container);
        }
        for (LinearLayout resultContainer : containerList) {
            if (lastContainer == null) {
                LinearLayout child = (LinearLayout) resultContainer.findViewWithTag(childId);
                if (child != null) {
                    resultContainer.removeView(child);
                    if (resultContainer.getChildCount() == 0) {
                        linearLayout.removeView(resultContainer);
                        break;
                    } else {
                        lastContainer = resultContainer;
                    }
                }
            } else {
                View view = resultContainer.getChildAt(0);
                resultContainer.removeView(view);
                lastContainer.addView(view);
                if (resultContainer.getChildCount() == 0) {
                    linearLayout.removeView(resultContainer);
                    break;
                } else {
                    lastContainer = resultContainer;
                }
            }
        }
    }

    public View findChildById(String childId) {
        int containerCount = linearLayout.getChildCount();

        for (int i = 0; i < containerCount; i++) {
            LinearLayout container = (LinearLayout) linearLayout.getChildAt(i);
            LinearLayout child = (LinearLayout) container.findViewWithTag(childId);
            if (child != null) {
                return child;
            }
        }
        return null;
    }

    public void updateChildInfo(String childId, UserInfo userInfo) {
        int containerCount = linearLayout.getChildCount();

        LinearLayout lastContainer = null;
        for (int i = 0; i < containerCount; i++) {
            LinearLayout container = (LinearLayout) linearLayout.getChildAt(i);
            LinearLayout child = (LinearLayout) container.findViewWithTag(childId);
            if (child != null) {
                AsyncImageView imageView = (AsyncImageView)child.findViewById(R.id.rc_user_portrait);
                imageView.setAvatar(userInfo.getPortraitUri());
                if (enableTitle) {
                    TextView textView = (TextView)child.findViewById(R.id.rc_user_name);
                    textView.setText(userInfo.getName());
                }
            }
        }
    }

    public void updateChildState(String childId, String state) {
        int containerCount = linearLayout.getChildCount();

        for (int i = 0; i < containerCount; i++) {
            LinearLayout container = (LinearLayout) linearLayout.getChildAt(i);
            LinearLayout child = (LinearLayout) container.findViewWithTag(childId);
            if (child != null) {
                TextView textView = (TextView)child.findViewById(R.id.rc_voip_member_state);
                textView.setText(state);
            }
        }
    }

    public void updateChildState(String childId, boolean visible) {
        int containerCount = linearLayout.getChildCount();

        for (int i = 0; i < containerCount; i++) {
            LinearLayout container = (LinearLayout) linearLayout.getChildAt(i);
            LinearLayout child = (LinearLayout) container.findViewWithTag(childId);
            if (child != null) {
                TextView textView = (TextView)child.findViewById(R.id.rc_voip_member_state);
                textView.setVisibility(visible ? VISIBLE : GONE);
            }
        }
    }
}
