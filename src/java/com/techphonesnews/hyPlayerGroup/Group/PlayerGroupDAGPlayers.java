package com.techphonesnews.hyPlayerGroup.Group;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.map.MapCodec;
import com.hypixel.hytale.codec.codecs.set.SetCodec;

import java.util.*;

final class PlayerGroupDAGPlayers {
    public static final Codec<PlayerGroupDAGPlayers> CODEC;
    private final Map<UUID, Set<UUID>> playersGroups = new HashMap<>();
    private final Map<UUID, Set<String>> playersPermissions = new HashMap<>();

    public Map<UUID, Set<UUID>> playersGroups() {
        return playersGroups;
    }

    public Map<UUID, Set<String>> playersPermissions() {
        return playersPermissions;
    }

    static {
        CODEC = BuilderCodec.builder(PlayerGroupDAGPlayers.class, PlayerGroupDAGPlayers::new).append(
                        new KeyedCodec<Map<String, Set<UUID>>>(
                                "PlayersGroups",
                                new MapCodec<Set<UUID>, Map<String, Set<UUID>>>(
                                        new SetCodec<>(
                                                Codec.UUID_STRING,
                                                HashSet::new,
                                                false
                                        ),
                                        HashMap::new,
                                        false
                                )
                        ),
                        (players, playersGroups) -> {
                            for (Map.Entry<String, Set<UUID>> entry : playersGroups.entrySet()) {
                                players.playersGroups().put(UUID.fromString(entry.getKey()), entry.getValue());
                            }

                        },
                        (players) -> {
                            Map<String, Set<UUID>> mutPlayersGroups = new HashMap<>();
                            for (Map.Entry<UUID, Set<UUID>> entry : players.playersGroups().entrySet()) {
                                mutPlayersGroups.put(entry.getKey().toString(), entry.getValue());
                            }
                            return mutPlayersGroups;
                        }
                ).add()
                .append(
                        new KeyedCodec<Map<String, Set<String>>>("PlayersPermissions", new MapCodec<Set<String>, Map<String, Set<String>>>(
                                new SetCodec<>(
                                        Codec.STRING,
                                        HashSet::new,
                                        false
                                ),
                                HashMap::new,
                                false
                        )
                        ),
                        (players, playersPermissions) -> {
                            for (Map.Entry<String, Set<String>> entry : playersPermissions.entrySet()) {
                                players.playersPermissions().put(UUID.fromString(entry.getKey()), entry.getValue());
                            }

                        },
                        (players) -> {
                            Map<String, Set<String>> mutPlayersPermissions = new HashMap<>();
                            for (Map.Entry<UUID, Set<String>> entry : players.playersPermissions().entrySet()) {
                                mutPlayersPermissions.put(entry.getKey().toString(), entry.getValue());
                            }
                            return mutPlayersPermissions;
                        }

                ).add()
                .build();
    }
}
