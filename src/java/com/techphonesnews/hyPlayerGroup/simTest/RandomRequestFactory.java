package com.techphonesnews.hyPlayerGroup.simTest;

import com.hypixel.hytale.logger.HytaleLogger;
import com.techphonesnews.hyPlayerGroup.Requests.*;

import java.util.UUID;

public final class RandomRequestFactory {

    private static final HytaleLogger LOGGER =
            HytaleLogger.forEnclosingClass();

    public static PlayerGroupGroupChangeRequest create(SimulationState state) {

        int r = state.random.nextInt(10);

        // CREATE GROUP
        if ((r == 0 || state.knownGroups.isEmpty())
                && state.canCreateGroup()) {

            String name = "group_" + UUID.randomUUID().toString().substring(0, 6);
            LOGGER.atInfo().log("CreateGroup " + name);
            return new CreateGroupRequest(name);
        }

        // DISBAND GROUP (BLIND)
        if (r == 1) {
            String g = state.randomGroup();
            LOGGER.atInfo().log("DisbandGroup " + g);
            return new DisbandGroupRequest(g);
        }

        // ADD GROUP PARENT
        if (r == 2) {
            return new AddGroupParentRequest(
                    state.randomGroup(),
                    state.randomGroup()
            );
        }

        // REMOVE GROUP PARENT
        if (r == 3) {
            return new RemoveGroupParentRequest(
                    state.randomGroup(),
                    state.randomGroup()
            );
        }

        // ADD PLAYER TO GROUP
        if (r == 4) {
            return new AddPlayerToGroupRequest(
                    state.getOrCreateRandomPlayer(),
                    state.randomGroup()
            );
        }

        // REMOVE PLAYER FROM GROUP
        if (r == 5 && !state.players.isEmpty()) {
            return new RemovePlayerFromGroupRequest(
                    state.randomPlayer(),
                    state.randomGroup()
            );
        }

        // ADD GROUP PERMISSION
        if (r == 6) {
            return new AddGroupPermissonRequest(
                    state.randomGroup(),
                    state.randomPermissionSet()
            );
        }

        // REMOVE GROUP PERMISSION
        if (r == 7) {
            return new RemoveGroupPermissionRequest(
                    state.randomGroup(),
                    state.randomPermissionSet()
            );
        }

        // ADD PLAYER PERMISSION
        if (r == 8 && !state.players.isEmpty()) {
            return new AddPlayerPermissionRequest(
                    state.randomPlayer(),
                    state.randomPermissionSet()
            );
        }

        // REMOVE PLAYER PERMISSION
        if (r == 9 && !state.players.isEmpty()) {
            return new RemovePlayerPermissonRequest(
                    state.randomPlayer(),
                    state.randomPermissionSet()
            );
        }

        return null;
    }
}
