package com.anjiawei.lib_image_loader;

import com.anjiawei.lib_image_loader.app.BaseImageLoaderStrategy;

public interface IImageLoader extends BaseImageLoaderStrategy {
    void setImageLoaderStrategy(BaseImageLoaderStrategy strategy);
}
