package de.stylextv.gs.world;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.map.MapView;
import org.bukkit.scheduler.BukkitRunnable;

import de.stylextv.gs.main.Main;
import de.stylextv.gs.player.ConnectionManager;
import de.stylextv.gs.render.BetterMapRenderer;

public class WorldUtil {
	
	private static int MCVERSION_1_8=0;
	private static int MCVERSION_1_14=1;
	private static int MCVERSION_1_15=2;
	
	private static File signFolder=new File("plugins/GamemodeSigns/signs");
	private static File customImagesFolder=new File("plugins/GamemodeSigns/images");
	public static File getCustomImagesFolder() {
		return customImagesFolder;
	}
	
	private static EnumUtil enumUtil;
	private static int mcVersion=MCVERSION_1_14;
	
	private static CopyOnWriteArrayList<BetterFrame> frames=new CopyOnWriteArrayList<BetterFrame>();
	private static CopyOnWriteArrayList<BetterFrame> gifFrames=new CopyOnWriteArrayList<BetterFrame>();
	private static HashMap<BetterFrame, File> savedFrames=new HashMap<BetterFrame, File>();
	private static HashMap<BetterFrame, File> savedGifFrames=new HashMap<BetterFrame, File>();
	
	public static void onEnable() {
		String version=Bukkit.getServer().getVersion();
		if(version.contains("1.15")) mcVersion=MCVERSION_1_15;
		else if(version.contains("1.8")) mcVersion=MCVERSION_1_8;
		if(mcVersion==MCVERSION_1_8) enumUtil=new EnumUtil18();
		else enumUtil=new EnumUtil114();
		
		customImagesFolder.mkdirs();
		
		new BukkitRunnable() {
			@Override
			public void run() {
				long currentTime=System.currentTimeMillis();
				if(signFolder.exists()) for(File f:signFolder.listFiles()) {
					try {
						String name=f.getName();
						if(name.endsWith(".gsign")) {
							BetterFrame frame=loadFrame(name, f, currentTime);
							
							if(frame.getMapViews().length>1) {
								savedGifFrames.put(frame,f);
								gifFrames.add(frame);
							} else {
								savedFrames.put(frame,f);
							}
						}
					} catch(Exception ex) {ex.printStackTrace();}
				}
			}
		}.runTaskLater(Main.getPlugin(), 20*2);
		new BukkitRunnable() {
			@Override
			public void run() {
				ConnectionManager.update();
				long currentTime=System.currentTimeMillis();
				for(BetterFrame frame:gifFrames) {
					if(frame.update(currentTime)) gifFrames.remove(frame);
				}
			}
		}.runTaskTimerAsynchronously(Main.getPlugin(), 0, 0);
		new BukkitRunnable() {
			@Override
			public void run() {
				for(BetterFrame frame:savedFrames.keySet()) {
					frame.update(0);
				}
				for(BetterFrame frame:frames) {
					if(frame.update(0)) frames.remove(frame);
				}
			}
		}.runTaskTimerAsynchronously(Main.getPlugin(), 0, 10);
	}
	public static void onDisable() {
		signFolder.mkdirs();
		for(BetterFrame frame:savedFrames.keySet()) {
			if(frame.isDead()) {
				savedFrames.get(frame).delete();
			} else frame.remove();
		}
		for(BetterFrame frame:savedGifFrames.keySet()) {
			gifFrames.remove(frame);
			if(frame.isDead()) {
				savedGifFrames.get(frame).delete();
			} else frame.remove();
		}
		try {
			for(BetterFrame frame:frames) {
				if(!frame.isDead()) {
					saveFrame(frame);
				}
			}
			for(BetterFrame frame:gifFrames) {
				saveFrame(frame);
			}
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private static BetterFrame loadFrame(String path, File f, long currentTime) throws IOException {
		path=path.replace(".gsign", "");
		String[] split=path.split(",");
		String w=split[0];
		int x=Integer.valueOf(split[1]);
		int y=Integer.valueOf(split[2]);
		int z=Integer.valueOf(split[3]);
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
			BlockFace dir=BlockFace.valueOf(split[4]);
			int delay=Integer.valueOf(split[5]);
			byte[] allBytes = Files.readAllBytes(f.toPath());
			int allBytesLength=allBytes.length;
			int l=128*128+4;
			int a=allBytesLength/l;
			BetterMapRenderer[] mapRenderers=new BetterMapRenderer[a];
			int[] mapIds=new int[a];
			for(int i=0; i<a; i++) {
				int index=i*l;
				int mapId=
						(0xff & allBytes[index  ]) << 24  |
						(0xff & allBytes[index+1]) << 16  |
						(0xff & allBytes[index+2]) << 8   |
						(0xff & allBytes[index+3]) << 0;
				mapIds[i]=mapId;
				byte[] bytes=new byte[128*128];
				for(int j=0; j<bytes.length; j++) {
					bytes[j]=allBytes[index+j+4];
				}
				mapRenderers[i]=new BetterMapRenderer(bytes);
			}
			
			BetterFrame frame=null;
			if(mcVersion==MCVERSION_1_14) {
				frame=new BetterFrame114(mapIds, loc, dir, mapRenderers, currentTime, delay);
			} else if(mcVersion==MCVERSION_1_15) {
				frame=new BetterFrame115(mapIds, loc, dir, mapRenderers, currentTime, delay);
			} else if(mcVersion==MCVERSION_1_8) {
				frame=new BetterFrame18(mapIds, loc, dir, mapRenderers, currentTime, delay);
			}
			return frame;
		} else {
			BetterFrame frame=null;
			if(mcVersion==MCVERSION_1_14) {
				frame=new BetterFrame114(blocked);
			} else if(mcVersion==MCVERSION_1_15) {
				frame=new BetterFrame115(blocked);
			} else if(mcVersion==MCVERSION_1_8) {
				frame=new BetterFrame18(blocked);
			}
			return frame;
		}
	}
	private static void saveFrame(BetterFrame frame) throws IOException {
		frame.remove();
	    MapView[] views=frame.getMapViews();
		Location loc=frame.getLocation();
		String path=loc.getWorld().getName()+","+loc.getBlockX()+","+loc.getBlockY()+","+loc.getBlockZ()+","+frame.getFacing()+","+frame.getDelay();
		
		FileOutputStream fos = new FileOutputStream(signFolder.getPath()+"/"+path+".gsign");
    	for(MapView view:views) {
    		int id=enumUtil.getMapId(view);
    		fos.write((byte)((id >> 24) & 0xff));
    		fos.write((byte)((id >> 16) & 0xff));
            fos.write((byte)((id >> 8) & 0xff));
            fos.write((byte)((id >> 0) & 0xff));
    		byte[] bytes=((BetterMapRenderer)view.getRenderers().get(0)).getData();
    		fos.write(bytes);
    	}
    	fos.close();
	}
	
	public static void removeAllDrewEntries(Player p) {
		for(BetterFrame frame:frames) {
			frame.removePlayer(p);
		}
		for(BetterFrame frame:savedFrames.keySet()) {
			frame.removePlayer(p);
		}
		for(BetterFrame frame:gifFrames) {
			frame.removePlayer(p);
		}
	}
	
	public static void spawnItemFrame(Location loc, BufferedImage image, BlockFace direction) {
		BetterFrame frame=null;
		if(mcVersion==MCVERSION_1_14) {
			frame=new BetterFrame114(loc, direction, new BetterMapRenderer[]{new BetterMapRenderer(image)}, 0, 0);
		} else if(mcVersion==MCVERSION_1_15) {
			frame=new BetterFrame115(loc, direction, new BetterMapRenderer[]{new BetterMapRenderer(image)}, 0, 0);
		} else if(mcVersion==MCVERSION_1_8) {
			frame=new BetterFrame18(loc, direction, new BetterMapRenderer[]{new BetterMapRenderer(image)}, 0, 0);
		}
		frames.add(frame);
	}
	public static void spawnItemFrame(Location loc, BufferedImage[] frames, int delay, long startTime, BlockFace direction) {
		BetterMapRenderer[] mapRenderers=new BetterMapRenderer[frames.length];
		for(int i=0; i<frames.length; i++) {
			mapRenderers[i]=new BetterMapRenderer(frames[i]);
		}
		new BukkitRunnable() {
			@Override
			public void run() {
				loc.getBlock().setType(Material.AIR);
				
				BetterFrame frame=null;
				if(mcVersion==MCVERSION_1_14) {
					frame=new BetterFrame114(loc, direction, mapRenderers, startTime, delay);
				} else if(mcVersion==MCVERSION_1_15) {
					frame=new BetterFrame115(loc, direction, mapRenderers, startTime, delay);
				} else if(mcVersion==MCVERSION_1_8) {
					frame=new BetterFrame18(loc, direction, mapRenderers, startTime, delay);
				}
				gifFrames.add(frame);
			}
		}.runTask(Main.getPlugin());
	}
	
	public static int getTotalAmountOfFrames() {
		return signFolder.listFiles().length;
	}
	
}
