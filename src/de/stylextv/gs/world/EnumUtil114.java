package de.stylextv.gs.world;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapView;

public class EnumUtil114 extends EnumUtil {
	
	@Override
	public MapView getMapView(ItemStack item) {
		if(item==null) return null;
		MapMeta meta=(MapMeta) item.getItemMeta();
		if(meta!=null) return meta.getMapView();
		return null;
	}
	@Override
	public int getMapId(MapView view) {
		return view.getId();
	}
	
}
