package io.rong.callkit.util;

import android.view.View;

import io.rong.imlib.model.UserInfo;

/**
 * Created by dengxudong on 2018/5/18.
 */

public interface ICallScrollView {
    void setScrollViewOverScrollMode(int mode);
    void removeChild(String childId);
    View findChildById(String childId);
    void updateChildState(String childId, boolean visible);
    void updateChildState(String childId, String state);
    void setChildPortraitSize(int size);
    void enableShowState(boolean enable);
    void addChild(String childId, UserInfo userInfo);
    void addChild(String childId, UserInfo userInfo, String state);
    void updateChildInfo(String childId, UserInfo userInfo);
    int dip2pix(int dipValue);
}
