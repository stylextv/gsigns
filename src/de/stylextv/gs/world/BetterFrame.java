package de.stylextv.gs.world;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.map.MapView;

public abstract class BetterFrame {
	
	public static int VIEW_DISTANCE_SQ=31*31;
	public static int CONTENT_RELOAD_DISTANCE_SQ=VIEW_DISTANCE_SQ*2;
	
	public abstract boolean update(long currentTime);
	
	public abstract void removePlayer(Player p);
	
	public abstract boolean isDead();
	public abstract BlockFace getFacing();
	public abstract Location getLocation();
	
	public abstract int getCurrentItemIndex();
	public abstract void setCurrentItemIndex(int currentItemIndex);
	public abstract int getDelay();
	
	public abstract MapView[] getMapViews();
	
}
