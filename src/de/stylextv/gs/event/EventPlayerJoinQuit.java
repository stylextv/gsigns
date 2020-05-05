package de.stylextv.gs.event;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import de.stylextv.gs.player.PlayerManager;

public class EventPlayerJoinQuit implements Listener {
	
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent e) {
		PlayerManager.onPlayerQuit(e);
	}
	
}
