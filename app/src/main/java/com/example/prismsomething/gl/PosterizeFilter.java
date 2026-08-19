package com.example.prismsomething.gl;

import android.opengl.GLES20;

public class PosterizeFilter extends AbstractFilter {
    private int uLevelsLoc;
    private float levels = 4f;

    @Override
    protected String getFragmentShader() {
        return "precision mediump float;\n" +
                "uniform sampler2D uTexture;\n" +
                "uniform float uLevels;\n" +
                "varying vec2 vTexCoord;\n" +
                "void main() {\n" +
                "  vec4 c = texture2D(uTexture, vTexCoord);\n" +
                "  vec3 q = floor(c.rgb * uLevels) / max(uLevels - 1.0, 1.0);\n" +
                "  gl_FragColor = vec4(q, c.a);\n" +
                "}\n";
    }

    @Override
    protected void onInit() {
        uLevelsLoc = GLES20.glGetUniformLocation(program, "uLevels");
    }

    @Override
    protected void bindUniforms() {
        GLES20.glUniform1f(uLevelsLoc, levels);
    }

    @Override
    public void setParam(float t) {
        levels = 2f + (float) Math.floor(t * 14f);
    }
}
