package com.anjiawei.lib_image_loader.app;

import android.app.Notification;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RemoteViews;

import com.anjiawei.lib_image_loader.R;
import com.anjiawei.lib_image_loader.image.Utils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.target.NotificationTarget;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * 图片加载类，外界唯一调用类，
 * 支持为 View， notification，appwidget加载图片
 */
public class ImageLoaderManager {
    private ImageLoaderManager() {
    }

    private static class SingletonHolder {
        private static ImageLoaderManager mInstance = new ImageLoaderManager();
    }

    public static ImageLoaderManager getInstance() {
        return SingletonHolder.mInstance;
    }

    /**
     * 为ImageView 加载图片
     *
     * @param imageView
     * @param url
     */
    public void displayImageForView(ImageView imageView, String url) {
        Glide.with(imageView.getContext())
                .asBitmap()
                .load(url)
                .apply(initCommonRequestOptions())
                .transition(BitmapTransitionOptions.withCrossFade())
                .into(imageView);
    }

    private RequestOptions initCommonRequestOptions() {
        RequestOptions options = new RequestOptions();
        options.placeholder(R.mipmap.b4y)
                .error(R.mipmap.b4y)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .skipMemoryCache(false)
                .priority(Priority.NORMAL);
        return options;
    }

    /**
     * 为ImageView加载圆形图片
     */
    public void displayImageForCircle(final ImageView imageView, String url) {
        Glide.with(imageView.getContext())
                .asBitmap()
                .load(url)
                .apply(initCommonRequestOptions())
                .into(new BitmapImageViewTarget(imageView) {
                   @Override
                   protected void setResource(Bitmap resource) {
                       RoundedBitmapDrawable circleBitmapDrawable =
                               RoundedBitmapDrawableFactory.create(imageView.getResources(), resource);
                       circleBitmapDrawable.setCircular(true);
                       imageView.setImageDrawable(circleBitmapDrawable);
                   }
                });
    }

    /**
     * 为ViewGroup 设置背景图片
     * @param group
     * @param url
     */
    public void displayImageForViewGroup(final ViewGroup group, String url) {
        Glide.with(group.getContext())
                .asBitmap()
                .load(url)
                .apply(initCommonRequestOptions())
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource,
                                                @Nullable Transition<? super Bitmap> transition) {
                        final Bitmap res = resource;
                        Observable.just(resource)
                                .map(new Function<Bitmap, Drawable>() {
                                    @Override
                                    public Drawable apply(Bitmap bitmap) {
                                        Drawable drawable =
                                                new BitmapDrawable(Utils.doBlur(res, 100, true));
                                        return drawable;
                                    }
                                })
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Consumer<Drawable>() {
                                    @Override
                                    public void accept(Drawable drawable) throws Exception {
                                        group.setBackground(drawable);
                                    }
                                });
                    }
                });
    }

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
    public void displayImageForNotification(Context context, RemoteViews rv, int id,
                                            Notification notification, int NOTIFICATION_ID, String url) {
        this.displayImageForTarget(context, initNotificationTarget(context, id, rv, notification, NOTIFICATION_ID), url);
    }

    private void displayImageForTarget(Context context, NotificationTarget target, String url) {
        Glide.with(context)
                .asBitmap()
                .load(url)
                .apply(initCommonRequestOptions())
                .transition(BitmapTransitionOptions.withCrossFade())
                .fitCenter()
                .into(target);
    }

    private NotificationTarget initNotificationTarget(Context context,
                                                      int id, RemoteViews rv,
                                                      Notification notification,
                                                      int NOTIFICATION_ID) {
        return new NotificationTarget(context, id, rv, notification, NOTIFICATION_ID);
    }
}
