package com.techphonesnews.hyPlayerGroup.Validator;

import com.techphonesnews.hyPlayerGroup.Group.PlayerGroupDAGFlat;
import com.techphonesnews.hyPlayerGroup.Group.PlayerGroupGroupData;

import java.util.*;

public class PlayerGroupValidator {

    public static void validate(PlayerGroupDAGFlat flat) {
        validateAncestorDescendantExistence(flat);
        validateNoSelfReachability(flat);
        validateAncestorsAndDescendants(flat);
        validatedirectMembersInPlayersGroups(flat);
        validatePlayerGroupsInDirectMembers(flat);
        validateTransitiveClosure(flat);
        validateGroupsByNameReferExistingGroups(flat);
        validateGroupNamesMatch(flat);
        validateGroupNamesAreUnique(flat);
        validateAllGroupsAppearInGroupsByName(flat);
        validatePermissionInheritance(flat);
    }

    public static void validatedirectMembersInPlayersGroups(PlayerGroupDAGFlat flat) {

        Map<UUID, Set<UUID>> playersGroups = flat.players().playersGroups();

        for (PlayerGroupGroupData group : flat.groups().values()) {
            UUID gid = group.id();

            for (UUID player : group.directMembers()) {
                Set<UUID> groupsOfPlayer = playersGroups.get(player);
                if (groupsOfPlayer == null || !groupsOfPlayer.contains(gid)) {
                    throw new AssertionError(
                            "Player " + player + " is in group " + gid + " (" + group.name() + ")" +
                                    " but not in playersGroups map"
                    );
                }
            }
        }
    }

    public static void validatePlayerGroupsInDirectMembers(PlayerGroupDAGFlat flat) {
        Map<UUID, Set<UUID>> playersGroups = flat.players().playersGroups();

        for (Map.Entry<UUID, Set<UUID>> entry : playersGroups.entrySet()) {
            UUID player = entry.getKey();

            for (UUID gid : entry.getValue()) {
                PlayerGroupGroupData group = flat.groups().get(gid);

                if (group == null) {
                    throw new AssertionError(
                            "playersGroups contains group " + gid +
                                    " for player " + player +
                                    " but group does not exist"
                    );
                }

                if (!group.directMembers().contains(player)) {
                    throw new AssertionError(
                            "playersGroups says player " + player +
                                    " is in group " + gid + " (" + group.name() + ")" +
                                    " but group.directMembers does not contain player"
                    );
                }
            }
        }
    }

    public static void validateAncestorsAndDescendants(PlayerGroupDAGFlat flat) {
        for (PlayerGroupGroupData g : flat.groups().values()) {
            for (UUID ancestor : g.ancestors()) {
                PlayerGroupGroupData a = flat.groups().get(ancestor);
                if (a == null) {
                    throw new AssertionError(
                            "Group " + g.id() +
                                    " has ancestor " + ancestor +
                                    " which does not exist"
                    );
                }

                if (!a.descendants().contains(g.id())) {
                    throw new AssertionError(
                            "Ancestor/descendant mismatch: " +
                                    "group " + ancestor + " (" + a.name() + ")" +
                                    " is ancestor of " + g.id() + " (" + g.name() + ")" +
                                    " but does not list it as descendant"
                    );
                }
            }
            for (UUID descendant :
                    g.descendants()) {
                PlayerGroupGroupData d = flat.groups().get(descendant);
                if (d == null) {
                    throw new AssertionError(
                            "Group " + g.id() + " (" + g.name() + ")" +
                                    " has descendant " + descendant +
                                    " which does not exist"
                    );
                }

                if (!d.ancestors().contains(g.id())) {
                    throw new AssertionError(
                            "Descendant/ancestor mismatch: " +
                                    "group " + descendant +
                                    " is descendant of " + g.id() + " (" + g.name() + ")" +
                                    " but does not list it as ancestor"
                    );
                }
            }
        }
    }

    public static void validateTransitiveClosure(PlayerGroupDAGFlat flat) {

        for (PlayerGroupGroupData a : flat.groups().values()) {
            UUID aId = a.id();

            for (UUID bId : a.descendants()) {
                PlayerGroupGroupData b = flat.groups().get(bId);
                if (b == null) {
                    throw new AssertionError(
                            "Group " + aId + " (" + a.name() + ")" + " has non-existing descendant " + bId
                    );
                }

                for (UUID cId : b.descendants()) {
                    if (!a.descendants().contains(cId)) {
                        throw new AssertionError(
                                "Transitivity violated: " +
                                        aId + " (" + a.name() + ")" + " -> " + bId + " (" + b.name() + ")" + " -> " + cId +
                                        " but " + cId + " not in descendants of " + aId
                        );
                    }

                    PlayerGroupGroupData c = flat.groups().get(cId);
                    if (c == null || !c.ancestors().contains(aId)) {
                        throw new AssertionError(
                                "Transitivity violated (ancestor side): " +
                                        aId + " (" + a.name() + ")" + " should be ancestor of " + cId + " (" + c.name() + ")"
                        );
                    }
                }
            }
        }
    }

    public static void validateNoSelfReachability(PlayerGroupDAGFlat flat) {

        for (PlayerGroupGroupData g : flat.groups().values()) {
            UUID id = g.id();

            if (g.ancestors().contains(id)) {
                throw new AssertionError(
                        "Group " + id + " (" + g.name() + ")" + " contains itself in ancestors"
                );
            }

            if (g.descendants().contains(id)) {
                throw new AssertionError(
                        "Group " + id + " (" + g.name() + ")" + " contains itself in descendants"
                );
            }
        }
    }

    public static void validateGroupsByNameReferExistingGroups(PlayerGroupDAGFlat flat) {

        Map<String, UUID> groupsByName = flat.groupsByName();
        Map<UUID, PlayerGroupGroupData> groups = flat.groups();

        for (Map.Entry<String, UUID> entry : groupsByName.entrySet()) {
            String name = entry.getKey();
            UUID id = entry.getValue();

            if (!groups.containsKey(id)) {
                throw new AssertionError(
                        "groupsByName contains entry '" + name +
                                "' -> " + id +
                                " but no such group exists"
                );
            }
        }
    }

    public static void validateGroupNamesMatch(PlayerGroupDAGFlat flat) {

        Map<String, UUID> groupsByName = flat.groupsByName();
        Map<UUID, PlayerGroupGroupData> groups = flat.groups();

        for (Map.Entry<String, UUID> entry : groupsByName.entrySet()) {
            String name = entry.getKey();
            UUID id = entry.getValue();

            PlayerGroupGroupData group = groups.get(id);
            if (group == null) {
                // wird eigentlich schon durch Invariant E abgefangen
                continue;
            }

            if (!name.equals(group.name())) {
                throw new AssertionError(
                        "Group name mismatch: groupsByName has '" + name +
                                "' but group " + id +
                                " has name '" + group.name() + "'"
                );
            }
        }
    }

    public static void validateGroupNamesAreUnique(PlayerGroupDAGFlat flat) {

        Map<String, UUID> groupsByName = flat.groupsByName();
        Set<UUID> seen = new HashSet<>();

        for (Map.Entry<String, UUID> entry : groupsByName.entrySet()) {
            UUID id = entry.getValue();

            if (!seen.add(id)) {
                throw new AssertionError(
                        "Multiple group names map to the same group id: " + id
                );
            }
        }
    }

    public static void validateAllGroupsAppearInGroupsByName(PlayerGroupDAGFlat flat) {

        Map<String, UUID> groupsByName = flat.groupsByName();
        Set<UUID> referenced = new HashSet<>(flat.groupsByName().values());

        for (UUID groupId : flat.groups().keySet()) {
            if (!referenced.contains(groupId)) {
                throw new AssertionError(
                        "Group " + groupId + " is missing from groupsByName"
                );
            }
        }
    }

    public static void validatePermissionInheritance(PlayerGroupDAGFlat flat) {

        for (PlayerGroupGroupData group : flat.groups().values()) {

            Set<String> ownPerms = group.permissions();

            for (UUID descId : group.descendants()) {
                PlayerGroupGroupData desc = flat.groups().get(descId);
                if (desc == null) {
                    throw new AssertionError(
                            "Descendant group " + descId + " does not exist"
                    );
                }

                if (!desc.permissions().containsAll(ownPerms)) {
                    Set<String> missing = new HashSet<>(ownPerms);
                    missing.removeAll(desc.permissions());

                    throw new AssertionError(
                            "Permission inheritance violated: group " + group.id() + " (" + group.name() + ")" +
                                    " permissions " + missing +
                                    " missing in descendant " + descId + " (" + desc.name() + ")"
                    );
                }
            }
        }
    }


    public static void validateAncestorDescendantExistence(PlayerGroupDAGFlat flat) {

        Set<UUID> existingGroups = flat.groups().keySet();

        for (PlayerGroupGroupData group : flat.groups().values()) {

            for (UUID ancestor : group.ancestors()) {
                if (!existingGroups.contains(ancestor)) {
                    throw new AssertionError(
                            "Group " + group.id() + " (" + group.name() + ")" +
                                    " has non-existing ancestor " + ancestor
                    );
                }
            }

            for (UUID descendant : group.descendants()) {
                if (!existingGroups.contains(descendant)) {
                    throw new AssertionError(
                            "Group " + group.id() + " (" + group.name() + ")" +
                                    " has non-existing descendant " + descendant
                    );
                }
            }
        }
    }

}
