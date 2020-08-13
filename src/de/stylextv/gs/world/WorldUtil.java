package de.stylextv.gs.world;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.UUID;
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
import de.stylextv.gs.packet.PacketListener;
import de.stylextv.gs.player.ConnectionManager;
import de.stylextv.gs.render.BetterMapRenderer;
import de.stylextv.gs.util.UUIDHelper;

public class WorldUtil {
	
	public static final int MCVERSION_1_8=0;
	public static final int MCVERSION_1_12=1;
	public static final int MCVERSION_1_13=2;
	public static final int MCVERSION_1_14=3;
	public static final int MCVERSION_1_15=4;
	public static final int MCVERSION_1_16=5;
	
	private static int FILE_HEADER_LENGTH=45;
	private static DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.##", new DecimalFormatSymbols(Locale.ENGLISH));
	
	private static File signFolder=new File("plugins/GamemodeSigns/signs");
	private static File customImagesFolder=new File("plugins/GamemodeSigns/images");
	
	private static EnumUtil enumUtil;
	private static int mcVersion=MCVERSION_1_14;
	
	private static CopyOnWriteArrayList<BetterFrame> frames=new CopyOnWriteArrayList<BetterFrame>();
	private static CopyOnWriteArrayList<BetterFrame> gifFrames=new CopyOnWriteArrayList<BetterFrame>();
	private static ConcurrentHashMap<BetterFrame, File> savedFrames=new ConcurrentHashMap<BetterFrame, File>();
	private static ConcurrentHashMap<BetterFrame, File> savedGifFrames=new ConcurrentHashMap<BetterFrame, File>();
	
	public static void onEnable() {
		String version=Bukkit.getServer().getVersion();
		if(version.contains("1.16")) mcVersion=MCVERSION_1_16;
		else if(version.contains("1.15")) mcVersion=MCVERSION_1_15;
		else if(version.contains("1.14")) mcVersion=MCVERSION_1_14;
		else if(version.contains("1.13")) mcVersion=MCVERSION_1_13;
		else if(version.contains("1.12")) mcVersion=MCVERSION_1_12;
		else if(version.contains("1.8")) mcVersion=MCVERSION_1_8;
		else Bukkit.getConsoleSender().sendMessage(Vars.PREFIX_CONSOLE+"The server-version (§c"+version+"§r) you are running is not supported by this plugin!");
		if(mcVersion<=MCVERSION_1_12) enumUtil=new EnumUtil18();
		else enumUtil=new EnumUtil114();
		
		customImagesFolder.mkdirs();
		
		PacketListener packetListener = new PacketListener();
		packetListener.start();
		
		new BukkitRunnable() {
			@Override
			public void run() {
				long currentTime=System.currentTimeMillis();
				int loaded=0;
				if(signFolder.exists()) for(File f:signFolder.listFiles()) {
					try {
						String name=f.getName();
						if(name.endsWith(".gsign")) {
							BetterFrame frame=loadFrame(f, currentTime);
							
							if(frame.getMapViews().length>1) {
								savedGifFrames.put(frame,f);
								gifFrames.add(frame);
							} else {
								savedFrames.put(frame,f);
							}
						}
						loaded++;
					} catch(Exception ex) {
						Bukkit.getConsoleSender().sendMessage(Vars.PREFIX_CONSOLE+"Deleted old/corrupted file: §c"+f.getName());
						f.delete();
					}
				}
				Bukkit.getConsoleSender().sendMessage(Vars.PREFIX_CONSOLE+"Succesfully loaded §"+(loaded == 0 ? "e" : "a")+loaded+"§r item-frames in "+DECIMAL_FORMAT.format((System.currentTimeMillis()-currentTime)/1000.0)+"s.");
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
	
	private static BetterFrame loadFrame(File f, long currentTime) throws IOException, DataFormatException {
		byte[] allBytes = Files.readAllBytes(f.toPath());
		
		Inflater inflater = new Inflater();
		inflater.setInput(allBytes);
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream(allBytes.length);
		byte[] buffer = new byte[allBytes.length*3];//(128*128+4*2)+FILE_HEADER_LENGTH
		while(!inflater.finished()) {
			int count = inflater.inflate(buffer);
		    outputStream.write(buffer, 0, count);
		}
		outputStream.close();
		inflater.end();
		allBytes = outputStream.toByteArray();
		
		byte[] uidBuffer=new byte[16];
		for(int i=0; i<uidBuffer.length; i++) {
			uidBuffer[i]=allBytes[i];
		}
		World world=Bukkit.getWorld(UUIDHelper.getUUIDFromBytes(uidBuffer));
		
		for(int i=0; i<uidBuffer.length; i++) {
			uidBuffer[i]=allBytes[i+16];
		}
		UUID signUid=UUIDHelper.getUUIDFromBytes(uidBuffer);
		
		int x=
				(0xff & allBytes[16*2  ]) << 24  |
				(0xff & allBytes[16*2+1]) << 16  |
				(0xff & allBytes[16*2+2]) << 8   |
				(0xff & allBytes[16*2+3]) << 0;
		int y=
				(0xff & allBytes[16*2+4]) << 24  |
				(0xff & allBytes[16*2+5]) << 16  |
				(0xff & allBytes[16*2+6]) << 8   |
				(0xff & allBytes[16*2+7]) << 0;
		int z=
				(0xff & allBytes[16*2+8]) << 24  |
				(0xff & allBytes[16*2+9]) << 16  |
				(0xff & allBytes[16*2+10]) << 8   |
				(0xff & allBytes[16*2+11]) << 0;
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
		
		int allBytesLength=allBytes.length-FILE_HEADER_LENGTH;
		int l=128*128+4*2;
		int a=allBytesLength/l;
		BetterMapRenderer[] mapRenderers=new BetterMapRenderer[a];
		int[] mapIds=new int[a];
		int[] delays=new int[a];
		for(int i=0; i<a; i++) {
			int index=i*l+FILE_HEADER_LENGTH;
			int mapId=
					(0xff & allBytes[index  ]) << 24  |
					(0xff & allBytes[index+1]) << 16  |
					(0xff & allBytes[index+2]) << 8   |
					(0xff & allBytes[index+3]) << 0;
			delays[i]=
					(0xff & allBytes[index+4]) << 24  |
					(0xff & allBytes[index+5]) << 16  |
					(0xff & allBytes[index+6]) << 8   |
					(0xff & allBytes[index+7]) << 0;
			mapIds[i]=mapId;
			byte[] bytes=new byte[128*128];
			for(int j=0; j<bytes.length; j++) {
				bytes[j]=allBytes[index+j+8];
			}
			mapRenderers[i]=new BetterMapRenderer(bytes);
		}
		
		if(itemFrame==null) {
			
			int facing=allBytes[44];
			BlockFace dir=BlockFace.values()[facing];
			
			BetterFrame frame=null;
			if(mcVersion==MCVERSION_1_14) {
				frame=new BetterFrame114(signUid, mapIds, loc, dir, mapRenderers, currentTime, delays);
			} else if(mcVersion==MCVERSION_1_16) {
				frame=new BetterFrame116(signUid, mapIds, loc, dir, mapRenderers, currentTime, delays);
			} else if(mcVersion==MCVERSION_1_15) {
				frame=new BetterFrame115(signUid, mapIds, loc, dir, mapRenderers, currentTime, delays);
			} else if(mcVersion==MCVERSION_1_13) {
				frame=new BetterFrame113(signUid, mapIds, loc, dir, mapRenderers, currentTime, delays);
			} else if(mcVersion==MCVERSION_1_12) {
				frame=new BetterFrame112(signUid, mapIds, loc, dir, mapRenderers, currentTime, delays);
			} else if(mcVersion==MCVERSION_1_8) {
				frame=new BetterFrame18(signUid, mapIds, loc, dir, mapRenderers, currentTime, delays);
			}
			return frame;
		} else {
			BetterFrame frame=null;
			if(mcVersion==MCVERSION_1_14) {
				frame=new BetterFrame114(signUid, mapIds, itemFrame, mapRenderers, currentTime, delays);
			} else if(mcVersion==MCVERSION_1_16) {
				frame=new BetterFrame116(signUid, mapIds, itemFrame, mapRenderers, currentTime, delays);
			} else if(mcVersion==MCVERSION_1_15) {
				frame=new BetterFrame115(signUid, mapIds, itemFrame, mapRenderers, currentTime, delays);
			} else if(mcVersion==MCVERSION_1_13) {
				frame=new BetterFrame113(signUid, mapIds, itemFrame, mapRenderers, currentTime, delays);
			} else if(mcVersion==MCVERSION_1_12) {
				frame=new BetterFrame112(signUid, mapIds, itemFrame, mapRenderers, currentTime, delays);
			} else if(mcVersion==MCVERSION_1_8) {
				frame=new BetterFrame18(signUid, mapIds, itemFrame, mapRenderers, currentTime, delays);
			}
			return frame;
		}
	}
	private static void saveFrame(BetterFrame frame) throws IOException {
	    MapView[] views=frame.getMapViews();
		Location loc=frame.getLocation();
		
		int l=128*128+4*2;
		byte[] totalBytes=new byte[views.length*l+FILE_HEADER_LENGTH];
		
		byte[] worldUidBytes=UUIDHelper.getBytesFromUUID(loc.getWorld().getUID());
		for(int i=0; i<worldUidBytes.length; i++) {
			totalBytes[i]=worldUidBytes[i];
		}
		byte[] signUidBytes=UUIDHelper.getBytesFromUUID(frame.getSignUid());
		for(int i=0; i<signUidBytes.length; i++) {
			totalBytes[i+16]=signUidBytes[i];
		}
		int x=loc.getBlockX();
		int y=loc.getBlockY();
		int z=loc.getBlockZ();
		totalBytes[16*2  ]=((byte)((x >> 24) & 0xff));
		totalBytes[16*2+1]=((byte)((x >> 16) & 0xff));
		totalBytes[16*2+2]=((byte)((x >> 8) & 0xff));
		totalBytes[16*2+3]=((byte)((x >> 0) & 0xff));
		
		totalBytes[16*2+4]=((byte)((y >> 24) & 0xff));
		totalBytes[16*2+5]=((byte)((y >> 16) & 0xff));
		totalBytes[16*2+6]=((byte)((y >> 8) & 0xff));
		totalBytes[16*2+7]=((byte)((y >> 0) & 0xff));
		
		totalBytes[16*2+8]=((byte)((z >> 24) & 0xff));
		totalBytes[16*2+9]=((byte)((z >> 16) & 0xff));
		totalBytes[16*2+10]=((byte)((z >> 8) & 0xff));
		totalBytes[16*2+11]=((byte)((z >> 0) & 0xff));
		
		int facing=0;
		BlockFace face=frame.getFacing();
		for(BlockFace check:BlockFace.values()) {
			if(face.equals(check)) break;
			facing++;
		}
		totalBytes[44]=(byte)facing;
		
    	for(int i=0; i<views.length; i++) {
    		int index=i*l+FILE_HEADER_LENGTH;
    		
    		MapView view=views[i];
    		int id=enumUtil.getMapId(view);
    		int delay=frame.getDelay(i);
    		totalBytes[index  ]=((byte)((id >> 24) & 0xff));
    		totalBytes[index+1]=((byte)((id >> 16) & 0xff));
    		totalBytes[index+2]=((byte)((id >> 8) & 0xff));
    		totalBytes[index+3]=((byte)((id >> 0) & 0xff));
    		totalBytes[index+4]=((byte)((delay >> 24) & 0xff));
    		totalBytes[index+5]=((byte)((delay >> 16) & 0xff));
    		totalBytes[index+6]=((byte)((delay >> 8) & 0xff));
    		totalBytes[index+7]=((byte)((delay >> 0) & 0xff));
    		byte[] bytes=((BetterMapRenderer)view.getRenderers().get(0)).getData();
    		for(int j=0; j<bytes.length; j++) {
    			totalBytes[index+8+j]=bytes[j];
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
		int number=0;
		while(new File(signFolder.getPath()+"/"+number+".gsign").exists()) {
			number++;
		}
		FileOutputStream fos = new FileOutputStream(signFolder.getPath()+"/"+number+".gsign");
		
		// Compress the data
		byte[] buf = new byte[totalBytes.length];
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
	
	public static void spawnItemFrame(UUID signUid, Location loc, byte[] image, BlockFace direction) {
		BetterFrame frame=null;
		if(mcVersion==MCVERSION_1_14) {
			frame=new BetterFrame114(signUid, loc, direction, new BetterMapRenderer[]{new BetterMapRenderer(image)}, 0, null);
		} else if(mcVersion==MCVERSION_1_16) {
			frame=new BetterFrame116(signUid, loc, direction, new BetterMapRenderer[]{new BetterMapRenderer(image)}, 0, null);
		} else if(mcVersion==MCVERSION_1_15) {
			frame=new BetterFrame115(signUid, loc, direction, new BetterMapRenderer[]{new BetterMapRenderer(image)}, 0, null);
		} else if(mcVersion==MCVERSION_1_13) {
			frame=new BetterFrame113(signUid, loc, direction, new BetterMapRenderer[]{new BetterMapRenderer(image)}, 0, null);
		} else if(mcVersion==MCVERSION_1_12) {
			frame=new BetterFrame112(signUid, loc, direction, new BetterMapRenderer[]{new BetterMapRenderer(image)}, 0, null);
		} else if(mcVersion==MCVERSION_1_8) {
			frame=new BetterFrame18(signUid, loc, direction, new BetterMapRenderer[]{new BetterMapRenderer(image)}, 0, null);
		}
		frames.add(frame);
	}
	public static void spawnItemFrame(UUID signUid, Location loc, byte[][] frames, int[] delays, long startTime, BlockFace direction) {
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
					frame=new BetterFrame114(signUid, loc, direction, mapRenderers, startTime, delays);
				} else if(mcVersion==MCVERSION_1_16) {
					frame=new BetterFrame116(signUid, loc, direction, mapRenderers, startTime, delays);
				} else if(mcVersion==MCVERSION_1_15) {
					frame=new BetterFrame115(signUid, loc, direction, mapRenderers, startTime, delays);
				} else if(mcVersion==MCVERSION_1_13) {
					frame=new BetterFrame113(signUid, loc, direction, mapRenderers, startTime, delays);
				} else if(mcVersion==MCVERSION_1_12) {
					frame=new BetterFrame112(signUid, loc, direction, mapRenderers, startTime, delays);
				} else if(mcVersion==MCVERSION_1_8) {
					frame=new BetterFrame18(signUid, loc, direction, mapRenderers, startTime, delays);
				}
				gifFrames.add(frame);
			}
		}.runTask(Main.getPlugin());
	}
	
	public static UUID randomSignUid() {
		UUID uid=UUID.randomUUID();
		while(true) {
			boolean exists=false;
			for(BetterFrame frame:frames) {
				if(frame.getSignUid().compareTo(uid)==0) {
					exists=true;
					break;
				}
			}
			if(!exists) for(BetterFrame frame:gifFrames) {
				if(frame.getSignUid().compareTo(uid)==0) {
					exists=true;
					break;
				}
			}
			if(!exists) for(BetterFrame frame:savedFrames.keySet()) {
				if(frame.getSignUid().compareTo(uid)==0) {
					exists=true;
					break;
				}
			}
			
			if(exists) {
				uid=UUID.randomUUID();
			} else break;
		}
		return uid;
	}
	public static BetterFrame getFrame(ItemFrame itemFrame) {
		for(BetterFrame frame:frames) {
			if(frame.getItemFrame().equals(itemFrame)) {
				return frame;
			}
		}
		for(BetterFrame frame:gifFrames) {
			if(frame.getItemFrame().equals(itemFrame)) {
				return frame;
			}
		}
		for(BetterFrame frame:savedFrames.keySet()) {
			if(frame.getItemFrame().equals(itemFrame)) {
				return frame;
			}
		}
		return null;
	}
	public static void removeSign(UUID uid) {
		for(BetterFrame frame:frames) {
			if(frame.getSignUid().compareTo(uid)==0) {
				frame.getItemFrame().remove();
				frames.remove(frame);
			}
		}
		for(BetterFrame frame:savedGifFrames.keySet()) {
			if(frame.getSignUid().compareTo(uid)==0) {
				frame.getItemFrame().remove();
				savedGifFrames.get(frame).delete();
				savedGifFrames.remove(frame);
				gifFrames.remove(frame);
			}
		}
		for(BetterFrame frame:gifFrames) {
			if(frame.getSignUid().compareTo(uid)==0) {
				frame.getItemFrame().remove();
				gifFrames.remove(frame);
			}
		}
		for(BetterFrame frame:savedFrames.keySet()) {
			if(frame.getSignUid().compareTo(uid)==0) {
				frame.getItemFrame().remove();
				savedFrames.get(frame).delete();
				savedFrames.remove(frame);
			}
		}
	}
	public static boolean isFrame(int entityId) {
		for(BetterFrame frame:frames) {
			if(frame.getItemFrame().getEntityId()==entityId) {
				return true;
			}
		}
		for(BetterFrame frame:savedGifFrames.keySet()) {
			if(frame.getItemFrame().getEntityId()==entityId) {
				return true;
			}
		}
		for(BetterFrame frame:gifFrames) {
			if(frame.getItemFrame().getEntityId()==entityId) {
				return true;
			}
		}
		for(BetterFrame frame:savedFrames.keySet()) {
			if(frame.getItemFrame().getEntityId()==entityId) {
				return true;
			}
		}
		return false;
	}
	
	public static File getCustomImagesFolder() {
		return customImagesFolder;
	}
	public static int getTotalAmountOfFrames() {
		File[] files=signFolder.listFiles();
		if(files==null) return 0;
		return files.length;
	}
	public static int getMcVersion() {
		return mcVersion;
	}
	
}
