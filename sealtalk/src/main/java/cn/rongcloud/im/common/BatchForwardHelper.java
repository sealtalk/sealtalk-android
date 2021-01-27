package cn.rongcloud.im.common;


import android.text.TextUtils;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingDeque;

import io.rong.imkit.IMCenter;
import io.rong.imlib.IRongCallback;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.MessageContent;
import io.rong.message.GIFMessage;
import io.rong.message.ImageMessage;
import io.rong.imlib.location.message.LocationMessage;
import io.rong.message.MediaMessageContent;

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
                        MessageWrapper wrapper = messagelist.poll();
                        if (wrapper == null) continue;

                        Message message = wrapper.getMessage();
                        MessageContent messageContent = message.getContent();
                        if ((messageContent instanceof ImageMessage && ((ImageMessage) messageContent).getRemoteUri() == null)
                                || (messageContent instanceof GIFMessage && ((GIFMessage) messageContent).getRemoteUri() == null)) {
                            IMCenter.getInstance().sendMediaMessage(message, null, null, new IRongCallback.ISendMediaMessageCallback() {
                                @Override
                                public void onProgress(Message message, int i) {
                                    if(wrapper.callback != null) {
                                        wrapper.callback.onProgress(message, i);
                                    }
                                }

                                @Override
                                public void onCanceled(Message message) {

                                }

                                @Override
                                public void onAttached(Message message) {
                                    if(wrapper.callback != null) {
                                        wrapper.callback.onAttached(message);
                                    }
                                }

                                @Override
                                public void onSuccess(Message message) {
                                    if(wrapper.callback != null) {
                                        wrapper.callback.onSuccess(message);
                                    }
                                }

                                @Override
                                public void onError(Message message, RongIMClient.ErrorCode errorCode) {
                                    if(wrapper.callback != null) {
                                        wrapper.callback.onError(message, errorCode);
                                    }
                                }
                            });
                        } else if (messageContent instanceof LocationMessage) {
                            //todo check the diff with sendLocationMessage ?
                            IMCenter.getInstance().sendMessage(message, null, null, wrapper.getCallback());
                        } else if (messageContent instanceof MediaMessageContent && (((MediaMessageContent) messageContent).getMediaUrl() == null
                                || TextUtils.isEmpty(((MediaMessageContent) messageContent).getMediaUrl().toString()))) {
                            IMCenter.getInstance().sendMediaMessage(message, null, null, wrapper.getCallback());
                        } else {
                            IMCenter.getInstance().sendMessage(message, null, null, wrapper.getCallback());
                        }
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
