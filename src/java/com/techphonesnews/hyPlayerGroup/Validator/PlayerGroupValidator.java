package com.techphonesnews.hyPlayerGroup.Validator;

import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.util.BsonUtil;
import com.techphonesnews.hyPlayerGroup.Group.PlayerGroupDAG;
import com.techphonesnews.hyPlayerGroup.Group.PlayerGroupDAGFlat;
import com.techphonesnews.hyPlayerGroup.Group.PlayerGroupGroupData;
import com.techphonesnews.hyPlayerGroup.Requests.PlayerGroupGroupChangeRequest;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;

public class PlayerGroupValidator {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public static void validate(PlayerGroupDAGFlat flat, List<String> debugMessages) {
        LOGGER.atInfo().log("Validating PlayerGroupDAGFlat");
        validateAncestorDescendantExistence(flat, debugMessages);
        validateNoSelfReachability(flat, debugMessages);
        validateAncestorsAndDescendants(flat, debugMessages);
        validatedirectMembersInPlayersGroups(flat, debugMessages);
        validatePlayerGroupsInDirectMembers(flat, debugMessages);
        validateTransitiveClosure(flat, debugMessages);
        validateGroupsByNameReferExistingGroups(flat, debugMessages);
        validateGroupNamesMatch(flat, debugMessages);
        validateGroupNamesAreUnique(flat, debugMessages);
        validateAllGroupsAppearInGroupsByName(flat, debugMessages);
        validatePermissionInheritance(flat, debugMessages);
    }

    private static void printDebugMessages(List<String> debugMessages) {
        for (String message : debugMessages) {
            LOGGER.atWarning().log(message);
        }
    }

    private static void validatedirectMembersInPlayersGroups(PlayerGroupDAGFlat flat, List<String> debugMessages) {

        Map<UUID, Set<UUID>> playersGroups = flat.players().playersGroups();

        for (PlayerGroupGroupData group : flat.groups().values()) {
            UUID gid = group.id();

            for (UUID player : group.directMembers()) {
                Set<UUID> groupsOfPlayer = playersGroups.get(player);
                if (groupsOfPlayer == null || !groupsOfPlayer.contains(gid)) {
                    printDebugMessages(debugMessages);
                    throw new AssertionError(
                            "Player " + player + " is in group " + gid + " (" + group.name() + ")" +
                                    " but not in playersGroups map"
                    );
                }
            }
        }
    }

    private static void validatePlayerGroupsInDirectMembers(PlayerGroupDAGFlat flat, List<String> debugMessages) {
        Map<UUID, Set<UUID>> playersGroups = flat.players().playersGroups();

        for (Map.Entry<UUID, Set<UUID>> entry : playersGroups.entrySet()) {
            UUID player = entry.getKey();

            for (UUID gid : entry.getValue()) {
                PlayerGroupGroupData group = flat.groups().get(gid);

                if (group == null) {
                    printDebugMessages(debugMessages);
                    throw new AssertionError(
                            "playersGroups contains group " + gid +
                                    " for player " + player +
                                    " but group does not exist"
                    );
                }

                if (!group.directMembers().contains(player)) {
                    printDebugMessages(debugMessages);
                    throw new AssertionError(
                            "playersGroups says player " + player +
                                    " is in group " + gid + " (" + group.name() + ")" +
                                    " but group.directMembers does not contain player"
                    );
                }
            }
        }
    }

    private static void validateAncestorsAndDescendants(PlayerGroupDAGFlat flat, List<String> debugMessages) {
        for (PlayerGroupGroupData g : flat.groups().values()) {
            LOGGER.atInfo().log("Validating group " + g.id());
            for (UUID ancestor : g.ancestors()) {
                PlayerGroupGroupData a = flat.groups().get(ancestor);
                if (a == null) {
                    printDebugMessages(debugMessages);
                    throw new AssertionError(
                            "Group " + g.id() +
                                    " has ancestor " + ancestor +
                                    " which does not exist"
                    );
                }

                if (!a.descendants().contains(g.id())) {
                    printDebugMessages(debugMessages);
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
                    printDebugMessages(debugMessages);
                    throw new AssertionError(
                            "Group " + g.id() + " (" + g.name() + ")" +
                                    " has descendant " + descendant +
                                    " which does not exist"
                    );
                }

                if (!d.ancestors().contains(g.id())) {
                    printDebugMessages(debugMessages);
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

    private static void validateTransitiveClosure(PlayerGroupDAGFlat flat, List<String> debugMessages) {

        for (PlayerGroupGroupData a : flat.groups().values()) {
            UUID aId = a.id();

            for (UUID bId : a.descendants()) {
                PlayerGroupGroupData b = flat.groups().get(bId);
                if (b == null) {
                    printDebugMessages(debugMessages);
                    throw new AssertionError(
                            "Group " + aId + " (" + a.name() + ")" + " has non-existing descendant " + bId
                    );
                }

                for (UUID cId : b.descendants()) {
                    if (!a.descendants().contains(cId)) {
                        printDebugMessages(debugMessages);
                        throw new AssertionError(
                                "Transitivity violated: " +
                                        aId + " (" + a.name() + ")" + " -> " + bId + " (" + b.name() + ")" + " -> " + cId +
                                        " but " + cId + " not in descendants of " + aId
                        );
                    }

                    PlayerGroupGroupData c = flat.groups().get(cId);
                    if (c == null || !c.ancestors().contains(aId)) {
                        printDebugMessages(debugMessages);
                        throw new AssertionError(
                                "Transitivity violated (ancestor side): " +
                                        aId + " (" + a.name() + ")" + " should be ancestor of " + cId + " (" + c.name() + ")"
                        );
                    }
                }
            }
        }
    }

    private static void validateNoSelfReachability(PlayerGroupDAGFlat flat, List<String> debugMessages) {

        for (PlayerGroupGroupData g : flat.groups().values()) {
            UUID id = g.id();

            if (g.ancestors().contains(id)) {
                printDebugMessages(debugMessages);
                throw new AssertionError(
                        "Group " + id + " (" + g.name() + ")" + " contains itself in ancestors"
                );
            }

            if (g.descendants().contains(id)) {
                printDebugMessages(debugMessages);
                throw new AssertionError(
                        "Group " + id + " (" + g.name() + ")" + " contains itself in descendants"
                );
            }
        }
    }

    private static void validateGroupsByNameReferExistingGroups(PlayerGroupDAGFlat flat, List<String> debugMessages) {

        Map<String, UUID> groupsByName = flat.groupsByName();
        Map<UUID, PlayerGroupGroupData> groups = flat.groups();

        for (Map.Entry<String, UUID> entry : groupsByName.entrySet()) {
            String name = entry.getKey();
            UUID id = entry.getValue();

            if (!groups.containsKey(id)) {
                printDebugMessages(debugMessages);
                throw new AssertionError(
                        "groupsByName contains entry '" + name +
                                "' -> " + id +
                                " but no such group exists"
                );
            }
        }
    }

    private static void validateGroupNamesMatch(PlayerGroupDAGFlat flat, List<String> debugMessages) {

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
                printDebugMessages(debugMessages);
                throw new AssertionError(
                        "Group name mismatch: groupsByName has '" + name +
                                "' but group " + id +
                                " has name '" + group.name() + "'"
                );
            }
        }
    }

    private static void validateGroupNamesAreUnique(PlayerGroupDAGFlat flat, List<String> debugMessages) {

        Map<String, UUID> groupsByName = flat.groupsByName();
        Set<UUID> seen = new HashSet<>();

        for (Map.Entry<String, UUID> entry : groupsByName.entrySet()) {
            UUID id = entry.getValue();

            if (!seen.add(id)) {
                printDebugMessages(debugMessages);
                throw new AssertionError(
                        "Multiple group names map to the same group id: " + id
                );
            }
        }
    }

    private static void validateAllGroupsAppearInGroupsByName(PlayerGroupDAGFlat flat, List<String> debugMessages) {

        Map<String, UUID> groupsByName = flat.groupsByName();
        Set<UUID> referenced = new HashSet<>(flat.groupsByName().values());

        for (UUID groupId : flat.groups().keySet()) {
            if (!referenced.contains(groupId)) {
                printDebugMessages(debugMessages);
                throw new AssertionError(
                        "Group " + groupId + " is missing from groupsByName"
                );
            }
        }
    }

    private static void validatePermissionInheritance(PlayerGroupDAGFlat flat, List<String> debugMessages) {

        for (PlayerGroupGroupData group : flat.groups().values()) {

            Set<String> ownPerms = group.permissions();

            for (UUID descId : group.descendants()) {
                PlayerGroupGroupData desc = flat.groups().get(descId);
                if (desc == null) {
                    printDebugMessages(debugMessages);
                    throw new AssertionError(
                            "Descendant group " + descId + " does not exist"
                    );
                }

                if (!desc.permissions().containsAll(ownPerms)) {
                    Set<String> missing = new HashSet<>(ownPerms);
                    missing.removeAll(desc.permissions());

                    printDebugMessages(debugMessages);
                    throw new AssertionError(
                            "Permission inheritance violated: group " + group.id() + " (" + group.name() + ")" +
                                    " permissions " + missing +
                                    " missing in descendant " + descId + " (" + desc.name() + ")"
                    );
                }
            }
        }
    }


    private static void validateAncestorDescendantExistence(PlayerGroupDAGFlat flat, List<String> debugMessages) {

        Set<UUID> existingGroups = flat.groups().keySet();

        for (PlayerGroupGroupData group : flat.groups().values()) {

            for (UUID ancestor : group.ancestors()) {
                if (!existingGroups.contains(ancestor)) {
                    printDebugMessages(debugMessages);
                    throw new AssertionError(
                            "Group " + group.id() + " (" + group.name() + ")" +
                                    " has non-existing ancestor " + ancestor
                    );
                }
            }

            for (UUID descendant : group.descendants()) {
                if (!existingGroups.contains(descendant)) {
                    printDebugMessages(debugMessages);
                    throw new AssertionError(
                            "Group " + group.id() + " (" + group.name() + ")" +
                                    " has non-existing descendant " + descendant
                    );
                }
            }
        }
    }

}
