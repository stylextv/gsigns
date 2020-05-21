package de.stylextv.gs.world;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

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
import de.stylextv.gs.main.Vars;
import de.stylextv.gs.player.ConnectionManager;
import de.stylextv.gs.render.BetterMapRenderer;

public class WorldUtil {
	
	public static final int MCVERSION_1_8=0;
	public static final int MCVERSION_1_12=1;
	public static final int MCVERSION_1_13=2;
	public static final int MCVERSION_1_14=3;
	public static final int MCVERSION_1_15=4;
	
	private static File signFolder=new File("plugins/GamemodeSigns/signs");
	private static File customImagesFolder=new File("plugins/GamemodeSigns/images");
	public static File getCustomImagesFolder() {
		return customImagesFolder;
	}
	
	private static EnumUtil enumUtil;
	private static int mcVersion=MCVERSION_1_14;
	
	private static CopyOnWriteArrayList<BetterFrame> frames=new CopyOnWriteArrayList<BetterFrame>();
	private static CopyOnWriteArrayList<BetterFrame> gifFrames=new CopyOnWriteArrayList<BetterFrame>();
	private static ConcurrentHashMap<BetterFrame, File> savedFrames=new ConcurrentHashMap<BetterFrame, File>();
	private static ConcurrentHashMap<BetterFrame, File> savedGifFrames=new ConcurrentHashMap<BetterFrame, File>();
	
	public static void onEnable() {
		String version=Bukkit.getServer().getVersion();
		if(version.contains("1.15")) mcVersion=MCVERSION_1_15;
		else if(version.contains("1.13")) mcVersion=MCVERSION_1_13;
		else if(version.contains("1.12")) mcVersion=MCVERSION_1_12;
		else if(version.contains("1.8")) mcVersion=MCVERSION_1_8;
		if(mcVersion<=MCVERSION_1_12) enumUtil=new EnumUtil18();
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
					} catch(Exception ex) {
						Bukkit.getConsoleSender().sendMessage(Vars.PREFIX_CONSOLE+"Deleted old/corrupted file: "+f.getName());
						f.delete();
					}
				}
			}
		}.runTaskLater(Main.getPlugin(), 2);
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
			}
		}
		for(BetterFrame frame:savedGifFrames.keySet()) {
			gifFrames.remove(frame);
			if(frame.isDead()) {
				savedGifFrames.get(frame).delete();
			}
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
	
	private static BetterFrame loadFrame(String path, File f, long currentTime) throws IOException, DataFormatException {
		path=path.replace(".gsign", "");
		String[] split=path.split(",");
		String w=split[0];
		int x=Integer.valueOf(split[1]);
		int y=Integer.valueOf(split[2]);
		int z=Integer.valueOf(split[3]);
		World world=Bukkit.getWorld(w);
		Location loc=new Location(world, x, y, z);
		
		ItemFrame itemFrame=null;
		for(Entity e:loc.getChunk().getEntities()) {
			if(e instanceof ItemFrame) {
				Location eLoc=e.getLocation();
				if(eLoc.getBlockX()==x&&eLoc.getBlockY()==y&&eLoc.getBlockZ()==z) {
					itemFrame=(ItemFrame) e;
					break;
				}
			}
		}
		
		byte[] allBytes = Files.readAllBytes(f.toPath());
		
		Inflater inflater = new Inflater();
		inflater.setInput(allBytes);
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream(allBytes.length);  
		byte[] buffer = new byte[(128*128+4)*2];
		while(!inflater.finished()) {
			int count = inflater.inflate(buffer);
		    outputStream.write(buffer, 0, count);
		}
		outputStream.close();
		inflater.end();
		allBytes = outputStream.toByteArray();
		
		BlockFace dir=BlockFace.valueOf(split[4]);
		int delay=Integer.valueOf(split[5]);
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
		
		if(itemFrame==null) {
			BetterFrame frame=null;
			if(mcVersion==MCVERSION_1_14) {
				frame=new BetterFrame114(mapIds, loc, dir, mapRenderers, currentTime, delay);
			} else if(mcVersion==MCVERSION_1_15) {
				frame=new BetterFrame115(mapIds, loc, dir, mapRenderers, currentTime, delay);
			} else if(mcVersion==MCVERSION_1_13) {
				frame=new BetterFrame113(mapIds, loc, dir, mapRenderers, currentTime, delay);
			} else if(mcVersion==MCVERSION_1_12) {
				frame=new BetterFrame112(mapIds, loc, dir, mapRenderers, currentTime, delay);
			} else if(mcVersion==MCVERSION_1_8) {
				frame=new BetterFrame18(mapIds, loc, dir, mapRenderers, currentTime, delay);
			}
			return frame;
		} else {
			BetterFrame frame=null;
			if(mcVersion==MCVERSION_1_14) {
				frame=new BetterFrame114(mapIds, itemFrame, dir, mapRenderers, currentTime, delay);
			} else if(mcVersion==MCVERSION_1_15) {
				frame=new BetterFrame115(mapIds, itemFrame, dir, mapRenderers, currentTime, delay);
			} else if(mcVersion==MCVERSION_1_13) {
				frame=new BetterFrame113(mapIds, itemFrame, dir, mapRenderers, currentTime, delay);
			} else if(mcVersion==MCVERSION_1_12) {
				frame=new BetterFrame112(mapIds, itemFrame, dir, mapRenderers, currentTime, delay);
			} else if(mcVersion==MCVERSION_1_8) {
				frame=new BetterFrame18(mapIds, itemFrame, dir, mapRenderers, currentTime, delay);
			}
			return frame;
		}
	}
	private static void saveFrame(BetterFrame frame) throws IOException {
	    MapView[] views=frame.getMapViews();
		Location loc=frame.getLocation();
		String path=loc.getWorld().getName()+","+loc.getBlockX()+","+loc.getBlockY()+","+loc.getBlockZ()+","+frame.getFacing()+","+frame.getDelay();
		
		int l=128*128+4;
		byte[] totalBytes=new byte[views.length*l];
    	for(int i=0; i<views.length; i++) {
    		int index=i*l;
    		
    		MapView view=views[i];
    		int id=enumUtil.getMapId(view);
    		totalBytes[index  ]=((byte)((id >> 24) & 0xff));
    		totalBytes[index+1]=((byte)((id >> 16) & 0xff));
    		totalBytes[index+2]=((byte)((id >> 8) & 0xff));
    		totalBytes[index+3]=((byte)((id >> 0) & 0xff));
    		byte[] bytes=((BetterMapRenderer)view.getRenderers().get(0)).getData();
    		for(int j=0; j<bytes.length; j++) {
    			totalBytes[index+4+j]=bytes[j];
    		}
    	}
    	
	    Deflater compressor = new Deflater();
		compressor.setLevel(Deflater.BEST_SPEED);
		
		// Give the compressor the data to compress
		compressor.setInput(totalBytes);
		compressor.finish();
		
		// Create an expandable byte array to hold the compressed data.
		// It is not necessary that the compressed data will be smaller than
		// the uncompressed data.
		FileOutputStream fos = new FileOutputStream(signFolder.getPath()+"/"+path+".gsign");
		
		// Compress the data
		byte[] buf = new byte[totalBytes.length*2];
		while (!compressor.finished()) {
		      int count = compressor.deflate(buf);
		      fos.write(buf, 0, count);
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
	
	public static void spawnItemFrame(Location loc, byte[] image, BlockFace direction) {
		BetterFrame frame=null;
		if(mcVersion==MCVERSION_1_14) {
			frame=new BetterFrame114(loc, direction, new BetterMapRenderer[]{new BetterMapRenderer(image)}, 0, 0);
		} else if(mcVersion==MCVERSION_1_15) {
			frame=new BetterFrame115(loc, direction, new BetterMapRenderer[]{new BetterMapRenderer(image)}, 0, 0);
		} else if(mcVersion==MCVERSION_1_13) {
			frame=new BetterFrame113(loc, direction, new BetterMapRenderer[]{new BetterMapRenderer(image)}, 0, 0);
		} else if(mcVersion==MCVERSION_1_12) {
			frame=new BetterFrame112(loc, direction, new BetterMapRenderer[]{new BetterMapRenderer(image)}, 0, 0);
		} else if(mcVersion==MCVERSION_1_8) {
			frame=new BetterFrame18(loc, direction, new BetterMapRenderer[]{new BetterMapRenderer(image)}, 0, 0);
		}
		frames.add(frame);
	}
	public static void spawnItemFrame(Location loc, byte[][] frames, int delay, long startTime, BlockFace direction) {
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
				} else if(mcVersion==MCVERSION_1_13) {
					frame=new BetterFrame113(loc, direction, mapRenderers, startTime, delay);
				} else if(mcVersion==MCVERSION_1_12) {
					frame=new BetterFrame112(loc, direction, mapRenderers, startTime, delay);
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
	public static int getMcVersion() {
		return mcVersion;
	}
	
}
