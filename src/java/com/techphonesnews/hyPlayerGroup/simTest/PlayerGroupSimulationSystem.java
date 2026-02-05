package com.techphonesnews.hyPlayerGroup.simTest;

import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.system.tick.TickingSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.techphonesnews.hyPlayerGroup.Group.PlayerGroupDAGFlat;
import com.techphonesnews.hyPlayerGroup.HyPlayerGroupPlugin;
import com.techphonesnews.hyPlayerGroup.Requests.*;

import javax.annotation.Nonnull;
import java.util.Queue;

public final class PlayerGroupSimulationSystem
        extends TickingSystem<EntityStore> {

    private final Queue<PlayerGroupGroupChangeRequest> queue;
    private final SimulationState state = new SimulationState();
    private final int requestsPerTick;
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public PlayerGroupSimulationSystem(
            Queue<PlayerGroupGroupChangeRequest> queue,
            int requestsPerTick
    ) {
        this.queue = queue;
        this.requestsPerTick = requestsPerTick;

        PlayerGroupDAGFlat flat = HyPlayerGroupPlugin.get().getDAGFlat();

        state.aliveGroups.addAll(flat.groupsByName().keySet());
        state.knownGroups.addAll(flat.groupsByName().keySet());

        state.players.addAll(flat.players().playersGroups().keySet());
        state.players.addAll(flat.players().playersPermissions().keySet());
    }

    @Override
    public void tick(float delta, int tick, @Nonnull Store<EntityStore> store) {

        PlayerGroupDAGFlat flat = HyPlayerGroupPlugin.get().getDAGFlat();
        LOGGER.atInfo().log("Alive Groups: " + state.aliveGroups.size());
        LOGGER.atInfo().log("Known Groups: " + state.knownGroups.size());
        LOGGER.atInfo().log("Players: " + state.players.size());
        LOGGER.atInfo().log("GroupsFlat: " + flat.groups().size());

        int randomrequests = state.random.nextInt(requestsPerTick + 1);

        for (int i = 0; i < randomrequests; i++) {
            PlayerGroupGroupChangeRequest req =
                    RandomRequestFactory.create(state);

            if (req == null) {
                continue;
            }

            queue.add(req);

            // Feedback NACH dem Erzeugen
            if (req instanceof CreateGroupRequest(String name)) {
                state.onGroupCreated(name);
            }

            if (req instanceof DisbandGroupRequest(String name)) {
                state.onGroupDisbandAttempt(name);
            }
        }
    }
}
