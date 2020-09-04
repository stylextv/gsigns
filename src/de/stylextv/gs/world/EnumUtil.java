package de.stylextv.gs.world;

import org.bukkit.inventory.ItemStack;
import org.bukkit.map.MapView;

public abstract class EnumUtil {
	
	public abstract MapView getMapView(ItemStack item);
	public abstract int getMapId(MapView view);
	
}
