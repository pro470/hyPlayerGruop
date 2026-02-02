package com.techphonesnews.hyPlayerGroup.Requests;

import com.techphonesnews.hyPlayerGroup.Group.PlayerGroupAffected;
import com.techphonesnews.hyPlayerGroup.Group.PlayerGroupDAG;

import javax.annotation.Nonnull;

sealed public interface PlayerGroupGroupChangeRequest permits AddGroupParentRequest, AddGroupPermissonRequest, AddPlayerPermissionRequest, AddPlayerToGroupRequest, CreateGroupRequest, DisbandGroupRequest, RemoveGroupParentRequest, RemoveGroupPermissionRequest, RemovePlayerFromGroupRequest, RemovePlayerPermissonRequest, TestSuccesfulBuildFlatRequest {
    void apply(PlayerGroupDAG dag);

    void event();

    String debugMessage();

    @Nonnull
    PlayerGroupAffected affected();

}
