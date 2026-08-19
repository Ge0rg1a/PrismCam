package com.example.prismsomething.gl;

import android.opengl.GLES20;

public class ColorAdjustFilter extends AbstractFilter {
    private int uBrightnessLoc;
    private int uContrastLoc;
    private int uSaturationLoc;
    private float brightness = 0f;
    private float contrast = 1f;
    private float saturation = 1f;

    @Override
    protected String getFragmentShader() {
        return "precision mediump float;\n" +
                "uniform sampler2D uTexture;\n" +
                "uniform float uBrightness;\n" +
                "uniform float uContrast;\n" +
                "uniform float uSaturation;\n" +
                "varying vec2 vTexCoord;\n" +
                "void main() {\n" +
                "  vec4 c = texture2D(uTexture, vTexCoord);\n" +
                "  vec3 rgb = c.rgb + uBrightness;\n" +
                "  rgb = (rgb - 0.5) * uContrast + 0.5;\n" +
                "  float l = dot(rgb, vec3(0.299, 0.587, 0.114));\n" +
                "  rgb = mix(vec3(l), rgb, uSaturation);\n" +
                "  gl_FragColor = vec4(clamp(rgb, 0.0, 1.0), c.a);\n" +
                "}\n";
    }

    @Override
    protected void onInit() {
        uBrightnessLoc = GLES20.glGetUniformLocation(program, "uBrightness");
        uContrastLoc = GLES20.glGetUniformLocation(program, "uContrast");
        uSaturationLoc = GLES20.glGetUniformLocation(program, "uSaturation");
    }

    @Override
    protected void bindUniforms() {
        GLES20.glUniform1f(uBrightnessLoc, brightness);
        GLES20.glUniform1f(uContrastLoc, contrast);
        GLES20.glUniform1f(uSaturationLoc, saturation);
    }

    @Override
    public void setParam(float t) {
        contrast = 1f + t;
        saturation = 1f + t;
        brightness = 0.1f * t;
    }
}
