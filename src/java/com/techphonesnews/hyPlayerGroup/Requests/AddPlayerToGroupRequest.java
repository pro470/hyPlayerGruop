package com.techphonesnews.hyPlayerGroup.Requests;

import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.event.events.permissions.PlayerGroupEvent;
import com.techphonesnews.hyPlayerGroup.Group.PlayerGroupDAG;

import java.util.UUID;

public record AddPlayerToGroupRequest(UUID player, String group) implements PlayerGroupGroupChangeRequest {
    @Override
    public void apply(PlayerGroupDAG dag) {

    }

    @Override
    public void event() {
        HytaleServer.get()
                .getEventBus()
                .<Void, PlayerGroupEvent.Added>dispatchFor(PlayerGroupEvent.Added.class)
                .dispatch(new PlayerGroupEvent.Added(player, group));
    }
}
