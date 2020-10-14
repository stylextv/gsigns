package de.stylextv.gs.world;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.server.MapInitializeEvent;
import org.bukkit.map.MapView;

public class MapManager {
	
	public static int FORCED_OFFSET = 10000;
	
	private final Set<Short> occupiedIds = new HashSet<>();
	
	@SuppressWarnings("deprecation")
	public void searchForVanillaMaps() {
		for(short s = 0; s < Short.MAX_VALUE; s++) {
			try {
				MapView view = null;
				if(WorldUtil.getMcVersion() <= WorldUtil.MCVERSION_1_12) {
					try {
						view = (MapView) Bukkit.class.getMethod("getMap", short.class).invoke(Bukkit.class, s);
					} catch(Exception ex) {ex.printStackTrace();}
				} else view = Bukkit.getMap(s);
				if(view != null) {
					occupiedIds.add(s);
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
	public void onMapInitialize(MapInitializeEvent e) {
		short s = 0;
		if(WorldUtil.getMcVersion() <= WorldUtil.MCVERSION_1_12) {
			try {
				s=(short) e.getMap().getClass().getMethod("getId").invoke(e.getMap());
			} catch(Exception ex) {ex.printStackTrace();}
		} else s=(short)e.getMap().getId();
		if(s > FORCED_OFFSET && !occupiedIds.contains(s)) {
			occupiedIds.add(s);
		}
	}
	
	public short getNextFreeIdFor(Player p) {
		Set<Short> occupied = WorldUtil.getOccupiedIdsFor(p);
		occupied.addAll(occupiedIds);
		
		int largest = FORCED_OFFSET;
		for(Short s : occupied) {
			if (s > largest) { largest = s; }
		}
		
		if (largest + 1 < Short.MAX_VALUE) { return (short) (largest + 1); }

		for (short s = 0; s < Short.MAX_VALUE; s++) {
			if (!occupied.contains(s)) {
				return s;
			}
		}
		
		throw new RuntimeException("'" + p + "' reached the maximum amount of available Map-IDs");
	}
	
}
