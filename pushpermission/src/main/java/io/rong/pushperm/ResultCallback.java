package io.rong.pushperm;

import io.rong.pushperm.perm.PermissionType;

/**
 * 跳转设置的回调接口
 */
public interface ResultCallback {

     public enum FailedType {
          /**
           * 不支持
           */
          NO_SUPPORT,
          /**
           * 拦截
           */
          INTERCEPT,
          /**
           * 取消
           */
          CANNEL;
     }

     /**
      * 假如权限早已经开启， 则会回调此接口并不会跳转设置界面
      * @param value 权限值，{@link PermissionType}
      */
     void onAreadlyOpened(String value);

     /**
      * 跳转权限设置界面之前会调用此接口。可在此接口方法拦截跳转。
      * @param value 权限值，{@link PermissionType}
      * @return true 拦截跳转， false 不拦截
      */
     boolean onBeforeShowDialog(String value);

     /**
      * 跳转设置界面成功
      * @param value 权限值，{@link PermissionType}
      */
     void onGoToSetting(String value);

     /**
      * 跳转设置页面失败
      * @param value 权限值，{@link PermissionType}
      * @param type 失败原因 {@link FailedType}
      * @see FailedType
      */
     void onFailed(String value, FailedType type);
}
