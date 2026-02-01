package com.techphonesnews.hyPlayerGroup.Group;

import javax.annotation.Nonnull;
import java.util.*;

public record PlayerGroupDAGFlat(Map<UUID, PlayerGroupGroupData> groups, Map<String, UUID> groupsByName,
                                 PlayerGroupPlayerData players) {

    public PlayerGroupDAGFlat(@Nonnull Map<UUID, PlayerGroupGroupData> groups, @Nonnull Map<String, UUID> groupsByName, @Nonnull PlayerGroupPlayerData players) {
        this.groups = Collections.unmodifiableMap(groups);
        this.groupsByName = Collections.unmodifiableMap(groupsByName);
        this.players = players;
    }

    public PlayerGroupGroupData getGroup(@Nonnull String name) {
        UUID id = groupsByName.get(name);
        if (id != null) {
            return groups.get(id);
        }
        return null;
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
        UUID id = groupsByName.get(group);
        if (id != null) {
            return groups.get(id).permissions();
        }
        return Set.of();

    }
}
