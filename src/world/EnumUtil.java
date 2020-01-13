package de.stylextv.gs.world;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ItemFrame;
import org.bukkit.inventory.ItemStack;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

public abstract class EnumUtil {
	
	public abstract ItemFrame spawnItemFrame(int id, World world, Location loc, BlockFace dir, MapRenderer renderer);
	public abstract ItemFrame spawnItemFrame(World world, Location loc, BlockFace dir, MapRenderer renderer);
	
	public abstract MapView getMapView(ItemStack item);
	public abstract int getMapId(MapView view);
	
}
