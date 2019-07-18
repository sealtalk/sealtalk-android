package cn.rongcloud.im.utils;

import androidx.arch.core.util.Function;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;


/**
 * 设置并监听单一数据源，并做数据转换时使用 LiveData
 * 方便于当需要切换数据源时自动取消掉前一个数据源的监听
 *
 * @param <F> 数据源类型
 * @param <R> 转换的结果类型
 */
public class SingleSourceMapLiveData<F, R> extends MutableLiveData<R> {
    private LiveData<F> lastSource;
    private F lastData;
    private R lastResult;
    private Function<F, R> lastMapFunction;

    /**
     * 创建对象时传入需要转换数据的方法
     *
     * @param mapFunction 将数据源的F类型转为结果数据类型R
     */
    public SingleSourceMapLiveData(final Function<F, R> mapFunction) {
        lastMapFunction = mapFunction;
    }

    private final Observer<F> observer = new Observer<F>() {
        @Override
        public void onChanged(F t) {
            if (t != null && t == lastData) {
                return;
            }

            lastData = t;
            R mapResult = lastMapFunction.apply(t);

            lastResult = mapResult;
            setValue(lastResult);
        }
    };

    /**
     * 设置数据源，当有已设置过的数据源时会取消该数据源的监听
     *
     * @param source
     */
    public void setSource(LiveData<F> source) {
        if (lastSource == source) {
            return;
        }

        if (lastSource != null) {
            lastSource.removeObserver(observer);
        }
        lastSource = source;
        if (hasActiveObservers()) {
            lastSource.observeForever(observer);
        }
    }

    @Override
    protected void onActive() {
        super.onActive();
        if (lastSource != null) {
            lastSource.observeForever(observer);
        }
    }

    @Override
    protected void onInactive() {
        super.onInactive();

        if (lastSource != null) {
            lastSource.removeObserver(observer);
        }
    }
}
