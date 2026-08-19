package com.example.prismsomething.gl;

import android.opengl.GLES20;

public class SharpenFilter extends AbstractFilter {
    private int uTexelLoc;
    private int uStrengthLoc;
    private float strength = 0.5f;

    @Override
    protected String getFragmentShader() {
        return "precision mediump float;\n" +
                "uniform sampler2D uTexture;\n" +
                "uniform vec2 uTexel;\n" +
                "uniform float uStrength;\n" +
                "varying vec2 vTexCoord;\n" +
                "void main() {\n" +
                "  vec2 tx = uTexel;\n" +
                "  vec3 orig = texture2D(uTexture, vTexCoord).rgb;\n" +
                "  vec3 sharp = orig * 5.0\n" +
                "    - texture2D(uTexture, vTexCoord + vec2(tx.x, 0.0)).rgb\n" +
                "    - texture2D(uTexture, vTexCoord - vec2(tx.x, 0.0)).rgb\n" +
                "    - texture2D(uTexture, vTexCoord + vec2(0.0, tx.y)).rgb\n" +
                "    - texture2D(uTexture, vTexCoord - vec2(0.0, tx.y)).rgb;\n" +
                "  vec3 outc = mix(orig, clamp(sharp, 0.0, 1.0), uStrength);\n" +
                "  gl_FragColor = vec4(outc, 1.0);\n" +
                "}\n";
    }

    @Override
    protected void onInit() {
        uTexelLoc = GLES20.glGetUniformLocation(program, "uTexel");
        uStrengthLoc = GLES20.glGetUniformLocation(program, "uStrength");
    }

    @Override
    protected void bindUniforms() {
        GLES20.glUniform2f(uTexelLoc, 1f / Math.max(width, 1), 1f / Math.max(height, 1));
        GLES20.glUniform1f(uStrengthLoc, strength);
    }

    @Override
    public void setParam(float t) {
        strength = t;
    }
}
