package io.rong.pushperm.perm;

import android.content.Context;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 小米权限检查，用于小米特殊的权限检查
 */
class MiCheckPermission implements IModelCheckPermmission {
    @Override
    public int checkAutoStartPermission(Context context) {
        int result = -1;
        try {
            Class<?> aClass = Class.forName("android.miui.AppOpsUtils");
            Method getApplicationAutoStart = aClass.getMethod("getApplicationAutoStart", Context.class, String.class);
            Object invoke = getApplicationAutoStart.invoke(null, context,  context.getApplicationInfo().packageName);
            result = (int)invoke;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return result;
    }

}
