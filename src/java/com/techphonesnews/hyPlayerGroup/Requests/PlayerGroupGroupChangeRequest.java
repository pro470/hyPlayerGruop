package com.techphonesnews.hyPlayerGroup.Requests;

import com.techphonesnews.hyPlayerGroup.Group.PlayerGroupDAG;

sealed public interface PlayerGroupGroupChangeRequest permits AddGroupParentRequest,
        RemoveGroupParentRequest,
        AddPlayerToGroupRequest,
        RemovePlayerFromGroupRequest,
        AddPlayerPermissionRequest,
        RemovePlayerPermissonRequest,
        AddGroupPermissonRequest,
        RemoveGroupPermissionRequest {
    void apply(PlayerGroupDAG dag);

    void event();
}
