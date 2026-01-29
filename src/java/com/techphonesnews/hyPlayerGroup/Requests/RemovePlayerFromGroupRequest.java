package com.techphonesnews.hyPlayerGroup.Requests;

import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.event.events.permissions.PlayerGroupEvent;
import com.techphonesnews.hyPlayerGroup.Group.PlayerGroupDAG;

import java.util.UUID;

public record RemovePlayerFromGroupRequest(UUID player, String group) implements PlayerGroupGroupChangeRequest {
    @Override
    public void apply(PlayerGroupDAG dag) {

    }

    @Override
    public void event() {
        HytaleServer.get()
                .getEventBus()
                .<Void, PlayerGroupEvent.Removed>dispatchFor(PlayerGroupEvent.Removed.class)
                .dispatch(new PlayerGroupEvent.Removed(player, group));
    }
}
