package com.mediusecho.particlehats.commands.subcommands;

import java.util.ArrayList;
import java.util.List;

import com.mediusecho.particlehats.Core;
import com.mediusecho.particlehats.commands.Command;
import com.mediusecho.particlehats.commands.CommandPermission;
import com.mediusecho.particlehats.commands.Sender;
import com.mediusecho.particlehats.locale.Message;

public class DebugDeleteMenu extends Command {

	@Override
	public List<String> tabCompelete (Core core, Sender sender, String label, ArrayList<String> args)
	{
		return new ArrayList<String>(core.getDatabase().getMenus(false).keySet());
	}
	
	@Override
	public boolean execute(Core core, Sender sender, String label, ArrayList<String> args) 
	{
		String menuName = args.get(0);
		core.getDatabase().deleteMenu(menuName);
		return false;
	}

	@Override
	public String getName() {
		return "delete";
	}

	@Override
	public Message getUsage() {
		return Message.COMMAND_ARGUMENT_NONE;
	}

	@Override
	public Message getDescription() {
		return Message.COMMAND_ARGUMENT_NONE;
	}

	@Override
	public CommandPermission getPermission() {
		return CommandPermission.ALL;
	}

}
