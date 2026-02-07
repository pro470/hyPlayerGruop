package com.techphonesnews.hyPlayerGroup.Requests;

import com.techphonesnews.hyPlayerGroup.Group.PlayerGroupAffected;
import com.techphonesnews.hyPlayerGroup.Group.PlayerGroupDAG;

sealed public interface PlayerGroupGroupChangeRequest permits AddGroupParentRequest, AddGroupPermissonRequest, AddPlayerPermissionRequest, AddPlayerToGroupRequest, CreateGroupRequest, DependedAndRequest, DependedOnFailRequest, DependedOnSuccessRequest, DependedOrRequest, DisbandGroupRequest, RemoveGroupParentRequest, RemoveGroupPermissionRequest, RemovePlayerFromGroupRequest, RemovePlayerPermissonRequest, TestSuccesfulBuildFlatRequest {
    void apply(PlayerGroupDAG dag);

    void event();

    String debugMessage();

    void affected(PlayerGroupAffected affected);

    Boolean succeeded();
}
