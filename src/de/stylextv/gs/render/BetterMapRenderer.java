package de.stylextv.gs.render;

import java.awt.image.BufferedImage;

import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

import de.stylextv.gs.map.MapColorPalette;

public class BetterMapRenderer extends MapRenderer {
	
	private byte[] data;
	
	public BetterMapRenderer(BufferedImage image) {
		int l=128*128;
		int[] pixels = new int[image.getWidth() * image.getHeight()];
		image.getRGB(0, 0, image.getWidth(), image.getHeight(), pixels, 0, image.getWidth());
		this.data=new byte[l];
		for(int i=0; i<l; i++) {
			int rgb=pixels[i];
			int r = (rgb >> 16) & 0xFF;
			int g = (rgb >> 8) & 0xFF;
			int b = rgb & 0xFF;
			this.data[i]=MapColorPalette.getColor(r, g, b);
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
