package de.stylextv.gs.world;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.imageio.ImageIO;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.scheduler.BukkitRunnable;

import de.stylextv.gs.main.Main;
import de.stylextv.gs.render.GifMapRenderer;
import de.stylextv.gs.render.ImageMapRenderer;

public class WorldUtil {
	
	private static File imageFolder=new File("plugins/GamemodeSigns/images");
	private static File customImagesFolder=new File("plugins/GamemodeSigns/customImages");
	public static File getCustomImagesFolder() {
		return customImagesFolder;
	}
	
	private static EnumUtil enumUtil;
	
	private static CopyOnWriteArrayList<ItemFrame> frames=new CopyOnWriteArrayList<ItemFrame>();
	private static CopyOnWriteArrayList<ItemFrame> gifFrames=new CopyOnWriteArrayList<ItemFrame>();
	private static HashMap<ItemFrame, File> savedFrames=new HashMap<ItemFrame, File>();
	private static HashMap<ItemFrame, File> savedGifFrames=new HashMap<ItemFrame, File>();
	
	public static void onEnable() {
		String version=Bukkit.getServer().getVersion();
		if(version.contains("1.8")) enumUtil=new EnumUtil18();
		else enumUtil=new EnumUtil114();
		
		customImagesFolder.mkdirs();
		
		new BukkitRunnable() {
			@Override
			public void run() {
				long currentTime=System.currentTimeMillis();
				if(imageFolder.exists()) for(File f:imageFolder.listFiles()) {
					try {
						String name=f.getName();
						if(name.endsWith(".ggif")) {
							name=name.replace(".ggif", "");
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
								int delay=Integer.valueOf(split[6]);
								byte[] allBytes = Files.readAllBytes(f.toPath());
								int allBytesLength=allBytes.length;
								int l=128*128;
								int a=allBytesLength/l;
								byte[][] framesData=new byte[a][];
								for(int i=0; i<a; i++) {
									byte[] bytes=new byte[128*128];
									for(int j=0; j<l; j++) {
										bytes[j]=allBytes[i*l+j];
									}
									framesData[i]=bytes;
								}
								GifMapRenderer renderer=new GifMapRenderer(framesData, delay, currentTime);
								
								ItemFrame frame=enumUtil.spawnItemFrame(id, world, loc, dir, renderer);
								savedGifFrames.put(frame,f);
								gifFrames.add(frame);
							} else savedFrames.put(blocked,f);
						} else {
							name=name.replace(".png", "");
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
								
								ItemFrame frame=enumUtil.spawnItemFrame(id, world, loc, dir, new ImageMapRenderer(image));
								savedFrames.put(frame,f);
							} else savedFrames.put(blocked,f);
						}
					} catch(Exception ex) {ex.printStackTrace();}
				}
			}
		}.runTaskLater(Main.getPlugin(), 20*2);
		new BukkitRunnable() {
			@Override
			public void run() {
				long currentTime=System.currentTimeMillis();
				for(ItemFrame frame:gifFrames) {
				    MapView view=enumUtil.getMapView(frame.getItem());
					if(!frame.isDead()&&view!=null) {
					    MapRenderer r=view.getRenderers().get(0);
						if(r instanceof GifMapRenderer) {
							GifMapRenderer renderer=(GifMapRenderer) r;
							if(renderer.update(currentTime)) {
								for(Player all:Bukkit.getOnlinePlayers()) {
									if(all.getLocation().distanceSquared(frame.getLocation())<20*20) {
										all.sendMap(view);
									}
								}
							}
						}
					} else gifFrames.remove(frame);
				}
			}
		}.runTaskTimerAsynchronously(Main.getPlugin(), 0, 0);
		new BukkitRunnable() {
			@Override
			public void run() {
				for(ItemFrame frame:savedFrames.keySet()) {
				    MapView view=enumUtil.getMapView(frame.getItem());
					if(!frame.isDead()&&view!=null) {
					    MapRenderer r=view.getRenderers().get(0);
					    if(r instanceof ImageMapRenderer) {
							ImageMapRenderer renderer=(ImageMapRenderer) r;
							for(Player all:Bukkit.getOnlinePlayers()) {
								if(renderer.shouldDrawTo(all,frame)) {
									all.sendMap(view);
								}
							}
					    }
					}
				}
				for(ItemFrame frame:frames) {
				    MapView view=enumUtil.getMapView(frame.getItem());
					if(!frame.isDead()&&view!=null) {
					    MapRenderer r=view.getRenderers().get(0);
					    if(r instanceof ImageMapRenderer) {
							ImageMapRenderer renderer=(ImageMapRenderer) r;
							for(Player all:Bukkit.getOnlinePlayers()) {
								if(renderer.shouldDrawTo(all,frame)) {
									all.sendMap(view);
								}
							}
					    }
					} else frames.remove(frame);
				}
			}
		}.runTaskTimerAsynchronously(Main.getPlugin(), 0, 10);
	}
	public static void onDisable() {
		imageFolder.mkdirs();
		for(ItemFrame frame:savedFrames.keySet()) {
			if(frame.isDead()) {
				savedFrames.get(frame).delete();
			} else frame.remove();
		}
		for(ItemFrame frame:savedGifFrames.keySet()) {
			gifFrames.remove(frame);
			if(frame.isDead()) {
				savedGifFrames.get(frame).delete();
			} else frame.remove();
		}
		try {
			for(ItemFrame frame:frames) {
				if(!frame.isDead()) {
					frame.remove();
				    MapView view=enumUtil.getMapView(frame.getItem());
					Location loc=frame.getLocation();
					File file=new File("plugins/GamemodeSigns/images/"+enumUtil.getMapId(view)+","+loc.getWorld().getName()+","+loc.getBlockX()+","+loc.getBlockY()+","+loc.getBlockZ()+","+frame.getFacing()+".png");
					ImageIO.write(((ImageMapRenderer)view.getRenderers().get(0)).getImage(), "PNG", file);
				}
			}
			for(ItemFrame frame:gifFrames) {
				frame.remove();
			    MapView view=enumUtil.getMapView(frame.getItem());
				Location loc=frame.getLocation();
				GifMapRenderer renderer=(GifMapRenderer)view.getRenderers().get(0);
			    try (FileOutputStream fos = new FileOutputStream("plugins/GamemodeSigns/images/"+enumUtil.getMapId(view)+","+loc.getWorld().getName()+","+loc.getBlockX()+","+loc.getBlockY()+","+loc.getBlockZ()+","+frame.getFacing()+","+renderer.getDelay()+".ggif")) {
			    	for(byte[] bytes:renderer.getFramesData()) fos.write(bytes);
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
		    MapView view=enumUtil.getMapView(frame.getItem());
			
			((ImageMapRenderer)view.getRenderers().get(0)).removePlayer(p);
		} catch(Exception ex) {}
	}
	
	public static void spawnItemFrame(World world, Location loc, BufferedImage image, BlockFace direction) {
		ItemFrame frame=enumUtil.spawnItemFrame(world, loc, direction, new ImageMapRenderer(image));
		frames.add(frame);
	}
	public static void spawnItemFrame(World world, Location loc, BufferedImage[] frames, int delay, long startTime, BlockFace direction) {
		GifMapRenderer renderer=new GifMapRenderer(frames,delay,startTime);
		new BukkitRunnable() {
			@Override
			public void run() {
				loc.getBlock().setType(Material.AIR);
				
				ItemFrame frame=enumUtil.spawnItemFrame(world, loc, direction, renderer);
				gifFrames.add(frame);
			}
		}.runTask(Main.getPlugin());
	}
	
	public static int getTotalAmountOfFrames() {
		return imageFolder.listFiles().length;
	}
	
}
