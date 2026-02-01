package com.techphonesnews.hyPlayerGroup.simTest;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

public final class SimulationState {

    final Set<String> groups = new HashSet<>();
    final Set<UUID> players = new HashSet<>();

    final Random random = new Random();

    String randomGroup() {
        return groups.stream()
                .skip(random.nextInt(groups.size()))
                .findFirst()
                .orElseThrow();
    }

    UUID randomPlayer() {
        return players.stream()
                .skip(random.nextInt(players.size()))
                .findFirst()
                .orElseThrow();
    }

    UUID getOrCreateRandomPlayer() {
        // 70 % existierenden Player nehmen, wenn möglich
        if (!(players.size() > 200) && (players.isEmpty() || random.nextDouble() < 0.5)) {
            // sonst neuen Player erzeugen
            UUID player = UUID.randomUUID();
            players.add(player);
            return player;
        }

        return randomPlayer();
    }

    Set<String> randomPermissionSet() {
        int size = 1 + random.nextInt(3);
        Set<String> set = new HashSet<>();
        for (int i = 0; i < size; i++) {
            set.add("perm." + random.nextInt(20));
        }
        return set;
    }
}
