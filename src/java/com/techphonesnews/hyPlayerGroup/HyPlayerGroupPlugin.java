package com.techphonesnews.hyPlayerGroup;

import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.system.tick.TickingSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.permissions.PermissionsModule;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.Config;
import com.hypixel.hytale.sneakythrow.SneakyThrow;
import com.techphonesnews.hyPlayerGroup.Group.*;
import com.techphonesnews.hyPlayerGroup.Permissions.PlayerGroupPermissionsProvider;
import com.techphonesnews.hyPlayerGroup.Requests.PlayerGroupGroupChangeRequest;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicReference;

public class HyPlayerGroupPlugin extends JavaPlugin {

    protected static HyPlayerGroupPlugin instance;
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private final AtomicReference<PlayerGroupDAGFlat> dagFlat = new AtomicReference<PlayerGroupDAGFlat>(new PlayerGroupDAGFlat(Map.of(), new PlayerGroupPlayerData(Map.of(), Map.of())));
    private final Config<PlayerGroupDAG> dag;
    private final ConcurrentLinkedQueue<PlayerGroupGroupChangeRequest> queue = new ConcurrentLinkedQueue<>();
    private CompletableFuture<FinishedBuild> buildingDAGFlat;

    public final PlayerGroupPermissionsProvider provider = new PlayerGroupPermissionsProvider(dagFlat, queue);

    public static HyPlayerGroupPlugin get() {
        return instance;
    }

    public HyPlayerGroupPlugin(@Nonnull JavaPluginInit init) {
        super(init);
        LOGGER.atInfo().log("Hello from " + this.getName() + " version " + this.getManifest().getVersion().toString());
        instance = this;
        this.dag = this.withConfig(PlayerGroupDAG.name, PlayerGroupDAG.CODEC);
    }

    @Override
    protected void setup() {
        LOGGER.atInfo().log("Setting up plugin " + this.getName());
        super.setup();
        PermissionsModule.get().addProvider(provider);
        Universe.get().getEntityStoreRegistry().registerSystem(new HandleRequestSystem());
        getLogger().atInfo().log("MyPlugin was successfully setup!");
    }

    @Override
    protected void start() {
        super.start();
    }

    public PlayerGroupDAGFlat getDagFlat() {
        return dagFlat.get();
    }

    public void submitRequest(PlayerGroupGroupChangeRequest request) {
        queue.add(request);
    }

    public Boolean isBuildingDAGFlat() {
        return buildingDAGFlat != null && !buildingDAGFlat.isDone();
    }

    public PlayerGroupDAGFlat getNewBuildingDAGFlat() {
        return buildingDAGFlat.join().flat;
    }

    @Override
    protected void shutdown() {
        LOGGER.atInfo().log("Shutting down plugin " + this.getName());
        dag.save();
        super.shutdown();
        LOGGER.atInfo().log("Plugin " + this.getName() + " was successfully shutdown!");
    }

    private record FinishedBuild(PlayerGroupDAGFlat flat, List<PlayerGroupGroupChangeRequest> requests) {

    }

    private class HandleRequestSystem extends TickingSystem<EntityStore> {

        @Override
        public void tick(float v, int i, @Nonnull Store<EntityStore> store) {
            if (buildingDAGFlat != null) {
                if (!buildingDAGFlat.isDone()) {
                    return;
                } else {
                    buildingDAGFlat = null;
                }
            }

            if (queue.isEmpty()) {
                return;
            }

            buildingDAGFlat = CompletableFuture.supplyAsync(
                    SneakyThrow.sneakySupplier(
                            () -> {
                                List<PlayerGroupGroupChangeRequest> requests = new ArrayList<>();
                                while (!queue.isEmpty()) {
                                    PlayerGroupGroupChangeRequest request = queue.poll();
                                    request.apply(dag.get());
                                    requests.add(request);
                                }

                                PlayerGroupDAGFlat bFlat = PlayerGroupDAG.buildFlat(dag.get());
                                dagFlat.set(bFlat);
                                return new FinishedBuild(bFlat, requests);
                            }
                    )
            ).whenComplete(SneakyThrow.sneakyConsumer(
                    (finished, _) -> {
                        for (PlayerGroupGroupChangeRequest request : finished.requests) {
                            request.event();
                        }
                        dag.save();
                    }
            ));

        }
    }
}