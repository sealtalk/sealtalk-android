package io.rong.callkit.util;

public interface CallKitSearchBarListener {
    /**
     * 开始搜索
     * EditText 中输入内容后，会触发此回调
     * @param keyword 搜索关键字
     */
    void onSearchStart(String keyword);

    /**
     * 软键盘中"搜索"被点击后，触发此回调
     * 此回调被触发后，仅收起软键盘
     */
    void onSoftSearchKeyClick();

    /**
     * 搜索控件中，点击"清除"后，触发此回调
     */
    void onClearButtonClick();
}
