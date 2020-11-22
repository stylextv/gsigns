package de.stylextv.gs.command;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import de.stylextv.gs.lang.LanguageManager;
import de.stylextv.gs.permission.PermissionUtil;
import de.stylextv.gs.world.BetterSign;
import de.stylextv.gs.world.WorldUtil;

public class MainTabCompleter implements TabCompleter {
	
	private static final ArrayList<String> COMMAND_SUGGESTIONS = new ArrayList<String>();
	private static final ArrayList<String> CREATE_SUGGESTIONS = new ArrayList<String>();
	private static final ArrayList<String> LIST_SUGGESTIONS = new ArrayList<String>();
	private static final ArrayList<String> REMOVE_SUGGESTIONS = new ArrayList<String>();
	private static final ArrayList<String> UUID_SUGGESTIONS = new ArrayList<String>();
	
	static {
		COMMAND_SUGGESTIONS.add("create");
		COMMAND_SUGGESTIONS.add("remove");
		COMMAND_SUGGESTIONS.add("cancel");
		COMMAND_SUGGESTIONS.add("listfiles");
		COMMAND_SUGGESTIONS.add("listsigns");
		COMMAND_SUGGESTIONS.add("gui");
		COMMAND_SUGGESTIONS.add("tp");
		COMMAND_SUGGESTIONS.add("play");
		COMMAND_SUGGESTIONS.add("pause");
		COMMAND_SUGGESTIONS.add("update");
		COMMAND_SUGGESTIONS.add("help");
		COMMAND_SUGGESTIONS.add("info");
		
		CREATE_SUGGESTIONS.add(LanguageManager.parseMsg("trans.command.suggestion.create"));
		
		LIST_SUGGESTIONS.add(LanguageManager.parseMsg("trans.command.suggestion.list"));
		
		REMOVE_SUGGESTIONS.add(LanguageManager.parseMsg("trans.command.suggestion.remove"));
		
		UUID_SUGGESTIONS.add(LanguageManager.parseMsg("trans.command.suggestion.uuid"));
	}
	public static void recreateFromLanguage() {
		CREATE_SUGGESTIONS.clear();
		LIST_SUGGESTIONS.clear();
		REMOVE_SUGGESTIONS.clear();
		UUID_SUGGESTIONS.clear();
		
		CREATE_SUGGESTIONS.add(LanguageManager.parseMsg("trans.command.suggestion.create"));
		
		LIST_SUGGESTIONS.add(LanguageManager.parseMsg("trans.command.suggestion.list"));
		
		REMOVE_SUGGESTIONS.add(LanguageManager.parseMsg("trans.command.suggestion.remove"));
		
		UUID_SUGGESTIONS.add(LanguageManager.parseMsg("trans.command.suggestion.uuid"));
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		if(sender instanceof Player && (cmd.getName().equalsIgnoreCase("gs")||cmd.getName().equalsIgnoreCase("gsigns")||cmd.getName().equalsIgnoreCase("gamemodesigns"))) {
			Player p = (Player) sender;
			if(PermissionUtil.hasCreatePermission(p)||PermissionUtil.hasListPermission(p)||PermissionUtil.hasUpdatePermission(p)||PermissionUtil.hasRemovePermission(p)||PermissionUtil.hasPausePermission(p)||PermissionUtil.hasGuiPermission(p)||PermissionUtil.hasTeleportPermission(p)) {
				if(args.length==1) {
					return COMMAND_SUGGESTIONS;
				} else if(args.length==2) {
					if(args[0].equalsIgnoreCase("create")) {
						return CREATE_SUGGESTIONS;
					} else if(args[0].equalsIgnoreCase("listfiles") || args[0].equalsIgnoreCase("listsigns")) {
						return LIST_SUGGESTIONS;
					} else if(args[0].equalsIgnoreCase("remove")) {
						if(WorldUtil.getSigns().isEmpty()) return REMOVE_SUGGESTIONS;
						ArrayList<String> suggestions = new ArrayList<String>();
						for(BetterSign sign:WorldUtil.getSigns()) suggestions.add(sign.getUid().toString());
						return suggestions;
					} else if(args[0].equalsIgnoreCase("tp") || args[0].equalsIgnoreCase("play") || args[0].equalsIgnoreCase("pause")) {
						if(WorldUtil.getSigns().isEmpty()) return UUID_SUGGESTIONS;
						ArrayList<String> suggestions = new ArrayList<String>();
						for(BetterSign sign:WorldUtil.getSigns()) suggestions.add(sign.getUid().toString());
						return suggestions;
					}
				}
			}
		}
		return null;
	}
	
}
