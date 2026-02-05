package com.techphonesnews.hyPlayerGroup.Group;

import java.util.*;

public class PlayerGroupPlayerData {

    public final Map<UUID, Set<UUID>> playersGroups;

    public final Map<UUID, Set<String>> playersPermissions;

    public Map<UUID, Set<UUID>> playersGroups() {
        return playersGroups;
    }

    public Map<UUID, Set<String>> playersPermissions() {
        return playersPermissions;
    }

    private PlayerGroupPlayerData(Map<UUID, Set<UUID>> playersGroups, Map<UUID, Set<String>> playersPermissions) {
        this.playersGroups = Collections.unmodifiableMap(playersGroups);
        this.playersPermissions = Collections.unmodifiableMap(playersPermissions);
    }

    //TODO: optimise this hell
    static PlayerGroupPlayerData PlayerGroupPlayerDataPlayerGroups(Map<UUID, Set<UUID>> playersGroups, PlayerGroupDAGFlat flat, Set<UUID> affected) {
        Map<UUID, Set<UUID>> mutPlayersGroups = new HashMap<>();
        for (Map.Entry<UUID, Set<UUID>> entry : playersGroups.entrySet()) {
            UUID player = entry.getKey();
            Set<UUID> groups;
            boolean affectedPlayer = false;
            Set<UUID> playerGroups = flat.players().playersGroups().get(player);
            if (playerGroups != null) {
                for (UUID group : affected) {
                    if (entry.getValue().contains(group) || playerGroups.contains(group)) {
                        affectedPlayer = true;
                        break;
                    }
                }
            } else {
                affectedPlayer = true;
            }
            if (affectedPlayer) {
                groups = Set.copyOf(entry.getValue());
            } else {
                groups = flat.players().playersGroups().get(player);
            }
            mutPlayersGroups.put(entry.getKey(), groups);
        }
        return new PlayerGroupPlayerData(Collections.unmodifiableMap(mutPlayersGroups), flat.players().playersPermissions());
    }

    static PlayerGroupPlayerData PlayerGroupPlayerDataPlayersPermissions
            (Map<UUID, Set<String>> playersPermissions, PlayerGroupDAGFlat flat, Set<UUID> affected) {
        Map<UUID, Set<String>> mutPlayersPermissions = new HashMap<>();
        for (Map.Entry<UUID, Set<String>> entry : playersPermissions.entrySet()) {
            UUID player = entry.getKey();
            Set<String> groups;
            if (affected.contains(player)) {
                groups = Set.copyOf(entry.getValue());
            } else {
                groups = flat.players().playersPermissions().get(player);
            }
            mutPlayersPermissions.put(entry.getKey(), groups);
        }
        return new PlayerGroupPlayerData(flat.players().playersGroups(), Collections.unmodifiableMap(mutPlayersPermissions));
    }

    public static PlayerGroupPlayerData NewPlayerGroupPlayerData
            (Map<UUID, Set<UUID>> playersGroups, Map<UUID, Set<String>> playersPermissions) {
        Map<UUID, Set<UUID>> mutPlayersGroups = new HashMap<>();
        Map<UUID, Set<String>> mutPlayersPermissions = new HashMap<>();
        for (Map.Entry<UUID, Set<UUID>> entry : playersGroups.entrySet()) {
            mutPlayersGroups.put(entry.getKey(), Set.copyOf(entry.getValue()));
        }
        for (Map.Entry<UUID, Set<String>> entry : playersPermissions.entrySet()) {
            mutPlayersPermissions.put(entry.getKey(), Set.copyOf(entry.getValue()));
        }
        return new PlayerGroupPlayerData(Collections.unmodifiableMap(mutPlayersGroups), Collections.unmodifiableMap(mutPlayersPermissions));

    }
}
