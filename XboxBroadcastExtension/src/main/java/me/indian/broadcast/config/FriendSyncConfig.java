package me.indian.broadcast.config;


import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;

public class FriendSyncConfig extends OkaeriConfig {

    @Comment({""})
    @Comment({"The amount of time in seconds to check for follower changes"})
    @Comment({"This can be no lower than 20 due to xbox rate limits"})
    @Comment({"unless you turn off auto-unfollow which then you can use 10"})
    private int updateInterval = 20;

    @Comment({""})
    @Comment({"Should we automatically follow people that follow us"})
    private boolean autoFollow = false;

    @Comment({""})
    @Comment({"Should we automatically unfollow people that no longer follow us"})
    private boolean autoUnfollow = false;


    public int getUpdateInterval() {
        return this.updateInterval;
    }

    public boolean isAutoFollow() {
        return this.autoFollow;
    }

    public boolean isAutoUnfollow() {
        return this.autoUnfollow;
    }
}