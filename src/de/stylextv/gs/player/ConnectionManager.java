package de.stylextv.gs.player;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class ConnectionManager {
	
	private static int SENDS_PER_3TICKS=40;
	private static int TICK_OSCILLATOR;
	
	private static HashMap<Player, Integer> sendCounts=new HashMap<Player, Integer>();
	
	public static void update() {
		if(TICK_OSCILLATOR==2) {
			TICK_OSCILLATOR=0;
			for(Player all:Bukkit.getOnlinePlayers()) {
				sendCounts.put(all, 0);
			}
		} else TICK_OSCILLATOR++;
	}
	
	public static int canSend(Player p, int amount) {
		if(!p.isOnline()) return 0;
		
		Integer i=sendCounts.get(p);
		if(i==null) i=0;
		if(i+amount<=SENDS_PER_3TICKS) {
			sendCounts.put(p, i+amount);
			return amount;
		} else {
			sendCounts.put(p, SENDS_PER_3TICKS);
			return SENDS_PER_3TICKS-i;
		}
	}
	public static void removePlayer(Player p) {
		sendCounts.remove(p);
	}
	
}
