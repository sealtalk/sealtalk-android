package io.rong.recognizer;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechUtility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.Locale;
import java.util.Random;

import io.rong.common.RLog;

public class Recognizer extends RelativeLayout implements RecognizerListener {
    private final static String TAG = "Recognizer";

    private ImageView imgMic;
    private RelativeLayout rlBottom;

    private Random random;
    private IRecognizedResult resultCallBack;
    private SpeechRecognizer mIat = null;
    private AnimationDrawable animStart;
    private AnimationDrawable animEnd;
    private static String mAppId;

    /**
     * 开发者可以通过此接口设置自己从科大讯飞官网申请的 appId。
     * 此方法可以在 SDK init 之后调用。
     *
     * 注意： appid 必须和下载的SDK保持一致，否则会出现10407错误
     * @param appId 自定义的 appId
     */
    public static void setAppId(String appId) {
        mAppId = appId;
    }

    public Recognizer(Context context) {
        super(context);
        /**
         * 语音输入模块集成的是讯分SDK,开发者要集成需要去讯飞开放平台获取自身的APPID替换下面的id.
         */
        if (SpeechUtility.getUtility() == null) {
            SpeechUtility.createUtility(context.getApplicationContext(), SpeechConstant.APPID + "=" + (mAppId == null ? "5a430817" : mAppId));
        }
        initViews();
    }

    public Recognizer(Context context, AttributeSet attrs) {
        super(context, attrs);
        initViews();
    }

    private void initViews() {
        setClickable(true);
        setBackgroundColor(getResources().getColor(R.color.rc_recognizerview_bg_normal));
        RelativeLayout recognizerContainer = (RelativeLayout) LayoutInflater.from(getContext()).inflate(R.layout.rc_view_recognizer, null);
        View rlMic = recognizerContainer.findViewById(R.id.rl_mic);
        rlMic.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mIat == null || !mIat.isListening()) {
                    startRecognize();
                } else {
                    reset();
                }
            }
        });
        imgMic = (ImageView) recognizerContainer.findViewById(R.id.img_mic);

        TextView tvClear = (TextView) recognizerContainer.findViewById(R.id.btn_clear);
        tvClear.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != resultCallBack) {
                    resultCallBack.onClearClick();
                }
            }
        });
        rlBottom = (RelativeLayout) recognizerContainer.findViewById(R.id.rl_bottom);
        addView(recognizerContainer);
        random = new Random();
    }

    /**
     * 初始化监听器。
     */
    private static InitListener mInitListener = new InitListener() {

        @Override
        public void onInit(int code) {
            RLog.i(TAG, "onInit " + code);
        }
    };

    public void startRecognize() {
        if (null == mIat) {
            mIat = SpeechRecognizer.createRecognizer(getContext(), mInitListener);
        }
        if (mIat.isListening()) {
            return;
        }
        setParam();
        int ret = mIat.startListening(this);
        if (ret != ErrorCode.SUCCESS) {
            RLog.d(TAG, "startRecognize ret error " + ret);
        }
    }

    /**
     * 参数设置,设置听写参数，详见《科大讯飞MSC API手册(Android)》SpeechConstant类
     *
     * @param
     * @return
     */
    private void setParam() {
        // 清空参数
        mIat.setParameter(SpeechConstant.PARAMS, null);

        // 设置听写引擎
        mIat.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
        // 设置返回结果格式
        mIat.setParameter(SpeechConstant.RESULT_TYPE, "json");


        mIat.setParameter(SpeechConstant.DOMAIN, "iat");
        if ("zh".equals(Locale.getDefault().getLanguage().toLowerCase())) {
            mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
            mIat.setParameter(SpeechConstant.ACCENT, "mandarin ");
        } else {
            mIat.setParameter(SpeechConstant.LANGUAGE, "en_us");
        }
        // 设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理
        mIat.setParameter(SpeechConstant.VAD_BOS, "4000");
        // 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音
        mIat.setParameter(SpeechConstant.VAD_EOS, "1000");
        // 设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点
        mIat.setParameter(SpeechConstant.ASR_PTT, "1");

        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        // 注：AUDIO_FORMAT参数语记需要更新版本才能生效
        mIat.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
        mIat.setParameter(SpeechConstant.ASR_AUDIO_PATH, Environment.getExternalStorageDirectory() + "/msc/iat.wav");
    }

    private void setRandomImageResource() {
        int num = random.nextInt(3) + 1;
        switch (num) {
            case 1:
                imgMic.setImageResource(R.drawable.rc_recognize_volume_01);
                break;
            case 2:
                imgMic.setImageResource(R.drawable.rc_recognize_volume_02);
                break;
            default:
                imgMic.setImageResource(R.drawable.rc_recognize_volume_03);
                break;
        }
    }

    private void changeVolume(int volume) {
        if (null != imgMic) {
            switch (volume / 2) {
                case 0:
                    setRandomImageResource();
                    break;
                case 1:
                    imgMic.setImageResource(R.drawable.rc_recognize_volume_02);
                    break;
                case 2:
                    imgMic.setImageResource(R.drawable.rc_recognize_volume_03);
                    break;
                case 3:
                    imgMic.setImageResource(R.drawable.rc_recognize_volume_04);
                    break;
                case 4:
                    imgMic.setImageResource(R.drawable.rc_recognize_volume_05);
                    break;
                case 5:
                    imgMic.setImageResource(R.drawable.rc_recognize_volume_06);
                    break;
                case 6:
                    imgMic.setImageResource(R.drawable.rc_recognize_volume_07);
                    break;
                case 7:
                    imgMic.setImageResource(R.drawable.rc_recognize_volume_08);
                    break;
                case 8:
                    imgMic.setImageResource(R.drawable.rc_recognize_volume_09);
                    break;
                case 9:
                    imgMic.setImageResource(R.drawable.rc_recognize_volume_10);
                    break;
                case 10:
                    imgMic.setImageResource(R.drawable.rc_recognize_volume_11);
                    break;
                case 11:
                    imgMic.setImageResource(R.drawable.rc_recognize_volume_12);
                    break;
                case 12:
                    imgMic.setImageResource(R.drawable.rc_recognize_volume_13);
                    break;
                default:
                    imgMic.setImageResource(R.drawable.rc_recognize_volume_14);
                    break;
            }
        }
    }


    private void endOfSpeech() {
        if (null == imgMic) return;
        imgMic.setImageResource(R.drawable.rc_anim_speech_end);
        animEnd = (AnimationDrawable) imgMic.getDrawable();
        imgMic.clearAnimation();
        animEnd.start();
    }


    private void beginOfSpeech() {
        if (null == imgMic) return;
        imgMic.setImageResource(R.drawable.rc_anim_speech_start);
        animStart = (AnimationDrawable) imgMic.getDrawable();
        imgMic.clearAnimation();
        animStart.start();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (null != mIat) {
            mIat.cancel();
            mIat.destroy();
            mIat = null;
        }
        if (null != resultCallBack) {
            resultCallBack = null;
        }
        if (animEnd != null) {
            animEnd.stop();
            animEnd = null;
        }
        if (animStart != null) {
            animStart.stop();
            animEnd = null;
        }
        if (SpeechUtility.getUtility() != null) {
            SpeechUtility.getUtility().destroy();
        }
        mInitListener = null;
    }

    private String parseIatResult(String json) {
        {
            StringBuffer ret = new StringBuffer();
            JSONTokener jsonTokener = new JSONTokener(json);
            try {
                JSONObject jsonObject = new JSONObject(jsonTokener);
                JSONArray jsonArray = jsonObject.getJSONArray("ws");
                for (int i = 0; i < jsonArray.length(); ++i) {
                    // 转写结果词，默认使用第一个结果
                    JSONArray items = jsonArray.getJSONObject(i).getJSONArray("cw");
                    JSONObject obj = items.getJSONObject(0);
//				如果需要多候选结果，解析数组其他字段0
//				for(int j = 0; j < items.length(); j++)
//				{
//					JSONObject obj = items.getJSONObject(j);
//					ret.append(obj.getString("w"));
//				}
                    ret.append(obj.getString("w"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return ret.toString();
        }

    }

    private void printResult(RecognizerResult result) {
        String json = result.getResultString();
        String text = parseIatResult(json);
        try {
            JSONObject obj = new JSONObject(json);
            boolean isLast = obj.getBoolean("ls");
            if (isLast) {
                endOfSpeech();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (!TextUtils.isEmpty(text)) {
            rlBottom.setVisibility(View.VISIBLE);
        }
        if (resultCallBack != null) {
            resultCallBack.onResult(text);
        }
    }

    @Override
    public void onResult(RecognizerResult recognizerResult, boolean b) {
        printResult(recognizerResult);
        if (imgMic != null) {
            imgMic.setImageResource(R.drawable.rc_recognize_disable);
        }
    }

    @Override
    public void onError(SpeechError speechError) {
        if (speechError.getErrorCode() == ErrorCode.ERROR_NO_NETWORK) {
            Toast.makeText(getContext(), getContext().getString(R.string.rc_plugin_recognize_check_network), Toast.LENGTH_SHORT).show();
        }
        if (imgMic != null) {
            imgMic.setImageResource(R.drawable.rc_recognize_disable);
        }
    }

    @Override
    public void onEvent(int eventType, int i1, int i2, Bundle bundle) {
        RLog.d(TAG, "RecognizerView onEvent eventType: " + eventType);
    }

    @Override
    public void onVolumeChanged(int volume, byte[] bytes) {
        changeVolume(volume);
    }

    @Override
    public void onBeginOfSpeech() {
        RLog.d(TAG, "RecognizerView onBeginOfSpeech");
        beginOfSpeech();
    }

    @Override
    public void onEndOfSpeech() {
        RLog.d(TAG, "RecognizerView onEndOfSpeech");
        endOfSpeech();
    }

    public void setResultCallBack(IRecognizedResult resultCallBack) {
        this.resultCallBack = resultCallBack;
    }

    private void reset() {
        if (null != mIat) {
            mIat.cancel();
            mIat.destroy();
            mIat = null;
        }
        if (null != animEnd) {
            animEnd.stop();
            animEnd = null;
        }
        if (animStart != null) {
            animStart.stop();
            animStart = null;
        }
        if (imgMic != null) {
            imgMic.setImageResource(R.drawable.rc_recognize_disable);
        }
    }
}
