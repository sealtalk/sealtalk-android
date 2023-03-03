package cn.rongcloud.im.utils;

import android.os.Binder;

/** 用来Intent传输数据使用，特别是大数据 Created by cjl on 2023/01/12. */
public class IntentDataTransferBinder<T> extends Binder {

    public T data;

    public IntentDataTransferBinder(T data) {
        this.data = data;
    }
}
