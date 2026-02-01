package com.techphonesnews.hyPlayerGroup.validator;

import com.techphonesnews.hyPlayerGroup.Group.PlayerGroupDAGFlat;
import com.techphonesnews.hyPlayerGroup.Group.PlayerGroupGroupData;
import com.techphonesnews.hyPlayerGroup.Group.PlayerGroupPlayerData;
import com.techphonesnews.hyPlayerGroup.Validator.PlayerGroupValidator;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;


public class PlayerGroupValidatorTest {

    private static PlayerGroupGroupData group(
            UUID id,
            String name,
            Set<UUID> ancestors,
            Set<UUID> descendants,
            Set<UUID> directMembers,
            Set<String> permissions
    ) {
        return new PlayerGroupGroupData(
                id,
                name,
                ancestors,
                descendants,
                directMembers,
                permissions
        );
    }

    private static PlayerGroupDAGFlat flat(
            Map<UUID, PlayerGroupGroupData> groups,
            Map<String, UUID> byName,
            PlayerGroupPlayerData players
    ) {
        return new PlayerGroupDAGFlat(groups, byName, players);
    }

    private static PlayerGroupPlayerData emptyPlayers() {
        return new PlayerGroupPlayerData(
                Map.of(),
                Map.of()
        );
    }

    @Test
    public void ancestorDescendantExistence_invalid_missingAncestor() {
        UUID a = UUID.randomUUID();
        UUID ghost = UUID.randomUUID();

        var gA = group(a, "A", Set.of(ghost), Set.of(), Set.of(), Set.of());

        PlayerGroupDAGFlat flat = flat(
                Map.of(a, gA),
                Map.of("A", a),
                emptyPlayers()
        );

        assertThrows(AssertionError.class, () ->
                PlayerGroupValidator.validateAncestorDescendantExistence(flat));
    }

    @Test
    public void noSelfReachability_invalidA() {
        UUID a = UUID.randomUUID();

        var gA = group(a, "A", Set.of(a), Set.of(), Set.of(), Set.of());

        PlayerGroupDAGFlat flat = flat(
                Map.of(a, gA),
                Map.of("A", a),
                emptyPlayers()
        );

        assertThrows(AssertionError.class, () ->
                PlayerGroupValidator.validateNoSelfReachability(flat));
    }

    @Test
    public void noSelfReachability_invalidD() {
        UUID a = UUID.randomUUID();

        var gA = group(a, "A", Set.of(), Set.of(a), Set.of(), Set.of());

        PlayerGroupDAGFlat flat = flat(
                Map.of(a, gA),
                Map.of("A", a),
                emptyPlayers()
        );

        assertThrows(AssertionError.class, () ->
                PlayerGroupValidator.validateNoSelfReachability(flat));
    }

    @Test
    public void ancestors_invalid_asymmetry() {
        UUID a = UUID.randomUUID();
        UUID b = UUID.randomUUID();

        var gA = group(a, "A", Set.of(), Set.of(b), Set.of(), Set.of());
        var gB = group(b, "B", Set.of(), Set.of(), Set.of(), Set.of());

        PlayerGroupDAGFlat flat = flat(
                Map.of(a, gA, b, gB),
                Map.of("A", a, "B", b),
                emptyPlayers()
        );

        assertThrows(AssertionError.class, () ->
                PlayerGroupValidator.validateAncestorsAndDescendants(flat));
    }


    @Test
    public void Descendants_invalid_asymmetry() {
        UUID a = UUID.randomUUID();
        UUID b = UUID.randomUUID();

        var gA = group(a, "A", Set.of(), Set.of(), Set.of(), Set.of());
        var gB = group(b, "B", Set.of(a), Set.of(), Set.of(), Set.of());

        PlayerGroupDAGFlat flat = flat(
                Map.of(a, gA, b, gB),
                Map.of("A", a, "B", b),
                emptyPlayers()
        );

        assertThrows(AssertionError.class, () ->
                PlayerGroupValidator.validateAncestorsAndDescendants(flat));
    }

    @Test
    public void groupsByName_invalid_missingGroup() {
        UUID ghost = UUID.randomUUID();

        PlayerGroupDAGFlat flat = flat(
                Map.of(),
                Map.of("ghost", ghost),
                emptyPlayers()
        );

        assertThrows(AssertionError.class, () ->
                PlayerGroupValidator.validateGroupsByNameReferExistingGroups(flat));
    }

    @Test
    public void groupNamesMismatch_invalid() {
        UUID a = UUID.randomUUID();

        var gA = group(a, "REAL", Set.of(), Set.of(), Set.of(), Set.of());

        PlayerGroupDAGFlat flat = flat(
                Map.of(a, gA),
                Map.of("FAKE", a),
                emptyPlayers()
        );

        assertThrows(AssertionError.class, () ->
                PlayerGroupValidator.validateGroupNamesMatch(flat));
    }

    @Test
    public void groupNamesNotUnique_invalid() {
        UUID a = UUID.randomUUID();

        PlayerGroupDAGFlat flat = flat(
                Map.of(a, group(a, "A", Set.of(), Set.of(), Set.of(), Set.of())),
                Map.of("A", a, "B", a),
                emptyPlayers()
        );

        assertThrows(AssertionError.class, () ->
                PlayerGroupValidator.validateGroupNamesAreUnique(flat));
    }

    @Test
    public void groupMissingFromGroupsByName_invalid() {
        UUID a = UUID.randomUUID();

        PlayerGroupDAGFlat flat = flat(
                Map.of(a, group(a, "A", Set.of(), Set.of(), Set.of(), Set.of())),
                Map.of(),
                emptyPlayers()
        );

        assertThrows(AssertionError.class, () ->
                PlayerGroupValidator.validateAllGroupsAppearInGroupsByName(flat));
    }

    @Test
    public void permissionInheritance_invalid() {
        UUID parent = UUID.randomUUID();
        UUID child = UUID.randomUUID();

        var gP = group(parent, "P", Set.of(), Set.of(child), Set.of(), Set.of("perm.a"));
        var gC = group(child, "C", Set.of(parent), Set.of(), Set.of(), Set.of());

        PlayerGroupDAGFlat flat = flat(
                Map.of(parent, gP, child, gC),
                Map.of("P", parent, "C", child),
                emptyPlayers()
        );

        assertThrows(AssertionError.class, () ->
                PlayerGroupValidator.validatePermissionInheritance(flat));
    }

    @Test
    public void directMembersInPlayersGroups_invalid_missingPlayerGroupEntry() {
        UUID groupId = UUID.randomUUID();
        UUID playerId = UUID.randomUUID();

        PlayerGroupGroupData group = new PlayerGroupGroupData(
                groupId,
                "group",
                Set.of(),
                Set.of(),
                Set.of(playerId),
                Set.of()
        );

        PlayerGroupPlayerData players = new PlayerGroupPlayerData(
                Map.of(),
                Map.of()
        );

        PlayerGroupDAGFlat flat = new PlayerGroupDAGFlat(
                Map.of(groupId, group),
                Map.of("group", groupId),
                players
        );

        assertThrows(AssertionError.class, () ->
                PlayerGroupValidator.validatedirectMembersInPlayersGroups(flat));
    }

    @Test
    public void playerGroupsInDirectMembers_invalid_missingDirectMember() {
        UUID groupId = UUID.randomUUID();
        UUID playerId = UUID.randomUUID();

        PlayerGroupGroupData group = new PlayerGroupGroupData(
                groupId,
                "group",
                Set.of(),
                Set.of(),
                Set.of(),
                Set.of()
        );

        PlayerGroupPlayerData players = new PlayerGroupPlayerData(
                Map.of(playerId, Set.of(groupId)),
                Map.of()
        );

        PlayerGroupDAGFlat flat = new PlayerGroupDAGFlat(
                Map.of(groupId, group),
                Map.of("group", groupId),
                players
        );

        assertThrows(AssertionError.class, () ->
                PlayerGroupValidator.validatePlayerGroupsInDirectMembers(flat));
    }

    @Test
    public void transitiveClosure_invalid_missingIndirectDescendantALost() {
        UUID a = UUID.randomUUID();
        UUID b = UUID.randomUUID();
        UUID c = UUID.randomUUID();

        PlayerGroupGroupData A = new PlayerGroupGroupData(
                a, "A",
                Set.of(),
                Set.of(b, c),
                Set.of(),
                Set.of()
        );

        PlayerGroupGroupData B = new PlayerGroupGroupData(
                b, "B",
                Set.of(a),
                Set.of(c),
                Set.of(),
                Set.of()
        );

        PlayerGroupGroupData C = new PlayerGroupGroupData(
                c, "C",
                Set.of(b),
                Set.of(),
                Set.of(),
                Set.of()
        );

        PlayerGroupDAGFlat flat = new PlayerGroupDAGFlat(
                Map.of(a, A, b, B, c, C),
                Map.of("A", a, "B", b, "C", c),
                new PlayerGroupPlayerData(Map.of(), Map.of())
        );

        assertThrows(AssertionError.class, () ->
                PlayerGroupValidator.validateTransitiveClosure(flat));
    }

    @Test
    public void transitiveClosure_invalid_missingIndirectDescendantCLost() {
        UUID a = UUID.randomUUID();
        UUID b = UUID.randomUUID();
        UUID c = UUID.randomUUID();

        PlayerGroupGroupData A = new PlayerGroupGroupData(
                a, "A",
                Set.of(),
                Set.of(b),
                Set.of(),
                Set.of()
        );

        PlayerGroupGroupData B = new PlayerGroupGroupData(
                b, "B",
                Set.of(a),
                Set.of(c),
                Set.of(),
                Set.of()
        );

        PlayerGroupGroupData C = new PlayerGroupGroupData(
                c, "C",
                Set.of(b, a),
                Set.of(),
                Set.of(),
                Set.of()
        );

        PlayerGroupDAGFlat flat = new PlayerGroupDAGFlat(
                Map.of(a, A, b, B, c, C),
                Map.of("A", a, "B", b, "C", c),
                new PlayerGroupPlayerData(Map.of(), Map.of())
        );

        assertThrows(AssertionError.class, () ->
                PlayerGroupValidator.validateTransitiveClosure(flat));
    }

    @Test
    void transitiveClosure_valid() {
        UUID a = UUID.randomUUID();
        UUID b = UUID.randomUUID();
        UUID c = UUID.randomUUID();

        PlayerGroupGroupData A = new PlayerGroupGroupData(
                a, "A",
                Set.of(),
                Set.of(b, c),
                Set.of(),
                Set.of()
        );

        PlayerGroupGroupData B = new PlayerGroupGroupData(
                b, "B",
                Set.of(a),
                Set.of(c),
                Set.of(),
                Set.of()
        );

        PlayerGroupGroupData C = new PlayerGroupGroupData(
                c, "C",
                Set.of(a, b),
                Set.of(),
                Set.of(),
                Set.of()
        );

        PlayerGroupDAGFlat flat = new PlayerGroupDAGFlat(
                Map.of(a, A, b, B, c, C),
                Map.of("A", a, "B", b, "C", c),
                new PlayerGroupPlayerData(Map.of(), Map.of())
        );

        assertDoesNotThrow(() ->
                PlayerGroupValidator.validateTransitiveClosure(flat));
    }

    @Test
    void playerGroupsInDirectMembers_valid() {
        UUID groupId = UUID.randomUUID();
        UUID playerId = UUID.randomUUID();

        PlayerGroupGroupData group = new PlayerGroupGroupData(
                groupId,
                "group",
                Set.of(),
                Set.of(),
                Set.of(playerId),
                Set.of()
        );

        PlayerGroupPlayerData players = new PlayerGroupPlayerData(
                Map.of(playerId, Set.of(groupId)),
                Map.of()
        );

        PlayerGroupDAGFlat flat = new PlayerGroupDAGFlat(
                Map.of(groupId, group),
                Map.of("group", groupId),
                players
        );

        assertDoesNotThrow(() ->
                PlayerGroupValidator.validatePlayerGroupsInDirectMembers(flat));
    }

    @Test
    void directMembersInPlayersGroups_valid() {
        UUID groupId = UUID.randomUUID();
        UUID playerId = UUID.randomUUID();

        PlayerGroupGroupData group = new PlayerGroupGroupData(
                groupId,
                "group",
                Set.of(),
                Set.of(),
                Set.of(playerId),
                Set.of()
        );

        PlayerGroupPlayerData players = new PlayerGroupPlayerData(
                Map.of(playerId, Set.of(groupId)),
                Map.of()
        );

        PlayerGroupDAGFlat flat = new PlayerGroupDAGFlat(
                Map.of(groupId, group),
                Map.of("group", groupId),
                players
        );

        assertDoesNotThrow(() ->
                PlayerGroupValidator.validatedirectMembersInPlayersGroups(flat));
    }

    @Test
    void ancestorsAndDescendants_valid() {
        UUID a = UUID.randomUUID();
        UUID b = UUID.randomUUID();

        var gA = group(a, "A", Set.of(), Set.of(b), Set.of(), Set.of());
        var gB = group(b, "B", Set.of(a), Set.of(), Set.of(), Set.of());

        PlayerGroupDAGFlat flat = flat(
                Map.of(a, gA, b, gB),
                Map.of("A", a, "B", b),
                emptyPlayers()
        );

        assertDoesNotThrow(() ->
                PlayerGroupValidator.validateAncestorsAndDescendants(flat));
    }

    @Test
    void noSelfReachability_valid() {
        UUID a = UUID.randomUUID();

        var gA = group(a, "A", Set.of(), Set.of(), Set.of(), Set.of());

        PlayerGroupDAGFlat flat = flat(
                Map.of(a, gA),
                Map.of("A", a),
                emptyPlayers()
        );

        assertDoesNotThrow(() ->
                PlayerGroupValidator.validateNoSelfReachability(flat));
    }

    @Test
    void ancestorDescendantExistence_valid() {
        UUID a = UUID.randomUUID();
        UUID b = UUID.randomUUID();

        var gA = group(a, "A", Set.of(), Set.of(b), Set.of(), Set.of());
        var gB = group(b, "B", Set.of(a), Set.of(), Set.of(), Set.of());

        PlayerGroupDAGFlat flat = flat(
                Map.of(a, gA, b, gB),
                Map.of("A", a, "B", b),
                emptyPlayers()
        );

        assertDoesNotThrow(() ->
                PlayerGroupValidator.validateAncestorDescendantExistence(flat));
    }
}
