package me.indian.broadcast.config;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;

public class ExtensionConfig extends OkaeriConfig {

    @Comment("Configuration for session")
    private SessionConfig session = new SessionConfig();

    @Comment({""})
    @Comment("Configuration for friend synchronization")
    private FriendSyncConfig friendSync = new FriendSyncConfig();


    public SessionConfig getSession() {
        return this.session;
    }

    public FriendSyncConfig getFriendSync() {
        return this.friendSync;
    }

}