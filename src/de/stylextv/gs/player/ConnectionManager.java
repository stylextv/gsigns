package de.stylextv.gs.player;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class ConnectionManager {
	
	private static int SENDS_PER_TICK=1;
	
	private static HashMap<Player, Integer> sendCounts=new HashMap<Player, Integer>();
	
	public static void update() {
		for(Player all:Bukkit.getOnlinePlayers()) {
			sendCounts.put(all, 0);
		}
	}
	
	public static boolean canSend(Player p) {
		int i=sendCounts.get(p);
		if(i<SENDS_PER_TICK) {
			sendCounts.put(p, i+1);
			return true;
		}
		return false;
	}
	public static void removePlayer(Player p) {
		sendCounts.remove(p);
	}
	
}
