package com.techphonesnews.hyPlayerGroup.simTest;

import com.hypixel.hytale.logger.HytaleLogger;
import com.techphonesnews.hyPlayerGroup.Requests.*;

import java.util.UUID;

public final class RandomRequestFactory {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public static PlayerGroupGroupChangeRequest create(SimulationState state) {

        int r = state.random.nextInt(10);

        // CREATE GROUP
        if (r == 0 || state.groups.isEmpty()) {
            LOGGER.atInfo().log("Creating group");
            String name = "group_" + UUID.randomUUID().toString().substring(0, 6);
            state.groups.add(name);
            return new CreateGroupRequest(name);
        }

        // ADD PLAYER TO GROUP
        if (r == 4 || state.players.isEmpty()) {
            LOGGER.atInfo().log("Adding player to group");
            UUID player = state.getOrCreateRandomPlayer();
            return new AddPlayerToGroupRequest(player, state.randomGroup());
        }

        // DISBAND GROUP
        if (r == 1) {
            LOGGER.atInfo().log("Disbanding group");
            String g = state.randomGroup();
            return new DisbandGroupRequest(g);
        }

        // ADD GROUP PARENT
        if (r == 2) {
            LOGGER.atInfo().log("Adding group parent");
            String child = state.randomGroup();
            String parent = state.randomGroup();
            return new AddGroupParentRequest(parent, child);
        }

        // REMOVE GROUP PARENT
        if (r == 3) {
            LOGGER.atInfo().log("Removing group parent");
            String child = state.randomGroup();
            String parent = state.randomGroup();
            return new RemoveGroupParentRequest(parent, child);
        }


        // REMOVE PLAYER FROM GROUP
        if (r == 5) {
            LOGGER.atInfo().log("Removing player from group");
            return new RemovePlayerFromGroupRequest(
                    state.randomPlayer(),
                    state.randomGroup()
            );
        }

        // ADD GROUP PERMISSION
        if (r == 6) {
            LOGGER.atInfo().log("Adding group permission");
            return new AddGroupPermissonRequest(
                    state.randomGroup(),
                    state.randomPermissionSet()
            );
        }

        // REMOVE GROUP PERMISSION
        if (r == 7) {
            LOGGER.atInfo().log("Removing group permission");
            return new RemoveGroupPermissionRequest(
                    state.randomGroup(),
                    state.randomPermissionSet()
            );
        }

        // ADD PLAYER PERMISSION
        if (r == 8) {
            LOGGER.atInfo().log("Adding player permission");
            return new AddPlayerPermissionRequest(
                    state.randomPlayer(),
                    state.randomPermissionSet()
            );
        }

        // REMOVE PLAYER PERMISSION
        if (r == 9) {
            LOGGER.atInfo().log("Removing player permission");
            return new RemovePlayerPermissonRequest(
                    state.randomPlayer(),
                    state.randomPermissionSet()
            );
        }

        return null;
    }
}
