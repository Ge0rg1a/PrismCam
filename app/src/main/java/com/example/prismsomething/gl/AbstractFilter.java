package com.example.prismsomething.gl;

import android.opengl.GLES20;

import java.nio.FloatBuffer;

public abstract class AbstractFilter {
    protected int program;
    protected int aPositionLoc;
    protected int aTexCoordLoc;
    protected int uTextureLoc;

    protected final FloatBuffer vertexBuffer;
    protected final FloatBuffer texBuffer;

    protected AbstractFilter() {
        vertexBuffer = GlUtil.toBuffer(GlUtil.QUAD_COORDS);
        texBuffer = GlUtil.toBuffer(GlUtil.QUAD_TEX);
    }

    public void setup() {
        program = ShaderUtil.createProgram(getVertexShader(), getFragmentShader());
        if (program == 0) {
            throw new RuntimeException("filter program failed");
        }
        aPositionLoc = GLES20.glGetAttribLocation(program, "aPosition");
        aTexCoordLoc = GLES20.glGetAttribLocation(program, "aTexCoord");
        uTextureLoc = GLES20.glGetUniformLocation(program, "uTexture");
        onInit();
    }

    protected void onInit() {
    }

    protected int width;
    protected int height;

    public void setParam(float t) {
    }

    protected String getVertexShader() {
        return DEFAULT_VERTEX;
    }

    protected abstract String getFragmentShader();

    public void draw(int inputTex, int outputFbo, int width, int height) {
        this.width = width;
        this.height = height;
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, outputFbo);
        GLES20.glViewport(0, 0, width, height);
        GLES20.glUseProgram(program);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        bindInputTexture(inputTex);
        GLES20.glUniform1i(uTextureLoc, 0);

        bindUniforms();
        drawQuad();
    }

    protected void bindInputTexture(int tex) {
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, tex);
    }

    protected void bindUniforms() {
    }

    protected void drawQuad() {
        GLES20.glEnableVertexAttribArray(aPositionLoc);
        GLES20.glVertexAttribPointer(aPositionLoc, 2, GLES20.GL_FLOAT, false, 8, vertexBuffer);
        GLES20.glEnableVertexAttribArray(aTexCoordLoc);
        GLES20.glVertexAttribPointer(aTexCoordLoc, 2, GLES20.GL_FLOAT, false, 8, texBuffer);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        GLES20.glDisableVertexAttribArray(aPositionLoc);
        GLES20.glDisableVertexAttribArray(aTexCoordLoc);
    }

    public void release() {
        if (program != 0) {
            GLES20.glDeleteProgram(program);
            program = 0;
        }
    }

    public static final String DEFAULT_VERTEX =
            "attribute vec4 aPosition;\n" +
                    "attribute vec2 aTexCoord;\n" +
                    "varying vec2 vTexCoord;\n" +
                    "void main() {\n" +
                    "  gl_Position = aPosition;\n" +
                    "  vTexCoord = aTexCoord;\n" +
                    "}\n";
}
