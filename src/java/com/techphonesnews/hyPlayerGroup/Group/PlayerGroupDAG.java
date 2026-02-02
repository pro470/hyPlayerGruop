package com.techphonesnews.hyPlayerGroup.Group;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.map.MapCodec;

import java.util.*;
import java.util.function.Function;

public final class PlayerGroupDAG {
    public static String name = "PlayerGroupDAG";
    private final Map<UUID, PlayerGroupDAGGroup> groups = new HashMap<>();
    private final Map<String, UUID> groupsByName = new HashMap<>();
    private final PlayerGroupDAGPlayers players = new PlayerGroupDAGPlayers();

    public static final BuilderCodec<PlayerGroupDAG> CODEC;

    public Set<UUID> groups() {
        return groups.keySet();
    }

    public void addGroup(UUID id) {
        groups.put(id, new PlayerGroupDAGGroup(id));
    }

    public UUID addGroup(String name) {
        if (groupsByName.containsKey(name)) {
            return null;
        }
        UUID id = UUID.randomUUID();
        addGroup(id);
        groups.get(id).setName(name);
        groupsByName.put(name, id);
        return id;
    }

    public PlayerGroupDAGGroup getGroup(UUID id) {
        return groups.get(id);
    }

    public UUID getGroupId(String name) {
        return groupsByName.get(name);
    }

    public void removeGroup(String name) {
        UUID id = groupsByName.get(name);
        if (id != null) {
            removeGroup(id);
        }
    }

    public void removeGroup(UUID id) {
        PlayerGroupDAGGroup group = groups.remove(id);
        if (group != null) {
            groupsByName.remove(group.name());
            for (UUID uuid : group.members()) {
                Set<UUID> member = players.playersGroups().get(uuid);
                if (member != null) {
                    member.remove(id);
                }
            }
            for (UUID uuid : group.parents()) {
                Set<UUID> parent = groups.get(uuid).children();
                if (parent != null) {
                    parent.remove(id);
                }
            }
            for (UUID uuid : group.children()) {
                Set<UUID> child = groups.get(uuid).parents();
                if (child != null) {
                    child.remove(id);
                }
            }
        }
    }

    public void addMember(String groupName, UUID uuid) {
        addMember(groupsByName.get(groupName), uuid);
    }

    public void addMember(UUID groupId, UUID uuid) {
        PlayerGroupDAGGroup group = groups.get(groupId);
        if (group == null) {
            return;
        }
        group.addMember(uuid);
        Set<UUID> member = players.playersGroups().get(uuid);
        if (member == null) {
            players.playersGroups().put(uuid, new HashSet<>());
            member = players.playersGroups().get(uuid);
            if (member != null) {
                member.add(groupId);
            }
        } else {
            member.add(groupId);
        }
    }

    public void removeMember(String group, UUID player) {
        removeMember(groupsByName.get(group), player);
    }

    public void removeMember(UUID groupId, UUID player) {
        PlayerGroupDAGGroup group = groups.get(groupId);
        if (group == null) {
            return;
        }
        group.removeMember(player);
        Set<UUID> member = players.playersGroups().get(player);
        if (member != null) {
            member.remove(groupId);
        }
    }

    public void addParent(String ParentName, String ChildName) {
        UUID parentId = groupsByName.get(ParentName);
        UUID childId = groupsByName.get(ChildName);
        if (parentId == null || childId == null) {
            return;
        }
        addParent(parentId, childId);
    }

    public void addParent(UUID ParentId, UUID ChildId) {
        if (ParentId.equals(ChildId)) {
            return;
        }

        if (createsCycle(ParentId, ChildId)) {
            return;
        }

        PlayerGroupDAGGroup parent = groups.get(ParentId);
        PlayerGroupDAGGroup child = groups.get(ChildId);
        if (parent == null || child == null) {
            return;
        }
        child.addParent(ParentId);
        parent.addChild(ChildId);
    }

    public void removeParent(String parent, String child) {
        UUID parentId = groupsByName.get(parent);
        UUID childId = groupsByName.get(child);
        if (parentId == null || childId == null) {
            return;
        }
        removeParent(parentId, childId);
    }

    public void removeParent(UUID parentId, UUID childId) {
        PlayerGroupDAGGroup parent = groups.get(parentId);
        PlayerGroupDAGGroup child = groups.get(childId);
        if (parent == null || child == null) {
            return;
        }
        child.removeParent(parentId);
        parent.removeChild(childId);
    }

    public void changeName(UUID id, String name) {
        String oldName = groups.get(id).name();
        groupsByName.remove(oldName);
        groupsByName.put(name, id);
        groups.get(id).setName(name);
    }

    public void addGroupPermissions(String groupName, Set<String> permissions) {
        UUID id = groupsByName.get(groupName);
        if (id == null) {
            return;
        }
        addGroupPermissions(id, permissions);
    }

    public void addGroupPermissions(UUID id, Set<String> permissions) {
        PlayerGroupDAGGroup group = groups.get(id);
        if (group == null) {
            return;
        }
        group.permissions().addAll(permissions);
    }

    public void addPlayerPermissions(UUID player, Set<String> permissions) {
        Set<String> playerperms = players.playersPermissions().get(player);
        if (playerperms == null) {
            players.playersPermissions().put(player, new HashSet<>(permissions));
        } else {
            playerperms.addAll(permissions);
        }
    }

    public void removeGroupPermissions(String groupName, Set<String> permissions) {
        UUID id = groupsByName.get(groupName);
        if (id == null) {
            return;
        }
        removeGroupPermissions(id, permissions);
    }

    public void removeGroupPermissions(UUID id, Set<String> permissions) {
        PlayerGroupDAGGroup group = groups.get(id);
        if (group == null) {
            return;
        }
        group.permissions().removeAll(permissions);
    }

    public void removePlayerPermissions(UUID player, Set<String> permissions) {
        Set<String> playerperms = players.playersPermissions().get(player);
        if (playerperms == null) {
            return;
        }
        playerperms.removeAll(permissions);
    }

    private Boolean createsCycle(UUID ParentId, UUID ChildId) {
        return dfs(
                ChildId,
                new HashSet<>(),
                group -> {
                    PlayerGroupDAGGroup DAGgroup = this.groups.get(group);
                    if (DAGgroup == null) return null;
                    return DAGgroup.children();
                },
                group -> group.equals(ParentId)
        );
    }

    static Boolean dfs(
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
            for (UUID n : nextNodes) {
                queue.push(n);
            }
        }

        return false;
    }

    private static <T> Set<T> dfsWithCache(
            UUID start,
            Map<UUID, Set<T>> cache,
            NextProvider nextProvider,
            Function<UUID, Collection<T>> getOwnCollection
    ) {
        Deque<Visit> stack = new ArrayDeque<>();
        Set<UUID> expanded = new HashSet<>();

        stack.push(new Visit(start, VisitType.ENTER));

        while (!stack.isEmpty()) {
            Visit visit = stack.pop();
            UUID node = visit.id();

            if (cache.containsKey(node)) continue;

            if (visit.type() == VisitType.ENTER) {

                if (expanded.add(node)) {
                    stack.push(new Visit(node, VisitType.EXIT));

                    Collection<UUID> next = nextProvider.next(node);
                    if (next != null) {
                        for (UUID n : next) {
                            if (!cache.containsKey(n)) {
                                stack.push(new Visit(n, VisitType.ENTER));
                            }
                        }
                    }
                }

            } else {
                Set<T> result = new HashSet<>();

                Collection<T> self = getOwnCollection.apply(node);
                if (self != null) {
                    result.addAll(self);
                }

                Collection<UUID> next = nextProvider.next(node);
                if (next != null) {
                    for (UUID n : next) {
                        result.addAll(cache.get(n));
                    }
                }

                cache.put(node, result);
            }
        }

        return cache.get(start);
    }

    public static PlayerGroupDAGFlat buildFlat(PlayerGroupDAG dag, PlayerGroupDAGFlat flat, PlayerGroupAffected
            affected) {
        Map<UUID, PlayerGroupGroupData> groups = new HashMap<>();
        Map<UUID, Set<UUID>> ancestorsCache = new HashMap<>();
        Map<UUID, Set<UUID>> descendantsCache = new HashMap<>();
        Map<UUID, Set<String>> permissionsCache = new HashMap<>();

        PlayerGroupPlayerData playerData = new PlayerGroupPlayerData(dag.players.playersGroups(), dag.players.playersPermissions());

        Function<UUID, Collection<UUID>> safeParents =
                g -> {
                    PlayerGroupDAGGroup gg = dag.groups.get(g);
                    return gg != null ? gg.parents() : Set.of();
                };

        Function<UUID, Collection<UUID>> safeChildren =
                g -> {
                    PlayerGroupDAGGroup gg = dag.groups.get(g);
                    return gg != null ? gg.children() : Set.of();
                };

        NextProvider safeParentsNext =
                g -> {
                    PlayerGroupDAGGroup gg = dag.groups.get(g);
                    return gg != null ? gg.parents() : Set.of();
                };

        NextProvider safeChildrenNext =
                g -> {
                    PlayerGroupDAGGroup gg = dag.groups.get(g);
                    return gg != null ? gg.children() : Set.of();
                };
        Function<UUID, Collection<String>> safePermissions =
                g -> {
                    PlayerGroupDAGGroup gg = dag.groups.get(g);
                    return gg != null ? gg.permissions() : Set.of();
                };
        for (PlayerGroupDAGGroup group : dag.groups.values()) {

            PlayerGroupGroupData old;
            Map<UUID, PlayerGroupGroupData> flatGroups = flat.groups();
            if (flatGroups == null) {
                old = null;
            } else {
                if (group.id() == null) {
                    old = null;
                } else {
                    old = flatGroups.get(group.id());
                }
            }

            Set<UUID> ancestors =
                    affected.ancestors().contains(group.id())
                            ? dfsWithCache(
                            group.id(),
                            ancestorsCache,
                            safeParentsNext,
                            safeParents
                    )
                            : (old != null ? old.ancestors() : Set.of());

            Set<UUID> descendants =
                    affected.descendants().contains(group.id())
                            ? dfsWithCache(
                            group.id(),
                            descendantsCache,
                            safeChildrenNext,
                            safeChildren
                    )
                            : (old != null ? old.descendants() : Set.of());

            Set<UUID> directMembers =
                    affected.directMembers().contains(group.id())
                            ? new HashSet<>(group.members())
                            : (old != null ? old.directMembers() : Set.of());

            Set<String> permissions =
                    affected.permissions().contains(group.id())
                            ? dfsWithCache(
                            group.id(),
                            permissionsCache,
                            safeParentsNext,
                            safePermissions
                    )
                            : (old != null ? old.permissions() : Set.of());

            groups.put(group.id(), new PlayerGroupGroupData(
                    group.id(),
                    group.name(),
                    ancestors,
                    descendants,
                    directMembers,
                    permissions
            ));
        }

        return new PlayerGroupDAGFlat(groups, new HashMap<>(dag.groupsByName), playerData);
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
                        new KeyedCodec<Map<String, UUID>>(
                                "GroupByName",
                                new MapCodec<UUID, Map<String, UUID>>(Codec.UUID_STRING, HashMap::new, false)
                        ),
                        (dag, map) -> dag.groupsByName.putAll(map),
                        (dag) -> dag.groupsByName
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

    private enum VisitType {
        ENTER,
        EXIT
    }

    private record Visit(UUID id, VisitType type) {
    }

    @FunctionalInterface
    interface NextProvider {
        Collection<UUID> next(UUID id);
    }

    @FunctionalInterface
    interface VisitConsumer {
        Boolean visit(UUID id);
    }
}
