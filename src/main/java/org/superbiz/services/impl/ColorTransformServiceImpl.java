package org.superbiz.services.impl;

import org.superbiz.model.Color;
import org.superbiz.services.api.ColorTransformService;

public class ColorTransformServiceImpl implements ColorTransformService {
    @Override
    public Color toCMYK(Color rgbColor) {
        rgbColor.setName("CMYK" + rgbColor.getName());
        return rgbColor;
    }
}
