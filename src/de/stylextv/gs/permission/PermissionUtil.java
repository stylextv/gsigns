package de.stylextv.gs.permission;

import org.bukkit.entity.Player;

public class PermissionUtil {
	
	public static boolean hasCreatePermission(Player p) {
		return p.hasPermission("gsigns.*")||p.hasPermission("gsigns.create");
	}
	
}
