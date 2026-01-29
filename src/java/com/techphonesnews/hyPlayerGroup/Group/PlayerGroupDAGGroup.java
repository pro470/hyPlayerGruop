package com.techphonesnews.hyPlayerGroup.Group;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.set.SetCodec;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

final class PlayerGroupDAGGroup {

    public static final Codec<PlayerGroupDAGGroup> CODEC;
    private final UUID id;
    private String name;

    private final Set<UUID> parents = new HashSet<>();
    private final Set<UUID> children = new HashSet<>();

    private final Set<UUID> members = new HashSet<>();

    private final Set<String> permissions = new HashSet<>();

    public PlayerGroupDAGGroup(UUID id) {
        this.id = id;
    }

    public PlayerGroupDAGGroup() {
        this.id = null;
    }

    public UUID id() {
        return id;
    }

    public Set<UUID> members() {
        return members;
    }

    public Set<UUID> parents() {
        return parents;
    }

    public Set<UUID> children() {
        return children;
    }

    public Set<String> permissions() {
        return permissions;
    }

    public String name() {
        return name;
    }

    public void addParent(UUID parentId) {
        parents.add(parentId);
    }

    public void removeParent(UUID parentId) {
        parents.remove(parentId);
    }

    public void addChild(UUID childId) {
        children.add(childId);
    }

    public void removeChild(UUID childId) {
        children.remove(childId);
    }

    public void addMember(UUID uuid) {
        members.add(uuid);
    }

    public void removeMember(UUID uuid) {
        members.remove(uuid);
    }

    public void addPermission(String permission) {
        permissions.add(permission);
    }

    public void removePermission(String permission) {
        permissions.remove(permission);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    static {
        CODEC = BuilderCodec.builder(PlayerGroupDAGGroup.class, PlayerGroupDAGGroup::new)
                .append(
                        new KeyedCodec<UUID>(
                                "Id",
                                Codec.UUID_STRING
                        ),
                        (group, id) -> group = new PlayerGroupDAGGroup(id),
                        PlayerGroupDAGGroup::id
                ).add()
                .append(
                        new KeyedCodec<String>(
                                "Name",
                                Codec.STRING
                        ),
                        PlayerGroupDAGGroup::setName,
                        PlayerGroupDAGGroup::name
                ).add()
                .append(
                        new KeyedCodec<Set<UUID>>(
                                "Parents",
                                new SetCodec<>(
                                        Codec.UUID_STRING,
                                        HashSet::new,
                                        false
                                )
                        ),
                        (group, parents) -> group.parents.addAll(parents),
                        PlayerGroupDAGGroup::parents
                ).add()
                .append(
                        new KeyedCodec<Set<UUID>>(
                                "Children",
                                new SetCodec<>(
                                        Codec.UUID_STRING,
                                        HashSet::new,
                                        false
                                )
                        ),
                        (group, children) -> group.children.addAll(children),
                        PlayerGroupDAGGroup::children
                ).add()
                .append(
                        new KeyedCodec<Set<UUID>>(
                                "Members",
                                new SetCodec<>(
                                        Codec.UUID_STRING,
                                        HashSet::new,
                                        false
                                )
                        ),
                        (group, members) -> group.members.addAll(members),
                        PlayerGroupDAGGroup::members
                ).add()
                .append(
                        new KeyedCodec<Set<String>>(
                                "Permissions",
                                new SetCodec<>(
                                        Codec.STRING,
                                        HashSet::new,
                                        false
                                )
                        ),
                        (group, permissions) -> group.permissions.addAll(permissions),
                        PlayerGroupDAGGroup::permissions
                ).add()
                .build();
    }
}
