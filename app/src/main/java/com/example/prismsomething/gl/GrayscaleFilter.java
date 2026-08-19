package com.example.prismsomething.gl;

public class GrayscaleFilter extends AbstractFilter {
    @Override
    protected String getFragmentShader() {
        return "precision mediump float;\n" +
                "uniform sampler2D uTexture;\n" +
                "varying vec2 vTexCoord;\n" +
                "void main() {\n" +
                "  vec4 c = texture2D(uTexture, vTexCoord);\n" +
                "  float l = dot(c.rgb, vec3(0.299, 0.587, 0.114));\n" +
                "  gl_FragColor = vec4(vec3(l), c.a);\n" +
                "}\n";
    }
}
