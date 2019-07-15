package io.rong.recognizer.speechtotext;

import androidx.annotation.NonNull;

/**
 * 语音转文字的识别结果回传/监听
 */
public interface RecognizeResult {
    class ResultData {
        private boolean isError;
        private String data;
        private boolean isLast;

        public ResultData(boolean isError, String data) {
            this.isError = isError;
            this.data = data;
        }

        public ResultData(boolean isError, String data, boolean isLast) {
            this(isError, data);
            this.isLast = isLast;
        }

        public boolean isError() {
            return isError;
        }

        public String getData() {
            return data;
        }

        public boolean isLast() {
            return isLast;
        }
    }
    void onResult(@NonNull ResultData resultData);
}
