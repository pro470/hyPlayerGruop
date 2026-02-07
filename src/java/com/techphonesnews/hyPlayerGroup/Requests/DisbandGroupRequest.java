package com.techphonesnews.hyPlayerGroup.Requests;

import com.techphonesnews.hyPlayerGroup.Group.PlayerGroupAffected;
import com.techphonesnews.hyPlayerGroup.Group.PlayerGroupDAG;
import com.techphonesnews.hyPlayerGroup.HyPlayerGroupPlugin;

public final class DisbandGroupRequest implements PlayerGroupGroupChangeRequest {
    private final String name;
    private boolean succeeded = false;

    public DisbandGroupRequest(String name) {
        this.name = name;
    }

    @Override
    public void apply(PlayerGroupDAG dag) {
        succeeded = dag.removeGroup(name);
    }

    @Override
    public void event() {

    }

    @Override
    public String debugMessage() {
        return "Disbanding group " + name;
    }

    @Override
    public void affected(PlayerGroupAffected affected) {
        if (HyPlayerGroupPlugin.get().getDAGFlat().getGroup(name) == null)
            return;

        affected.ancestors().addAll(HyPlayerGroupPlugin.get().getDAGFlat().getGroup(name).descendants());
        affected.permissions().addAll(HyPlayerGroupPlugin.get().getDAGFlat().getGroup(name).descendants());
        affected.descendants().addAll(HyPlayerGroupPlugin.get().getDAGFlat().getGroup(name).ancestors());
        affected.directMembers().add(HyPlayerGroupPlugin.get().getDAGFlat().getGroup(name).id());
    }

    @Override
    public Boolean succeeded() {
        return succeeded;
    }

    public String getName() {
        return name;
    }
}
