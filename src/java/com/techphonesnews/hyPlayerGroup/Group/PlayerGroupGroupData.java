package com.techphonesnews.hyPlayerGroup.Group;

import javax.annotation.Nonnull;
import java.util.*;

public record PlayerGroupGroupData(UUID id, String name, Set<UUID> ancestors, Set<UUID> descendants,
                                   Set<UUID> directMembers, Set<String> permissions) {

    public PlayerGroupGroupData(@Nonnull UUID id, @Nonnull String name, @Nonnull Set<UUID> ancestors, @Nonnull Set<UUID> descendants, @Nonnull Set<UUID> directMembers, @Nonnull Set<String> permissions) {
        this.id = id;
        this.name = name;
        this.ancestors = Collections.unmodifiableSet(ancestors);
        this.descendants = Collections.unmodifiableSet(descendants);
        this.directMembers = Collections.unmodifiableSet(directMembers);
        this.permissions = Collections.unmodifiableSet(permissions);
    }

}
