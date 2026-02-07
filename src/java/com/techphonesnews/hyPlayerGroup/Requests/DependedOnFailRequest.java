package com.techphonesnews.hyPlayerGroup.Requests;

import com.techphonesnews.hyPlayerGroup.Group.PlayerGroupAffected;
import com.techphonesnews.hyPlayerGroup.Group.PlayerGroupDAG;

public final class DependedOnFailRequest implements PlayerGroupGroupChangeRequest {

    private final PlayerGroupGroupChangeRequest dependedOn;
    private final PlayerGroupGroupChangeRequest request;
    private Boolean succeeded;

    public DependedOnFailRequest(PlayerGroupGroupChangeRequest request, PlayerGroupGroupChangeRequest dependedOn) {
        this.request = request;
        this.dependedOn = dependedOn;
    }

    @Override
    public void apply(PlayerGroupDAG dag) {
        if (dependedOn.succeeded()) {
            succeeded = false;
            return;
        }
        request.apply(dag);
        succeeded = request.succeeded();
    }

    @Override
    public void event() {
        request.event();
    }

    @Override
    public String debugMessage() {
        return request.debugMessage() + " \nDepended on fail: " + request.debugMessage();
    }

    @Override
    public void affected(PlayerGroupAffected affected) {
        request.affected(affected);
    }

    @Override
    public Boolean succeeded() {
        return succeeded;
    }
}
