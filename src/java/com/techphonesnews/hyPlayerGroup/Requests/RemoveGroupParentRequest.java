package com.techphonesnews.hyPlayerGroup.Requests;

import com.techphonesnews.hyPlayerGroup.Group.PlayerGroupAffected;
import com.techphonesnews.hyPlayerGroup.Group.PlayerGroupDAG;
import com.techphonesnews.hyPlayerGroup.HyPlayerGroupPlugin;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Objects;
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
    public void affected(PlayerGroupAffected affected) {
        if (parentId != null) {
            affected.descendants().add(parentId);
        }
        if (childId != null) {
            affected.ancestors().add(childId);
            affected.permissions().add(childId);
        }
        if (HyPlayerGroupPlugin.get().getDAGFlat().getGroup(Parent) != null) {
            affected.descendants().addAll(Objects.requireNonNull(HyPlayerGroupPlugin.get().getDAGFlat().getGroup(Parent)).ancestors());

        }
        if (HyPlayerGroupPlugin.get().getDAGFlat().getGroup(Child) != null) {
            affected.ancestors().addAll(Objects.requireNonNull(HyPlayerGroupPlugin.get().getDAGFlat().getGroup(Child)).descendants());
            affected.permissions().addAll(Objects.requireNonNull(HyPlayerGroupPlugin.get().getDAGFlat().getGroup(Child)).descendants());
        }
    }
}
