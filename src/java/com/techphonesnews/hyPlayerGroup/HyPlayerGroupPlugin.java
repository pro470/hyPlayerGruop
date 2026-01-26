package com.techphonesnews.hyPlayerGroup;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.permissions.PermissionsModule;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.util.Config;
import com.techphonesnews.hyPlayerGroup.Group.PlayerGroupMemberGroup;
import com.techphonesnews.hyPlayerGroup.Group.PlayerGroupMemberProvider;
import com.techphonesnews.hyPlayerGroup.Permissions.PlayerGroupPermissionsProvider;
import com.techphonesnews.hyPlayerGroup.Systems.PlayerGroupSaveSystem;

import javax.annotation.Nonnull;

public class HyPlayerGroupPlugin extends JavaPlugin {

    protected static HyPlayerGroupPlugin instance;
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    Config<PlayerGroupPermissionsProvider> config;

    public static HyPlayerGroupPlugin get() {
        return instance;
    }

    public HyPlayerGroupPlugin(@Nonnull JavaPluginInit init) {
        super(init);
        LOGGER.atInfo().log("Hello from " + this.getName() + " version " + this.getManifest().getVersion().toString());
        instance = this;
        PlayerGroupMemberProvider.CODEC.register("Group", PlayerGroupMemberGroup.class, PlayerGroupMemberGroup.CODEC);
        this.config = this.withConfig(PlayerGroupPermissionsProvider.name, PlayerGroupPermissionsProvider.CODEC);
    }

    @Override
    protected void setup() {
        LOGGER.atInfo().log("Setting up plugin " + this.getName());
        super.setup();
        PermissionsModule.get().addProvider(this.config.get());
        Universe.get().getEntityStoreRegistry().registerSystem(new PlayerGroupSaveSystem(this.config.get().getInterval()));
        getLogger().atInfo().log("MyPlugin was successfully setup!");
    }

    @Override
    protected void start() {
        // You can disable core plugins here
        // Uncomment to disable Hytale's Teleport plugin
        //PluginIdentifier identifier = PluginIdentifier.fromString("Hytale:Teleport");
        //HytaleServer.get().getPluginManager().unload(identifier);
        // You can also override previously registered command here,
        // by registering in "start()" instead of "setup()"
        super.start();
    }

    public Config<PlayerGroupPermissionsProvider> getConfig() {
        return this.config;
    }

    @Override
    protected void shutdown() {
        LOGGER.atInfo().log("Shutting down plugin " + this.getName());
        this.config.save();
        super.shutdown();
        LOGGER.atInfo().log("Plugin " + this.getName() + " was successfully shutdown!");
    }
}