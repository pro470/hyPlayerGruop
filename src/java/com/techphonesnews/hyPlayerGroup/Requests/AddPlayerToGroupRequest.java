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

public final class AddPlayerToGroupRequest implements PlayerGroupGroupChangeRequest {

    private final UUID player;
    private final String group;

    private UUID groupId;

    public AddPlayerToGroupRequest(UUID player, String group) {
        this.player = player;
        this.group = group;
    }

    @Override
    public void apply(PlayerGroupDAG dag) {
        dag.addMember(group, player);
        groupId = dag.getGroupId(group);

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
    @Nonnull
    public PlayerGroupAffected affected() {

        if (groupId == null)
            return PlayerGroupAffected.EMPTY;

        return new PlayerGroupAffected(Set.of(), Set.of(), Set.of(), Set.of(groupId));
    }
}
