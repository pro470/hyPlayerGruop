package com.techphonesnews.hyPlayerGroup.Group;

import java.util.*;

public record PlayerGroupPlayerData(Map<UUID, Set<UUID>> playersGroups, Map<UUID, Set<String>> playersPermissions) {

    public PlayerGroupPlayerData(Map<UUID, Set<UUID>> playersGroups, Map<UUID, Set<String>> playersPermissions) {
        Map<UUID, Set<UUID>> mutPlayersGroups = new HashMap<>();
        Map<UUID, Set<String>> mutPlayersPermissions = new HashMap<>();
        for (Map.Entry<UUID, Set<UUID>> entry : playersGroups.entrySet()) {
            mutPlayersGroups.put(entry.getKey(), Set.copyOf(entry.getValue()));
        }
        for (Map.Entry<UUID, Set<String>> entry : playersPermissions.entrySet()) {
            mutPlayersPermissions.put(entry.getKey(), Set.copyOf(entry.getValue()));
        }
        this.playersGroups = Collections.unmodifiableMap(mutPlayersGroups);
        this.playersPermissions = Collections.unmodifiableMap(mutPlayersPermissions);
    }
}
