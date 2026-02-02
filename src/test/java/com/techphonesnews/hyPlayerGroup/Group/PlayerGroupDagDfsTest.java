package com.techphonesnews.hyPlayerGroup.Group;

import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class PlayerGroupDagDfsTest {

    /*
     * Hilfsfunktion: einfache Graph-Struktur
     */
    private static Map<UUID, List<UUID>> graph(Object[][] edges) {
        Map<UUID, List<UUID>> g = new HashMap<>();
        for (Object[] e : edges) {
            UUID from = (UUID) e[0];
            UUID to = (UUID) e[1];
            g.computeIfAbsent(from, _ -> new ArrayList<>()).add(to);
        }
        return g;
    }

    @Test
    void dfs_visits_all_nodes_depth_first_without_order_assumption() {
        UUID A = UUID.randomUUID();
        UUID B = UUID.randomUUID();
        UUID C = UUID.randomUUID();
        UUID D = UUID.randomUUID();

        Map<UUID, List<UUID>> g = graph(new Object[][]{
                {A, B},
                {B, C},
                {A, D}
        });

        Set<UUID> visited = new HashSet<>();
        List<UUID> order = new ArrayList<>();

        boolean result = PlayerGroupDAG.dfs(
                A,
                visited,
                id -> g.getOrDefault(id, List.of()),
                id -> {
                    order.add(id);
                    return false;
                }
        );

        assertFalse(result);

        assertEquals(
                Set.of(A, B, C, D),
                new HashSet<>(order),
                "DFS sollte alle erreichbaren Nodes besuchen"
        );

        // 2️⃣ Root zuerst
        assertEquals(
                A,
                order.getFirst(),
                "DFS sollte beim Startknoten beginnen"
        );

        int idxB = order.indexOf(B);
        int idxC = order.indexOf(C);
        int idxD = order.indexOf(D);

        assertTrue(
                (idxC < idxD) || (idxD < idxB),
                "DFS darf kein Sibling (D) zwischen Parent (B) und Child (C) besuchen"
        );

        assertEquals(
                order.size(),
                new HashSet<>(order).size(),
                "DFS darf keinen Knoten zweimal besuchen"
        );
    }

    @Test
    void dfs_handles_cycles_using_visited() {
        UUID A = UUID.randomUUID();
        UUID B = UUID.randomUUID();

        // A -> B -> A (Zyklus)
        Map<UUID, List<UUID>> g = graph(new Object[][]{
                {A, B},
                {B, A}
        });

        Set<UUID> visited = new HashSet<>();
        List<UUID> seen = new ArrayList<>();

        boolean result = PlayerGroupDAG.dfs(
                A,
                visited,
                id -> g.getOrDefault(id, List.of()),
                id -> {
                    seen.add(id);
                    return false;
                }
        );

        assertFalse(result);
        assertEquals(Set.of(A, B), visited);
        assertEquals(2, seen.size(), "Knoten dürfen nicht doppelt besucht werden");
    }

    @Test
    void dfs_stops_immediately_when_consumer_returns_true() {
        UUID A = UUID.randomUUID();
        UUID B = UUID.randomUUID();
        UUID C = UUID.randomUUID();

        // A -> B -> C
        Map<UUID, List<UUID>> g = graph(new Object[][]{
                {A, B},
                {B, C}
        });

        Set<UUID> visited = new HashSet<>();
        List<UUID> seen = new ArrayList<>();

        boolean result = PlayerGroupDAG.dfs(
                A,
                visited,
                id -> g.getOrDefault(id, List.of()),
                id -> {
                    seen.add(id);
                    return id.equals(B); // stoppe bei B
                }
        );

        assertTrue(result);
        assertEquals(List.of(A, B), seen);
        assertFalse(visited.contains(C), "DFS darf nach true nicht weiterlaufen");
    }

    @Test
    void dfs_does_nothing_if_start_is_already_visited() {
        UUID A = UUID.randomUUID();

        Set<UUID> visited = new HashSet<>();
        visited.add(A);

        boolean result = PlayerGroupDAG.dfs(
                A,
                visited,
                _ -> List.of(),
                _ -> {
                    fail("visit darf nicht aufgerufen werden");
                    return false;
                }
        );

        assertFalse(result);
    }
}
