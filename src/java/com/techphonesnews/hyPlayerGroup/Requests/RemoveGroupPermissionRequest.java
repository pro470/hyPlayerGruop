package com.techphonesnews.hyPlayerGroup.Requests;

import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.event.events.permissions.GroupPermissionChangeEvent;
import com.techphonesnews.hyPlayerGroup.Group.PlayerGroupAffected;
import com.techphonesnews.hyPlayerGroup.Group.PlayerGroupDAG;
import com.techphonesnews.hyPlayerGroup.Group.PlayerGroupGroupData;
import com.techphonesnews.hyPlayerGroup.HyPlayerGroupPlugin;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public record RemoveGroupPermissionRequest(String groupName,
                                           Set<String> permissions) implements PlayerGroupGroupChangeRequest {
    @Override
    public void apply(PlayerGroupDAG dag) {
        dag.removeGroupPermissions(groupName, permissions);
    }

    @Override
    public void event() {
        HytaleServer.get()
                .getEventBus()
                .<Void, GroupPermissionChangeEvent.Removed>dispatchFor(GroupPermissionChangeEvent.Removed.class)
                .dispatch(new GroupPermissionChangeEvent.Removed(groupName, permissions));
    }

    @Override
    public String debugMessage() {
        StringBuilder strpermission = new StringBuilder();
        for (String permission : permissions) {
            strpermission.append(", ").append(permission);
        }
        return "Removing permissions permissions" + " to group " + groupName + " " + strpermission;
    }

    @Override
    @Nonnull
    public PlayerGroupAffected affected() {
        PlayerGroupGroupData group = HyPlayerGroupPlugin.get().getDAGFlat().getGroup(groupName);
        if (group == null)
            return PlayerGroupAffected.EMPTY;
        Set<UUID> children = new HashSet<>(group.descendants());
        children.add(group.id());
        return new PlayerGroupAffected(Set.of(), Set.of(), Set.copyOf(children), Set.of());
    }

}
