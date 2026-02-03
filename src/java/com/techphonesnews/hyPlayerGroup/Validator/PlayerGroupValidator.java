package com.techphonesnews.hyPlayerGroup.Validator;

import com.techphonesnews.hyPlayerGroup.Group.PlayerGroupDAG;
import com.techphonesnews.hyPlayerGroup.Group.PlayerGroupDAGFlat;
import com.techphonesnews.hyPlayerGroup.Group.PlayerGroupDAGGroup;
import com.techphonesnews.hyPlayerGroup.Group.PlayerGroupGroupData;

import java.util.*;

import static com.techphonesnews.hyPlayerGroup.Group.PlayerGroupDAG.dfs;

public class PlayerGroupValidator {

    public static void validate(PlayerGroupDAGFlat flat, PlayerGroupDAG dag) {
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
        validateFlatGroupsMatchDag(dag, flat);
        validateGroupNamesMatchDag(dag, flat);
        validateDescendantsMatchDag(dag, flat);
        validateAncestorsMatchDag(dag, flat);
        validateDirectMembersMatchDag(dag, flat);
        validatePlayersMatchDag(dag, flat);
        validatePermissionsMatchDagWithInheritance(dag, flat);
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

    static void validateFlatGroupsMatchDag(
            PlayerGroupDAG dag,
            PlayerGroupDAGFlat flat
    ) {
        Set<UUID> dagIds = dag.groups();
        Set<UUID> flatIds = flat.groups().keySet();

        if (!dagIds.equals(flatIds)) {
            throw new AssertionError(
                    "Group ID mismatch between DAG and Flat\n" +
                            "DAG: " + dagIds + "\n" +
                            "Flat: " + flatIds
            );
        }
    }

    static void validateGroupNamesMatchDag(
            PlayerGroupDAG dag,
            PlayerGroupDAGFlat flat
    ) {
        for (PlayerGroupDAGGroup dagGroup : dag.groupsValues()) {
            PlayerGroupGroupData flatGroup =
                    flat.groups().get(dagGroup.id());

            if (flatGroup == null) {
                throw new AssertionError("Missing flat group " + dagGroup.id());
            }

            if (!dagGroup.getName().equals(flatGroup.name())) {
                throw new AssertionError(
                        "Name mismatch for group " + dagGroup.id() +
                                ": DAG=" + dagGroup.getName() +
                                ", Flat=" + flatGroup.name()
                );
            }

            UUID byName = flat.groupsByName().get(flatGroup.name());
            if (!dagGroup.id().equals(byName)) {
                throw new AssertionError(
                        "groupsByName incorrect for " + flatGroup.name()
                );
            }
        }
    }

    static void validateDescendantsMatchDag(
            PlayerGroupDAG dag,
            PlayerGroupDAGFlat flat
    ) {
        for (PlayerGroupDAGGroup dagGroup : dag.groupsValues()) {
            Set<UUID> dfsDesc = new HashSet<>();

            dfs(
                    dagGroup.id(),
                    new HashSet<>(),
                    id -> dag.getGroup(id).children(),
                    id -> {
                        if (!id.equals(dagGroup.id())) dfsDesc.add(id);
                        return false;
                    }
            );

            Set<UUID> flatDesc =
                    flat.groups().get(dagGroup.id()).descendants();

            if (!dfsDesc.equals(flatDesc)) {
                throw new AssertionError(
                        "Descendants mismatch for group " + dagGroup.getName() +
                                "\nDAG=" + dfsDesc +
                                "\nFlat=" + flatDesc
                );
            }
        }
    }

    static void validateAncestorsMatchDag(
            PlayerGroupDAG dag,
            PlayerGroupDAGFlat flat
    ) {
        for (PlayerGroupDAGGroup dagGroup : dag.groupsValues()) {
            Set<UUID> dfsAnc = new HashSet<>();

            dfs(
                    dagGroup.id(),
                    new HashSet<>(),
                    id -> dag.getGroup(id).parents(),
                    id -> {
                        if (!id.equals(dagGroup.id())) dfsAnc.add(id);
                        return false;
                    }
            );

            Set<UUID> flatAnc =
                    flat.groups().get(dagGroup.id()).ancestors();

            if (!dfsAnc.equals(flatAnc)) {
                throw new AssertionError(
                        "Ancestors mismatch for group " + dagGroup.getName()
                );
            }
        }
    }

    static void validateDirectMembersMatchDag(
            PlayerGroupDAG dag,
            PlayerGroupDAGFlat flat
    ) {
        for (var dagGroup : dag.groupsValues()) {
            Set<UUID> dagMembers = dagGroup.members();
            Set<UUID> flatMembers =
                    flat.groups().get(dagGroup.id()).directMembers();

            if (!dagMembers.equals(flatMembers)) {
                throw new AssertionError(
                        "Direct members mismatch for group " + dagGroup.getName()
                );
            }
        }
    }

    static void validatePlayersMatchDag(
            PlayerGroupDAG dag,
            PlayerGroupDAGFlat flat
    ) {
        for (var entry : dag.getPlayerGroups().entrySet()) {
            UUID player = entry.getKey();
            Set<UUID> dagGroups = entry.getValue();

            Set<UUID> flatGroups =
                    flat.players().playersGroups().get(player);

            if (!dagGroups.equals(flatGroups)) {
                throw new AssertionError(
                        "Player groups mismatch for player " + player
                );
            }
        }
    }

    static void validatePermissionsMatchDagWithInheritance(
            PlayerGroupDAG dag,
            PlayerGroupDAGFlat flat
    ) {
        for (var dagGroup : dag.groupsValues()) {

            Set<String> expected = new HashSet<>();

            // DFS über parents inkl. self
            dfs(
                    dagGroup.id(),
                    new HashSet<>(),
                    id -> dag.getGroup(id).parents(),
                    id -> {
                        expected.addAll(
                                dag.getGroup(id).permissions()
                        );
                        return false;
                    }
            );

            Set<String> flatPerms =
                    flat.groups().get(dagGroup.id()).permissions();

            if (!expected.equals(flatPerms)) {
                throw new AssertionError(
                        "Permission mismatch for group " + dagGroup.getName() +
                                "\nExpected=" + expected +
                                "\nFlat=" + flatPerms
                );
            }
        }
    }
}
