package de.stylextv.gs.world;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.map.MapView;

public interface BetterFrame {
	
	public static int VIEW_DISTANCE_SQ=31*31;
	public static int CONTENT_RELOAD_DISTANCE_SQ=VIEW_DISTANCE_SQ*2;
	
	public boolean update(long currentTime);
	
	public void removePlayer(Player p);
	
	public void remove();
	public boolean isDead();
	public BlockFace getFacing();
	public Location getLocation();
	
	public int getCurrentItemIndex();
	public void setCurrentItemIndex(int currentItemIndex);
	public int getDelay();
	
	public MapView[] getMapViews();
	
}
