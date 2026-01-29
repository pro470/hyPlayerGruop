package com.techphonesnews.hyPlayerGroup.Requests;

import com.techphonesnews.hyPlayerGroup.Group.PlayerGroupDAG;

public final record RemoveGroupParentRequest(String Parent, String Child) implements PlayerGroupGroupChangeRequest {
    @Override
    public void apply(PlayerGroupDAG dag) {

    }

    @Override
    public void event() {

    }
}
