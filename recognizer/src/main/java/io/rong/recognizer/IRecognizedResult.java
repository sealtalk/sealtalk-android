package io.rong.recognizer;

/**
 * Created by zwfang on 16/11/9.
 */

public interface IRecognizedResult {
    void onResult(String data);
    void onClearClick();
}
