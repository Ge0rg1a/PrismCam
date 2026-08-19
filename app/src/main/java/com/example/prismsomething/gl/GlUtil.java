package com.example.prismsomething.gl;

import android.opengl.GLES11Ext;
import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public final class GlUtil {
    private GlUtil() {
    }

    public static final float[] QUAD_COORDS = {
            -1f, -1f,
            1f, -1f,
            -1f, 1f,
            1f, 1f,
    };

    public static final float[] QUAD_TEX = {
            0f, 0f,
            1f, 0f,
            0f, 1f,
            1f, 1f,
    };

    public static FloatBuffer toBuffer(float[] array) {
        FloatBuffer b = ByteBuffer.allocateDirect(array.length * 4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        b.put(array).position(0);
        return b;
    }

    public static int genTexture2D() {
        int[] t = new int[1];
        GLES20.glGenTextures(1, t, 0);
        int id = t[0];
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, id);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        return id;
    }

    public static int genTextureOes() {
        int[] t = new int[1];
        GLES20.glGenTextures(1, t, 0);
        int id = t[0];
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, id);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        return id;
    }

    public static int genFbo() {
        int[] f = new int[1];
        GLES20.glGenFramebuffers(1, f, 0);
        return f[0];
    }

    public static void checkFbo() {
        int s = GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER);
        if (s != GLES20.GL_FRAMEBUFFER_COMPLETE) {
            throw new RuntimeException("fbo incomplete 0x" + Integer.toHexString(s));
        }
    }
}
