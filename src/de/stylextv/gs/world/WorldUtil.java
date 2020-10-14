package de.stylextv.gs.world;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.server.MapInitializeEvent;
import org.bukkit.scheduler.BukkitRunnable;

import de.stylextv.gs.main.Main;
import de.stylextv.gs.main.Variables;
import de.stylextv.gs.packet.PacketListener;
import de.stylextv.gs.player.ConnectionManager;
import de.stylextv.gs.util.UUIDHelper;

public class WorldUtil {
	
	public static final int MCVERSION_1_8=0;
	public static final int MCVERSION_1_9=1;
	public static final int MCVERSION_1_11=3;
	public static final int MCVERSION_1_12=4;
	public static final int MCVERSION_1_13=5;
	public static final int MCVERSION_1_14=6;
	public static final int MCVERSION_1_15=7;
	public static final int MCVERSION_1_16=8;
	
	private static int FILE_HEADER_LENGTH=45;
	private static DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.##", new DecimalFormatSymbols(Locale.ENGLISH));
	
	private static File signFolder=new File("plugins/GSigns/signs");
	private static File customImagesFolder=new File("plugins/GSigns/images");
	
	private static MapManager mapManager;
	private static int mcVersion=MCVERSION_1_14;
	
	private static CopyOnWriteArrayList<BetterFrame> frames=new CopyOnWriteArrayList<BetterFrame>();
	private static CopyOnWriteArrayList<BetterFrame> gifFrames=new CopyOnWriteArrayList<BetterFrame>();
	private static ConcurrentHashMap<BetterFrame, File> savedFrames=new ConcurrentHashMap<BetterFrame, File>();
	private static ConcurrentHashMap<BetterFrame, File> savedGifFrames=new ConcurrentHashMap<BetterFrame, File>();
	
	private static boolean inGifUpdate;
	
	public static void onEnable() {
		String version=Bukkit.getServer().getVersion();
		mcVersion=Integer.valueOf(version.split("MC: 1\\.")[1].split("\\.")[0])-8;
		if(mcVersion!=MCVERSION_1_8 && !(mcVersion>=MCVERSION_1_12&&mcVersion<=MCVERSION_1_16)) {
			Bukkit.getConsoleSender().sendMessage(Variables.PREFIX_CONSOLE+"The server-version (§c"+version+"§r) you are running is not supported by this plugin!");
		}
		
		customImagesFolder.mkdirs();
		
		mapManager = new MapManager();
		mapManager.searchForVanillaMaps();
		
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
							
							if(frame.getImages().length>1) {
								savedGifFrames.put(frame,f);
								gifFrames.add(frame);
							} else {
								savedFrames.put(frame,f);
							}
						}
						loaded++;
					} catch(Exception ex) {
						Bukkit.getConsoleSender().sendMessage(Variables.PREFIX_CONSOLE+"Deleted old/corrupted file: §c"+f.getName());
						ex.printStackTrace();
						f.delete();
					}
				}
				Bukkit.getConsoleSender().sendMessage(Variables.PREFIX_CONSOLE+"Succesfully loaded §"+(loaded == 0 ? "e" : "a")+loaded+"§r item-frames in "+DECIMAL_FORMAT.format((System.currentTimeMillis()-currentTime)/1000.0)+"s.");
			}
		}.runTaskLater(Main.getPlugin(), 2);
		new BukkitRunnable() {
			@Override
			public void run() {
				if(!inGifUpdate) {
					inGifUpdate=true;
					
					ConnectionManager.update();
					long currentTime=System.currentTimeMillis();
					for(BetterFrame frame:gifFrames) {
						if(frame.update(currentTime)) gifFrames.remove(frame);
					}
					for(BetterFrame frame:savedFrames.keySet()) {
						frame.update(0);
					}
					for(BetterFrame frame:frames) {
						if(frame.update(0)) frames.remove(frame);
					}
					
					inGifUpdate=false;
				}
			}
		}.runTaskTimerAsynchronously(Main.getPlugin(), 0, 0);
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
		int l=128*128+4;
		int a=allBytesLength/l;
		byte[][] images=new byte[a][];
		int[] delays=new int[a];
		for(int i=0; i<a; i++) {
			int index=i*l+FILE_HEADER_LENGTH;
			delays[i]=
					(0xff & allBytes[index  ]) << 24  |
					(0xff & allBytes[index+1]) << 16  |
					(0xff & allBytes[index+2]) << 8   |
					(0xff & allBytes[index+3]) << 0;
			byte[] bytes=new byte[128*128];
			for(int j=0; j<bytes.length; j++) {
				bytes[j]=allBytes[index+j+4];
			}
			images[i]=bytes;
		}
		
		if(itemFrame==null) {
			
			int facing=allBytes[44];
			BlockFace dir=BlockFace.values()[facing];
			
			return new BetterFrame(signUid, loc, dir, images, currentTime, delays);
		} else {
			return new BetterFrame(signUid, itemFrame, images, currentTime, delays);
		}
	}
	private static void saveFrame(BetterFrame frame) throws IOException {
	    byte[][] images=frame.getImages();
		Location loc=frame.getLocation();
		
		int l=128*128+4;
		byte[] totalBytes=new byte[images.length*l+FILE_HEADER_LENGTH];
		
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
		
    	for(int i=0; i<images.length; i++) {
    		int index=i*l+FILE_HEADER_LENGTH;
    		
    		byte[] data=images[i];
    		int delay=frame.getDelay(i);
    		totalBytes[index  ]=((byte)((delay >> 24) & 0xff));
    		totalBytes[index+1]=((byte)((delay >> 16) & 0xff));
    		totalBytes[index+2]=((byte)((delay >> 8) & 0xff));
    		totalBytes[index+3]=((byte)((delay >> 0) & 0xff));
    		for(int j=0; j<data.length; j++) {
    			totalBytes[index+4+j]=data[j];
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
		frames.add(new BetterFrame(signUid, loc, direction, new byte[][]{image}, 0, null));
	}
	public static void spawnItemFrame(UUID signUid, Location loc, byte[][] frames, int[] delays, long startTime, BlockFace direction) {
		int[] delaysCopy=new int[delays.length];
		
		int compactedFrameIndex=0;
		byte[] currentHead=null;
		for(int i=0; i<frames.length; i++) {
			if(currentHead==null) {
				byte[] frame0=frames[i];
				if(i+1==frames.length) {
					frames[compactedFrameIndex]=frame0;
					delaysCopy[compactedFrameIndex]=delays[i];
					compactedFrameIndex++;
				} else {
					byte[] frame1=frames[i+1];
					boolean equal=true;
					for(int j=0; j<frame0.length; j++) {
						if(frame0[j]!=frame1[j]) {
							equal=false;
							break;
						}
					}
					if(equal) {
						currentHead=frame0;
						frames[compactedFrameIndex]=frame0;
						delaysCopy[compactedFrameIndex]=delays[i]+delays[i+1];
						i++;
					} else {
						frames[compactedFrameIndex]=frame0;
						delaysCopy[compactedFrameIndex]=delays[i];
						compactedFrameIndex++;
					}
				}
			} else {
				byte[] frame0=frames[i];
				boolean equal=true;
				for(int j=0; j<frame0.length; j++) {
					if(frame0[j]!=currentHead[j]) {
						equal=false;
						break;
					}
				}
				if(equal) {
					delaysCopy[compactedFrameIndex]=delaysCopy[compactedFrameIndex]+delays[i];
				} else {
					currentHead=null;
					compactedFrameIndex++;
					i--;
				}
			}
		}
		if(currentHead!=null) compactedFrameIndex++;
		
		boolean b=compactedFrameIndex!=1;
		if(compactedFrameIndex != frames.length) {
			byte[][] newFrames=new byte[compactedFrameIndex][];
			int[] newDelays=null;
			if(b) newDelays=new int[compactedFrameIndex];
			for(int i=0; i<compactedFrameIndex; i++) {
				newFrames[i]=frames[i];
				if(b) newDelays[i]=delaysCopy[i];
			}
			
			frames=newFrames;
			delays=newDelays;
		}
		
		int[] delaysF=delays;
		byte[][] images=frames;
		new BukkitRunnable() {
			@Override
			public void run() {
				loc.getBlock().setType(Material.AIR);
				
				gifFrames.add(new BetterFrame(signUid, loc, direction, images, b?startTime:0, delaysF));
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
		int entityId = itemFrame.getEntityId();
		for(BetterFrame frame:frames) {
			if(frame.getEntityId()==entityId) {
				return frame;
			}
		}
		for(BetterFrame frame:gifFrames) {
			if(frame.getEntityId()==entityId) {
				return frame;
			}
		}
		for(BetterFrame frame:savedFrames.keySet()) {
			if(frame.getEntityId()==entityId) {
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
			if(frame.getEntityId()==entityId) {
				return true;
			}
		}
		for(BetterFrame frame:savedGifFrames.keySet()) {
			if(frame.getEntityId()==entityId) {
				return true;
			}
		}
		for(BetterFrame frame:gifFrames) {
			if(frame.getEntityId()==entityId) {
				return true;
			}
		}
		for(BetterFrame frame:savedFrames.keySet()) {
			if(frame.getEntityId()==entityId) {
				return true;
			}
		}
		return false;
	}
	
	public static void onMapInitialize(MapInitializeEvent e) {
		mapManager.onMapInitialize(e);
	}
	public static Set<Short> getOccupiedIdsFor(OfflinePlayer p) {
		Set<Short> ids = new HashSet<>();
		for(BetterFrame frame:frames) {
			frame.getOccupiedIdsFor(p, ids);
		}
		for(BetterFrame frame:savedGifFrames.keySet()) {
			frame.getOccupiedIdsFor(p, ids);
		}
		for(BetterFrame frame:gifFrames) {
			frame.getOccupiedIdsFor(p, ids);
		}
		for(BetterFrame frame:savedFrames.keySet()) {
			frame.getOccupiedIdsFor(p, ids);
		}
		return ids;
	}
	public static boolean isIdUsedBy(OfflinePlayer p, short id) {
		if(id > MapManager.FORCED_OFFSET) {
			for(BetterFrame frame:frames) {
				if(frame.isIdUsedBy(p, id)) return true;
			}
			for(BetterFrame frame:savedGifFrames.keySet()) {
				if(frame.isIdUsedBy(p, id)) return true;
			}
			for(BetterFrame frame:gifFrames) {
				if(frame.isIdUsedBy(p, id)) return true;
			}
			for(BetterFrame frame:savedFrames.keySet()) {
				if(frame.isIdUsedBy(p, id)) return true;
			}
		}
		return false;
	}
	
	public static MapManager getMapManager() {
		return mapManager;
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
