package com.example.prismsomething.gl;

public class InvertFilter extends AbstractFilter {
    @Override
    protected String getFragmentShader() {
        return "precision mediump float;\n" +
                "uniform sampler2D uTexture;\n" +
                "varying vec2 vTexCoord;\n" +
                "void main() {\n" +
                "  vec4 c = texture2D(uTexture, vTexCoord);\n" +
                "  gl_FragColor = vec4(1.0 - c.rgb, c.a);\n" +
                "}\n";
    }
}
