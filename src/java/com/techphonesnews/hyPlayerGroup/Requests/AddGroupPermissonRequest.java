package com.techphonesnews.hyPlayerGroup.Requests;

import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.event.events.permissions.GroupPermissionChangeEvent;
import com.techphonesnews.hyPlayerGroup.Group.PlayerGroupAffected;
import com.techphonesnews.hyPlayerGroup.Group.PlayerGroupDAG;
import com.techphonesnews.hyPlayerGroup.HyPlayerGroupPlugin;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public final class AddGroupPermissonRequest implements PlayerGroupGroupChangeRequest {

    private final String groupName;
    private final Set<String> permissions;

    private UUID groupId;

    private Boolean succeeded;

    public AddGroupPermissonRequest(String groupName, Set<String> permissions) {
        this.groupName = groupName;
        this.permissions = permissions;
    }

    @Override
    public void apply(PlayerGroupDAG dag) {
        succeeded = dag.addGroupPermissions(groupName, permissions);
        if (succeeded) {
            groupId = dag.getGroupId(groupName);
        }
    }

    @Override
    public void event() {
        HytaleServer.get()
                .getEventBus()
                .<Void, GroupPermissionChangeEvent.Added>dispatchFor(GroupPermissionChangeEvent.Added.class)
                .dispatch(new GroupPermissionChangeEvent.Added(groupName, permissions));
    }

    @Override
    public String debugMessage() {
        StringBuilder strpermission = new StringBuilder();
        for (String permission : permissions) {
            strpermission.append(", ").append(permission);
        }
        return "Adding permissions permissions" + " to group " + groupName + " " + strpermission;
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
