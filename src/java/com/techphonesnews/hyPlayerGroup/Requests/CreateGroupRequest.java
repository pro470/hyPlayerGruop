package com.techphonesnews.hyPlayerGroup.Requests;

import com.techphonesnews.hyPlayerGroup.Group.PlayerGroupAffected;
import com.techphonesnews.hyPlayerGroup.Group.PlayerGroupDAG;

public final class CreateGroupRequest implements PlayerGroupGroupChangeRequest {
    private final String name;

    private boolean succeeded = false;

    public CreateGroupRequest(String name) {
        this.name = name;
    }

    @Override
    public void apply(PlayerGroupDAG dag) {
        if (dag.addGroup(name) == null) {
            succeeded = false;
        } else {
            succeeded = true;
        }
    }

    @Override
    public void event() {

    }

    @Override
    public String debugMessage() {
        return "Creating group " + name;
    }

    @Override
    public void affected(PlayerGroupAffected affected) {
    }

    @Override
    public Boolean succeeded() {
        return succeeded;
    }

    public String getName() {
        return name;
    }
}
