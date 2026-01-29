package com.techphonesnews.hyPlayerGroup.Permissions;

import com.hypixel.hytale.server.core.permissions.provider.PermissionProvider;
import com.techphonesnews.hyPlayerGroup.Group.PlayerGroupDAGFlat;
import com.techphonesnews.hyPlayerGroup.Requests.*;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicReference;

public class PlayerGroupPermissionsProvider implements PermissionProvider {

    public static final String name = "PlayerGroupPermissionsProvider";

    private final AtomicReference<PlayerGroupDAGFlat> dagFlat;
    private final ConcurrentLinkedQueue<PlayerGroupGroupChangeRequest> queue;

    public PlayerGroupPermissionsProvider(AtomicReference<PlayerGroupDAGFlat> dagFlat, ConcurrentLinkedQueue<PlayerGroupGroupChangeRequest> queue) {
        this.dagFlat = dagFlat;
        this.queue = queue;
    }

    @Nonnull
    @Override
    public String getName() {
        return name;
    }

    @Override
    public void addUserPermissions(@Nonnull UUID uuid, @Nonnull Set<String> var2) {
        queue.add(new AddPlayerPermissionRequest(uuid, var2));
    }

    @Override
    public void removeUserPermissions(@Nonnull UUID uuid, @Nonnull Set<String> var2) {
        queue.add(new RemovePlayerPermissonRequest(uuid, var2));
    }

    @Override
    public Set<String> getUserPermissions(@Nonnull UUID uuid) {
        return dagFlat.get().getPlayerPermissions(uuid);
    }

    @Override
    public void addGroupPermissions(@Nonnull String group, @Nonnull Set<String> var2) {
        queue.add(new AddGroupPermissonRequest(group, var2));
    }

    @Override
    public void removeGroupPermissions(@Nonnull String group, @Nonnull Set<String> var2) {
        queue.add(new RemoveGroupPermissionRequest(group, var2));
    }

    @Override
    public Set<String> getGroupPermissions(@Nonnull String group) {
        return dagFlat.get().getGroupPermissions(group);
    }

    @Override
    public void addUserToGroup(@Nonnull UUID uuid, @Nonnull String var2) {
        queue.add(new AddPlayerToGroupRequest(uuid, var2));
    }

    @Override
    public void removeUserFromGroup(@Nonnull UUID uuid, @Nonnull String var2) {
        queue.add(new RemovePlayerFromGroupRequest(uuid, var2));
    }

    @Override
    public Set<String> getGroupsForUser(@Nonnull UUID uuid) {
        return dagFlat.get().getPlayerGroups(uuid);
    }
}
