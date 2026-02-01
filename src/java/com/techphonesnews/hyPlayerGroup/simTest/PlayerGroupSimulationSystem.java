package com.techphonesnews.hyPlayerGroup.simTest;

import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.system.tick.TickingSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.techphonesnews.hyPlayerGroup.Group.PlayerGroupDAGFlat;
import com.techphonesnews.hyPlayerGroup.Group.PlayerGroupGroupData;
import com.techphonesnews.hyPlayerGroup.HyPlayerGroupPlugin;
import com.techphonesnews.hyPlayerGroup.Requests.DisbandGroupRequest;
import com.techphonesnews.hyPlayerGroup.Requests.PlayerGroupGroupChangeRequest;

import javax.annotation.Nonnull;
import java.util.Queue;

public final class PlayerGroupSimulationSystem
        extends TickingSystem<EntityStore> {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private final Queue<PlayerGroupGroupChangeRequest> queue;
    private final SimulationState state = new SimulationState();
    private final int requestsPerTick;

    public PlayerGroupSimulationSystem(
            Queue<PlayerGroupGroupChangeRequest> queue,
            int requestsPerTick
    ) {
        this.queue = queue;
        this.requestsPerTick = requestsPerTick;
        PlayerGroupDAGFlat flat = HyPlayerGroupPlugin.get().getDAGFlat();
        state.groups.addAll(flat.groupsByName().keySet());
        state.players.addAll(flat.players().playersPermissions().keySet());
        state.players.addAll(flat.players().playersGroups().keySet());
    }

    @Override
    public void tick(float delta, int tick, @Nonnull Store<EntityStore> store) {
        PlayerGroupDAGFlat flat = HyPlayerGroupPlugin.get().getDAGFlat();
        if (flat.groups().size() > 100) {
            int toRemove = flat.groups().size() - 100;
            for (PlayerGroupGroupData group : flat.groups().values()) {
                if (toRemove == 0) {
                    break;
                }
                queue.add(new DisbandGroupRequest(group.name()));
                toRemove--;
            }
        }
        for (int i = 0; i < requestsPerTick; i++) {
            PlayerGroupGroupChangeRequest req =
                    RandomRequestFactory.create(state);

            if (req != null) {
                LOGGER.atInfo().log("Creating request");
                queue.add(req);
            }
        }
    }
}
