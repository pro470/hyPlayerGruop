package com.techphonesnews.hyPlayerGroup.Requests;

import com.techphonesnews.hyPlayerGroup.Group.PlayerGroupAffected;
import com.techphonesnews.hyPlayerGroup.Group.PlayerGroupDAG;
import com.techphonesnews.hyPlayerGroup.HyPlayerGroupPlugin;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public record DisbandGroupRequest(String name) implements PlayerGroupGroupChangeRequest {
    @Override
    public void apply(PlayerGroupDAG dag) {
        dag.removeGroup(name);
    }

    @Override
    public void event() {

    }

    @Override
    public String debugMessage() {
        return "Disbanding group " + name;
    }

    @Nonnull
    @Override
    public PlayerGroupAffected affected() {
        if (HyPlayerGroupPlugin.get().getDAGFlat().getGroup(name) == null)
            return PlayerGroupAffected.EMPTY;

        Set<UUID> children = new HashSet<>(HyPlayerGroupPlugin.get().getDAGFlat().getGroup(name).descendants());
        Set<UUID> parents = new HashSet<>(HyPlayerGroupPlugin.get().getDAGFlat().getGroup(name).ancestors());

        return new PlayerGroupAffected(children, parents, children, Set.of());
    }
}
