package com.techphonesnews.hyPlayerGroup.Requests;

import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.event.events.permissions.GroupPermissionChangeEvent;
import com.techphonesnews.hyPlayerGroup.Group.PlayerGroupAffected;
import com.techphonesnews.hyPlayerGroup.Group.PlayerGroupDAG;
import com.techphonesnews.hyPlayerGroup.HyPlayerGroupPlugin;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class RemoveGroupPermissionRequest implements PlayerGroupGroupChangeRequest {

    private String groupName;
    private Set<String> permissions;

    private UUID groupId;

    public RemoveGroupPermissionRequest(String groupName, Set<String> permissions) {
        this.groupName = groupName;
        this.permissions = permissions;
    }

    @Override
    public void apply(PlayerGroupDAG dag) {
        dag.removeGroupPermissions(groupName, permissions);
        groupId = dag.getGroupId(groupName);
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
        Set<UUID> children = new HashSet<>();
        if (groupId != null) {
            children.add(groupId);
        }

        if (HyPlayerGroupPlugin.get().getDAGFlat().getGroup(groupName) != null) {
            children.addAll(HyPlayerGroupPlugin.get().getDAGFlat().getGroup(groupName).descendants());

        }
        return new PlayerGroupAffected(Set.of(), Set.of(), children, Set.of());
    }

}
