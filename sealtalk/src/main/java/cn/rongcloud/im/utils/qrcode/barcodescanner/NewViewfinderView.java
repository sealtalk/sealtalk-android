/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.rongcloud.im.utils.qrcode.barcodescanner;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.View;

import com.google.zxing.ResultPoint;

import java.util.ArrayList;
import java.util.List;

import cn.rongcloud.im.R;

/**
 * This view is overlaid on top of the camera preview. It adds the viewfinder rectangle and partial
 * transparency outside it, as well as the laser scanner animation and result points.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 */
public class NewViewfinderView extends View {
    protected static final String TAG = NewViewfinderView.class.getSimpleName();

    protected static final int MAX_RESULT_POINTS = 20;

    protected final Paint paint;
    private Paint traAnglePaint;
    private Paint linePaint;
    private final int triAngleColor = Color.parseColor("#3A91F3");
    private final int lineColor = Color.parseColor("#3A91F3");
    private final int triAngleWidth = dp2px(4);
    private final int triAngleLength = dp2px(20);
    private int lineOffsetCount = 0;
    protected Bitmap resultBitmap;
    protected final int maskColor; //蒙在摄像头上面区域的半透明颜色
    protected final int resultColor;
    protected final int laserColor;
    protected final int resultPointColor;
    protected int scannerAlpha;
    protected List<ResultPoint> possibleResultPoints;
    protected List<ResultPoint> lastPossibleResultPoints;
    protected CameraPreview cameraPreview;
    private boolean allowScanAnimation = true;

    // Cache the framingRect and previewFramingRect, so that we can still draw it after the preview
    // stopped.
    protected Rect framingRect;
    protected Rect previewFramingRect;

    // This constructor is used when the class is built from an XML resource.
    public NewViewfinderView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // Initialize these once for performance rather than calling them every time in onDraw().
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        traAnglePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        traAnglePaint.setColor(triAngleColor);
        traAnglePaint.setStrokeWidth(triAngleWidth);
        traAnglePaint.setStyle(Paint.Style.STROKE);

        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setColor(lineColor);

        Resources resources = getResources();

        // Get setted attributes on view
        TypedArray attributes = getContext().obtainStyledAttributes(attrs, R.styleable.zxing_finder);

        this.maskColor = attributes.getColor(R.styleable.zxing_finder_zxing_viewfinder_mask,
                resources.getColor(R.color.zxing_viewfinder_mask));
        this.resultColor = attributes.getColor(R.styleable.zxing_finder_zxing_result_view,
                resources.getColor(R.color.zxing_result_view));
        this.laserColor = attributes.getColor(R.styleable.zxing_finder_zxing_viewfinder_laser,
                resources.getColor(R.color.zxing_viewfinder_laser));
        this.resultPointColor = attributes.getColor(R.styleable.zxing_finder_zxing_possible_result_points,
                resources.getColor(R.color.zxing_possible_result_points));

        attributes.recycle();


        scannerAlpha = 0;
        possibleResultPoints = new ArrayList<>(5);
        lastPossibleResultPoints = null;
    }

    public void setCameraPreview(CameraPreview view) {
        this.cameraPreview = view;
        view.addStateListener(new CameraPreview.StateListener() {
            @Override
            public void previewSized() {
                refreshSizes();
                invalidate();
            }

            @Override
            public void previewStarted() {

            }

            @Override
            public void previewStopped() {

            }

            @Override
            public void cameraError(Exception error) {

            }
        });
    }

    protected void refreshSizes() {
        if (cameraPreview == null) {
            return;
        }
        Rect framingRect = cameraPreview.getFramingRect();
        Rect previewFramingRect = cameraPreview.getPreviewFramingRect();
        if (framingRect != null && previewFramingRect != null) {
            this.framingRect = framingRect;
            this.previewFramingRect = previewFramingRect;
        }
    }

    @SuppressLint("DrawAllocation")
    @Override
    public void onDraw(Canvas canvas) {
        refreshSizes();
        if (framingRect == null || previewFramingRect == null) {
            return;
        }

        Rect frame = framingRect;
        Rect previewFrame = previewFramingRect;

        int width = canvas.getWidth();
        int height = canvas.getHeight();

        // Draw the exterior (i.e. outside the framing rect) darkened
        paint.setColor(resultBitmap != null ? resultColor : maskColor);
        canvas.drawRect(0, 0, width, frame.top, paint);
        canvas.drawRect(0, frame.top, frame.left, frame.bottom + 1, paint);
        canvas.drawRect(frame.right + 1, frame.top, width, frame.bottom + 1, paint);
        canvas.drawRect(0, frame.bottom + 1, width, height, paint);

        // 四个角落的三角
        Path leftTopPath = new Path();
        leftTopPath.moveTo(frame.left + triAngleLength, frame.top + triAngleWidth / 2);
        leftTopPath.lineTo(frame.left + triAngleWidth / 2, frame.top + triAngleWidth / 2);
        leftTopPath.lineTo(frame.left + triAngleWidth / 2, frame.top + triAngleLength);
        canvas.drawPath(leftTopPath, traAnglePaint);

        Path rightTopPath = new Path();
        rightTopPath.moveTo(frame.right - triAngleLength, frame.top + triAngleWidth / 2);
        rightTopPath.lineTo(frame.right - triAngleWidth / 2, frame.top + triAngleWidth / 2);
        rightTopPath.lineTo(frame.right - triAngleWidth / 2, frame.top + triAngleLength);
        canvas.drawPath(rightTopPath, traAnglePaint);

        Path leftBottomPath = new Path();
        leftBottomPath.moveTo(frame.left + triAngleWidth / 2, frame.bottom - triAngleLength);
        leftBottomPath.lineTo(frame.left + triAngleWidth / 2, frame.bottom - triAngleWidth / 2);
        leftBottomPath.lineTo(frame.left + triAngleLength, frame.bottom - triAngleWidth / 2);
        canvas.drawPath(leftBottomPath, traAnglePaint);

        Path rightBottomPath = new Path();
        rightBottomPath.moveTo(frame.right - triAngleLength, frame.bottom - triAngleWidth / 2);
        rightBottomPath.lineTo(frame.right - triAngleWidth / 2, frame.bottom - triAngleWidth / 2);
        rightBottomPath.lineTo(frame.right - triAngleWidth / 2, frame.bottom - triAngleLength);
        canvas.drawPath(rightBottomPath, traAnglePaint);

        if (isFailNetwork) {
            String text = getResources().getString(R.string.common_network_unavailable);
            String text2 = getResources().getString(R.string.common_network_check);
            Paint networkPaint = new Paint();
            Rect textRect = new Rect();
            networkPaint.getTextBounds(text, 0, text.length(), textRect);
            networkPaint.setTextSize(50);
            networkPaint.setAntiAlias(true);
            networkPaint.setColor(getResources().getColor(R.color.white));
            canvas.drawText(text, width / 2 - textRect.width() * 2 - textRect.width() / 2 + 20, height / 2 - textRect.height() * 3, networkPaint);
            canvas.drawText(text2, width / 2 - textRect.width() * 2 - textRect.width() / 2 + 20, height / 2 + textRect.height() * 3, networkPaint);
            Paint paint = new Paint();
            paint.setColor(Color.BLACK);
            paint.setAlpha(150);
            paint.setStyle(Paint.Style.FILL);
            canvas.drawRect(0, 0, width, height, paint);
        } else {
            if(allowScanAnimation) {
                //循环划线，从上到下
                if (lineOffsetCount > frame.bottom - frame.top - dp2px(10)) {
                    lineOffsetCount = 0;
                } else {
                    lineOffsetCount = lineOffsetCount + 6;
                    Rect lineRect = new Rect();
                    lineRect.left = frame.left;
                    lineRect.top = frame.top + lineOffsetCount;
                    lineRect.right = frame.right;
                    lineRect.bottom = frame.top + dp2px(10) + lineOffsetCount;
                    canvas.drawBitmap(((BitmapDrawable) (getResources().getDrawable(R.drawable.zxing_scanline))).getBitmap(), null, lineRect, linePaint);
                }
                postInvalidateDelayed(10L, frame.left, frame.top, frame.right, frame.bottom);
            }
        }
        Paint paint = new Paint();
        paint.setStrokeWidth(2);
        paint.setColor(Color.parseColor("#3A91F3"));
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawRect(frame, paint);
    }

    private boolean isFailNetwork;

    public void networkChange(boolean isFailNetwork) {
        this.isFailNetwork = isFailNetwork;
        postInvalidate();
    }


    /**
     * Only call from the UI thread.
     *
     * @param point a point to draw, relative to the preview frame
     */
    public void addPossibleResultPoint(ResultPoint point) {
        List<ResultPoint> points = possibleResultPoints;
        points.add(point);
        int size = points.size();
        if (size > MAX_RESULT_POINTS) {
            // trim it
            points.subList(0, size - MAX_RESULT_POINTS / 2).clear();
        }
    }

    private int dp2px(int dp) {
        float density = getContext().getResources().getDisplayMetrics().density;
        return (int) (dp * density + 0.5f);
    }

    public boolean isAllowScanAnimation() {
        return allowScanAnimation;
    }

    public void setAllowScanAnimation(boolean allowScanAnimation) {
        this.allowScanAnimation = allowScanAnimation;
    }
}
