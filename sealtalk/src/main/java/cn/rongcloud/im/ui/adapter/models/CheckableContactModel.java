package cn.rongcloud.im.ui.adapter.models;

public class CheckableContactModel<T> extends ContactModel<T>  {
    private CheckType checkType = CheckType.NONE;
    private String id;
    private String firstChar;

    public CheckableContactModel(T bean, int type) {
        super(bean, type);
    }

    public CheckType getCheckType() {
        return checkType;
    }

    public void setCheckType(CheckType checkType) {
        this.checkType = checkType;
    }

    public void setId(String id) {
        this.id = id;
    }
    public String getId() {
        return id;
    }

    public String getFirstChar() {
        return firstChar;
    }

    public void setFirstChar(String firstChar) {
        this.firstChar = firstChar;
    }
}
