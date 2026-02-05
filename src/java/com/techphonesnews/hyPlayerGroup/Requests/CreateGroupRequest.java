package com.techphonesnews.hyPlayerGroup.Requests;

import com.techphonesnews.hyPlayerGroup.Group.PlayerGroupAffected;
import com.techphonesnews.hyPlayerGroup.Group.PlayerGroupDAG;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;

public record CreateGroupRequest(String name) implements PlayerGroupGroupChangeRequest {
    @Override
    public void apply(PlayerGroupDAG dag) {
        dag.addGroup(name);
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
}
