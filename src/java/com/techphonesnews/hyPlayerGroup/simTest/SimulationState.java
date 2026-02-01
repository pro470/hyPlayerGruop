package com.techphonesnews.hyPlayerGroup.simTest;

import java.util.*;
import java.util.stream.Collectors;

public final class SimulationState {

    // Gruppen, die aktuell wirklich im DAG existieren
    final Set<String> aliveGroups = new HashSet<>();

    // Alle Gruppen-IDs, die jemals existiert haben oder absichtlich "null" sind
    final Set<String> knownGroups = new HashSet<>();

    final Set<UUID> players = new HashSet<>();

    static final int MAX_ALIVE_GROUPS = 150;
    static final double ALIVE_RATIO = 0.6; // 60 % alive, 40 % null

    final Random random = new Random();

    boolean canCreateGroup() {
        return aliveGroups.size() < MAX_ALIVE_GROUPS;
    }

    int maxKnownGroups() {
        return (int) Math.ceil(aliveGroups.size() / ALIVE_RATIO);
    }

    void onGroupCreated(String name) {
        aliveGroups.add(name);
        knownGroups.add(name);
        rebalanceKnownGroups();
    }

    void onGroupDisbandAttempt(String name) {
        aliveGroups.remove(name);
        rebalanceKnownGroups();
    }

    private void rebalanceKnownGroups() {
        int maxKnown = maxKnownGroups();

        while (knownGroups.size() > maxKnown) {
            String dead = randomDeadGroup();
            if (dead == null) {
                break;
            }
            knownGroups.remove(dead);
        }
    }

    private String randomDeadGroup() {
        return knownGroups.stream()
                .filter(g -> !aliveGroups.contains(g))
                .skip(random.nextInt(
                        Math.max(1,
                                knownGroups.size() - aliveGroups.size())))
                .findFirst()
                .orElse(null);
    }
    /* -----------------------------
       Random Auswahl (BLIND)
     ----------------------------- */

    String randomGroup() {
        return knownGroups.stream()
                .skip(random.nextInt(knownGroups.size()))
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
        if (!(players.size() > 400) && (players.isEmpty() || random.nextDouble() < 0.5)) {
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
