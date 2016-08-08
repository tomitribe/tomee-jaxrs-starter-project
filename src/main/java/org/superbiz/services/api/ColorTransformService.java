package org.superbiz.services.api;

import org.superbiz.model.Color;

public interface ColorTransformService {

    Color toCMYK(Color rgbColor);
}
