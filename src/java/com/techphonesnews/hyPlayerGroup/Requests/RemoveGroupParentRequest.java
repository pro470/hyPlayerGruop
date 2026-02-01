package com.techphonesnews.hyPlayerGroup.Requests;

import com.techphonesnews.hyPlayerGroup.Group.PlayerGroupAffected;
import com.techphonesnews.hyPlayerGroup.Group.PlayerGroupDAG;
import com.techphonesnews.hyPlayerGroup.HyPlayerGroupPlugin;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class RemoveGroupParentRequest implements PlayerGroupGroupChangeRequest {

    private final String Parent;

    private final String Child;

    private UUID parentId;
    private UUID childId;

    public RemoveGroupParentRequest(String Parent, String Child) {
        this.Parent = Parent;
        this.Child = Child;
    }

    @Override
    public void apply(PlayerGroupDAG dag) {
        dag.removeParent(Parent, Child);
        parentId = dag.getGroupId(Parent);
        childId = dag.getGroupId(Child);
    }

    @Override
    public void event() {

    }

    @Override
    public String debugMessage() {
        return "Removing group parent " + Parent + " to group child " + Child;
    }

    @Override
    @Nonnull
    public PlayerGroupAffected affected() {
        Set<UUID> parents = new HashSet<>();
        Set<UUID> children = new HashSet<>();
        if (parentId != null) {
            parents.add(parentId);
        }
        if (childId != null) {
            children.add(childId);
        }
        if (HyPlayerGroupPlugin.get().getDAGFlat().getGroup(Parent) != null) {
            parents.addAll(HyPlayerGroupPlugin.get().getDAGFlat().getGroup(Parent).ancestors());

        }
        if (HyPlayerGroupPlugin.get().getDAGFlat().getGroup(Child) != null) {
            children.addAll(HyPlayerGroupPlugin.get().getDAGFlat().getGroup(Child).descendants());

        }
        return new PlayerGroupAffected(children, parents, children, Set.of());
    }
}
