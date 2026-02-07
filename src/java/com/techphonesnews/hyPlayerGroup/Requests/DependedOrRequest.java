package com.techphonesnews.hyPlayerGroup.Requests;

import com.techphonesnews.hyPlayerGroup.Group.PlayerGroupAffected;
import com.techphonesnews.hyPlayerGroup.Group.PlayerGroupDAG;

import java.util.Collection;
import java.util.stream.Collectors;

public final class DependedOrRequest implements PlayerGroupGroupChangeRequest {
    private final Collection<? extends PlayerGroupGroupChangeRequest> dependencies;
    private final PlayerGroupGroupChangeRequest request;

    private boolean succeeded = false;

    public DependedOrRequest(Collection<? extends PlayerGroupGroupChangeRequest> dependencies, PlayerGroupGroupChangeRequest request) {
        this.dependencies = dependencies;
        this.request = request;
    }

    @Override
    public void apply(PlayerGroupDAG dag) {
        if (doesDependencySucceed()) {
            request.apply(dag);
            succeeded = request.succeeded();
        }
    }

    @Override
    public void event() {
        request.event();
    }

    @Override
    public String debugMessage() {
        return request.debugMessage() + " \nOr Depended on: " + dependencies.stream().map(PlayerGroupGroupChangeRequest::debugMessage).collect(Collectors.joining(", "));
    }

    @Override
    public void affected(PlayerGroupAffected affected) {
        request.affected(affected);
    }

    @Override
    public Boolean succeeded() {
        return succeeded;
    }

    private boolean doesDependencySucceed() {
        for (PlayerGroupGroupChangeRequest request : dependencies) {
            if (request.succeeded() == null) {
                return false;
            }
            if (request.succeeded()) {
                return true;
            }
        }
        return false;
    }
}
