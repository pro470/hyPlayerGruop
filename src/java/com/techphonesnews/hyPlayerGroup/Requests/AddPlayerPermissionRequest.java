package com.techphonesnews.hyPlayerGroup.Requests;

import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.event.events.permissions.PlayerPermissionChangeEvent;
import com.techphonesnews.hyPlayerGroup.Group.PlayerGroupAffected;
import com.techphonesnews.hyPlayerGroup.Group.PlayerGroupDAG;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public record AddPlayerPermissionRequest(UUID player,
                                         Set<String> permissions) implements PlayerGroupGroupChangeRequest {
    @Override
    public void apply(PlayerGroupDAG dag) {
        dag.addPlayerPermissions(player, permissions);
    }

    @Override
    public void event() {
        HytaleServer.get()
                .getEventBus()
                .<Void, PlayerPermissionChangeEvent.PermissionsAdded>dispatchFor(PlayerPermissionChangeEvent.PermissionsAdded.class)
                .dispatch(new PlayerPermissionChangeEvent.PermissionsAdded(player, permissions));
    }

    @Override
    public String debugMessage() {
        StringBuilder strpermission = new StringBuilder();
        for (String permission : permissions) {
            strpermission.append(", ").append(permission);
        }
        return "Adding permissions permissions" + " to player " + player + " " + strpermission;
    }

    @Override
    public void affected(PlayerGroupAffected affected) {
        affected.playersPermissions().add(player);
    }
}
