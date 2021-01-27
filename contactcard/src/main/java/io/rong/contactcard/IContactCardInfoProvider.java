package io.rong.contactcard;

import java.util.List;

import io.rong.imlib.model.UserInfo;

public interface IContactCardInfoProvider {

    interface IContactCardInfoCallback {
        void getContactCardInfoCallback(List<? extends UserInfo> list);
    }

    // 获取 APP 中的所有用户信息
    void getContactAllInfoProvider(IContactCardInfoCallback contactInfoCallback);

    // 获取 APP 中的指定用户信息(userId) - 异步方法
    void getContactAppointedInfoProvider(String userId, String name, String portrait,
                                         IContactCardInfoCallback contactInfoCallback);

}
