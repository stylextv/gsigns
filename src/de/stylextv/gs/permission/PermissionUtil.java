package de.stylextv.gs.permission;

import org.bukkit.entity.Player;

public class PermissionUtil {
	
	public static boolean hasCreatePermission(Player p) {
		return hasStarPermission(p)||p.hasPermission("gsigns.create");
	}
	public static boolean hasRemovePermission(Player p) {
		return hasStarPermission(p)||p.hasPermission("gsigns.remove");
	}
	public static boolean hasListPermission(Player p) {
		return hasStarPermission(p)||p.hasPermission("gsigns.list");
	}
	public static boolean hasUpdatePermission(Player p) {
		return hasStarPermission(p)||p.hasPermission("gsigns.update");
	}
	private static boolean hasStarPermission(Player p) {
		return p.hasPermission("gsigns.*");
	}
	
}
