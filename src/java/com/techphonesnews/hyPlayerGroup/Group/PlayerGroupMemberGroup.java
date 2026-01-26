package com.techphonesnews.hyPlayerGroup.Group;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.techphonesnews.hyPlayerGroup.Permissions.PlayerGroupPermission;

import java.util.*;

public class PlayerGroupMemberGroup implements PlayerGroupMemberProvider {
    public static final BuilderCodec<PlayerGroupMemberGroup> CODEC;
    private String groupName;

    @Override
    public Set<UUID> getPlayersFlat(Map<String, PlayerGroupGroup> groups, Map<UUID, PlayerGroupMemberProvider> groupmembers, Set<UUID> alreadyseen) {
        PlayerGroupGroup playergroup = groups.get(groupName);
        if (playergroup == null) {
            return Collections.emptySet();
        }
        Set<UUID> players = new HashSet<>(playergroup.members());
        for (UUID uuid : playergroup.groupMembers()) {
            if (alreadyseen.contains(uuid)) {
                continue;
            }
            alreadyseen.add(uuid);
            PlayerGroupMemberProvider playergroupmember = groupmembers.get(uuid);
            players.addAll(playergroupmember.getPlayersFlat(groups, groupmembers, alreadyseen));
        }
        return players;
    }

    @Override
    public Set<UUID> getOnlinePlayersFlat(Map<String, PlayerGroupGroup> groups, Map<UUID, PlayerGroupMemberProvider> groupmembers, Set<UUID> alreadyseen) {
        return Set.of();
    }

    @Override
    public List<UUID> getPlayers() {
        return List.of();
    }

    @Override
    public List<UUID> getOnlinePlayers() {
        return List.of();
    }

    @Override
    public Boolean wantToSetPermissions() {
        return null;
    }

    @Override
    public PlayerGroupPermission getPermissions() {
        return null;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    static {
        CODEC = BuilderCodec.builder(PlayerGroupMemberGroup.class, PlayerGroupMemberGroup::new).append(
                        new KeyedCodec<String>("GroupName", Codec.STRING),
                        PlayerGroupMemberGroup::setGroupName,
                        PlayerGroupMemberGroup::getGroupName
                ).add()
                .build();
    }
}
