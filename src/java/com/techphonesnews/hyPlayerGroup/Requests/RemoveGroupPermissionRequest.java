package com.techphonesnews.hyPlayerGroup.Requests;

import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.event.events.permissions.GroupPermissionChangeEvent;
import com.techphonesnews.hyPlayerGroup.Group.PlayerGroupAffected;
import com.techphonesnews.hyPlayerGroup.Group.PlayerGroupDAG;
import com.techphonesnews.hyPlayerGroup.HyPlayerGroupPlugin;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public final class RemoveGroupPermissionRequest implements PlayerGroupGroupChangeRequest {

    private final String groupName;
    private final Set<String> permissions;

    private UUID groupId;

    private Boolean succeeded;

    public RemoveGroupPermissionRequest(String groupName, Set<String> permissions) {
        this.groupName = groupName;
        this.permissions = permissions;
    }

    @Override
    public void apply(PlayerGroupDAG dag) {
        succeeded = dag.removeGroupPermissions(groupName, permissions);
        if (!succeeded) return;
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
    public void affected(PlayerGroupAffected affected) {
        if (groupId != null) {
            affected.permissions().add(groupId);
        }

        if (HyPlayerGroupPlugin.get().getDAGFlat().getGroup(groupName) != null) {
            affected.permissions().addAll(Objects.requireNonNull(HyPlayerGroupPlugin.get().getDAGFlat().getGroup(groupName)).descendants());

        }
    }

    @Override
    public Boolean succeeded() {
        return succeeded;
    }
}
