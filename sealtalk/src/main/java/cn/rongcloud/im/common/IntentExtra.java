package cn.rongcloud.im.common;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import cn.rongcloud.im.utils.IntentDataTransferBinder;

public class IntentExtra {
    public static final String STR_TARGET_ID = "target_id";

    public static final String START_FROM_ID = "from_id";

    public static final String SERIA_CONVERSATION_TYPE = "conversation_type";

    public static final String SERIA_CONVERSATION_IDENTIFIER = "conversation_identifier";

    public static final String SERIA_USER_GROUP_CHECKED_LIST = "user_group_checked_list";

    public static final String SERIA_USER_GROUP_INFO = "user_group_info";

    public static final String SERIA_USER_GROUP_TITLE = "user_group_title";

    public static final String SERIA_USER_GROUP_CAN_EDIT = "user_group_can_edit";

    public static final String SERIA_USER_GROUP_SOURCE_CODE = "user_group_source_code";

    public static final String SERIA_INTENT_BINDER = "intent_binder";

    public static final String SERIA_INTENT_BUNDLE = "intent_bundle";

    public static final String LIST_STR_ID_LIST = "id_list";

    public static final String LIST_CAN_NOT_CHECK_ID_LIST = "list_can_not_check_id_list";

    public static final String LIST_EXCLUDE_ID_LIST = "exculde_id_list";

    public static final String TITLE = "title";

    public static final String LIST_ALREADY_CHECKED_USER_ID_LIST =
            "list_already_check_user_id_list";

    public static final String LIST_ALREADY_CHECKED_GROUP_ID_LIST =
            "list_already_check_group_id_list";

    public static final String BOOLEAN_CONFIRM_FORWARD = "boolean_confirm_forward";

    public static final String STR_GROUP_NAME = "group_name";

    public static final String STR_CHAT_NAME = "chat_name";

    public static final String STR_CHAT_PORTRAIT = "chat_portrait";

    public static final String URL = "url";

    public static final String ORGIN = "orgin";

    public static final String IMAGE_PREVIEW_TYPE = "image_preview_type";

    public static final String USER_ID = "userId";

    public static final String SERIA_QRCODE_DISPLAY_TYPE = "qrcode_display";

    public static final String PARCEL_MESSAGE = "message";

    public static final String FORWARD_MESSAGE_LIST = "forward_message_list";

    public static final String FORWARD_MESSAGE_ID_LIST = "messageIds";

    public static final String BOOLEAN_FORWARD_USE_SDK = "forward_use_sdk";

    public static final String GROUP_ID = "group_id";

    public static final String GROUP_NICK_NAME = "group_nick_name";

    public static final String BOOLEAN_ENABLE_TOAST = "enable_toast";

    public static final String BOOLEAN_KICKED_BY_OTHER_USER = "kick_by_other_user";

    public static final String BOOLEAN_CONNECT_TIME_OUT = "connect_timeout";

    public static final String OPERATE_PICTURE_ACTION = "operate_picture_action";

    public static final String BOOLEAN_USER_ABANDON = "user_abandon";

    public static final String BOOLEAN_USER_BLOCKED = "user_blocked";

    public static final String BOOLEAN_KICKED_BY_SECURITY = "kick_by_security";

    /** 创建群组时是否返回结果，默认是 false ，代表创建群组后直接跳转到群聊界面 */
    public static final String BOOLEAN_CREATE_GROUP_RETURN_RESULT = "create_group_return_result";

    public static final String MANAGEMENT_LEFT_SELECT_COUNT = "management_left_select_count";

    public static final String GROUP_LIST = "group_list";
    public static final String FRIEND_LIST = "friend_list";
    public static final String FORWARD_FINISH = "forward_finish";
    public static final String IS_SELECT = "is_select";

    public static final String STR_POKE_MESSAGE = "poke_message";
    public static final String CONFIRM_SEND = "confirm_send";

    public static void setResultWithBinder(Activity activity, Object data) {
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        IntentDataTransferBinder transferBinder = new IntentDataTransferBinder(data);
        bundle.putBinder(IntentExtra.SERIA_INTENT_BINDER, transferBinder);
        intent.putExtra(IntentExtra.SERIA_INTENT_BUNDLE, bundle);
        activity.setResult(Activity.RESULT_OK, intent);
        activity.finish();
    }

    public static IntentDataTransferBinder extractIntentBinder(Intent data) {
        if (data == null) {
            return null;
        }
        Bundle bundle = data.getBundleExtra(IntentExtra.SERIA_INTENT_BUNDLE);
        if (bundle == null) {
            return null;
        }
        if (!bundle.containsKey(IntentExtra.SERIA_INTENT_BINDER)) {
            return null;
        }
        return (IntentDataTransferBinder) bundle.getBinder(IntentExtra.SERIA_INTENT_BINDER);
    }
}
