package de.stylextv.gs.world;

import java.lang.reflect.InvocationTargetException;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.map.MapView;

public class EnumUtil18 extends EnumUtil {
	
	@SuppressWarnings("deprecation")
	@Override
	public MapView getMapView(ItemStack item) {
		if(item==null) return null;
		try {
			return (MapView) Bukkit.class.getMethod("getMap", short.class).invoke(Bukkit.class, item.getDurability());
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | SecurityException | NoSuchMethodException ex) {ex.printStackTrace();}
		return null;
	}
	@Override
	public int getMapId(MapView view) {
		try {
			short id=(short) view.getClass().getMethod("getId").invoke(view);
			return id;
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | SecurityException | NoSuchMethodException ex) {ex.printStackTrace();}
		return 0;
	}
	
}
