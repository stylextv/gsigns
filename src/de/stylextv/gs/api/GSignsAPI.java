package de.stylextv.gs.api;

import java.util.UUID;

import org.bukkit.Location;

public interface GSignsAPI {
	
	public UUID createSign(String code, Location corner1, Location corner2);
	
	public void removeSign(UUID uid);
	
}
