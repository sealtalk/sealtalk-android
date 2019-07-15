package io.rong.pushperm.perm;

import android.content.Context;

/**
 * 检查机型权限接口
 */
interface IModelCheckPermmission {

    /**
     * 检查手机自启动权限。如果当前机型没有适配，
     *  即 没有此机型的检查方法， 则会返回 -1. 当有相关的检查方法并且权限是开启状态的则
     *  返回为 0. 否则返回 1.
     * @param context 上下文
     * @return -1 没有适配；  0 开启; 1 没有开启，
     */
    int checkAutoStartPermission(Context context);
}
