package com.personoid.humanoid.commands;

import com.personoid.api.utils.bukkit.Message;
import com.personoid.humanoid.handlers.CommandHandler.Command.Requirement;
import com.personoid.humanoid.handlers.CommandHandler.Command.Requirement.Type;

public class CommonRequirements {
    public static final Requirement player = new Requirement(Type.PLAYER) {
        @Override
        public String onFailure() {
            return Message.toColor("&cYou must be a player to use this command!");
        }
    };
}
