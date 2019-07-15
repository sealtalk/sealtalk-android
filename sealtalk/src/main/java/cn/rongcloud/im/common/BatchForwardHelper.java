package cn.rongcloud.im.common;


import java.util.Queue;
import java.util.concurrent.LinkedBlockingDeque;

import io.rong.imkit.RongIM;
import io.rong.imlib.IRongCallback;
import io.rong.imlib.model.Message;

/**
 * 批量发送消息工具类，将消息排序延迟 300ms 发送
 */
public class BatchForwardHelper {
    private static final String TAG = "BatchForwardHelper";
    private static BatchForwardHelper instance;
    private Queue<MessageWrapper> messagelist = new LinkedBlockingDeque<>();
    private Object object = new Object();

    static {
        instance = new BatchForwardHelper();
    }

    public static BatchForwardHelper getInstance() {
        return instance;
    }

    private BatchForwardHelper() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (true) {

                        if (messagelist.isEmpty()) {
                            synchronized (object) {
                                object.wait();
                            }
                        }
                        MessageWrapper wapper = messagelist.poll();
                        RongIM.getInstance().sendMessage(wapper.getMessage(), null, null, wapper.getCallback());
                        Thread.sleep(300);//这里需要延迟 300ms 来发送，防止消息阻塞
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void batchSendMessage(Message message, IRongCallback.ISendMediaMessageCallback callback) {
        messagelist.offer(new MessageWrapper(message, callback));
        synchronized (object) {
            object.notify();
        }
    }

    private class MessageWrapper {
        private Message message;
        private IRongCallback.ISendMediaMessageCallback callback;

        public MessageWrapper(Message message, IRongCallback.ISendMediaMessageCallback callback) {
            this.message = message;
            this.callback = callback;
        }

        public Message getMessage() {
            return message;
        }

        public IRongCallback.ISendMediaMessageCallback getCallback() {
            return callback;
        }
    }

}
