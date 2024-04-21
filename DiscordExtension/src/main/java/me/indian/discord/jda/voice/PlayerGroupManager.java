package me.indian.discord.jda.voice;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.dv8tion.jda.api.entities.Member;

public class PlayerGroupManager {

    private final Map<String, VoiceChatMember> voiceChatMembers = new HashMap<>();
    private final Map<String, Group> groupMap = new HashMap<>();

    public void addVoiceChatMember(final VoiceChatMember voiceChatMember) {
        this.voiceChatMembers.put(voiceChatMember.getName(), voiceChatMember);
    }

    public void removeVoiceChatMember(final VoiceChatMember voiceChatMember) {
        this.voiceChatMembers.remove(voiceChatMember.getName());
    }

    public List<Group> createGroups(final int maxDistance) {
        if (!this.voiceChatMembers.isEmpty()) {
            for (final VoiceChatMember voiceChatMember : this.voiceChatMembers.values()) {
                this.removeFromGroups(voiceChatMember);
                final Group group = this.findOrCreateGroup(voiceChatMember, maxDistance);
                group.addVoiceMember(voiceChatMember);
                this.groupMap.put(group.getId(), group);
            }
        }
        return this.groupMap.values().stream().toList();
    }

    public void removeGroup(final Group group) {
        this.groupMap.remove(group.getId());
    }

    public void removeFromGroups(final VoiceChatMember voiceChatMember) {
        for (final Group group : this.groupMap.values()) {
            if (group.getVoiceChatMembers().contains(voiceChatMember)) {
                group.removeVoiceMember(voiceChatMember);
            }
        }
    }

    public VoiceChatMember getVoiceChatMemberByMember(final Member member) {
        return this.voiceChatMembers.values().stream()
                .filter(voiceChatMember -> voiceChatMember.getMember().getIdLong() == member.getIdLong())
                .findFirst().orElse(null);
    }

    private Group findOrCreateGroup(final VoiceChatMember voiceChatMember, final int maxDistance) {
        for (final Group group : this.groupMap.values()) {
            if (this.isWithinDistance(voiceChatMember, group.getVoiceChatMembers(), maxDistance)) {
                return group;
            }
        }
        final Group newGroup = new Group(UUID.randomUUID().toString());
        this.groupMap.put(newGroup.getId(), newGroup);
        return newGroup;
    }

    private boolean isWithinDistance(final VoiceChatMember voiceChatMember, final List<VoiceChatMember> voiceChatMembers, final int maxDistance) {
        for (final VoiceChatMember member : voiceChatMembers) {
            if (member == null || voiceChatMember == null) continue;

            final double distance = this.calculateDistance(voiceChatMember, member);
            if (distance <= maxDistance) {
                return true;
            }
        }
        return false;
    }

    private double calculateDistance(final VoiceChatMember voiceChatMember1, final VoiceChatMember voiceChatMember2) {
        final double xDistance = voiceChatMember1.getX() - voiceChatMember2.getX();
        final double yDistance = voiceChatMember1.getY() - voiceChatMember2.getY();
        final double zDistance = voiceChatMember1.getZ() - voiceChatMember2.getZ();

        return Math.sqrt(xDistance * xDistance + yDistance * yDistance + zDistance * zDistance);
    }

    public Map<String, Group> getGroupMap() {
        return this.groupMap;
    }
}