package com.techphonesnews.hyPlayerGroup.simTest;

import org.apache.commons.rng.simple.RandomSource;
import org.apache.commons.rng.UniformRandomProvider;

import java.util.*;
import java.util.stream.Collectors;

public final class SimulationState {

    final Set<String> aliveGroups = new HashSet<>();

    final Set<String> knownGroups = new HashSet<>();

    final Set<UUID> players = new HashSet<>();

    static final int MAX_ALIVE_GROUPS = 150;
    static final double ALIVE_RATIO = 0.6;

    final UniformRandomProvider random;

    public SimulationState() {
        this.random = RandomSource.XO_RO_SHI_RO_128_PP.create();
    }

    public SimulationState(long seed) {
        this.random = RandomSource.XO_RO_SHI_RO_128_PP.create(seed);
    }

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
        List<String> deadGroups = knownGroups.stream()
                .filter(g -> !aliveGroups.contains(g))
                .toList();

        if (deadGroups.isEmpty()) return null;

        int idx = random.nextInt(deadGroups.size());
        return deadGroups.get(idx);
    }

    String randomGroup() {
        List<String> list = new ArrayList<>(knownGroups);
        return list.get(random.nextInt(list.size()));
    }

    UUID randomPlayer() {
        List<UUID> list = new ArrayList<>(players);
        return list.get(random.nextInt(list.size()));
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
