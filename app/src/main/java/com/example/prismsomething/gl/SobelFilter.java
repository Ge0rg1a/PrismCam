package com.example.prismsomething.gl;

import android.opengl.GLES20;

public class SobelFilter extends AbstractFilter {
    private int uTexelLoc;

    private static final String LUM = "vec3(0.299, 0.587, 0.114)";

    @Override
    protected String getFragmentShader() {
        return "precision mediump float;\n" +
                "uniform sampler2D uTexture;\n" +
                "uniform vec2 uTexel;\n" +
                "varying vec2 vTexCoord;\n" +
                "void main() {\n" +
                "  vec2 t = vTexCoord;\n" +
                "  vec2 tx = uTexel;\n" +
                "  float ltl = dot(texture2D(uTexture, t + vec2(-tx.x, tx.y)).rgb, " + LUM + ");\n" +
                "  float ltm = dot(texture2D(uTexture, t + vec2(0.0, tx.y)).rgb, " + LUM + ");\n" +
                "  float ltr = dot(texture2D(uTexture, t + vec2(tx.x, tx.y)).rgb, " + LUM + ");\n" +
                "  float lml = dot(texture2D(uTexture, t + vec2(-tx.x, 0.0)).rgb, " + LUM + ");\n" +
                "  float lmr = dot(texture2D(uTexture, t + vec2(tx.x, 0.0)).rgb, " + LUM + ");\n" +
                "  float lbl = dot(texture2D(uTexture, t + vec2(-tx.x, -tx.y)).rgb, " + LUM + ");\n" +
                "  float lbm = dot(texture2D(uTexture, t + vec2(0.0, -tx.y)).rgb, " + LUM + ");\n" +
                "  float lbr = dot(texture2D(uTexture, t + vec2(tx.x, -tx.y)).rgb, " + LUM + ");\n" +
                "  float gx = (ltr + 2.0 * lmr + lbr) - (ltl + 2.0 * lml + lbl);\n" +
                "  float gy = (lbl + 2.0 * lbm + lbr) - (ltl + 2.0 * ltm + ltr);\n" +
                "  float g = clamp(sqrt(gx * gx + gy * gy), 0.0, 1.0);\n" +
                "  gl_FragColor = vec4(vec3(g), 1.0);\n" +
                "}\n";
    }

    @Override
    protected void onInit() {
        uTexelLoc = GLES20.glGetUniformLocation(program, "uTexel");
    }

    @Override
    protected void bindUniforms() {
        GLES20.glUniform2f(uTexelLoc, 1f / Math.max(width, 1), 1f / Math.max(height, 1));
    }
}
