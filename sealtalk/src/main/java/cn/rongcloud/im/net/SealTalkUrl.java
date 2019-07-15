package cn.rongcloud.im.net;

public class SealTalkUrl {
    public static final String DOMAIN = "http://api.sealtalk.im";

    public static final String LOGIN = "/user/login";

    public static final String GET_TOKEN = "/user/get_token";

    public static final String GET_USER_INFO = "/user/{user_id}";

    public static final String SEND_CODE = "/user/send_code_yp";

    public static final String VERIFY_CODE = "/user/verify_code_yp";

    public static final String REGISTER = "/user/register";

    public static final String REGION_LIST = "/user/regionlist";

    public static final String CHECK_PHONE_AVAILABLE = "/user/check_phone_available";

    public static final String RESET_PASSWORD = "/user/reset_password";

    public static final String GET_IMAGE_UPLOAD_TOKEN = "/user/get_image_token";

    public static final String GROUP_CREATE = "/group/create";

    public static final String GROUP_ADD_MEMBER = "/group/add";

    public static final String GROUP_JOIN = "/group/join";

    public static final String GROUP_KICK_MEMBER = "/group/kick";

    public static final String GROUP_QUIT = "/group/quit";

    public static final String GROUP_DISMISS = "/group/dismiss";

    public static final String GROUP_TRANSFER = "/group/transfer";

    public static final String GROUP_RENAME = "/group/rename";

    public static final String GROUP_SET_BULLETIN = "/group/set_bulletin";

    public static final String GROUP_GET_BULLETIN = "/group/get_bulletin";

    public static final String GROUP_SET_PORTRAIT_URL = "/group/set_portrait_uri";

    public static final String GROUP_SET_DISPLAY_NAME = "/group/set_display_name";

    public static final String GROUP_GET_INFO = "/group/{group_id}";

    public static final String GROUP_GET_MEMBER_INFO = "/group/{group_id}/members";

    public static final String GROUP_SAVE_TO_CONTACT = "/group/fav";

    public static final String GROUP_GET_ALL_IN_CONTACT = "/user/favgroups";

    public static final String GET_FRIEND_ALL = "/friendship/all";

    public static final String GET_BLACK_LIST = "/user/blacklist";

    public static final String ADD_BLACK_LIST = "/user/add_to_blacklist";

    public static final String REMOVE_BLACK_LIST = "/user/remove_from_blacklist";

    public static final String SET_NICK_NAME = "/user/set_nickname";

    public static final String SET_PORTRAIT = "/user/set_portrait_uri";

    public static final String ARGEE_FRIENDS = "/friendship/agree";

    public static final String GET_FRIEND_PROFILE = "/friendship/{friendId}/profile";

    public static final String SET_DISPLAY_NAME = "/friendship/set_display_name";

    public static final String INVITE_FRIEND = "/friendship/invite";

    public static final String DELETE_FREIND = "/friendship/delete";

    public static final String CLIENT_VERSION = "/misc/client_version";

    public static final String CHANGE_PASSWORD = "/user/change_password";

    public static final String GET_DISCOVERY_CHAT_ROOM = "/misc/demo_square";

    public static final String FIND_FRIEND = "/user/find/{region}/{phone}";

    public static final String GROUP_REMOVE_MANAGER = "/group/remove_manager";

    public static final String GROUP_ADD_MANAGER = "/group/set_manager";
}
