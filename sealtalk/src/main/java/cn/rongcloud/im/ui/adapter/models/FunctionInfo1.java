package cn.rongcloud.im.ui.adapter.models;

public class FunctionInfo1 {
    private int nameResId;
    private int drawableRes;
    private boolean isShowDot;
    public FunctionInfo1(int name, int drawableRes) {
        this.nameResId = name;
        this.drawableRes = drawableRes;
    }

    public int getNameResId() {
        return nameResId;
    }

    public int getDrawableRes() {
        return drawableRes;
    }

    public boolean isShowDot() {
        return isShowDot;
    }

    public void setShowDot(boolean showDot) {
        isShowDot = showDot;
    }
}
