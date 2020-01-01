package de.stylextv.gs.world;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import javax.imageio.ImageIO;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.scheduler.BukkitRunnable;

import de.stylextv.gs.main.Main;
import de.stylextv.gs.render.ImageMapRenderer;

public class WorldUtil {
	
	private static File imageFolder=new File("plugins/GamemodeSigns/images");
	private static File customImagesFolder=new File("plugins/GamemodeSigns/customImages");
	public static File getCustomImagesFolder() {
		return customImagesFolder;
	}
	
	private static ArrayList<ItemFrame> frames=new ArrayList<ItemFrame>();
	private static HashMap<ItemFrame, File> savedFrames=new HashMap<ItemFrame, File>();
	
	public static void onEnable() {
		customImagesFolder.mkdirs();
		
		new BukkitRunnable() {
			@SuppressWarnings("deprecation")
			@Override
			public void run() {
				if(imageFolder.exists()) for(File f:imageFolder.listFiles()) {
					try {
						String name=f.getName().replace(".png", "");
						String[] split=name.split(",");
						int id=Integer.valueOf(split[0]);
						String w=split[1];
						int x=Integer.valueOf(split[2]);
						int y=Integer.valueOf(split[3]);
						int z=Integer.valueOf(split[4]);
						World world=Bukkit.getWorld(w);
						Location loc=new Location(world, x, y, z);
						
						ItemFrame blocked=null;
						for(Entity e:loc.getChunk().getEntities()) {
							if(e instanceof ItemFrame) {
								Location eLoc=e.getLocation();
								if(eLoc.getBlockX()==x&&eLoc.getBlockY()==y&&eLoc.getBlockZ()==z) {
									blocked=(ItemFrame) e;
									break;
								}
							}
						}
						if(blocked==null) {
							BlockFace dir=BlockFace.valueOf(split[5]);
							BufferedImage image=ImageIO.read(f);
							
							ItemFrame frame=(ItemFrame) world.spawnEntity(loc, EntityType.ITEM_FRAME);
							frame.setFacingDirection(dir);
							
							MapView view=Bukkit.getMap(id);
							view.getRenderers().clear();
							for(MapRenderer r:view.getRenderers()) view.removeRenderer(r);
							view.addRenderer(new ImageMapRenderer(image));
							ItemStack item = new ItemStack(Material.FILLED_MAP, 1);
							MapMeta meta=(MapMeta) item.getItemMeta();
							meta.setMapView(view);
							item.setItemMeta(meta);
							
							frame.setItem(item);
							savedFrames.put(frame,f);
						} else savedFrames.put(blocked,f);
					} catch(Exception ex) {}
				}
			}
		}.runTaskLater(Main.getPlugin(), 20*2);
	}
	public static void onDisable() {
		imageFolder.mkdirs();
		for(ItemFrame frame:savedFrames.keySet()) {
			if(frame.isDead()) {
				savedFrames.get(frame).delete();
			} else frame.remove();
		}
		try {
			for(ItemFrame frame:frames) {
				if(!frame.isDead()) {
					frame.remove();
					MapMeta meta=(MapMeta) frame.getItem().getItemMeta();
					MapView view=meta.getMapView();
					Location loc=frame.getLocation();
					File file=new File("plugins/GamemodeSigns/images/"+view.getId()+","+loc.getWorld().getName()+","+loc.getBlockX()+","+loc.getBlockY()+","+loc.getBlockZ()+","+frame.getFacing()+".png");
					ImageIO.write(((ImageMapRenderer)view.getRenderers().get(0)).getImage(), "PNG", file);
				}
			}
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public static void removeAllDrewEntries(Player p) {
		for(ItemFrame frame:frames) {
			removeDrewEntry(frame, p);
		}
		for(ItemFrame frame:savedFrames.keySet()) {
			removeDrewEntry(frame, p);
		}
	}
	private static void removeDrewEntry(ItemFrame frame, Player p) {
		try {
			MapMeta meta=(MapMeta) frame.getItem().getItemMeta();
			MapView view=meta.getMapView();
			
			((ImageMapRenderer)view.getRenderers().get(0)).removePlayer(p);
		} catch(Exception ex) {}
	}
	
	public static void spawnItemFrame(World world, Location loc, BufferedImage image, BlockFace direction) {
		ItemFrame frame=(ItemFrame) world.spawnEntity(loc, EntityType.ITEM_FRAME);
		frame.setFacingDirection(direction);
		
		MapView view = Bukkit.createMap(world);
		view.getRenderers().clear();
		for(MapRenderer r:view.getRenderers()) view.removeRenderer(r);
		view.addRenderer(new ImageMapRenderer(image));
		ItemStack item = new ItemStack(Material.FILLED_MAP, 1);
		MapMeta meta=(MapMeta) item.getItemMeta();
		meta.setMapView(view);
		item.setItemMeta(meta);
		
		frame.setItem(item);
		frames.add(frame);
	}
	
	public static int getTotalAmountOfFrames() {
		return imageFolder.listFiles().length;
	}
	
}
