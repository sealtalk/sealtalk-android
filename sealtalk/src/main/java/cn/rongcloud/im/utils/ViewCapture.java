package cn.rongcloud.im.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.View;

/**
 * 截图视图
 */
public class ViewCapture {

    public static Bitmap getViewBitmap(View view) {
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Drawable bgDrawable = view.getBackground();
        canvas.drawColor(Color.WHITE);  // 默认为白色背景
        if (bgDrawable != null) {
            bgDrawable.draw(canvas);
        }

        view.draw(canvas);
        return bitmap;
    }
}
