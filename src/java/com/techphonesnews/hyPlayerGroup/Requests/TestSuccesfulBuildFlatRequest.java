package com.techphonesnews.hyPlayerGroup.Requests;

import com.techphonesnews.hyPlayerGroup.Group.PlayerGroupAffected;
import com.techphonesnews.hyPlayerGroup.Group.PlayerGroupDAG;

public record TestSuccesfulBuildFlatRequest() implements PlayerGroupGroupChangeRequest {
    @Override
    public void apply(PlayerGroupDAG dag) {

    }

    @Override
    public void event() {

    }

    @Override
    public String debugMessage() {
        return "------------------------SUCCESSFUL BUILD------------------------";
    }

    @Override
    public void affected(PlayerGroupAffected affected) {
    }

    @Override
    public Boolean succeeded() {
        return true;
    }
}
