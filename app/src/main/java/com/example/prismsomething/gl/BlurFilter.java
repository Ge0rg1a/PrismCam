package com.example.prismsomething.gl;

import android.opengl.GLES20;

public class BlurFilter extends AbstractFilter {
    private int uTexelLoc;
    private int uDirLoc;
    private int uRadiusLoc;
    private int scratchFbo = 0;
    private int scratchTex = 0;
    private float radius = 1f;

    @Override
    protected String getFragmentShader() {
        return "precision mediump float;\n" +
                "uniform sampler2D uTexture;\n" +
                "uniform vec2 uTexel;\n" +
                "uniform vec2 uDirection;\n" +
                "uniform float uRadius;\n" +
                "varying vec2 vTexCoord;\n" +
                "void main() {\n" +
                "  vec2 o1 = uDirection * uTexel * 1.0 * uRadius;\n" +
                "  vec2 o2 = uDirection * uTexel * 2.0 * uRadius;\n" +
                "  vec2 o3 = uDirection * uTexel * 3.0 * uRadius;\n" +
                "  vec2 o4 = uDirection * uTexel * 4.0 * uRadius;\n" +
                "  vec4 sum = texture2D(uTexture, vTexCoord) * 0.227027;\n" +
                "  sum += texture2D(uTexture, vTexCoord + o1) * 0.1945946;\n" +
                "  sum += texture2D(uTexture, vTexCoord - o1) * 0.1945946;\n" +
                "  sum += texture2D(uTexture, vTexCoord + o2) * 0.1216216;\n" +
                "  sum += texture2D(uTexture, vTexCoord - o2) * 0.1216216;\n" +
                "  sum += texture2D(uTexture, vTexCoord + o3) * 0.0547216;\n" +
                "  sum += texture2D(uTexture, vTexCoord - o3) * 0.0547216;\n" +
                "  sum += texture2D(uTexture, vTexCoord + o4) * 0.0162162;\n" +
                "  sum += texture2D(uTexture, vTexCoord - o4) * 0.0162162;\n" +
                "  gl_FragColor = sum;\n" +
                "}\n";
    }

    @Override
    protected void onInit() {
        uTexelLoc = GLES20.glGetUniformLocation(program, "uTexel");
        uDirLoc = GLES20.glGetUniformLocation(program, "uDirection");
        uRadiusLoc = GLES20.glGetUniformLocation(program, "uRadius");
    }

    public void setScratch(int fbo, int tex) {
        scratchFbo = fbo;
        scratchTex = tex;
    }

    @Override
    public void setParam(float t) {
        radius = 0.5f + t * 8f;
    }

    @Override
    public void draw(int inputTex, int outputFbo, int width, int height) {
        this.width = width;
        this.height = height;
        float texelX = 1f / Math.max(width, 1);
        float texelY = 1f / Math.max(height, 1);

        GLES20.glUseProgram(program);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, scratchFbo);
        GLES20.glViewport(0, 0, width, height);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, inputTex);
        GLES20.glUniform1i(uTextureLoc, 0);
        GLES20.glUniform2f(uTexelLoc, texelX, texelY);
        GLES20.glUniform2f(uDirLoc, 1f, 0f);
        GLES20.glUniform1f(uRadiusLoc, radius);
        drawQuad();

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, outputFbo);
        GLES20.glViewport(0, 0, width, height);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, scratchTex);
        GLES20.glUniform2f(uDirLoc, 0f, 1f);
        drawQuad();
    }
}
