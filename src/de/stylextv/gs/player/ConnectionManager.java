package de.stylextv.gs.player;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class ConnectionManager {
	
	private static int SENDS_PER_TICK=15;
	private static float SEND_COOLDOWN=1.35f;
	
	private static HashMap<Player, Integer> sendCounts=new HashMap<Player, Integer>();
	private static HashMap<Player, Integer> cooldowns=new HashMap<Player, Integer>();
	
	public static void update() {
		for(Player all:Bukkit.getOnlinePlayers()) {
			sendCounts.put(all, 0);
			Integer cd=cooldowns.get(all);
			if(cd!=null) {
				if(cd<=1) cooldowns.remove(all);
				else cooldowns.put(all, cd-1);
			}
		}
	}
	
	public static boolean canSend(Player p, int amount) {
		if(!p.isOnline()||cooldowns.get(p)!=null) return false;
		
		int i=sendCounts.get(p);
		if(i+amount<=SENDS_PER_TICK) {
			sendCounts.put(p, i+amount);
			return true;
		} else {
			cooldowns.put(p, Math.min(Math.round((float)(i+amount)/SENDS_PER_TICK * SEND_COOLDOWN), 10) );
			return i==0;
		}
	}
	public static void removePlayer(Player p) {
		sendCounts.remove(p);
		cooldowns.remove(p);
	}
	
}
