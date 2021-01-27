package cn.rongcloud.im.ui.adapter.models;

public class ContactModel<T> {
    int type;
    T bean;

    public ContactModel(T bean, int type) {
        super();
        this.bean = bean;
        this.type = type;
    }

    public T getBean() {
        return bean;
    }

    public void setBean(T bean) {
        this.bean = bean;
    }

    public int getType() {
        return type;
    }
}
