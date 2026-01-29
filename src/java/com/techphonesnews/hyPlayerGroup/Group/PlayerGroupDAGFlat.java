package com.techphonesnews.hyPlayerGroup.Group;

import javax.annotation.Nonnull;
import java.util.*;

public class PlayerGroupDAGFlat {

    private final Map<UUID, PlayerGroupGroupData> groups;

    private final PlayerGroupPlayerData players;

    public PlayerGroupDAGFlat(@Nonnull Map<UUID, PlayerGroupGroupData> groups, @Nonnull PlayerGroupPlayerData players) {
        this.groups = Map.copyOf(groups);
        this.players = players;
    }

    public PlayerGroupGroupData getGroup(@Nonnull UUID id) {
        return groups.get(id);
    }

    public Set<String> getPlayerGroups(@Nonnull UUID id) {

        HashSet<String> groupNames = new HashSet<>();
        for (UUID uuid : players.playersGroups().get(id)) {
            PlayerGroupGroupData playerGroupGroupData = groups.get(uuid);
            if (playerGroupGroupData != null) {
                groupNames.add(playerGroupGroupData.name());
            }
        }
        return Collections.unmodifiableSet(groupNames);
    }

    public Collection<PlayerGroupGroupData> getGroups() {
        return groups.values();
    }

    public Set<String> getPlayerPermissions(UUID player) {
        return players.playersPermissions().get(player);
    }

    public Set<String> getGroupPermissions(String group) {
        for (PlayerGroupGroupData playerGroupGroupData : groups.values()) {
            if (playerGroupGroupData.name().equals(group)) {
                return playerGroupGroupData.permissions();
            }
        }
        return null;

    }
}
