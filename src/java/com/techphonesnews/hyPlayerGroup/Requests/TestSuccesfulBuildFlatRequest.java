package com.techphonesnews.hyPlayerGroup.Requests;

import com.techphonesnews.hyPlayerGroup.Group.PlayerGroupAffected;
import com.techphonesnews.hyPlayerGroup.Group.PlayerGroupDAG;

import javax.annotation.Nonnull;

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

    @Nonnull
    @Override
    public PlayerGroupAffected affected() {
        return PlayerGroupAffected.EMPTY;
    }
}
