package com.techphonesnews.hyPlayerGroup.Systems;

import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.system.DelayedSystem;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.techphonesnews.hyPlayerGroup.HyPlayerGroupPlugin;

import javax.annotation.Nonnull;

public class PlayerGroupSaveSystem extends DelayedSystem<EntityStore> {
    public PlayerGroupSaveSystem(float intervalSec) {
        super(intervalSec);
    }

    @Override
    public void delayedTick(float var1, int var2, @Nonnull Store<EntityStore> var3) {
        HyPlayerGroupPlugin.get().getConfig().save();
    }
}
