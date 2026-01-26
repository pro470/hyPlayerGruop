package com.techphonesnews.hyPlayerGroup.Group;

import com.hypixel.hytale.codec.lookup.BuilderCodecMapCodec;
import com.techphonesnews.hyPlayerGroup.Permissions.PlayerGroupPermission;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public interface PlayerGroupMemberProvider {

    @Nonnull
    BuilderCodecMapCodec<PlayerGroupMemberProvider> CODEC = new BuilderCodecMapCodec<>("Type", true);

    Set<UUID> getPlayersFlat(Map<String, PlayerGroupGroup> groups, Map<UUID, PlayerGroupMemberProvider> groupmembers, Set<UUID> alreadyseen);

    Set<UUID> getOnlinePlayersFlat(Map<String, PlayerGroupGroup> groups, Map<UUID, PlayerGroupMemberProvider> groupmembers, Set<UUID> alreadyseen);

    List<UUID> getPlayers();

    List<UUID> getOnlinePlayers();

    Boolean wantToSetPermissions();

    PlayerGroupPermission getPermissions();

    String getGroupName();
}
