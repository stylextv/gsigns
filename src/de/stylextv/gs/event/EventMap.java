package de.stylextv.gs.event;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.MapInitializeEvent;

import de.stylextv.gs.world.WorldUtil;

public class EventMap implements Listener {
	
	@EventHandler
	public void onMapInitialize(MapInitializeEvent e) {
		WorldUtil.onMapInitialize(e);
	}
	
}
