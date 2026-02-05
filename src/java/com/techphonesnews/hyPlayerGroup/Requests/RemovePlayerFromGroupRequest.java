package com.techphonesnews.hyPlayerGroup.Requests;

import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.event.events.permissions.PlayerGroupEvent;
import com.techphonesnews.hyPlayerGroup.Group.PlayerGroupAffected;
import com.techphonesnews.hyPlayerGroup.Group.PlayerGroupDAG;
import com.techphonesnews.hyPlayerGroup.Group.PlayerGroupGroupData;
import com.techphonesnews.hyPlayerGroup.HyPlayerGroupPlugin;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class RemovePlayerFromGroupRequest implements PlayerGroupGroupChangeRequest {
    private final UUID player;
    private final String group;

    private UUID groupId;

    public RemovePlayerFromGroupRequest(UUID player, String group) {
        this.player = player;
        this.group = group;
    }

    @Override
    public void apply(PlayerGroupDAG dag) {
        dag.removeMember(group, player);
        groupId = dag.getGroupId(group);
    }

    @Override
    public void event() {
        HytaleServer.get()
                .getEventBus()
                .<Void, PlayerGroupEvent.Removed>dispatchFor(PlayerGroupEvent.Removed.class)
                .dispatch(new PlayerGroupEvent.Removed(player, group));
    }

    @Override
    public String debugMessage() {
        return "Removing " + player + " from " + group;
    }

    @Override
    public void affected(PlayerGroupAffected affected) {

        if (groupId == null)
            return;
        affected.directMembers().add(groupId);
    }
}
