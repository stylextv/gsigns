package de.stylextv.gs.command;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import de.stylextv.gs.permission.PermissionUtil;

public class MainTabCompleter implements TabCompleter {
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		if(sender instanceof Player && (cmd.getName().equalsIgnoreCase("gs")||cmd.getName().equalsIgnoreCase("gsigns")||cmd.getName().equalsIgnoreCase("gamemodesigns"))) {
			Player p = (Player) sender;
			if(PermissionUtil.hasCreatePermission(p)||PermissionUtil.hasListPermission(p)||PermissionUtil.hasRemovePermission(p)) {
				if(args.length==1) {
					ArrayList<String> tabs=new ArrayList<String>();
					tabs.add("create");
					tabs.add("remove");
					tabs.add("cancel");
					tabs.add("listfiles");
					tabs.add("help");
					tabs.add("info");
					return tabs;
				} else if(args.length==2) {
					if(args[0].equalsIgnoreCase("create")) {
						ArrayList<String> tabs=new ArrayList<String>();
						tabs.add("<code>");
						return tabs;
					} else if(args[0].equalsIgnoreCase("listfiles")) {
						ArrayList<String> tabs=new ArrayList<String>();
						tabs.add("[page]");
						return tabs;
					}
				}
			}
		}
		return null;
	}
	
}
