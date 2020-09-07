package com.anjiawei.lib_audio.model;

import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.ToOne;

/**
 * 收藏表
 */

public class Favourite {
    @Id(autoincrement = true)
    Long favouriteId;
    @NotNull
    String audioId;
    //一条收藏记录唯一对应一条实体
    @ToOne(joinProperty = "audioId")
    AudioBean audioBean;

    public Favourite(Long favouriteId, @NotNull String audioId) {
        this.favouriteId = favouriteId;
        this.audioId = audioId;
    }

}
