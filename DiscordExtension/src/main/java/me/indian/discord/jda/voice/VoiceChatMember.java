package me.indian.discord.jda.voice;

import net.dv8tion.jda.api.entities.Member;

public class VoiceChatMember {

    private final String name;
    private final Member member;

    private double x, y, z;

    public VoiceChatMember(final String name, final Member member, final double x, final double y, final double z) {
        this.name = name;
        this.member = member;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public String getName() {
        return this.name;
    }

    public Member getMember() {
        return this.member;
    }

    public double getX() {
        return this.x;
    }

    public void setX(final double x) {
        this.x = x;
    }

    public double getY() {
        return this.y;
    }

    public void setY(final double y) {
        this.y = y;
    }

    public double getZ() {
        return this.z;
    }

    public void setZ(final double z) {
        this.z = z;
    }

    @Override
    public String toString() {
        return "VoiceChatMember{" +
                "name='" + this.name + '\'' +
                ", memberId=" + this.member.getId() +
                ", position=(" + this.x + ", " + this.y + ", " + this.z + ')' +
                '}';
    }
}