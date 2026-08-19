package com.example.prismsomething.gl;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.net.Uri;
import android.opengl.GLES20;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Surface;

import androidx.camera.core.Preview;
import androidx.camera.core.SurfaceRequest;

import java.nio.ByteBuffer;
import java.util.EnumMap;
import java.util.concurrent.Executor;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class CameraRenderer implements android.opengl.GLSurfaceView.Renderer, Preview.SurfaceProvider {

    private static final String TAG = "CameraRenderer";

    public interface PhotoCallback {
        void onSaved(Uri uri);
        void onFailed();
    }

    public interface GLReadyListener {
        void onReady();
    }

    private final android.opengl.GLSurfaceView view;
    private final Executor mainExecutor;
    private final Object lock = new Object();

    private int oesTex;
    private SurfaceTexture surfaceTexture;
    private Surface surface;
    private SurfaceRequest pendingRequest;
    private boolean surfaceReady = false;

    private final float[] stm = new float[16];

    private int texAFbo, texATex;
    private int scratchFbo, scratchTex;
    private int photoFbo, photoTex;
    private int viewW, viewH;

    private final OesInputFilter oesInput = new OesInputFilter();
    private final EnumMap<FilterType, AbstractFilter> filters = new EnumMap<>(FilterType.class);
    private FilterType currentType = FilterType.NONE;
    private AbstractFilter current;

    private volatile int rotation = 0;

    private final float[] mvpMatrix = new float[16];
    private int bufferW = 0;
    private int bufferH = 0;

    private volatile boolean pendingPhoto = false;
    private PhotoCallback photoCallback;
    private GLReadyListener readyListener;

    private ByteBuffer photoBuffer;

    public CameraRenderer(android.opengl.GLSurfaceView view, Executor mainExecutor) {
        this.view = view;
        this.mainExecutor = mainExecutor;
    }

    public void setReadyListener(GLReadyListener l) {
        readyListener = l;
    }

    public void setFilter(FilterType type) {
        currentType = type;
        current = filters.get(type);
    }

    public void setParam(float t) {
        if (current != null) current.setParam(t);
    }

    public void takePhoto(PhotoCallback cb) {
        photoCallback = cb;
        pendingPhoto = true;
        view.requestRender();
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        oesTex = GlUtil.genTextureOes();
        surfaceTexture = new SurfaceTexture(oesTex);
        surfaceTexture.setOnFrameAvailableListener(this::onFrameAvailable);
        surface = new Surface(surfaceTexture);

        oesInput.setup();
        buildFilters();

        synchronized (lock) {
            if (pendingRequest != null) {
                fulfill(pendingRequest);
                pendingRequest = null;
            }
            surfaceReady = true;
        }
        mainExecutor.execute(() -> {
            if (readyListener != null) readyListener.onReady();
        });
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        viewW = width;
        viewH = height;
        GLES20.glViewport(0, 0, width, height);
        releaseFbo(texAFbo, texATex);
        releaseFbo(scratchFbo, scratchTex);
        releaseFbo(photoFbo, photoTex);
        int[] a = makeFbo(width, height);
        texAFbo = a[0];
        texATex = a[1];
        int[] s = makeFbo(width, height);
        scratchFbo = s[0];
        scratchTex = s[1];
        int[] p = makeFbo(width, height);
        photoFbo = p[0];
        photoTex = p[1];
        photoBuffer = ByteBuffer.allocateDirect(width * height * 4);
        AbstractFilter blur = filters.get(FilterType.BLUR);
        if (blur instanceof BlurFilter) {
            ((BlurFilter) blur).setScratch(scratchFbo, scratchTex);
        }
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        if (surfaceTexture == null) return;
        surfaceTexture.updateTexImage();
        surfaceTexture.getTransformMatrix(stm);

        oesInput.setTexMatrix(stm);
        updateQuadScale();
        oesInput.setMvpMatrix(buildVertexMvp());
        oesInput.draw(oesTex, texAFbo, viewW, viewH);

        if (current == null) current = filters.get(FilterType.NONE);
        current.draw(texATex, 0, viewW, viewH);

        if (pendingPhoto) {
            pendingPhoto = false;
            current.draw(texATex, photoFbo, viewW, viewH);
            readPhoto();
        }
    }

    private void onFrameAvailable(SurfaceTexture st) {
        view.requestRender();
    }

    private float[] buildVertexMvp() {
        android.opengl.Matrix.setIdentityM(mvpMatrix, 0);
        return mvpMatrix;
    }

    private void updateQuadScale() {
        float sx = 1f, sy = 1f;
        if (bufferW > 0 && bufferH > 0 && viewW > 0 && viewH > 0) {
            int ow = (rotation == 90 || rotation == 270) ? bufferH : bufferW;
            int oh = (rotation == 90 || rotation == 270) ? bufferW : bufferH;
            float va = (float) ow / oh;
            float sa = (float) viewW / viewH;
            if (va > sa) {
                sx = va / sa;
            } else {
                sy = sa / va;
            }
        }
        oesInput.setQuadScale(sx, sy);
    }

    @Override
    public void onSurfaceRequested(SurfaceRequest request) {
        bufferW = request.getResolution().getWidth();
        bufferH = request.getResolution().getHeight();
        request.setTransformationInfoListener(mainExecutor, info -> rotation = info.getRotationDegrees());
        synchronized (lock) {
            if (surfaceReady && surface != null) {
                fulfill(request);
            } else {
                pendingRequest = request;
            }
        }
    }

    private void fulfill(SurfaceRequest request) {
        try {
            surfaceTexture.setDefaultBufferSize(
                    request.getResolution().getWidth(),
                    request.getResolution().getHeight());
        } catch (Exception e) {
            Log.w(TAG, "set default buffer size failed", e);
        }
        request.provideSurface(surface, mainExecutor, result -> {
        });
    }

    private void buildFilters() {
        for (FilterType t : FilterType.values()) {
            AbstractFilter f;
            switch (t) {
                case NONE:
                    f = new NoneFilter();
                    break;
                case GRAYSCALE:
                    f = new GrayscaleFilter();
                    break;
                case INVERT:
                    f = new InvertFilter();
                    break;
                case COLOR_ADJUST:
                    f = new ColorAdjustFilter();
                    break;
                case GAMMA:
                    f = new GammaFilter();
                    break;
                case BLUR:
                    f = new BlurFilter();
                    break;
                case SHARPEN:
                    f = new SharpenFilter();
                    break;
                case SOBEL:
                    f = new SobelFilter();
                    break;
                case POSTERIZE:
                    f = new PosterizeFilter();
                    break;
                default:
                    f = new NoneFilter();
            }
            f.setup();
            filters.put(t, f);
        }
        current = filters.get(FilterType.NONE);
    }

    private int[] makeFbo(int w, int h) {
        int tex = GlUtil.genTexture2D();
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, tex);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, w, h, 0,
                GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
        int fbo = GlUtil.genFbo();
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fbo);
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER,
                GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, tex, 0);
        GlUtil.checkFbo();
        return new int[]{fbo, tex};
    }

    private void releaseFbo(int fbo, int tex) {
        if (fbo != 0) {
            GLES20.glDeleteFramebuffers(1, new int[]{fbo}, 0);
        }
        if (tex != 0) {
            GLES20.glDeleteTextures(1, new int[]{tex}, 0);
        }
    }

    private void readPhoto() {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, photoFbo);
        photoBuffer.position(0);
        GLES20.glReadPixels(0, 0, viewW, viewH, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, photoBuffer);
        final int w = viewW;
        final int h = viewH;
        final ByteBuffer buf = ByteBuffer.allocateDirect(w * h * 4);
        buf.put((ByteBuffer) photoBuffer.position(0)).flip();
        new Thread(() -> {
            try {
                Bitmap bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
                bmp.copyPixelsFromBuffer(buf);
                Matrix m = new Matrix();
                m.preScale(1f, -1f);
                bmp = Bitmap.createBitmap(bmp, 0, 0, w, h, m, true);
                Uri uri = saveBitmap(bmp);
                mainExecutor.execute(() -> {
                    if (photoCallback != null) {
                        if (uri != null) photoCallback.onSaved(uri);
                        else photoCallback.onFailed();
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "readPhoto", e);
                mainExecutor.execute(() -> {
                    if (photoCallback != null) photoCallback.onFailed();
                });
            }
        }).start();
    }

    private Uri saveBitmap(Bitmap bmp) {
        Context ctx = view.getContext();
        android.content.ContentValues cv = new android.content.ContentValues();
        cv.put(MediaStore.Images.Media.DISPLAY_NAME, "IMG_" + System.currentTimeMillis() + ".jpg");
        cv.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        Uri uri = ctx.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv);
        if (uri == null) return null;
        try (java.io.OutputStream os = ctx.getContentResolver().openOutputStream(uri)) {
            if (os != null && bmp.compress(Bitmap.CompressFormat.JPEG, 95, os)) {
                return uri;
            }
        } catch (java.io.IOException e) {
            Log.e(TAG, "saveBitmap", e);
        }
        return null;
    }

    public void release() {
        if (surfaceTexture != null) {
            surfaceTexture.release();
            surfaceTexture = null;
        }
        if (surface != null) {
            surface.release();
            surface = null;
        }
        synchronized (lock) {
            surfaceReady = false;
        }
    }
}
