package com.techphonesnews.hyPlayerGroup;

import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.system.tick.TickingSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.permissions.PermissionsModule;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.BsonUtil;
import com.hypixel.hytale.server.core.util.Config;
import com.hypixel.hytale.sneakythrow.SneakyThrow;
import com.techphonesnews.hyPlayerGroup.Group.*;
import com.techphonesnews.hyPlayerGroup.Permissions.PlayerGroupPermissionsProvider;
import com.techphonesnews.hyPlayerGroup.Requests.PlayerGroupGroupChangeRequest;
import com.techphonesnews.hyPlayerGroup.Validator.PlayerGroupValidator;
import com.techphonesnews.hyPlayerGroup.simTest.PlayerGroupSimulationSystem;

import javax.annotation.Nonnull;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicReference;

public class HyPlayerGroupPlugin extends JavaPlugin {

    protected static HyPlayerGroupPlugin instance;
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private final AtomicReference<PlayerGroupDAGFlat> dagFlat = new AtomicReference<PlayerGroupDAGFlat>(new PlayerGroupDAGFlat(Map.of(), Map.of(), new PlayerGroupPlayerData(Map.of(), Map.of())));
    private final Config<PlayerGroupDAG> dag;
    private final ConcurrentLinkedQueue<PlayerGroupGroupChangeRequest> queue = new ConcurrentLinkedQueue<>();
    private final Path dataDirectory;
    private CompletableFuture<FinishedBuild> buildingDAGFlat;

    public final PlayerGroupPermissionsProvider provider = new PlayerGroupPermissionsProvider(dagFlat, queue);

    public static HyPlayerGroupPlugin get() {
        return instance;
    }

    public HyPlayerGroupPlugin(@Nonnull JavaPluginInit init) {
        super(init);
        this.dataDirectory = init.getDataDirectory();
        LOGGER.atInfo().log("Hello from " + this.getName() + " version " + this.getManifest().getVersion().toString());
        instance = this;
        this.dag = this.withConfig(PlayerGroupDAG.name, PlayerGroupDAG.CODEC);
    }

    @Override
    protected void setup() {
        LOGGER.atInfo().log("Setting up plugin " + this.getName());
        super.setup();
        PlayerGroupDAG dag = this.dag.get();
        this.dagFlat.set(PlayerGroupDAG.buildFlat(dag, dagFlat.get(), new PlayerGroupAffected(dag.groups(), dag.groups(), dag.groups(), dag.groups())));
        PermissionsModule.get().addProvider(provider);
        Universe.get().getEntityStoreRegistry().registerSystem(new HandleRequestSystem());
        this.getEntityStoreRegistry().registerSystem(new PlayerGroupSimulationSystem(queue, 100));
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

    public PlayerGroupDAGFlat getDAGFlat() {
        return dagFlat.get();
    }

    private PlayerGroupDAGFlat getNewBuildingDAGFlat() {
        return buildingDAGFlat.join().flat;
    }

    public PlayerGroupDAGFlat ifNewWaitOnBuildingDAGFlat() {
        if (isBuildingDAGFlat()) {
            return getNewBuildingDAGFlat();
        }
        return dagFlat.get();
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

        private final List<PlayerGroupGroupChangeRequest> requests = new ArrayList<>();

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
                                        List<String> debugMessages = new ArrayList<>();
                                        PlayerGroupAffected affected = new PlayerGroupAffected(new HashSet<>(), new HashSet<>(), new HashSet<>(), new HashSet<>());
                                        while (!queue.isEmpty()) {
                                            PlayerGroupGroupChangeRequest request = queue.poll();
                                            request.apply(dag.get());
                                            affected.ancestors().addAll(request.affected().ancestors());
                                            affected.descendants().addAll(request.affected().descendants());
                                            affected.permissions().addAll(request.affected().permissions());
                                            affected.directMembers().addAll(request.affected().directMembers());
                                            requests.add(request);
                                            debugMessages.add(request.debugMessage());
                                        }

                                        PlayerGroupDAGFlat bFlat = PlayerGroupDAG.buildFlat(dag.get(), dagFlat.get(), affected);
                                        dagFlat.set(bFlat);
                                        PlayerGroupValidator.validate(bFlat);
                                        return new FinishedBuild(bFlat, requests);
                                    }
                            )
                    )
                    .whenComplete(
                            SneakyThrow.sneakyConsumer(
                                    (finished, exception) -> {

                                        if (exception != null) {

                                            Throwable cause =
                                                    exception instanceof CompletionException ce
                                                            ? ce.getCause()
                                                            : exception;

                                            panicform(
                                                    cause,
                                                    dag.get(),
                                                    dataDirectory,
                                                    requests
                                            );

                                            hardReset();
                                            return;
                                        }

                                        for (PlayerGroupGroupChangeRequest request : finished.requests) {
                                            request.event();
                                        }

                                        dag.save();
                                    }
                            )
                    );

        }

        public static void panicform(
                Throwable error,
                PlayerGroupDAG dag,
                Path dataDirectory,
                List<PlayerGroupGroupChangeRequest> requests) {
            try {
                String stamp = Instant.now()
                        .toString()
                        .replace(":", "-");

                Path panicDir = dataDirectory
                        .resolve("panic")
                        .resolve(stamp);

                Files.createDirectories(panicDir);

                // 1️⃣ DAG snapshot (write-only)
                BsonUtil.writeDocument(
                        panicDir.resolve("dag.json"),
                        PlayerGroupDAG.CODEC.encode(dag, new ExtraInfo())
                );

                List<String> debugMessages = requests.stream()
                        .map(PlayerGroupGroupChangeRequest::debugMessage)
                        .toList();

                requests.clear();

                // 3️⃣ Requests
                Files.write(
                        panicDir.resolve("requests.log"),
                        debugMessages
                );

                // 5️⃣ Error
                Files.writeString(
                        panicDir.resolve("error.txt"),
                        error.toString()
                );

                LOGGER.atWarning().log("PANIC FORM written to " + panicDir);

            } catch (Exception snapshotError) {
                LOGGER.atSevere().log("Failed to write panic snapshot:" + snapshotError);
            }
        }
    }

    private void hardReset() {
        LOGGER.atWarning().log("HARD RESETTING PLAYER GROUP SYSTEM");

        queue.clear();
        buildingDAGFlat = null;

        PlayerGroupDAG dag = this.dag.get();
        this.dagFlat.set(PlayerGroupDAG.buildFlat(dag, dagFlat.get(), new PlayerGroupAffected(dag.groups(), dag.groups(), dag.groups(), dag.groups())));
    }
}