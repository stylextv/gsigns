package de.stylextv.gs.render;

import java.awt.image.BufferedImage;

import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

public class ImageMapRenderer extends MapRenderer {
	
	private BufferedImage image;
	
	public ImageMapRenderer(BufferedImage image) {
		this.image=image;
	}
	
	@Override
	public void render(MapView view, MapCanvas canvas, Player p) {
		canvas.drawImage(0, 0, image);
	}
	
	public BufferedImage getImage() {
		return image;
	}
	
}
