package com.techphonesnews.hyPlayerGroup.Requests;

import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.event.events.permissions.PlayerGroupEvent;
import com.techphonesnews.hyPlayerGroup.Group.PlayerGroupAffected;
import com.techphonesnews.hyPlayerGroup.Group.PlayerGroupDAG;

import java.util.UUID;

public final class AddPlayerToGroupRequest implements PlayerGroupGroupChangeRequest {

    private final UUID player;
    private final String group;

    private UUID groupId;

    private Boolean succeeded;

    public AddPlayerToGroupRequest(UUID player, String group) {
        this.player = player;
        this.group = group;
    }

    @Override
    public void apply(PlayerGroupDAG dag) {
        succeeded = dag.addMember(group, player);
        if (succeeded) {
            groupId = dag.getGroupId(group);
        }
    }

    @Override
    public void event() {
        HytaleServer.get()
                .getEventBus()
                .<Void, PlayerGroupEvent.Added>dispatchFor(PlayerGroupEvent.Added.class)
                .dispatch(new PlayerGroupEvent.Added(player, group));
    }

    @Override
    public String debugMessage() {
        return "Adding player " + player + " to group " + group;
    }

    @Override
    public void affected(PlayerGroupAffected affected) {

        if (groupId != null)
            affected.directMembers().add(groupId);
    }

    @Override
    public Boolean succeeded() {
        return succeeded;
    }
}
