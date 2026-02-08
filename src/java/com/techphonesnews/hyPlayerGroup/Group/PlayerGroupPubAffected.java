package com.techphonesnews.hyPlayerGroup.Group;

import java.util.UUID;

public class PlayerGroupPubAffected {

    private final PlayerGroupAffected affected;

    public PlayerGroupPubAffected(PlayerGroupAffected affected) {
        this.affected = affected;
    }

    public boolean affectedAtAll(UUID uuid) {
        return affected.ancestors().contains(uuid) || affected.descendants().contains(uuid) || affected.directMembers().contains(uuid) || affected.permissions().contains(uuid) || affected.playersPermissions().contains(uuid);
    }

    public boolean affectedInAncestors(UUID uuid) {
        return affected.ancestors().contains(uuid);
    }

    public boolean affectedInDescendants(UUID uuid) {
        return affected.descendants().contains(uuid);
    }

    public boolean affectedInDirectMembers(UUID uuid) {
        return affected.directMembers().contains(uuid);
    }

    public boolean affectedInPermissions(UUID uuid) {
        return affected.permissions().contains(uuid);
    }

    public boolean affectedInPlayersPermissions(UUID uuid) {
        return affected.playersPermissions().contains(uuid);
    }
}
