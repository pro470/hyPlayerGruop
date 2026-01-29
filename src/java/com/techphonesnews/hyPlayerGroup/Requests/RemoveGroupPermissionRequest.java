package com.techphonesnews.hyPlayerGroup.Requests;

import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.event.events.permissions.GroupPermissionChangeEvent;
import com.techphonesnews.hyPlayerGroup.Group.PlayerGroupDAG;

import java.util.Set;

public record RemoveGroupPermissionRequest(String groupName,
                                                 Set<String> permissions) implements PlayerGroupGroupChangeRequest {
    @Override
    public void apply(PlayerGroupDAG dag) {

    }

    @Override
    public void event() {
        HytaleServer.get()
                .getEventBus()
                .<Void, GroupPermissionChangeEvent.Removed>dispatchFor(GroupPermissionChangeEvent.Removed.class)
                .dispatch(new GroupPermissionChangeEvent.Removed(groupName, permissions));
    }

}
