package io.rong.recognizer.speechtotext;

import androidx.annotation.Nullable;

/**
 * 解码处理的结果回调
 */
public interface DecodeCallback {
    /**
     * @param resultData 二进制结果数据
     */
    void onCallback(@Nullable byte[] resultData);
}
