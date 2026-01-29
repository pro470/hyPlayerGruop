package com.techphonesnews.hyPlayerGroup.Requests;

import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.event.events.permissions.GroupPermissionChangeEvent;
import com.techphonesnews.hyPlayerGroup.Group.PlayerGroupDAG;

import java.util.Set;

public record AddGroupPermissonRequest(String groupName,
                                             Set<String> permissions) implements PlayerGroupGroupChangeRequest {
    @Override
    public void apply(PlayerGroupDAG dag) {

    }

    @Override
    public void event() {
        HytaleServer.get()
                .getEventBus()
                .<Void, GroupPermissionChangeEvent.Added>dispatchFor(GroupPermissionChangeEvent.Added.class)
                .dispatch(new GroupPermissionChangeEvent.Added(groupName, permissions));
    }
}
