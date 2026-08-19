package com.example.prismsomething.gl;

import android.opengl.GLES11Ext;
import android.opengl.GLES20;

public class OesInputFilter extends AbstractFilter {
    private int uStmLoc;
    private int uMvpLoc;
    private int uQuadScaleLoc;
    private final float[] stm = new float[16];
    private final float[] mvp = new float[16];
    private final float[] quadScale = {1f, 1f};

    @Override
    protected String getVertexShader() {
        return "attribute vec4 aPosition;\n" +
                "attribute vec2 aTexCoord;\n" +
                "uniform mat4 uStm;\n" +
                "uniform mat4 uMvp;\n" +
                "uniform vec2 uQuadScale;\n" +
                "varying vec2 vTexCoord;\n" +
                "void main() {\n" +
                "  vTexCoord = (uStm * vec4(aTexCoord, 0.0, 1.0)).xy;\n" +
                "  gl_Position = uMvp * vec4(aPosition.xy * uQuadScale, aPosition.z, aPosition.w);\n" +
                "}\n";
    }

    @Override
    protected String getFragmentShader() {
        return "#extension GL_OES_EGL_image_external : require\n" +
                "precision mediump float;\n" +
                "uniform samplerExternalOES uTexture;\n" +
                "varying vec2 vTexCoord;\n" +
                "void main() {\n" +
                "  gl_FragColor = texture2D(uTexture, vTexCoord);\n" +
                "}\n";
    }

    @Override
    protected void onInit() {
        uStmLoc = GLES20.glGetUniformLocation(program, "uStm");
        uMvpLoc = GLES20.glGetUniformLocation(program, "uMvp");
        uQuadScaleLoc = GLES20.glGetUniformLocation(program, "uQuadScale");
    }

    @Override
    protected void bindInputTexture(int tex) {
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, tex);
    }

    @Override
    protected void bindUniforms() {
        GLES20.glUniformMatrix4fv(uStmLoc, 1, false, stm, 0);
        GLES20.glUniformMatrix4fv(uMvpLoc, 1, false, mvp, 0);
        GLES20.glUniform2f(uQuadScaleLoc, quadScale[0], quadScale[1]);
    }

    public void setTexMatrix(float[] m) {
        if (m != null && m.length >= 16) {
            System.arraycopy(m, 0, stm, 0, 16);
        }
    }

    public void setMvpMatrix(float[] m) {
        if (m != null && m.length >= 16) {
            System.arraycopy(m, 0, mvp, 0, 16);
        }
    }

    public void setQuadScale(float sx, float sy) {
        quadScale[0] = sx;
        quadScale[1] = sy;
    }
}
