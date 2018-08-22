package com.technocom.digitalslate.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.EmbossMaskFilter;
import android.graphics.MaskFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;


public class PaintView extends View {

    public static final int DEFAULT_COLOR = Color.WHITE;
    public static final int DEFAULT_BG_COLOR = Color.BLACK;
    private static final float TOUCH_TOLERANCE = 4;
    public static int BRUSH_SIZE = 20;
    private float mX, mY;
    private Path mPath;
    private Paint pencilPaint;
    private ArrayList<FingerPath> paths = new ArrayList<>();
    private int pencilColor;
    private int strokeWidth;
    private boolean emboss;
    private boolean blur;
    private int bgColor;
    private MaskFilter mEmboss;
    private MaskFilter mBlur;
    private Bitmap mBitmap;
    private Canvas mCanvas;
    private Paint mBitmapPaint = new Paint(Paint.DITHER_FLAG);
    public boolean isPencil = true;
    private File output;

    public PaintView(Context context) {
        this(context, null);
    }

    public PaintView(Context context, AttributeSet attrs) {
        super(context, attrs);
        pencilPaint = new Paint();
        pencilPaint.setAntiAlias(true);
        pencilPaint.setDither(true);
        pencilPaint.setColor(DEFAULT_COLOR);
        pencilPaint.setStyle(Paint.Style.STROKE);
        pencilPaint.setStrokeJoin(Paint.Join.ROUND);
        pencilPaint.setStrokeCap(Paint.Cap.ROUND);
        pencilPaint.setXfermode(null);
        pencilPaint.setAlpha(0xff);

        mEmboss = new EmbossMaskFilter(new float[]{1, 1, 1}, 0.4f, 6, 3.5f);
        mBlur = new BlurMaskFilter(5, BlurMaskFilter.Blur.NORMAL);
    }

    public void init(DisplayMetrics metrics) {
        int height = metrics.heightPixels;
        int width = metrics.widthPixels;
        mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
        mCanvas.drawColor(DEFAULT_BG_COLOR);
        bgColor = DEFAULT_BG_COLOR;
        pencilColor = DEFAULT_COLOR;
        strokeWidth = BRUSH_SIZE;
    }

    public void normal() {
        emboss = false;
        blur = false;
    }

    public void clear() {
        mCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        paths.clear();
        normal();
        invalidate();
    }


    public boolean saveBitmap(String path, String name) {
        boolean success;
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        output = new File(dir, name + ".jpg");
        OutputStream os;

        try {
            os = new FileOutputStream(output);
            mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
            os.flush();
            os.close();
            success = true;
            MediaScannerConnection.scanFile(getContext(), new String[]{output.toString()}, null,
                    new MediaScannerConnection.OnScanCompletedListener() {
                        public void onScanCompleted(String path, Uri uri) {
                        }
                    }
            );
        } catch (Exception e) {
            e.printStackTrace();
            success = false;
        }

        return success;
    }

    public File getFile() {
        return output;
    }

    public void changeColorAndText(int color, int size) {
        BRUSH_SIZE = size;
        strokeWidth = BRUSH_SIZE;
        isPencil = true;
        pencilPaint.setXfermode(null);
        pencilPaint.setAlpha(0xff);
        pencilPaint.setColor(color);
        pencilColor = color;
    }

    public void eraserColorAndText(int color, int size) {

        BRUSH_SIZE = size;
        strokeWidth = BRUSH_SIZE;
        isPencil = false;
        pencilPaint.setXfermode(null);
        pencilPaint.setAlpha(0xff);
        pencilPaint.setColor(color);
        pencilColor = color;
    }


    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();
        mCanvas.drawColor(bgColor);
        for (FingerPath fp : paths) {
            pencilPaint.setColor(fp.color);
            pencilPaint.setStrokeWidth(fp.strokeWidth);
            pencilPaint.setMaskFilter(null);

            if (fp.emboss)
                pencilPaint.setMaskFilter(mEmboss);
            else if (fp.blur)
                pencilPaint.setMaskFilter(mBlur);
            mCanvas.drawPath(fp.path, pencilPaint);
        }
        canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
        //canvas.restore();
    }

    private void pencilStart(float x, float y) {
        mPath = new Path();
        FingerPath fp = new FingerPath(pencilColor, emboss, blur, strokeWidth, mPath);
        paths.add(fp);

        mPath.reset();
        mPath.moveTo(x, y);
        mX = x;
        mY = y;
    }

    public void setBoardColor(int color) {
        bgColor = color;
        invalidate();
    }

    private void pencilMove(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);

        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
            mX = x;
            mY = y;
        }
    }

    private void pencilUp() {
        mPath.lineTo(mX, mY);
    }


    private void eraserStart(float x, float y) {
        mPath = new Path();
        FingerPath fp = new FingerPath(pencilColor, emboss, blur, strokeWidth, mPath);
        paths.add(fp);

        mPath.reset();
        mPath.moveTo(x, y);
        mX = x;
        mY = y;
    }

    private void eraserMove(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);

        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
            mX = x;
            mY = y;
        }
    }

    private void eraserUp() {
        mPath.lineTo(mX, mY);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (isPencil)
                    pencilStart(x, y);
                else
                    eraserStart(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                if (isPencil)
                    pencilMove(x, y);
                else
                    eraserMove(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                if (isPencil)
                    pencilUp();
                else
                    eraserUp();
                invalidate();
                break;
        }
        return true;
    }

}
