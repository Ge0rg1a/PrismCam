package com.example.prismsomething.gl;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

import androidx.core.content.ContextCompat;

import java.util.concurrent.Executor;

public class CameraSurfaceView extends GLSurfaceView {
    private final CameraRenderer renderer;

    public CameraSurfaceView(Context context) {
        this(context, null);
    }

    public CameraSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setEGLContextClientVersion(2);
        Executor main = ContextCompat.getMainExecutor(context);
        renderer = new CameraRenderer(this, main);
        setRenderer(renderer);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    public void setReadyListener(CameraRenderer.GLReadyListener l) {
        renderer.setReadyListener(l);
    }

    public void setFilter(FilterType type) {
        renderer.setFilter(type);
        requestRender();
    }

    public void setParam(float t) {
        renderer.setParam(t);
        requestRender();
    }

    public void takePhoto(CameraRenderer.PhotoCallback cb) {
        renderer.takePhoto(cb);
    }

    public CameraRenderer getRenderer() {
        return renderer;
    }

    public void releaseRenderer() {
        renderer.release();
    }
}
