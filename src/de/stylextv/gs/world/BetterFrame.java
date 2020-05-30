package de.stylextv.gs.world;

import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.map.MapView;

public abstract class BetterFrame {
	
	public static int VIEW_DISTANCE_SQ=32*32;
	public static int CONTENT_RELOAD_DISTANCE_SQ=VIEW_DISTANCE_SQ*2;
	
	public abstract boolean update(long currentTime);
	
	public abstract void removePlayer(Player p);
	
	public abstract boolean isDead();
	public abstract ItemFrame getItemFrame();
	public abstract BlockFace getFacing();
	public abstract Location getLocation();
	
	public abstract UUID getSignUid();
	public abstract int getCurrentItemIndex();
	public abstract void setCurrentItemIndex(int currentItemIndex);
	public abstract int getDelay(int index);
	
	public abstract MapView[] getMapViews();
	
}
