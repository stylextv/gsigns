package de.stylextv.gs.command;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import de.stylextv.gs.permission.PermissionUtil;

public class MainTabCompleter implements TabCompleter {
	
	private static final ArrayList<String> COMMAND_SUGGESTIONS = new ArrayList<String>();
	private static final ArrayList<String> CREATE_SUGGESTIONS = new ArrayList<String>();
	private static final ArrayList<String> LIST_SUGGESTIONS = new ArrayList<String>();
	
	static {
		COMMAND_SUGGESTIONS.add("create");
		COMMAND_SUGGESTIONS.add("remove");
		COMMAND_SUGGESTIONS.add("cancel");
		COMMAND_SUGGESTIONS.add("listfiles");
		COMMAND_SUGGESTIONS.add("update");
		COMMAND_SUGGESTIONS.add("help");
		COMMAND_SUGGESTIONS.add("info");
		
		CREATE_SUGGESTIONS.add("(Code)");
		
		LIST_SUGGESTIONS.add("[Page]");
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		if(sender instanceof Player && (cmd.getName().equalsIgnoreCase("gs")||cmd.getName().equalsIgnoreCase("gsigns")||cmd.getName().equalsIgnoreCase("gamemodesigns"))) {
			Player p = (Player) sender;
			if(PermissionUtil.hasCreatePermission(p)||PermissionUtil.hasListPermission(p)||PermissionUtil.hasUpdatePermission(p)||PermissionUtil.hasRemovePermission(p)) {
				if(args.length==1) {
					return COMMAND_SUGGESTIONS;
				} else if(args.length==2) {
					if(args[0].equalsIgnoreCase("create")) {
						return CREATE_SUGGESTIONS;
					} else if(args[0].equalsIgnoreCase("listfiles")) {
						return LIST_SUGGESTIONS;
					}
				}
			}
		}
		return null;
	}
	
}
