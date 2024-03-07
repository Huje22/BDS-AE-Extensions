package me.indian.broadcast.config;


import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;

public class FriendSyncConfig extends OkaeriConfig {

    @Comment({""})
    @Comment({"Ilość czasu w sekundach na sprawdzanie zmian w obserwujących"})
    @Comment({"Ta wartość nie może być niższa niż 20 z powodu limitów prędkości Xboxa"})
    @Comment({"chyba że wyłączysz automatyczne przestawanie obserwowania, wtedy możesz użyć 10"})
    private int updateInterval = 40;

    @Comment({""})
    @Comment({"Czy powinniśmy automatycznie obserwować osoby, które nas obserwują"})
    private boolean autoFollow = true;

    @Comment({""})
    @Comment({"Czy powinniśmy automatycznie przestawać obserwować osoby, które nas już nie obserwują"})
    private boolean autoUnfollow = true;


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