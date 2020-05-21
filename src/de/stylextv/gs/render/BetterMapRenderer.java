package de.stylextv.gs.render;

import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

public class BetterMapRenderer extends MapRenderer {
	
	private byte[] data;
	
	public BetterMapRenderer(byte[] data) {
		this.data=data;
	}
	
	@Override
	public void render(MapView view, MapCanvas canvas, Player p) {
		for(int i=0; i<data.length; i++) {
			canvas.setPixel(i%128, i/128, data[i]);
		}
	}
	
	public byte[] getData() {
		return data;
	}
	
}
