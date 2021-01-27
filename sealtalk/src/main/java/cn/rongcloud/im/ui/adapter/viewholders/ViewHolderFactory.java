package cn.rongcloud.im.ui.adapter.viewholders;

import android.view.View;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

public class ViewHolderFactory <T extends BaseItemViewHolder> {

    private static ViewHolderFactory factory = new ViewHolderFactory();

    private ViewHolderFactory () {

    }

    public static ViewHolderFactory getInstance() {
        return factory;
    }


    /**
     * 创建ViewHolder
     * @param viewResId
     * @param view
     * @return
     */
    public  T createViewHolder(int viewResId, View view) {

        try {
            final Class<T> viewHolderClaszz = getViewHolderClaszz(viewResId);
            if (viewHolderClaszz != null) {
                Constructor<T> constructor = viewHolderClaszz.getConstructor(View.class);
                return constructor.newInstance(view);
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }


    public  Class<T> getViewHolderClaszz (int viewResId) {
        return viewHolderMap.get(viewResId);
    }

    private  HashMap<Integer, Class<T>> viewHolderMap = new HashMap<>();


    /**
     *  用于添加 viewHolder 和 布局资源对应的映射
     * @param viewResId 布局资源
     * @param viewHolderClazz ViewHolder class
     */
    public void putViewHolder(int viewResId, Class<T> viewHolderClazz) {
        if (!viewHolderMap.containsKey(viewResId)) {
            viewHolderMap.put(viewResId, viewHolderClazz);
        }
    }
}
