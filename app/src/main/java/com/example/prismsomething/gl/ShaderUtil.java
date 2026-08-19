package com.example.prismsomething.gl;

import android.opengl.GLES20;
import android.util.Log;

public final class ShaderUtil {
    private static final String TAG = "ShaderUtil";

    private ShaderUtil() {
    }

    public static int createProgram(String vertexSrc, String fragmentSrc) {
        int vs = loadShader(GLES20.GL_VERTEX_SHADER, vertexSrc);
        if (vs == 0) return 0;
        int fs = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSrc);
        if (fs == 0) {
            GLES20.glDeleteShader(vs);
            return 0;
        }
        int program = GLES20.glCreateProgram();
        if (program == 0) return 0;
        GLES20.glAttachShader(program, vs);
        GLES20.glAttachShader(program, fs);
        GLES20.glLinkProgram(program);
        int[] linkStatus = new int[1];
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
        if (linkStatus[0] != GLES20.GL_TRUE) {
            Log.e(TAG, "link failed: " + GLES20.glGetProgramInfoLog(program));
            GLES20.glDeleteProgram(program);
            program = 0;
        }
        GLES20.glDeleteShader(vs);
        GLES20.glDeleteShader(fs);
        return program;
    }

    public static int loadShader(int type, String src) {
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, src);
        GLES20.glCompileShader(shader);
        int[] status = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, status, 0);
        if (status[0] != GLES20.GL_TRUE) {
            Log.e(TAG, "compile " + type + " failed: " + GLES20.glGetShaderInfoLog(shader));
            GLES20.glDeleteShader(shader);
            return 0;
        }
        return shader;
    }

    public static void checkGlError(String op) {
        int err = GLES20.glGetError();
        if (err != GLES20.GL_NO_ERROR) {
            Log.e(TAG, op + ": glError 0x" + Integer.toHexString(err));
            throw new RuntimeException(op + ": glError " + err);
        }
    }
}
