package cn.rongcloud.im.common;

import android.net.Uri;

import java.util.HashMap;
import java.util.Set;

import cn.rongcloud.im.net.SealTalkUrl;
import cn.rongcloud.im.net.SealTalkUrlCode;

public class ApiErrorCodeMap {

    /**
     * 一般 url 映射表
     */
    private final static HashMap<String, Integer> FULL_PATH_PREFIX_MAP = new HashMap<String, Integer>() {
        {
            put(SealTalkUrl.LOGIN, SealTalkUrlCode.LOGIN);
            put(SealTalkUrl.GET_TOKEN, SealTalkUrlCode.GET_TOKEN);
            put(SealTalkUrl.SEND_CODE, SealTalkUrlCode.SEND_CODE);
            put(SealTalkUrl.VERIFY_CODE, SealTalkUrlCode.VERIFY_CODE);
            put(SealTalkUrl.REGISTER, SealTalkUrlCode.REGISTER);
            put(SealTalkUrl.REGION_LIST, SealTalkUrlCode.REGION_LIST);
            put(SealTalkUrl.CHECK_PHONE_AVAILABLE, SealTalkUrlCode.CHECK_PHONE_AVAILABLE);
            put(SealTalkUrl.RESET_PASSWORD, SealTalkUrlCode.RESET_PASSWORD);
            put(SealTalkUrl.GET_IMAGE_UPLOAD_TOKEN, SealTalkUrlCode.GET_IMAGE_UPLOAD_TOKEN);
            put(SealTalkUrl.GROUP_CREATE, SealTalkUrlCode.GROUP_CREATE);
            put(SealTalkUrl.GROUP_ADD_MEMBER, SealTalkUrlCode.GROUP_ADD_MEMBER);
            put(SealTalkUrl.GROUP_JOIN, SealTalkUrlCode.GROUP_JOIN);
            put(SealTalkUrl.GROUP_KICK_MEMBER, SealTalkUrlCode.GROUP_KICK_MEMBER);
            put(SealTalkUrl.GROUP_QUIT, SealTalkUrlCode.GROUP_QUIT);
            put(SealTalkUrl.GROUP_DISMISS, SealTalkUrlCode.GROUP_DISMISS);
            put(SealTalkUrl.GROUP_TRANSFER, SealTalkUrlCode.GROUP_TRANSFER);
            put(SealTalkUrl.GROUP_RENAME, SealTalkUrlCode.GROUP_RENAME);
            put(SealTalkUrl.GROUP_SET_BULLETIN, SealTalkUrlCode.GROUP_SET_BULLETIN);
            put(SealTalkUrl.GROUP_GET_BULLETIN, SealTalkUrlCode.GROUP_GET_BULLETIN);
            put(SealTalkUrl.GROUP_SET_PORTRAIT_URL, SealTalkUrlCode.GROUP_SET_PORTRAIT_URL);
            put(SealTalkUrl.GROUP_SET_DISPLAY_NAME, SealTalkUrlCode.GROUP_SET_DISPLAY_NAME);
            put(SealTalkUrl.GROUP_SAVE_TO_CONTACT, SealTalkUrlCode.GROUP_SAVE_TO_CONTACT);
            put(SealTalkUrl.GROUP_GET_ALL_IN_CONTACT, SealTalkUrlCode.GROUP_GET_ALL_IN_CONTACT);
            put(SealTalkUrl.GET_FRIEND_ALL, SealTalkUrlCode.GET_FRIEND_ALL);
            put(SealTalkUrl.GET_BLACK_LIST, SealTalkUrlCode.GET_BLACK_LIST);
            put(SealTalkUrl.ADD_BLACK_LIST, SealTalkUrlCode.ADD_BLACK_LIST);
            put(SealTalkUrl.REMOVE_BLACK_LIST, SealTalkUrlCode.REMOVE_BLACK_LIST);
            put(SealTalkUrl.SET_NICK_NAME, SealTalkUrlCode.SET_NICK_NAME);
            put(SealTalkUrl.SET_PORTRAIT, SealTalkUrlCode.SET_PORTRAIT);
            put(SealTalkUrl.ARGEE_FRIENDS, SealTalkUrlCode.ARGEE_FRIENDS);
            put(SealTalkUrl.SET_DISPLAY_NAME, SealTalkUrlCode.SET_DISPLAY_NAME);
            put(SealTalkUrl.INVITE_FRIEND, SealTalkUrlCode.INVITE_FRIEND);
            put(SealTalkUrl.DELETE_FREIND, SealTalkUrlCode.DELETE_FREIND);
            put(SealTalkUrl.CLIENT_VERSION, SealTalkUrlCode.CLIENT_VERSION);
            put(SealTalkUrl.CHANGE_PASSWORD, SealTalkUrlCode.CHANGE_PASSWORD);
            put(SealTalkUrl.GET_DISCOVERY_CHAT_ROOM, SealTalkUrlCode.GET_DISCOVERY_CHAT_ROOM);
            put(SealTalkUrl.GROUP_REMOVE_MANAGER, SealTalkUrlCode.GROUP_REMOVE_MANAGER);
            put(SealTalkUrl.GROUP_ADD_MANAGER, SealTalkUrlCode.GROUP_ADD_MANAGER);
            put(SealTalkUrl.GROUP_COPY, SealTalkUrlCode.GROUP_COPY);
            put(SealTalkUrl.GROUP_MUTE_ALL, SealTalkUrlCode.GROUP_MUTE_ALL);
            put(SealTalkUrl.GROUP_SET_CERTIFICATION, SealTalkUrlCode.GROUP_SET_CERTIFICATION);
            put(SealTalkUrl.SET_PRIVACY, SealTalkUrlCode.SET_PRIVACY);
            put(SealTalkUrl.GET_PRIVACY, SealTalkUrlCode.GET_PRIVACY);
            put(SealTalkUrl.GET_SCREEN_CAPTURE, SealTalkUrlCode.GET_SCREEN_CAPTURE);
            put(SealTalkUrl.SET_SCREEN_CAPTURE, SealTalkUrlCode.SET_SCREEN_CAPTURE);
            put(SealTalkUrl.SEND_SC_MSG, SealTalkUrlCode.SEND_SC_MSG);
            put(SealTalkUrl.REGISTER_AND_LOGIN, SealTalkUrlCode.REGISTER_AND_LOGIN);
        }
    };

    /**
     * 查询类 url 映射
     * 由于此类 url 没法直接用 key 去进行查询，所以单独列出，用正则去区分查询
     */
    public final static HashMap<String, Integer> QUERY_PATH_REG_PREFIX_MAP = new HashMap<String, Integer>() {
        {
            put("/user/[a-zA-Z0-9+=-_]*", SealTalkUrlCode.GET_USER_INFO);
            put("/group/[a-zA-Z0-9+=-_]*", SealTalkUrlCode.GROUP_GET_INFO);
            put("/group/[a-zA-Z0-9+=-_]*/members", SealTalkUrlCode.GROUP_GET_MEMBER_INFO);
            put("/friendship/[a-zA-Z0-9+=-_]*/profile", SealTalkUrlCode.GET_FRIEND_PROFILE);
            put("/user/find/[a-zA-Z0-9+=-_]*/[a-zA-Z0-9+=-_]*", SealTalkUrlCode.FIND_FRIEND);
        }
    };


    /**
     * 获取 API 返回的错误码中对应的全局错误码
     *
     * @param apiPath
     * @param apiErrorCode
     * @return
     */
    public static int getApiErrorCode(String apiPath, int apiErrorCode) {
        ErrorCode errorCode = ErrorCode.API_ERR_OTHER;
        //删除返回 url 的前缀
        Uri baseUri = Uri.parse(SealTalkUrl.DOMAIN);
        String removeValue = baseUri.getPath() + "/";
        String realPath = apiPath.substring(apiPath.indexOf(removeValue) + 1);
        Integer prefix = FULL_PATH_PREFIX_MAP.get(realPath);
        // 如果在一般路径中没有找到此 api 则从查询 api 中查询
        if (prefix == null) {
            prefix = getPrefixFromQueryPath(realPath);
        }

        if (prefix != null) {
            errorCode = ErrorCode.fromCode(prefix + apiErrorCode);
            if (errorCode == ErrorCode.UNKNOWN_ERROR) {
                errorCode = ErrorCode.API_ERR_OTHER;
            }
        }

        return errorCode.getCode();
    }

    private static Integer getPrefixFromQueryPath(String apiPath) {
        Integer prefix = null;
        Set<String> pathSet = QUERY_PATH_REG_PREFIX_MAP.keySet();
        for (String pathReg : pathSet) {
            if (apiPath.matches(pathReg)) {
                prefix = QUERY_PATH_REG_PREFIX_MAP.get(pathReg);
                break;
            }
        }

        return prefix;
    }
}
