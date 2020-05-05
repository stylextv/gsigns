package de.stylextv.gs.event;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

import de.stylextv.gs.player.PlayerManager;

public class EventPlayerInteract implements Listener {
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent e) {
		PlayerManager.onPlayerInteract(e);
	}
	
}
