package com.example.prismsomething.gl;

import com.example.prismsomething.R;

public enum FilterType {
    NONE(R.string.filter_none, false),
    GRAYSCALE(R.string.filter_grayscale, false),
    INVERT(R.string.filter_invert, false),
    COLOR_ADJUST(R.string.filter_adjust, true),
    GAMMA(R.string.filter_gamma, true),
    BLUR(R.string.filter_blur, true),
    SHARPEN(R.string.filter_sharpen, true),
    SOBEL(R.string.filter_sobel, false),
    POSTERIZE(R.string.filter_posterize, true);

    public final int nameRes;
    public final boolean hasParam;

    FilterType(int nameRes, boolean hasParam) {
        this.nameRes = nameRes;
        this.hasParam = hasParam;
    }
}
