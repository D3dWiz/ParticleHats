package com.mediusecho.particlehats.managers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import com.mediusecho.particlehats.Core;
import com.mediusecho.particlehats.commands.MainCommand;
import com.mediusecho.particlehats.commands.Sender;
import com.mediusecho.particlehats.commands.subcommands.ClearCommand;
import com.mediusecho.particlehats.commands.subcommands.CreateCommand;
import com.mediusecho.particlehats.commands.subcommands.DebugCommand;
import com.mediusecho.particlehats.commands.subcommands.DebugDeleteMenu;
import com.mediusecho.particlehats.commands.subcommands.EditCommand;
import com.mediusecho.particlehats.commands.subcommands.OpenCommand;
import com.mediusecho.particlehats.commands.subcommands.ReloadCommand;

public class CommandManager implements CommandExecutor, TabCompleter {

	private final Core core;
	private final String command;
	
	private final MainCommand mainCommand;
	
	public CommandManager (final Core core, final String command)
	{
		this.core = core;
		this.command = command;
		
		mainCommand = new MainCommand();
		mainCommand.register(new ReloadCommand());
		mainCommand.register(new OpenCommand(core));
		mainCommand.register(new EditCommand(core));
		mainCommand.register(new CreateCommand());
		mainCommand.register(new DebugDeleteMenu());
		mainCommand.register(new DebugCommand());
		mainCommand.register(new ClearCommand());
		
		// Register our command executor
		core.getCommand(command).setExecutor(this);
	}
	
	
	public List<String> onTabComplete(CommandSender commandSender, Command cmd, String label, String[] args) 
	{
		Sender sender = new Sender(commandSender);
		
		List<String> arguments = mainCommand.tabCompelete(core, sender, label, new ArrayList<String>(Arrays.asList(args)));
		String currentCommand = args[args.length - 1];
		
		return sortCommandSuggestions(arguments, currentCommand);
	}

	public boolean onCommand(CommandSender commandSender, Command cmd, String label, String[] args) 
	{
		Sender sender = new Sender(commandSender);
		return mainCommand.execute(core, sender, label, new ArrayList<String>(Arrays.asList(args)));
	}
	
	/**
	 * Returns a list of commands matching the currentCommand
	 * @param commands List of the commands the player can execute
	 * @param currentCommand Command the player is currently typing
	 * @return
	 */
	private List<String> sortCommandSuggestions (List<String> commands, String currentCommand)
	{
		if (currentCommand.equals("")) {
			return commands;
		}
		
		List<String>  matchingCommands = new ArrayList<String>();
		commandLoop:
		for (String s : commands)
		{
			for (int i = 0; i < s.length(); i++)
			{
				if (i < currentCommand.length())
				{
					if (s.charAt(i) != currentCommand.charAt(i)) {
						continue commandLoop;
					}
				}
			}
			matchingCommands.add(s);
		}
		return matchingCommands;
	}
}
