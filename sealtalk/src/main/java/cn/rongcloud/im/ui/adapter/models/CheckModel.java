package cn.rongcloud.im.ui.adapter.models;

public class CheckModel<T> extends ContactModel<T> {

    public CheckModel(T bean, int type) {
        super(bean, type);
    }

    private CheckType checkType = CheckType.NONE;

    public CheckType getCheckType() {
        return checkType;
    }

    public void setCheckType(CheckType checkType) {
        this.checkType = checkType;
    }
}
