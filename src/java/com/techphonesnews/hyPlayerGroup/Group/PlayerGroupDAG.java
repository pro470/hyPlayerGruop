package com.techphonesnews.hyPlayerGroup.Group;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.map.MapCodec;
import com.techphonesnews.hyPlayerGroup.Permissions.PlayerGroupPermissionsProvider;

import java.util.*;

public final class PlayerGroupDAG {
    public static String name = "PlayerGroupDAG";
    private final Map<UUID, PlayerGroupDAGGroup> groups = new HashMap<>();
    private final PlayerGroupDAGPlayers players = new PlayerGroupDAGPlayers();

    public static final BuilderCodec<PlayerGroupDAG> CODEC;

    public void addGroup(UUID id) {
        groups.put(id, new PlayerGroupDAGGroup(id));
    }

    public UUID addGroup() {
        UUID id = UUID.randomUUID();
        addGroup(id);
        return id;
    }

    public PlayerGroupDAGGroup getGroup(UUID id) {
        return groups.get(id);
    }

    public void removeGroup(UUID id) {
        groups.remove(id);
    }

    public void addPermission(UUID id, String permission) {
        groups.get(id).addPermission(permission);
    }

    public void removePermission(UUID id, String permission) {
        groups.get(id).removePermission(permission);
    }

    public void addMember(UUID groupId, UUID uuid) {
        groups.get(groupId).addMember(uuid);
    }

    public void addParent(UUID ParentId, UUID ChildId) {
        if (ParentId.equals(ChildId)) {
            return;
        }

        if (createsCycle(ParentId, ChildId)) {
            return;
        }

        groups.get(ChildId).addParent(ParentId);
        groups.get(ParentId).addChild(ChildId);
    }

    private Boolean createsCycle(UUID ParentId, UUID ChildId) {
        return dfs(
                ChildId,
                new HashSet<>(),
                group -> groups.get(group).children(),
                group -> group.equals(ParentId)
        );
    }

    private static Set<UUID> createAncestorSet(UUID id, PlayerGroupDAG dag) {
        Set<UUID> ancestors = new HashSet<>();
        dfs(
                id,
                new HashSet<>(),
                group -> dag.groups.get(group).parents(),
                ancestors::add
        );
        return ancestors;
    }

    private static Set<UUID> createDescendantSet(UUID id, PlayerGroupDAG dag) {
        Set<UUID> descendants = new HashSet<>();
        dfs(
                id,
                new HashSet<>(),
                group -> dag.groups.get(group).children(),
                descendants::add
        );
        return descendants;
    }

    private static Set<String> createPermissionSet(UUID id, PlayerGroupDAG dag) {
        Set<String> permissions = new HashSet<>();
        dfs(
                id,
                new HashSet<>(),
                group -> dag.groups.get(group).parents(),
                group -> permissions.addAll(dag.groups.get(group).permissions())
        );
        return permissions;
    }

    private static Boolean dfs(
            UUID id,
            Set<UUID> visited,
            NextProvider next,
            VisitConsumer consumer
    ) {
        Deque<UUID> queue = new ArrayDeque<>();
        queue.add(id);

        while (!queue.isEmpty()) {
            UUID current = queue.pop();

            if (!visited.add(current)) continue;

            if (consumer.visit(current)) return true;

            Collection<UUID> nextNodes = next.next(current);
            if (nextNodes == null) continue;
            queue.addAll(nextNodes);
        }

        return false;
    }

    public static PlayerGroupDAGFlat buildFlat(PlayerGroupDAG dag) {
        Map<UUID, PlayerGroupGroupData> groups = new HashMap<>();

        for (PlayerGroupDAGGroup group : dag.groups.values()) {
            groups.put(group.id(), new PlayerGroupGroupData(
                    group.id(),
                    group.name(),
                    createAncestorSet(group.id(), dag),
                    createDescendantSet(group.id(), dag),
                    group.members(),
                    createPermissionSet(group.id(), dag)
            ));
        }

        return new PlayerGroupDAGFlat(groups, new PlayerGroupPlayerData(dag.players.playersGroups(), dag.players.playersPermissions()));
    }

    static {
        CODEC = BuilderCodec.builder(PlayerGroupDAG.class, PlayerGroupDAG::new).append(
                        new KeyedCodec<Map<String, PlayerGroupDAGGroup>>(
                                "Groups",
                                new MapCodec<PlayerGroupDAGGroup, Map<String, PlayerGroupDAGGroup>>(
                                        PlayerGroupDAGGroup.CODEC,
                                        HashMap::new,
                                        false
                                )
                        ),
                        (dag, map) -> {
                            for (Map.Entry<String, PlayerGroupDAGGroup> entry : map.entrySet()) {
                                dag.groups.put(UUID.fromString(entry.getKey()), entry.getValue());
                            }
                        },
                        (dag) -> {
                            Map<String, PlayerGroupDAGGroup> map = new HashMap<>();
                            for (Map.Entry<UUID, PlayerGroupDAGGroup> entry : dag.groups.entrySet()) {
                                map.put(entry.getKey().toString(), entry.getValue());
                            }
                            return map;
                        }
                ).add()
                .append(
                        new KeyedCodec<PlayerGroupDAGPlayers>(
                                "Players",
                                PlayerGroupDAGPlayers.CODEC
                        ),
                        (dag, players) -> {
                            dag.players.playersGroups().putAll(players.playersGroups());
                            dag.players.playersPermissions().putAll(players.playersPermissions());
                        },
                        (dag) -> dag.players
                ).add()
                .build();
    }

    @FunctionalInterface
    private interface NextProvider {
        Collection<UUID> next(UUID id);
    }

    @FunctionalInterface
    private interface VisitConsumer {
        Boolean visit(UUID id);
    }
}
