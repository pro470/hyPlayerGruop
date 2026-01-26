package com.techphonesnews.hyPlayerGroup.Permissions;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.map.MapCodec;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.event.events.permissions.GroupPermissionChangeEvent;
import com.hypixel.hytale.server.core.event.events.permissions.PlayerGroupEvent;
import com.hypixel.hytale.server.core.event.events.permissions.PlayerPermissionChangeEvent;
import com.hypixel.hytale.server.core.permissions.provider.PermissionProvider;
import com.hypixel.hytale.server.core.universe.Universe;
import com.techphonesnews.hyPlayerGroup.Group.PlayerGroupGroup;
import com.techphonesnews.hyPlayerGroup.Group.PlayerGroupMemberProvider;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

public class PlayerGroupPermissionsProvider implements PermissionProvider {

    private final Map<String, PlayerGroupGroup> playerGroups = new ConcurrentHashMap<>();
    private final Map<UUID, PlayerGroupMemberProvider> playerGroupMembers = new ConcurrentHashMap<>();
    private final Map<UUID, PlayerGroupPermission> playerGroupPermissions = new ConcurrentHashMap<>();
    private static Integer INTERVAL = 60;
    private static Integer DEFAULT_RANK = 5;

    public static final String name = "PlayerGroupPermissionsProvider";

    public static final BuilderCodec<PlayerGroupPermissionsProvider> CODEC;

    public Integer getInterval() {
        return INTERVAL;
    }

    @Nonnull
    @Override
    public String getName() {
        return name;
    }

    public void addMemberPermissions(@Nonnull UUID uuid, @Nonnull Set<String> var2) {
        PlayerGroupPermission perm = playerGroupPermissions.get(uuid);
        if (perm == null) {
            perm = new PlayerGroupPermission();
            playerGroupPermissions.put(uuid, perm);
        }
        perm.getPermissions().addAll(var2);
        PlayerGroupMemberProvider group = playerGroupMembers.get(uuid);
        if (group != null) {
            HytaleServer.get()
                    .getEventBus()
                    .<Void, GroupPermissionChangeEvent.Added>dispatchFor(GroupPermissionChangeEvent.Added.class)
                    .dispatch(new GroupPermissionChangeEvent.Added(group.getGroupName(), var2));

        } else {
            HytaleServer.get()
                    .getEventBus()
                    .<Void, PlayerPermissionChangeEvent.PermissionsAdded>dispatchFor(PlayerPermissionChangeEvent.PermissionsAdded.class)
                    .dispatch(new PlayerPermissionChangeEvent.PermissionsAdded(uuid, var2));
        }
    }

    public void addMemberPermissions(@Nonnull String group, @Nonnull Set<String> var2) {
        PlayerGroupGroup playerGroup = playerGroups.get(group);
        if (playerGroup == null) {
            return;
        }
        addUserPermissions(playerGroup.id(), var2);
    }

    public void removeMemberPermissions(@Nonnull UUID uuid, @Nonnull Set<String> var2) {
        PlayerGroupPermission perm = playerGroupPermissions.get(uuid);
        if (perm == null) {
            return;
        }
        perm.getPermissions().removeAll(var2);
        PlayerGroupMemberProvider group = playerGroupMembers.get(uuid);
        if (group != null) {
            HytaleServer.get()
                    .getEventBus()
                    .<Void, GroupPermissionChangeEvent.Removed>dispatchFor(GroupPermissionChangeEvent.Removed.class)
                    .dispatch(new GroupPermissionChangeEvent.Removed(group.getGroupName(), var2));

        } else {
            HytaleServer.get()
                    .getEventBus()
                    .<Void, PlayerPermissionChangeEvent.PermissionsRemoved>dispatchFor(PlayerPermissionChangeEvent.PermissionsRemoved.class)
                    .dispatch(new PlayerPermissionChangeEvent.PermissionsRemoved(uuid, var2));
        }
    }

    public void removeMemberPermissions(@Nonnull String group, @Nonnull Set<String> var2) {
        PlayerGroupGroup playerGroup = playerGroups.get(group);
        if (playerGroup == null) {
            return;
        }
        removeMemberPermissions(playerGroup.id(), var2);
    }

    public Boolean hasLowerRank(@Nonnull UUID sender, @Nonnull UUID receiver, @Nonnull String groupName) {
        PlayerGroupGroup group = playerGroups.get(groupName);
        if (group == null) {
            return false;
        }
        int receiverRank = playerGroupPermissions.get(receiver).getRank(group.id());
        return playerGroupPermissions.get(sender).hasRank(group.id(), receiverRank);
    }

    public Boolean isdirectMemberOfGroup(@Nonnull UUID uuid, @Nonnull String groupName) {
        PlayerGroupGroup group = playerGroups.get(groupName);
        if (group == null) {
            return false;
        }
        return group.members().contains(uuid);
    }

    @Override
    public void addUserPermissions(@Nonnull UUID uuid, @Nonnull Set<String> var2) {
        addMemberPermissions(uuid, var2);
    }

    @Override
    public void removeUserPermissions(@Nonnull UUID uuid, @Nonnull Set<String> var2) {
        removeMemberPermissions(uuid, var2);
    }

    @Override
    public Set<String> getUserPermissions(@Nonnull UUID uuid) {
        return Collections.unmodifiableSet(playerGroupPermissions.get(uuid).getPermissions());
    }

    @Override
    public void addGroupPermissions(@Nonnull String group, @Nonnull Set<String> var2) {
        addMemberPermissions(group, var2);
    }

    @Override
    public void removeGroupPermissions(@Nonnull String group, @Nonnull Set<String> var2) {
        removeMemberPermissions(group, var2);
    }

    @Override
    public Set<String> getGroupPermissions(@Nonnull String group) {
        PlayerGroupGroup playerGroup = playerGroups.get(group);
        if (playerGroup == null) {
            return Collections.emptySet();
        }
        return Collections.unmodifiableSet(playerGroupPermissions.get(playerGroup.id()).getPermissions());
    }

    @Override
    public void addUserToGroup(@Nonnull UUID uuid, @Nonnull String var2) {
        PlayerGroupGroup group = playerGroups.get(var2);
        PlayerGroupPermission playerGroupPermission = playerGroupPermissions.get(uuid);
        if (group == null) {
            return;
        }
        if (playerGroupMembers.containsKey(uuid)) {
            group.groupMembers().add(uuid);
        } else if (Universe.get().getPlayer(uuid) != null) {
            group.members().add(uuid);
        } else {
            return;
        }
        playerGroupPermission.setRank(group.id(), DEFAULT_RANK);
        HytaleServer.get()
                .getEventBus()
                .<Void, PlayerGroupEvent.Added>dispatchFor(PlayerGroupEvent.Added.class)
                .dispatch(new PlayerGroupEvent.Added(uuid, var2));
    }

    @Override
    public void removeUserFromGroup(@Nonnull UUID uuid, @Nonnull String var2) {
        PlayerGroupGroup group = playerGroups.get(var2);
        PlayerGroupPermission playerGroupPermission = playerGroupPermissions.get(uuid);
        if (group == null) {
            return;
        }
        if (playerGroupMembers.containsKey(uuid)) {
            group.groupMembers().remove(uuid);
        } else if (Universe.get().getPlayer(uuid) != null) {
            group.members().remove(uuid);
        } else {
            return;
        }
        playerGroupPermission.removeRank(group.id());
        HytaleServer.get()
                .getEventBus()
                .<Void, PlayerGroupEvent.Removed>dispatchFor(PlayerGroupEvent.Removed.class)
                .dispatch(new PlayerGroupEvent.Removed(uuid, var2));
    }

    @Override
    public Set<String> getGroupsForUser(@Nonnull UUID uuid) {
        Set<String> groups = new HashSet<>();

        for (UUID playerGroupId : playerGroupPermissions.get(uuid).getgroups()) {
            PlayerGroupMemberProvider group = playerGroupMembers.get(playerGroupId);
            groups.add(name + "." + group.getGroupName());
        }

        return Collections.unmodifiableSet(groups);
    }

    public List<UUID> getPlayersFlat(String group) {
        return getPlayersFlatInner(group, (provider, alreadySeen) -> provider.getPlayersFlat(playerGroups, playerGroupMembers, alreadySeen).stream().toList());
    }

    public List<UUID> getOnlinePlayersFlat(String group) {
        return getPlayersFlatInner(group, (provider, alreadySeen) -> provider.getOnlinePlayersFlat(playerGroups, playerGroupMembers, alreadySeen).stream().toList());
    }

    public List<UUID> getPlayersFlatInner(String group, BiFunction<PlayerGroupMemberProvider, Set<UUID>, List<UUID>> function) {
        PlayerGroupGroup playerGroup = playerGroups.get(group);
        if (playerGroup == null) {
            return Collections.emptyList();
        }
        Set<UUID> players = new HashSet<>(playerGroup.members());
        Set<UUID> alreadySeen = new HashSet<>();
        for (UUID uuid : playerGroup.groupMembers()) {
            if (alreadySeen.contains(uuid)) {
                continue;
            }
            alreadySeen.add(uuid);
            PlayerGroupMemberProvider playerGroupMember = playerGroupMembers.get(uuid);
            players.addAll(function.apply(playerGroupMember, alreadySeen));
        }
        return players.stream().toList();
    }

    static {
        CODEC = BuilderCodec.builder(PlayerGroupPermissionsProvider.class, PlayerGroupPermissionsProvider::new)
                .append(
                        new KeyedCodec<Map<String, PlayerGroupGroup>>("PlayerGroups", new MapCodec<PlayerGroupGroup, Map<String, PlayerGroupGroup>>(PlayerGroupGroup.CODEC, ConcurrentHashMap::new, false)),
                        (obj, val) -> obj.playerGroups.putAll(val),
                        (obj) -> obj.playerGroups
                ).add()
                .append(
                        new KeyedCodec<Map<String, PlayerGroupPermission>>("PlayerGroupPermissions", new MapCodec<PlayerGroupPermission, Map<String, PlayerGroupPermission>>(PlayerGroupPermission.CODEC, ConcurrentHashMap::new, false)),
                        (obj, val) -> {
                            for (Map.Entry<String, PlayerGroupPermission> entry : val.entrySet()) {
                                obj.playerGroupPermissions.put(UUID.fromString(entry.getKey()), entry.getValue());
                            }
                        },
                        (obj) -> {
                            Map<String, PlayerGroupPermission> playerGroupPermissions = new HashMap<>();
                            for (Map.Entry<UUID, PlayerGroupPermission> entry : obj.playerGroupPermissions.entrySet()) {
                                playerGroupPermissions.put(entry.getKey().toString(), entry.getValue());
                            }
                            return playerGroupPermissions;
                        }
                ).add()
                .append(
                        new KeyedCodec<Map<String, PlayerGroupMemberProvider>>("PlayerGroupMembers", new MapCodec<PlayerGroupMemberProvider, Map<String, PlayerGroupMemberProvider>>(
                                PlayerGroupMemberProvider.CODEC,
                                ConcurrentHashMap::new,
                                false)
                        ),
                        (obj, val) -> {
                            for (Map.Entry<String, PlayerGroupMemberProvider> entry : val.entrySet()) {
                                obj.playerGroupMembers.put(UUID.fromString(entry.getKey()), entry.getValue());
                            }
                        },
                        (obj) -> {
                            Map<String, PlayerGroupMemberProvider> playerGroupMembers = new HashMap<>();
                            for (Map.Entry<UUID, PlayerGroupMemberProvider> entry : obj.playerGroupMembers.entrySet()) {
                                playerGroupMembers.put(entry.getKey().toString(), entry.getValue());
                            }
                            return playerGroupMembers;
                        }
                ).add()
                .append(
                        new KeyedCodec<Integer>("Interval", Codec.INTEGER),
                        (obj, val) -> INTERVAL = val,
                        (obj) -> INTERVAL
                ).add()
                .append(
                        new KeyedCodec<Integer>("DefaultRank", Codec.INTEGER),
                        (obj, val) -> DEFAULT_RANK = val,
                        (obj) -> DEFAULT_RANK
                ).add()
                .build();
    }
}
