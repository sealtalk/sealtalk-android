package cn.rongcloud.im.ui.adapter.models;

public class FunctionInfo {
    private String id;
    private String name;
    private int drawableRes;
    private boolean isShowDot;
    private int dotNumber;
    private boolean isShowArrow = true;

    public FunctionInfo(String id, String name, int drawableRes) {
        this.id = id;
        this.name = name;
        this.drawableRes = drawableRes;
    }

    public FunctionInfo(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getDrawableRes() {
        return drawableRes;
    }

    public void setDrawableRes(int drawableRes) {
        this.drawableRes = drawableRes;
    }

    public boolean isShowDot() {
        return isShowDot;
    }

    public void setShowDot(boolean showDot) {
        isShowDot = showDot;
    }

    public boolean isShowArrow() {
        return isShowArrow;
    }

    public void setShowArrow(boolean showArrow) {
        isShowArrow = showArrow;
    }

    public int getDotNumber() {
        return dotNumber;
    }

    public void setDotNumber(int dotNumber) {
        this.dotNumber = dotNumber;
    }
}
