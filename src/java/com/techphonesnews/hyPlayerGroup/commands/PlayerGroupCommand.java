package com.techphonesnews.hyPlayerGroup.commands;

import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;

import javax.annotation.Nonnull;

public class PlayerGroupCommand extends AbstractCommandCollection {
    public PlayerGroupCommand() {
        super("playergroup", "");
        this.addAliases("group");
        this.addAliases("pg");
        this.addAliases("g");
    }
}
