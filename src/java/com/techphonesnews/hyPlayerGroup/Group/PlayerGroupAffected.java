package com.techphonesnews.hyPlayerGroup.Group;

import java.util.Set;
import java.util.UUID;

public record PlayerGroupAffected(Set<UUID> ancestors, Set<UUID> descendants, Set<UUID> permissions,
                                  Set<UUID> directMembers) {

    public static final PlayerGroupAffected EMPTY = new PlayerGroupAffected(Set.of(), Set.of(), Set.of(), Set.of());

    public Boolean isEmpty() {
        return ancestors.isEmpty() && descendants.isEmpty() && permissions.isEmpty() && directMembers.isEmpty();
    }
}
