package io.rong.recognizer.speechtotext;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import io.rong.recognizer.IflytekSpeech;

/**
 * speech to text utility 语音转文字的工具类
 */
public class SpeechToTextUtility {
    private SpeechRecognizer recognizer;
    private AsyncTask<String, Void, byte[]> task;

    /**
     * 构造工具类
     * @param context 注意资源释放（destroy 方法）时机，避免内存泄露
     */
    public SpeechToTextUtility(@NonNull Context context) {
        this(context, null);
    }

    /**
     * 构造工具类
     * @param context 注意资源释放（destroy 方法）时机，避免内存泄露
     * @param appID 科大讯飞平台上应用标识
     */
    public SpeechToTextUtility(@NonNull Context context, @Nullable String appID) {
        IflytekSpeech.initSDK(context, appID);

        recognizer = SpeechRecognizer.createRecognizer(context, new InitListener() {
            @Override
            public void onInit(int code) {

            }
        });
    }

    /**
     * 语音识别为文字
     * @param amrPath 语音文件，amr 格式
     * @param recognizeResult 识别结果回传/监听，可能会分段多次回传
     */
    public void recognize(@NonNull String amrPath,
                          @NonNull final RecognizeResult recognizeResult) {
        if (recognizer == null) {
            recognizeResult.onResult(
                    new RecognizeResult.ResultData(true, "recognizer init failed"));
            return;
        }

        task = new DecodeAmrTask(new DecodeCallback() {
            @Override
            public void onCallback(@Nullable byte[] resultData) {
                if (resultData == null) {
                    recognizeResult.onResult(
                            new RecognizeResult.ResultData(true, "decode amr failed"));
                    return;
                }
                
                startRecognize(resultData, recognizeResult);
            }
        }).execute(amrPath);
    }

    private void startRecognize(@NonNull byte[] audioData, final RecognizeResult recognizeResult) {
        // 清空之前设置的参数
        recognizer.setParameter(SpeechConstant.PARAMS, null);
        // 设置采样率为8k
        recognizer.setParameter(SpeechConstant.SAMPLE_RATE, "8000");
        // 设置音频来源为外部文件
        recognizer.setParameter(SpeechConstant.AUDIO_SOURCE, "-1");

        RecognizerListener recognizerListener = new RecognizerListener() {
            @Override
            public void onVolumeChanged(int i, byte[] bytes) {
                
            }

            @Override
            public void onBeginOfSpeech() {

            }

            @Override
            public void onEndOfSpeech() {

            }

            @Override
            public void onResult(RecognizerResult results, boolean isLast) {
                String data = IflytekSpeech.parseRecognizeResult(results);

                RecognizeResult.ResultData resultData;

                // 如果 data 为空字符串，作为"错误"处理
                if ("".equals(data)) {
                    resultData = new RecognizeResult.ResultData(true, data, isLast);
                } else {
                    resultData = new RecognizeResult.ResultData(false, data, isLast);
                }

                recognizeResult.onResult(resultData);
            }

            @Override
            public void onError(SpeechError speechError) {
                recognizeResult.onResult(
                        new RecognizeResult.ResultData(true, "recognize error"));
            }

            @Override
            public void onEvent(int i, int i1, int i2, Bundle bundle) {

            }
        };
        int ret = recognizer.startListening(recognizerListener);
        if (ret == ErrorCode.SUCCESS) {
            recognizer.writeAudio(audioData, 0, audioData.length);
            recognizer.stopListening();
        } else {
            recognizeResult.onResult(
                    new RecognizeResult.ResultData(true, "startListening failed"));
        }
    }

    /**
     * 取消解码及识别，并释放相应资源，有可能造成识别结果无返回
     */
    public void destroy() {
        if (task != null) {
            task.cancel(true);
        }
        if (recognizer != null) {
            recognizer.cancel();    // 在会话被取消后，当前会话结束，未返回的结果将不再返回。
            recognizer.destroy();
        }
    }
}
