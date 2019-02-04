package com.mediusecho.particlehats.commands;

public enum CommandPermission {

	/**
	 * Grants access to every command
	 */
	ALL("all"),
	RELOAD("reload"),
	HELP("help"),
	EDIT("edit"),
	CREATE("create");
	
	public static final String ROOT = "particlehats.command.";
	public final String value;
	
	private CommandPermission (String value)
	{
		this.value = ROOT + value;
	}
}
