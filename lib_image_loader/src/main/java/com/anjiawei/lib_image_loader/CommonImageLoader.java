package com.example.moduleimageload;

import android.app.Activity;
import android.content.Context;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CommonImageLoader {
    private static Map<Integer, Keeper> mKeepers = new HashMap<>();
    private static volatile CommonImageLoader mInstance;

    private CommonImageLoader() {
    }

    public static CommonImageLoader getInstance() {
        if (mInstance == null) {
            synchronized (CommonImageLoader.class) {
                if (mInstance == null) {
                    mInstance = new CommonImageLoader();
                }
            }
        }
        return mInstance;
    }

    // 创建新的keeper
    public void addGlideRequests(@NonNull Fragment fragment) {
        //  避免重复创建
        if (mKeepers.get(fragment.hashCode()) != null) {
            return;
        }

        Keeper keeper = new Keeper(fragment);
        mKeepers.put(keeper.key, keeper);
    }

    // 创建新的Keeper
    public void addGlideRequests(@NonNull Activity activity) {
        //  避免重复创建
        if (mKeepers.get(activity.hashCode()) != null) {
            return;
        }

        Keeper keeper = new Keeper(activity);
        mKeepers.put(keeper.key, keeper);
    }

    //hashCode 为 iHashCode 的对象需要使用图像加载功能
    public void iNeedLoadImageFunction(@NonNull Fragment fragment, int iHashCode) {
        //  查找到相应的Keeper，存储对象的hashCode
        Keeper keeper = mKeepers.get(fragment.hashCode());
        if (keeper == null) {
            //  错误抛出，说明fragment没有创建对应Keeper
            throw new IllegalArgumentException();
        }
        keeper.values.add(iHashCode);
    }

    // hashCode 为 iHashCode 的对象需要使用图像加载功能
    public void iNeedLoadImageFunction(@NonNull Activity activity, int iHashCode) {
        Keeper keeper = mKeepers.get(activity.hashCode());
        if (keeper == null) {
            //  错误抛出，说明activity没有创建对应Keeper
            throw new IllegalArgumentException();
        }
        keeper.values.add(iHashCode);
    }

    private RequestManager getGlideRequests(int hashCode) {
        for (Keeper keeper : mKeepers.values()) {
            if (keeper.values.contains(hashCode)) {
                return keeper.glideRequest;
            }
        }
        return Glide.with(MyApplication.getInstance());
    }


    public void displayImage(Object object, String uri, ImageView imageView) {
        displayImage(getGlideRequests(object.hashCode()),uri, imageView);
    }

    public void displayImage(Context context, String uri, ImageView imageView) {
        displayImage(Glide.with(context), uri, imageView);
        Glide.get(context).clearMemory();
    }

    private void displayImage(RequestManager requestManager, String uri, ImageView imageView) {
        requestManager.load(uri)
                .error(-1)
                .placeholder(-1)
                .dontAnimate()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imageView);
    }

    private static class Keeper {
        int key;
        RequestManager glideRequest;
        Set<Integer> values;

        public Keeper(@NonNull Activity activity) {
            key = activity.hashCode();
            values = new HashSet<>();
            values.add(activity.hashCode());
            glideRequest = Glide.with(activity);
        }

        public Keeper(@NonNull Fragment fragment) {
            key = fragment.hashCode();
            values = new HashSet<>();
            values.add(fragment.hashCode());
            glideRequest = Glide.with(fragment);
        }
    }
}
