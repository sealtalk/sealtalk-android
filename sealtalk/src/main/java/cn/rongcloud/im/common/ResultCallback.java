package cn.rongcloud.im.common;

/**
 * 执行 Task 的回调
 * @param <Result> 请求成功时的结果类
 */
public interface ResultCallback<Result> {
    void onSuccess(Result result);

    void onFail(int errorCode);
}
