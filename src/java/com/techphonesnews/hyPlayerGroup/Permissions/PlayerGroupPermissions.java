package com.techphonesnews.hyPlayerGroup.Permissions;

import javax.annotation.Nonnull;

public class PlayerGroupPermissions {
    public static final String NAMESPACE = "playergroup";
    public static final String GROUP = "playergroup.group";

    @Nonnull
    public static String fromGroup(@Nonnull String name) {
        return GROUP + "." + name;
    }

    @Nonnull
    public static String fromGroupPermission(@Nonnull String group, @Nonnull String permission) {
        return fromGroup(group) + "." + permission;
    }
}
