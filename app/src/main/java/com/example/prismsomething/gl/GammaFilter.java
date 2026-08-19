package com.example.prismsomething.gl;

import android.opengl.GLES20;

public class GammaFilter extends AbstractFilter {
    private int uGammaLoc;
    private float gamma = 1f;

    @Override
    protected String getFragmentShader() {
        return "precision mediump float;\n" +
                "uniform sampler2D uTexture;\n" +
                "uniform float uGamma;\n" +
                "varying vec2 vTexCoord;\n" +
                "void main() {\n" +
                "  vec4 c = texture2D(uTexture, vTexCoord);\n" +
                "  gl_FragColor = vec4(pow(c.rgb, vec3(1.0 / uGamma)), c.a);\n" +
                "}\n";
    }

    @Override
    protected void onInit() {
        uGammaLoc = GLES20.glGetUniformLocation(program, "uGamma");
    }

    @Override
    protected void bindUniforms() {
        GLES20.glUniform1f(uGammaLoc, Math.max(gamma, 0.01f));
    }

    @Override
    public void setParam(float t) {
        gamma = (float) Math.exp((t - 0.5f) * 2f);
    }
}
