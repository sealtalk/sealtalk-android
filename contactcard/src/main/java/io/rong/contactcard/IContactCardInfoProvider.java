package io.rong.contactcard;

import io.rong.imlib.model.UserInfo;
import java.util.List;

public interface IContactCardInfoProvider {

    interface IContactCardInfoCallback {
        /**
         * 获取用户信息回调
         *
         * @param list 用户信息列表
         */
        void getContactCardInfoCallback(List<? extends UserInfo> list);
    }

    /**
     * 获取 APP 中的所有用户信息
     *
     * @param contactInfoCallback {@link IContactCardInfoCallback}
     */
    void getContactAllInfoProvider(IContactCardInfoCallback contactInfoCallback);

    /**
     * 获取 APP 中的指定用户信息(userId) - 异步方法
     *
     * @param userId 用户 ID
     * @param name 名字
     * @param portrait 头像
     * @param contactInfoCallback {@link IContactCardInfoCallback}
     */
    void getContactAppointedInfoProvider(
            String userId,
            String name,
            String portrait,
            IContactCardInfoCallback contactInfoCallback);
}
