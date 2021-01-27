package cn.rongcloud.im.common;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ThreadManager {
    private static final String TAG = "ThreadManager";

    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int CORE_POOL_SIZE = Math.max(2, Math.min(CPU_COUNT - 1, 4));

    private static volatile ThreadManager sInstance;

    private static Executor mWorkThreadExecutor = Executors.newFixedThreadPool(CORE_POOL_SIZE);
    private static Handler mMainThreadHandler;


    private ThreadManager() {
        mMainThreadHandler = new Handler(Looper.getMainLooper());
    }

    public void init(Context context) {

    }

    public static ThreadManager getInstance() {
        if (sInstance == null) {
            synchronized (ThreadManager.class) {
                if (sInstance == null) {
                    sInstance = new ThreadManager();
                }
            }
        }
        return sInstance;
    }

    public void runOnWorkThread(Runnable runnable) {
        mWorkThreadExecutor.execute(runnable);
    }

    public void runOnUIThread(Runnable runnable) {
        mMainThreadHandler.post(runnable);
    }

    public boolean isInMainThread(){
        return Looper.getMainLooper().getThread() == Thread.currentThread();
    }

}
