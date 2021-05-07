package cn.rongcloud.im.common;

import android.app.Application;

import cn.rongcloud.im.R;
import cn.rongcloud.im.net.SealTalkUrlCode;
import cn.rongcloud.im.utils.log.SLog;

/**
 * 全局错误码枚举
 * <p>
 * 因为目前不同的 API 会根据业务情况返回相同的错误码，所以需要根据 API 区分每个错误码的提示，所以做出以下处理。
 * <p>
 * API 类型的错误码规则：
 * 每个 API 的错误码由 API 的 url 对应的代码{@link cn.rongcloud.im.net.SealTalkUrlCode} + API 对应的错误码
 * 比如登录接口 LOGIN，此 API 的 url 代码为 1，当 API 返回错误码 为 1000  且错误码偏移为 10000 时,对应 ErrorCode 为 11000
 */
public enum ErrorCode {
    API_COMMON_ERROR(400, 0),
    LOGIN_PRIVATE_SERVICE_ERROR(34005, R.string.seal_login_private_service_error),
    REGION_AND_PHONE_INVALID(SealTalkUrlCode.LOGIN + 400, R.string.seal_login_toast_no_register),
    PHONE_NO_REGISTER(SealTalkUrlCode.LOGIN + 1000, R.string.seal_login_toast_no_register),
    USERNAME_OR_PASSWORD_INVALID(SealTalkUrlCode.LOGIN + 1001, R.string.seal_login_toast_phone_or_psw_error),
    API_SEND_CODE_OVER_LIMIT(SealTalkUrlCode.SEND_CODE + 5000, R.string.login_error_send_message_frequency),
    API_VERIFY_CODE_EXPIRED(SealTalkUrlCode.VERIFY_CODE + 2000, R.string.login_error_captcha_overdue),
    CHECK_VERIFY_CODE_FAILED(SealTalkUrlCode.VERIFY_CODE + 1000, R.string.login_verification_code_error),
    REGISTER_PHONE_ALREADY_EXISTED(SealTalkUrlCode.REGISTER + 400, R.string.login_register_phone_already_existed),
    NO_GROUP_BULLET(SealTalkUrlCode.GROUP_GET_BULLETIN + 402, R.string.profile_group_has_no_notice),
    API_ERR_OTHER(-2, R.string.common_network_error_and_retry_after),
    NETWORK_ERROR(-3, R.string.common_network_unavailable),
    IM_ERROR(-4, 0),
    RTC_ERROR(-5, 0),
    IM_TOKEN_ERROR(-6, 0),
    QRCODE_ERROR(-7, 0),
    UNKNOWN_ERROR(999999, 0),
    NONE_ERROR(-1, 0),
    GROUP_IN_PROTECT(SealTalkUrlCode.GROUP_COPY + 20004, R.string.seal_group_manager_copy_in_protect),
    GROUP_COPYED_IN_SEVEN_DAYS(SealTalkUrlCode.GROUP_COPY + 20005, R.string.seal_group_manager_copy_copied_7),
    GROUP_NOT_EXIST(SealTalkUrlCode.GROUP_COPY + 20006, R.string.seal_group_manager_copy_not_exist),
    GROUP_FAIL(SealTalkUrlCode.GROUP_COPY + 20007, R.string.seal_group_manager_copy_fail),
    LOGIN_VERIFY_CODE_FAILED(SealTalkUrlCode.REGISTER_AND_LOGIN + 1000, R.string.login_verification_code_error),
    LOGIN_VERIFY_CODE_EXPIRED(SealTalkUrlCode.REGISTER_AND_LOGIN + 2000, R.string.login_error_captcha_overdue);


    private int code;
    private int messageResId;
    private static Application application;

    ErrorCode(int code, int messageResId) {
        this.code = code;
        this.messageResId = messageResId;
    }

    public int getCode() {
        return code;
    }

    public int getMessageResId() {
        return messageResId;
    }

    public String getMessage() {
        if (application == null) {
            SLog.e(LogTag.COMMON, "ErrorCode getMessage need init first.");
            return "";
        }
        // 默认错误提示语
        String msg = "";
        if (messageResId > 0) {
            msg = application.getResources().getString(messageResId);
        }
        return msg;
    }

    public static ErrorCode fromCode(int code) {
        for (ErrorCode errorCode : ErrorCode.values()) {
            if (errorCode.code == code)
                return errorCode;
        }

        return UNKNOWN_ERROR;
    }

    public static void init(Application application) {
        ErrorCode.application = application;
    }

}
