package com.anjiawei.lib_image_loader;

import android.app.Notification;
import android.content.Context;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RemoteViews;

import com.anjiawei.lib_image_loader.app.BaseImageLoaderStrategy;
import com.anjiawei.lib_image_loader.app.GlideImageLoaderStrategy;

public class ImageLoader {
    private BaseImageLoaderStrategy mStrategy;

    private ImageLoader() {
        mStrategy = new GlideImageLoaderStrategy();
    }

    private static class InstanceHolder {
        private static ImageLoader sInstance = new ImageLoader();
    }

    public static ImageLoader getInstance() {
        return InstanceHolder.sInstance;
    }

    public void displayImageForView(ImageView imageView, String url) {
        mStrategy.displayImageForView(imageView, url);
    }

    public void displayImageForCircle(ImageView imageView, String url) {
        mStrategy.displayImageForCircle(imageView, url);
    }

    public void displayImageForViewGroup(ViewGroup group, String url) {
        mStrategy.displayImageForViewGroup(group, url);
    }

    public void displayImageForNotification(Context context, RemoteViews rv,
                                            int id, Notification notification,
                                            int NOTIFICATION_ID, String url) {
        mStrategy.displayImageForNotification(context, rv, id, notification, NOTIFICATION_ID, url);
    }
}
