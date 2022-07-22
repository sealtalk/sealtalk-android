package cn.rongcloud.im.common;

import android.app.Activity;
import android.app.AlertDialog;
import io.rong.imlib.IRongCoreListener;
import io.rong.imlib.model.BlockedMessageInfo;
import io.rong.imlib.model.MessageBlockType;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

public class BlockListener implements IRongCoreListener.MessageBlockListener {
    private Map<Integer, String> map = new HashMap<>();
    private WeakReference<Activity> weakActivity;

    public BlockListener(Activity activity) {
        weakActivity = new WeakReference<>(activity);
        map.put(MessageBlockType.UNKNOWN.value, "未知类型");
        map.put(MessageBlockType.BLOCK_GLOBAL.value, " 全局敏感词");
        map.put(MessageBlockType.BLOCK_CUSTOM.value, "自定义敏感词拦截");
        map.put(MessageBlockType.BLOCK_THIRD_PATY.value, "第三方审核拦截");
    }

    @Override
    public void onMessageBlock(BlockedMessageInfo info) {
        if (weakActivity.get() == null) {
            return;
        }
        Activity activity = weakActivity.get();
        if (activity.isFinishing()) {
            return;
        }
        StringBuilder builder = new StringBuilder();
        builder.append("会话类型=" + info.getConversationType().getName())
                .append("\n")
                .append("会话ID=" + info.getTargetId())
                .append("\n")
                .append("被拦截的消息ID=" + info.getBlockMsgUId())
                .append("\n")
                .append("被拦截的ChannelID=" + info.getChannelId())
                .append("\n")
                .append(
                        "被拦截原因的类型="
                                + info.getType().value
                                + " ("
                                + map.get(info.getType().value)
                                + ")")
                .append("\n")
                .append("被拦截的扩展字段=" + info.getExtra());

        new AlertDialog.Builder(activity, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT)
                .setMessage(builder.toString())
                .setCancelable(true)
                .show();
    }
}
