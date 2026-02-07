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

    static PlayerGroupPlayerData PlayerGroupPlayerDataPlayerGroups(Map<UUID, Set<UUID>> playersGroups, PlayerGroupDAGFlat flat, Set<UUID> affected) {
        Map<UUID, Set<UUID>> mutPlayersGroups = new HashMap<>();
        for (Map.Entry<UUID, Set<UUID>> entry : playersGroups.entrySet()) {
            Set<UUID> groups = getGroupPlayerGroups(entry, flat, affected);
            mutPlayersGroups.put(entry.getKey(), groups);
        }
        return new PlayerGroupPlayerData(Collections.unmodifiableMap(mutPlayersGroups), flat.players().playersPermissions());
    }

    static PlayerGroupPlayerData PlayerGroupPlayerDataPlayersPermissions
            (Map<UUID, Set<String>> playersPermissions, PlayerGroupDAGFlat flat, Set<UUID> affected) {
        Map<UUID, Set<String>> mutPlayersPermissions = new HashMap<>();
        for (Map.Entry<UUID, Set<String>> entry : playersPermissions.entrySet()) {
            Set<String> groups = getGroupPermissions(entry, flat, affected);
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

    public static PlayerGroupPlayerData NewPlayerGroupPlayerDataWithCache(Map<UUID, Set<UUID>> playersGroups, Map<UUID, Set<String>> playersPermissions, PlayerGroupDAGFlat flat, Set<UUID> affectedGroups, Set<UUID> affectedPermissions) {
        Map<UUID, Set<UUID>> mutPlayersGroups = new HashMap<>();
        Map<UUID, Set<String>> mutPlayersPermissions = new HashMap<>();
        for (Map.Entry<UUID, Set<UUID>> entry : playersGroups.entrySet()) {
            Set<UUID> groups = getGroupPlayerGroups(entry, flat, affectedGroups);
            mutPlayersGroups.put(entry.getKey(), groups);
        }
        for (Map.Entry<UUID, Set<String>> entry : playersPermissions.entrySet()) {
            Set<String> groups = getGroupPermissions(entry, flat, affectedPermissions);
            mutPlayersPermissions.put(entry.getKey(), groups);
        }
        return new PlayerGroupPlayerData(Collections.unmodifiableMap(mutPlayersGroups), Collections.unmodifiableMap(mutPlayersPermissions));
    }

    private static Set<String> getGroupPermissions(Map.Entry<UUID, Set<String>> entry, PlayerGroupDAGFlat flat, Set<UUID> affectedPermissions) {
        UUID player = entry.getKey();
        Set<String> groups;
        if (affectedPermissions.contains(player)) {
            groups = Set.copyOf(entry.getValue());
        } else {
            groups = flat.players().playersPermissions().get(player);
        }
        return groups;
    }

    //TODO: optimise this hell
    private static Set<UUID> getGroupPlayerGroups(Map.Entry<UUID, Set<UUID>> entry, PlayerGroupDAGFlat flat, Set<UUID> affectedGroups) {

        UUID player = entry.getKey();
        Set<UUID> groups;
        boolean affectedPlayer = false;
        Set<UUID> playerGroups = flat.players().playersGroups().get(player);
        if (playerGroups != null) {
            for (UUID group : affectedGroups) {
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
        return groups;
    }
}
