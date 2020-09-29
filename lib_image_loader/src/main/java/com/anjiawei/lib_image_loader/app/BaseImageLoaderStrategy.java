package com.anjiawei.lib_image_loader.app;

import android.app.Notification;
import android.content.Context;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RemoteViews;

public interface BaseImageLoaderStrategy {
    /**
     * 为ImageView 加载图片
     *
     * @param imageView
     * @param url
     */
    void displayImageForView(ImageView imageView, String url);

    /**
     * 为ImageView加载圆形图片
     */
    void displayImageForCircle(ImageView imageView, String url);

    /**
     * 为ViewGroup 设置背景图片
     * @param group
     * @param url
     */
    void displayImageForViewGroup(final ViewGroup group, String url);

    /**
     * 为notification加载图片
     *
     * @param context
     * @param rv
     * @param id
     * @param notification
     * @param NOTIFICATION_ID
     * @param url
     */
    void displayImageForNotification(Context context, RemoteViews rv, int id,
                                     Notification notification, int NOTIFICATION_ID, String url);

}
