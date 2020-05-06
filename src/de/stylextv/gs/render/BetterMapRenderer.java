package de.stylextv.gs.render;

import java.awt.image.BufferedImage;

import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapPalette;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

public class BetterMapRenderer extends MapRenderer {
	
	private byte[] data;
	
	@SuppressWarnings("deprecation")
	public BetterMapRenderer(BufferedImage image) {
		int l=128*128;
		this.data=new byte[l];
		for(int j=0; j<l; j++) {
			int rgb=image.getRGB(j%128, j/128);
			int r = (rgb >> 16) & 0xFF;
			int g = (rgb >> 8) & 0xFF;
			int b = rgb & 0xFF;
			this.data[j]=MapPalette.matchColor(r,g,b);
		}
	}
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
