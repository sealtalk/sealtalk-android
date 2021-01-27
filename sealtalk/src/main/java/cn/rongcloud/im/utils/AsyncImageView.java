package cn.rongcloud.im.utils;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.net.Uri;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.widget.AppCompatImageView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.assist.LoadedFrom;
import com.nostra13.universalimageloader.core.display.CircleBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.process.BitmapProcessor;

import java.io.File;
import java.lang.ref.WeakReference;
import java.net.URLEncoder;

import cn.rongcloud.im.R;
import io.rong.common.RLog;

import io.rong.imkit.utils.RongUtils;
import io.rong.imkit.widget.RCMessageFrameLayout;

public class AsyncImageView extends AppCompatImageView {

    private final static String TAG = "AsyncImageView";

    private boolean isCircle;
    private float minShortSideSize = 0;
    private int mCornerRadius = 0;
    private final static int AVATAR_SIZE = 80;

    private Drawable mDefaultDrawable;
    private WeakReference<Bitmap> mWeakBitmap;
    private WeakReference<Bitmap> mShardWeakBitmap;
    private boolean mHasMask;

    public AsyncImageView(Context context) {
        super(context);
    }

    public AsyncImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (isInEditMode()) return;
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AsyncImageView);
        int resId = a.getResourceId(R.styleable.AsyncImageView_RCDefDrawable, 0);
        isCircle = a.getInt(R.styleable.AsyncImageView_RCShape, 0) == 1;
        minShortSideSize = a.getDimension(R.styleable.AsyncImageView_RCMinShortSideSize, 0);
        mCornerRadius = (int) a.getDimension(R.styleable.AsyncImageView_RCCornerRadius, 0);
        mHasMask = a.getBoolean(R.styleable.AsyncImageView_RCMask, false);

        if (resId != 0) {
            mDefaultDrawable = getResources().getDrawable(resId);
        }
        a.recycle();

        if (mDefaultDrawable != null) {
            DisplayImageOptions options = createDisplayImageOptions(resId, false);
            Drawable drawable = options.getImageForEmptyUri(null);
            Bitmap bitmap = drawableToBitmap(drawable);
            ImageViewAware imageViewAware = new ImageViewAware(this);
            options.getDisplayer().display(bitmap, imageViewAware, LoadedFrom.DISC_CACHE);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mHasMask) {
            Bitmap bitmap = mWeakBitmap == null ? null : mWeakBitmap.get();
            Drawable drawable = getDrawable();
            RCMessageFrameLayout parent = (RCMessageFrameLayout) getParent();
            Drawable background = parent.getBackgroundDrawable();

            if (bitmap == null || bitmap.isRecycled()) {
                int width = getWidth();
                int height = getHeight();
                if (width <= 0 || height <= 0)
                    return;
                try {
                    bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                } catch (OutOfMemoryError e) {
                    RLog.e(TAG, "onDraw OutOfMemoryError");
                    RLog.e(TAG, "onDraw", e);
                    System.gc();
                }
                if (bitmap != null) {
                    Canvas rCanvas = new Canvas(bitmap);
                    if (drawable != null) {
                        drawable.setBounds(0, 0, width, height);
                        drawable.draw(rCanvas);
                        if (background instanceof NinePatchDrawable) {
                            NinePatchDrawable patchDrawable = (NinePatchDrawable) background;
                            patchDrawable.setBounds(0, 0, width, height);
                            Paint maskPaint = patchDrawable.getPaint();
                            maskPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
                            patchDrawable.draw(rCanvas);
                        }
                        mWeakBitmap = new WeakReference<>(bitmap);
                    }
                    canvas.drawBitmap(bitmap, 0, 0, null);
                    getShardImage(background, bitmap, canvas);
                }
            } else {
                canvas.drawBitmap(bitmap, 0, 0, null);
                getShardImage(background, bitmap, canvas);
            }
        } else {
            super.onDraw(canvas);
        }
    }

    private void getShardImage(Drawable drawable_bg, Bitmap bp, Canvas canvas) {
        int width = bp.getWidth();
        int height = bp.getHeight();
        Bitmap bitmap = mShardWeakBitmap == null ? null : mShardWeakBitmap.get();

        if (width <= 0 || height <= 0)
            return;
        if (bitmap == null || bitmap.isRecycled()) {
            try {
                bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            } catch (OutOfMemoryError e) {
                RLog.e(TAG, "getShardImage OutOfMemoryError");
                RLog.e(TAG, "getShardImage", e);
                System.gc();
            }

            if (bitmap != null) {
                Canvas rCanvas = new Canvas(bitmap);
                Paint paint = new Paint();
                paint.setAntiAlias(true);
                Rect rect = new Rect(0, 0, width, height);
                Rect rectF = new Rect(0, 0, width, height);

                BitmapDrawable drawable_in = new BitmapDrawable(bp);
                drawable_in.setBounds(rectF);
                drawable_in.draw(rCanvas);
                if (drawable_bg instanceof NinePatchDrawable) {
                    NinePatchDrawable patchDrawable = (NinePatchDrawable) drawable_bg;
                    patchDrawable.setBounds(rect);
                    Paint maskPaint = patchDrawable.getPaint();
                    maskPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OVER));
                    patchDrawable.draw(rCanvas);
                }
                mShardWeakBitmap = new WeakReference<>(bitmap);
                canvas.drawBitmap(bitmap, 0, 0, paint);
            }
        } else {
            canvas.drawBitmap(bitmap, 0, 0, null);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        if (mWeakBitmap != null) {
            Bitmap bitmap = mWeakBitmap.get();
            if (bitmap != null && !bitmap.isRecycled())
                bitmap.recycle();
            mWeakBitmap = null;
        }
        if (mShardWeakBitmap != null) {
            Bitmap bitmap = mShardWeakBitmap.get();
            if (bitmap != null && !bitmap.isRecycled())
                bitmap.recycle();
            mShardWeakBitmap = null;
        }
        super.onDetachedFromWindow();
    }

    @Override
    public void invalidate() {
        if (mWeakBitmap != null) {
            Bitmap bitmap = mWeakBitmap.get();
            if (bitmap != null && !bitmap.isRecycled())
                bitmap.recycle();
            mWeakBitmap = null;
        }
        if (mShardWeakBitmap != null) {
            Bitmap bitmap = mShardWeakBitmap.get();
            if (bitmap != null && !bitmap.isRecycled())
                bitmap.recycle();
            mShardWeakBitmap = null;
        }
        super.invalidate();
    }

    /**
     * 设置默认图片
     */
    public void setDefaultDrawable() {
        if (mDefaultDrawable != null) {
            DisplayImageOptions options = createDisplayImageOptions(0, false);
            Bitmap bitmap = drawableToBitmap(mDefaultDrawable);
            ImageViewAware imageViewAware = new ImageViewAware(this);
            options.getDisplayer().display(bitmap, imageViewAware, LoadedFrom.DISC_CACHE);
        }
    }

    /**
     * 根据资源地址设置并显示 view，此方法会原图显示。
     *
     * @param imageUri 图片地址
     */
    public void setResource(Uri imageUri) {
        DisplayImageOptions options = createDisplayImageOptions(0, true);
        if (imageUri != null) {
            File file = new File(imageUri.getPath());
            if (!file.exists()) {
                ImageViewAware imageViewAware = new ImageViewAware(this);
                ImageLoader.getInstance().displayImage(imageUri.toString(), imageViewAware, options, null, null);
            } else {
                Bitmap bitmap = getBitmap(imageUri);
                if (bitmap != null) {
                    setLayoutParam(bitmap);
                    setImageBitmap(bitmap);
                } else {
                    setImageBitmap(null);
                    ViewGroup.LayoutParams params = getLayoutParams();
                    params.height = RongUtils.dip2px(80);
                    params.width = RongUtils.dip2px(110);
                    setLayoutParams(params);
                }
            }
        }
    }

    /**
     * 设置是否显示圆形边框。
     *
     * @param circle 是否显示圆形
     */
    public void setCircle(boolean circle) {
        isCircle = circle;
    }

    /**
     * 根据资源地址设置并显示 view，此方法会原图显示。
     *
     * @param imageUri 图片地址
     */
    public void setLocationResource(Uri imageUri, int defRes, final int w, final int h, final ImageLoadingListener loadingListener) {
        DisplayImageOptions.Builder builder = new DisplayImageOptions.Builder();
        DisplayImageOptions options = builder.resetViewBeforeLoading(false)
                .cacheInMemory(false)
                .cacheOnDisk(true)
                .bitmapConfig(Bitmap.Config.ARGB_8888)
                .showImageOnLoading(defRes)
                .preProcessor(new BitmapProcessor() {
                    @Override
                    public Bitmap process(Bitmap bitmap) {
                        int widthOrg = bitmap.getWidth();
                        int heightOrg = bitmap.getHeight();
                        int xTopLeft = (widthOrg - w) / 2;
                        int yTopLeft = (heightOrg - h) / 2;

                        if (xTopLeft <= 0 || yTopLeft <= 0) {
                            return bitmap;
                        }

                        try {
                            Bitmap result = Bitmap.createBitmap(bitmap, xTopLeft, yTopLeft, w, h);
                            if (!bitmap.isRecycled())
                                bitmap.recycle();
                            return result;
                        } catch (OutOfMemoryError e) {
                            return null;
                        }
                    }
                })
                .build();
        ImageLoader.getInstance().displayImage(imageUri == null ? null : imageUri.toString(), this, options, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {

            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                loadingListener.onLoadingFailed(imageUri, view, failReason);
            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                File file = ImageLoader.getInstance().getDiskCache().get(imageUri);
                if (file != null && file.exists())
                    loadingListener.onLoadingComplete(imageUri, view, loadedImage);
            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {
                loadingListener.onLoadingCancelled(imageUri, view);
            }
        });
    }

    /**
     * 根据资源地址设置并显示 view，次方法会原图显示。
     * 如果加载网络图片，加载前先显示设置的默认图片（前提已设置），成功后替换成下载的网络图片
     *
     * @param imageUri     图片地址
     * @param defaultResId 默认资源 id
     */
    public void setResource(String imageUri, int defaultResId) {
        if (imageUri == null && defaultResId <= 0) {
            return;
        }

        DisplayImageOptions options = createDisplayImageOptions(defaultResId, true);
        ImageLoader.getInstance().displayImage(imageUri, this, options);
    }

    private Bitmap drawableToBitmap(Drawable drawable) {
        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();
        Bitmap.Config config = drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565;
        Bitmap bitmap = Bitmap.createBitmap(width, height, config);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, width, height);
        drawable.draw(canvas);
        return bitmap;
    }

    /**
     * 设置头像，此方法会缓存头像，便于快速加载。
     * 此方法会对图片进行压缩，防止图片过大，卡顿，OOM 问题。
     * 如果加载网络图片，加载前先显示设置的默认图片（前提已设置），成功后替换成下载的网络图片
     *
     * @param imageUri     头像地址
     * @param defaultResId 默认头像
     */
    public void setAvatar(String imageUri, int defaultResId) {
        ImageViewAware imageViewAware = new ImageViewAware(this);
        ImageSize imageSize = new ImageSize(AVATAR_SIZE, AVATAR_SIZE);
        DisplayImageOptions options = createDisplayImageOptions(defaultResId, true);
        ImageLoader.getInstance().displayImage(imageUri, imageViewAware, options, imageSize, null, null);
    }

    /**
     * 根据name自动生成头像, 并设置
     *
     * @param uid          用户唯一表示
     * @param name         用户名
     * @param defaultResId 默认头像
     */
    public void setAvatar(String uid, String name, int defaultResId) {
        ImageViewAware imageViewAware = new ImageViewAware(this);
        ImageSize imageSize = new ImageSize(AVATAR_SIZE, AVATAR_SIZE);
        String[] arr = {name, uid};
        DisplayImageOptions options = createDisplayImageOptions(defaultResId, true, arr);
        String key = "";
        if (!TextUtils.isEmpty(name)) {
            key = URLEncoder.encode(name);
        }
        ImageLoader.getInstance().displayImage("avatar://" + uid + key, imageViewAware, options, imageSize, null, null);
    }

    /**
     * 设置头像，此方法会缓存头像，便于快速加载。
     * 此方法会对图片进行压缩，防止图片过大，卡顿，OOM 问题。
     * 如果加载网络图片，加载前先显示设置的默认图片（前提已设置），成功后替换成下载的网络图片
     * <p/>
     * 如果布局文件中未添加默认头像，在加载过程，无任何显示，反之会先显示默认头像
     *
     * @param imageUri 头像地址
     */
    public void setAvatar(Uri imageUri) {
        if (imageUri != null) {
            ImageViewAware imageViewAware = new ImageViewAware(this);
            ImageSize imageSize = new ImageSize(AVATAR_SIZE, AVATAR_SIZE);
            DisplayImageOptions options = createDisplayImageOptions(0, true);
            ImageLoader.getInstance().displayImage(imageUri.toString(), imageViewAware, options, imageSize, null, null);
        }
    }

    public void setBitmap(Bitmap bitmap) {
        setLayoutParam(bitmap);
        setImageBitmap(bitmap);
    }


    private Bitmap getBitmap(Uri uri) {
        Bitmap bitmap = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        options = new BitmapFactory.Options();

        try {
            bitmap = BitmapFactory.decodeFile(uri.getPath(), options);
        } catch (Exception e) {
            RLog.e(TAG, "getBitmap Exception : " + uri);
            RLog.e(TAG, "getBitmap", e);
        }
        return bitmap;
    }

    private DisplayImageOptions createDisplayImageOptions(int defaultResId, boolean cacheInMemory) {
        return createDisplayImageOptions(defaultResId, cacheInMemory, null);
    }

    private DisplayImageOptions createDisplayImageOptions(int defaultResId, boolean cacheInMemory, Object extraForDownloader) {
        DisplayImageOptions.Builder builder = new DisplayImageOptions.Builder();
        Drawable defaultDrawable = mDefaultDrawable;
        if (defaultResId > 0) {
            try {
                defaultDrawable = getContext().getResources().getDrawable(defaultResId);
            } catch (Resources.NotFoundException e) {
                e.printStackTrace();
            }
        }

        if (defaultDrawable != null) {
            builder.showImageOnLoading(defaultDrawable);
            builder.showImageForEmptyUri(defaultDrawable);
            builder.showImageOnFail(defaultDrawable);
        }
        if (extraForDownloader != null) {
            builder.extraForDownloader(extraForDownloader);
        }

        if (isCircle) {
            builder.displayer(new CircleBitmapDisplayer());
        } else if (mCornerRadius > 0) {
            builder.displayer(new RoundedBitmapDisplayer(mCornerRadius));
        } else {
            builder.displayer(new SimpleBitmapDisplayer());
        }

        return builder.resetViewBeforeLoading(false)
                .cacheInMemory(cacheInMemory)
                .cacheOnDisk(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();
    }

    public int getCornerRadius() {
        return mCornerRadius;
    }

    public void setCornerRadius(int mCornerRadius) {
        this.mCornerRadius = mCornerRadius;
    }

    private void setLayoutParam(Bitmap bitmap) {
        float width = bitmap.getWidth();
        float height = bitmap.getHeight();
        int finalWidth;
        int finalHeight;
        int minSize = 100;

        if (minShortSideSize > 0) {
            if (width <= minShortSideSize || height <= minShortSideSize) {
                float scale = width / height;

                if (scale > 1) {
                    finalHeight = (int) (minShortSideSize / scale);
                    if (finalHeight < minSize) {
                        finalHeight = minSize;
                    }
                    finalWidth = (int) minShortSideSize;
                } else {
                    finalHeight = (int) minShortSideSize;
                    finalWidth = (int) (minShortSideSize * scale);
                    if (finalWidth < minSize) {
                        finalWidth = minSize;
                    }
                }

                ViewGroup.LayoutParams params = getLayoutParams();
                params.height = finalHeight;
                params.width = finalWidth;
                setLayoutParams(params);
            } else {
                ViewGroup.LayoutParams params = getLayoutParams();
                params.height = (int) height;
                params.width = (int) width;
                setLayoutParams(params);
            }
        }
    }


    public void setLayoutParam(int width, int height) {
        ViewGroup.LayoutParams params = getLayoutParams();
        params.height = height;
        params.width = width;
        setLayoutParams(params);
    }

}