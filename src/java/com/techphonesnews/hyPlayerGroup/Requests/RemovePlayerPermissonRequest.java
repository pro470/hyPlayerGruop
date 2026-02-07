package com.techphonesnews.hyPlayerGroup.Requests;

import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.event.events.permissions.PlayerPermissionChangeEvent;
import com.techphonesnews.hyPlayerGroup.Group.PlayerGroupAffected;
import com.techphonesnews.hyPlayerGroup.Group.PlayerGroupDAG;

import java.util.Set;
import java.util.UUID;

public final class RemovePlayerPermissonRequest implements PlayerGroupGroupChangeRequest {
    private final UUID player;
    private final Set<String> permissions;
    private Boolean succeeded;

    public RemovePlayerPermissonRequest(UUID player, Set<String> permissions) {
        this.player = player;
        this.permissions = permissions;
    }

    @Override
    public void apply(PlayerGroupDAG dag) {
        succeeded = dag.removePlayerPermissions(player, permissions);
    }

    @Override
    public void event() {
        HytaleServer.get()
                .getEventBus()
                .<Void, PlayerPermissionChangeEvent.PermissionsRemoved>dispatchFor(PlayerPermissionChangeEvent.PermissionsRemoved.class)
                .dispatch(new PlayerPermissionChangeEvent.PermissionsRemoved(player, permissions));
    }

    @Override
    public String debugMessage() {
        StringBuilder strpermission = new StringBuilder();
        for (String permission : permissions) {
            strpermission.append(", ").append(permission);
        }
        return "Removing permissions permissions" + " to player " + player + " " + strpermission;
    }

    @Override
    public void affected(PlayerGroupAffected affected) {
        affected.playersPermissions().add(player);
    }

    @Override
    public Boolean succeeded() {
        return succeeded;
    }
}
