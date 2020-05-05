package de.stylextv.gs.render;

import java.awt.image.BufferedImage;
import java.util.ArrayList;

import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

public class ImageMapRenderer extends MapRenderer {
	
	private BufferedImage image;
	
	private ArrayList<Player> playersDrewTo = new ArrayList<Player>();
	
	public ImageMapRenderer(BufferedImage image) {
		this.image=image;
	}
	
	public boolean shouldDrawTo(Player p, ItemFrame frame) {
		if(p.getWorld()==frame.getWorld()&&p.getLocation().distanceSquared(frame.getLocation())<30*30) {
			if(playersDrewTo.contains(p)) {
				return false;
			}
			playersDrewTo.add(p);
			return true;
		} else {
			playersDrewTo.remove(p);
			return false;
		}
	}
	
	@Override
	public void render(MapView view, MapCanvas canvas, Player p) {
		if(!Thread.currentThread().getName().equals("Server thread")) {
			canvas.drawImage(0, 0, image);
		}
	}
	
	public void removePlayer(Player p) {
		playersDrewTo.remove(p);
	}
	public BufferedImage getImage() {
		return image;
	}
	
}
