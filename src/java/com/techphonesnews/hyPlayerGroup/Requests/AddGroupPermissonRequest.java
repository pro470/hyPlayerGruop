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

public record AddGroupPermissonRequest(String groupName,
                                       Set<String> permissions) implements PlayerGroupGroupChangeRequest {
    @Override
    public void apply(PlayerGroupDAG dag) {
        dag.addGroupPermissions(groupName, permissions);
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
    @Nonnull
    public PlayerGroupAffected affected() {
        if (HyPlayerGroupPlugin.get().getDAGFlat().getGroup(groupName) == null)
            return PlayerGroupAffected.EMPTY;

        Set<UUID> children = new HashSet<>(HyPlayerGroupPlugin.get().getDAGFlat().getGroup(groupName).descendants());
        children.add(HyPlayerGroupPlugin.get().getDAGFlat().getGroup(groupName).id());
        return new PlayerGroupAffected(Set.of(), Set.of(), Set.copyOf(children), Set.of());
    }
}
