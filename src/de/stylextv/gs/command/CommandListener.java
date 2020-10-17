package de.stylextv.gs.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class CommandListener implements CommandExecutor {
	
	private String cmd;
	
	public CommandListener(String cmd) {
		this.cmd = cmd;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(cmd.getName().equalsIgnoreCase(this.cmd)) return CommandHandler.onCommand(sender, cmd, label, args);
		return false;
	}
	
}
