package com.techphonesnews.hyPlayerGroup.Permissions;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.map.MapCodec;
import com.hypixel.hytale.codec.codecs.set.SetCodec;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerGroupPermission {
    public static final BuilderCodec<PlayerGroupPermission> CODEC;

    private Set<String> permissions = new HashSet<>();
    private final Map<UUID, Integer> rank = new ConcurrentHashMap<>();

    public PlayerGroupPermission(Set<String> permissions, Map<UUID, Integer> rank) {
        this.rank.putAll(rank);
        this.permissions = permissions;
    }

    public PlayerGroupPermission() {
    }

    public int getRank(UUID uuid) {
        return this.rank.get(uuid);
    }

    public void setRank(UUID uuid, int rank) {
        this.rank.put(uuid, rank);
    }

    public void removeRank(UUID uuid) {
        this.rank.remove(uuid);
    }

    public Set<UUID> getgroups() {
        return Collections.unmodifiableSet(rank.keySet());
    }

    public Set<String> getPermissions() {
        return this.permissions;
    }

    public Boolean hasPermission(Set<String> permission) {
        return this.permissions.containsAll(permission);
    }

    public Boolean hasRank(UUID uuid, int rank) {
        return this.rank.get(uuid) < rank;
    }

    static {

        CODEC = BuilderCodec.builder(PlayerGroupPermission.class, PlayerGroupPermission::new).append(
                        new KeyedCodec<Set<String>>("Permissions", new SetCodec(Codec.STRING, HashSet<String>::new, false)),
                        (obj, val) -> obj.permissions = val,
                        (obj) -> obj.permissions
                ).add()
                .append(
                        new KeyedCodec<Map<String, Integer>>("Rank", new MapCodec<Integer, Map<String, Integer>>(Codec.INTEGER, ConcurrentHashMap::new)),
                        (obj, val) -> {
                            for (Map.Entry<String, Integer> entry : val.entrySet()) {
                                obj.rank.put(UUID.fromString(entry.getKey()), entry.getValue());
                            }
                        },
                        (obj) -> {
                            Map<String, Integer> map = new HashMap<>();
                            for (Map.Entry<UUID, Integer> entry : obj.rank.entrySet()) {
                                map.put(entry.getKey().toString(), entry.getValue());
                            }
                            return map;
                        }
                ).add()
                .build();

    }

}
