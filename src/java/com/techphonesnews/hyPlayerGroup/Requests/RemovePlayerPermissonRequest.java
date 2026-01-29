package com.techphonesnews.hyPlayerGroup.Requests;

import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.event.events.permissions.PlayerPermissionChangeEvent;
import com.techphonesnews.hyPlayerGroup.Group.PlayerGroupDAG;

import java.util.Set;
import java.util.UUID;

public record RemovePlayerPermissonRequest(UUID player,
                                                 Set<String> permissions) implements PlayerGroupGroupChangeRequest {
    @Override
    public void apply(PlayerGroupDAG dag) {

    }

    @Override
    public void event() {
        HytaleServer.get()
                .getEventBus()
                .<Void, PlayerPermissionChangeEvent.PermissionsRemoved>dispatchFor(PlayerPermissionChangeEvent.PermissionsRemoved.class)
                .dispatch(new PlayerPermissionChangeEvent.PermissionsRemoved(player, permissions));
    }
}
