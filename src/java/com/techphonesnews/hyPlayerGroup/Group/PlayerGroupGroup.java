package com.techphonesnews.hyPlayerGroup.Group;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;

import java.util.*;

public record PlayerGroupGroup(UUID id, List<UUID> members, List<UUID> groupMembers) {
    public static final BuilderCodec<PlayerGroupGroup> CODEC = BuilderCodec.builder(PlayerGroupGroup.class, PlayerGroupGroup::new)
            .append(
                    new KeyedCodec<UUID>("Id", Codec.UUID_STRING),
                    (obj, val) -> obj = new PlayerGroupGroup(val, obj.members(), obj.groupMembers()),
                    PlayerGroupGroup::id
            ).add()
            .append(
                    new KeyedCodec<UUID[]>("Members", new ArrayCodec<>(Codec.UUID_STRING, UUID[]::new)),
                    (obj, val) -> obj.members().addAll(Arrays.asList(val)),
                    (obj) -> obj.members().toArray(new UUID[0])

            ).add()
            .append(
                    new KeyedCodec<UUID[]>("GroupMembers", new ArrayCodec<>(Codec.UUID_STRING, UUID[]::new)),
                    (obj, val) -> obj.groupMembers().addAll(Arrays.asList(val)),
                    (obj) -> obj.groupMembers().toArray(new UUID[0])
            ).add()
            .build();

    public PlayerGroupGroup {
        Objects.requireNonNull(id);
        Objects.requireNonNull(members);
        Objects.requireNonNull(groupMembers);
        members = new ArrayList<>(members);
        groupMembers = new ArrayList<>(groupMembers);
    }

    public PlayerGroupGroup() {
        this(UUID.randomUUID(), new ArrayList<>(), new ArrayList<>());
    }

    public List<UUID> getMembers() {
        return Collections.unmodifiableList(members);
    }

    public List<UUID> getGroupMembers() {
        return Collections.unmodifiableList(groupMembers);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof PlayerGroupGroup other && id.equals(other.id());
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
