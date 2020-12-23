package de.stylextv.gs.world;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;

import de.stylextv.gs.gui.GuiManager;
import de.stylextv.gs.util.UUIDHelper;

public class BetterSign {
	
	private static final int FILE_HEADER_LENGTH=45;
	
	private UUID uid;
	private File file;
	private int width;
	private int height;
	
	private CopyOnWriteArrayList<BetterFrame> frames = new CopyOnWriteArrayList<BetterFrame>();
	
	public BetterSign(UUID uid) {
		this.uid = uid;
	}
	
	public void setSize(int width, int height) {
		this.width = width;
		this.height = height;
	}
	public void addFrame(BetterFrame frame) {
		frames.add(frame);
	}
	
	public boolean update(long currentTime) {
		for(BetterFrame frame:frames) {
			if(frame.update(currentTime)) return true;
		}
		return false;
	}
	public boolean isDead() {
		for(BetterFrame frame:frames) {
			if(frame.isDead()) return true;
		}
		return false;
	}
	public void removePlayer(Player p) {
		for(BetterFrame frame:frames) {
			frame.removePlayer(p);
		}
	}
	public BetterFrame getFrame(int entityId) {
		for(BetterFrame frame:frames) {
			if(frame.getEntityId()==entityId) {
				return frame;
			}
		}
		return null;
	}
	public BetterFrame getFrame(Location loc, BlockFace facing) {
		BetterFrame first = frames.get(0);
		if(!(first.getFacing().equals(facing) && first.getLocation().getWorld().equals(loc.getWorld()))) {
			return null;
		}
		
		for(BetterFrame frame:frames) {
			Location check=frame.getLocation();
			if(check.getBlockX()==loc.getBlockX() && check.getBlockY()==loc.getBlockY() && check.getBlockZ()==loc.getBlockZ()) {
				return frame;
			}
		}
		return null;
	}
	public boolean isFrame(int entityId) {
		for(BetterFrame frame:frames) {
			if(frame.getEntityId()==entityId) {
				return true;
			}
		}
		return false;
	}
	public void removeItemFrames() {
		for(BetterFrame frame:frames) {
			frame.removeItemFrame();
		}
	}
	public void teleport(Player p) {
		World w=getWorld();
		double x=0;
		double y=Integer.MAX_VALUE;
		double z=0;
		for(BetterFrame frame:frames) {
			Location loc=frame.getLocation();
			x+=loc.getBlockX();
			if(loc.getBlockY() < y) y=loc.getBlockY();
			z+=loc.getBlockZ();
		}
		int l=frames.size();
		x=x/l+0.5;
		z=z/l+0.5;
		BetterFrame frame=frames.get(0);
		float yaw=frame.getLocation().getYaw();
		float pitch=frame.getLocation().getPitch();
		int vx=frame.getFacing().getModX();
		int vy=frame.getFacing().getModY();
		int vz=frame.getFacing().getModZ();
		double tempY=y;
		for(int j=0; j<2; j++) {
			tempY--;
			Block b2=new Location(w, x, tempY, z).getBlock();
			if(b2.getType().isSolid() || b2.getRelative(BlockFace.UP).getType().isSolid()) {
				tempY++;
				break;
			}
		}
		double saveX=x;
		double saveY=y;
		double saveZ=z;
		boolean saveOneFound=false;
		if(new Location(w, x, tempY-1, z).getBlock().getType().isSolid()) {
			saveY=tempY;
			saveOneFound=true;
		}
		for(int i=0; i<2; i++) {
			x+=vx;
			y+=vy;
			z+=vz;
			Block b=new Location(w, x, y, z).getBlock();
			if(b.getType().isSolid() || b.getRelative(BlockFace.UP).getType().isSolid()) {
				x-=vx;
				y-=vy;
				z-=vz;
				break;
			}
			tempY=y;
			for(int j=0; j<2; j++) {
				tempY--;
				Block b2=new Location(w, x, tempY, z).getBlock();
				if(b2.getType().isSolid() || b2.getRelative(BlockFace.UP).getType().isSolid()) {
					tempY++;
					break;
				}
			}
			if(new Location(w, x, tempY-1, z).getBlock().getType().isSolid()) {
				saveX=x;
				saveY=tempY;
				saveZ=z;
				saveOneFound=true;
			}
			if(!saveOneFound) {
				saveX=x;
				saveY=y;
				saveZ=z;
			}
		}
		p.teleport(new Location(w, saveX, saveY, saveZ, (yaw+180)%360, -pitch));
	}
	public void play() {
		if(!isGif()) return;
		long currentTime=System.currentTimeMillis();
		for(BetterFrame frame:frames) {
			frame.play(currentTime);
		}
		GuiManager.onPlayPause(this);
	}
	public void pause() {
		if(!isGif()) return;
		long currentTime=System.currentTimeMillis();
		for(BetterFrame frame:frames) {
			frame.pause(currentTime);
		}
		GuiManager.onPlayPause(this);
	}
	
	public void getOccupiedIdsFor(OfflinePlayer p, Set<Short> ids) {
		for(BetterFrame frame:frames) {
			frame.getOccupiedIdsFor(p, ids);
		}
	}
	public boolean isIdUsedBy(OfflinePlayer p, short id) {
		for(BetterFrame frame:frames) {
			if(frame.isIdUsedBy(p, id)) return true;
		}
		return false;
	}
	
	public UUID getUid() {
		return uid;
	}
	public int getAmountOfFrames() {
		return frames.size();
	}
	public World getWorld() {
		return frames.get(0).getLocation().getWorld();
	}
	public String getSize() {
		return width+"x"+height;
	}
	public int getTotalSize() {
		return width*height;
	}
	public boolean isGif() {
		return frames.get(0).isGif();
	}
	public boolean isPaused() {
		return frames.get(0).isPaused();
	}
	
	public void deleteFile() {
		if(file != null) file.delete();
	}
	public File getFile() {
		return file;
	}
	public void setFile(File file) {
		this.file = file;
	}
	
	public static BetterSign loadSign(File f, long currentTime) throws IOException, DataFormatException {
		int l=128*128+4;
		byte[] allBytes = Files.readAllBytes(f.toPath());
		
		Inflater inflater = new Inflater();
		inflater.setInput(allBytes);
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream(allBytes.length);
		byte[] buffer = new byte[allBytes.length*3];
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
		UUID signUid=UUIDHelper.getUUIDFromBytes(uidBuffer);
		
		for(int i=0; i<uidBuffer.length; i++) {
			uidBuffer[i]=allBytes[i+16];
		}
		World world=Bukkit.getWorld(UUIDHelper.getUUIDFromBytes(uidBuffer));
		
		BetterSign sign=new BetterSign(signUid);
		
		int facing=allBytes[32];
		BlockFace dir=BlockFace.values()[facing];
		
		int frameAmount=
				(0xff & allBytes[33]) << 24  |
				(0xff & allBytes[34]) << 16  |
				(0xff & allBytes[35]) << 8   |
				(0xff & allBytes[36]) << 0;
		
		int width=
				(0xff & allBytes[37]) << 24  |
				(0xff & allBytes[38]) << 16  |
				(0xff & allBytes[39]) << 8   |
				(0xff & allBytes[40]) << 0;
		int height=
				(0xff & allBytes[41]) << 24  |
				(0xff & allBytes[42]) << 16  |
				(0xff & allBytes[43]) << 8   |
				(0xff & allBytes[44]) << 0;
		sign.setSize(width, height);
		
		int fileIndex=FILE_HEADER_LENGTH;
		for(int frameIndex=0; frameIndex<frameAmount; frameIndex++) {
			int x=
					(0xff & allBytes[fileIndex  ]) << 24  |
					(0xff & allBytes[fileIndex+1]) << 16  |
					(0xff & allBytes[fileIndex+2]) << 8   |
					(0xff & allBytes[fileIndex+3]) << 0;
			int y=
					(0xff & allBytes[fileIndex+4]) << 24  |
					(0xff & allBytes[fileIndex+5]) << 16  |
					(0xff & allBytes[fileIndex+6]) << 8   |
					(0xff & allBytes[fileIndex+7]) << 0;
			int z=
					(0xff & allBytes[fileIndex+8]) << 24  |
					(0xff & allBytes[fileIndex+9]) << 16  |
					(0xff & allBytes[fileIndex+10]) << 8   |
					(0xff & allBytes[fileIndex+11]) << 0;
			int imageAmount=
					(0xff & allBytes[fileIndex+12]) << 24  |
					(0xff & allBytes[fileIndex+13]) << 16  |
					(0xff & allBytes[fileIndex+14]) << 8   |
					(0xff & allBytes[fileIndex+15]) << 0;
			
			byte[][] images=new byte[imageAmount][];
			int[] delays=new int[imageAmount];
			for(int i=0; i<imageAmount; i++) {
				int index=fileIndex+16+i*l;
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
			
			if(itemFrame==null) {
				sign.addFrame(new BetterFrame(sign, loc, dir, images, currentTime, delays));
			} else {
				sign.addFrame(new BetterFrame(sign, itemFrame, images, currentTime, delays));
			}
			
			fileIndex += 16 + imageAmount*l;
		}
		
		return sign;
	}
	public void save() throws IOException {
		//uid
		//world uid
		//facing
		//frame amount
		//width
		//height
		//  frame 0
		//  x,y,z
		//  image amount
		//    image 0
		//    delay
		//    data
		
		int l=128*128+4;
		int totalLength=FILE_HEADER_LENGTH;
		for(BetterFrame frame:frames) {
			totalLength += 16 + frame.getImages().length*l;
		}
		byte[] totalBytes=new byte[totalLength];
		
		byte[] signUidBytes=UUIDHelper.getBytesFromUUID(uid);
		for(int i=0; i<signUidBytes.length; i++) {
			totalBytes[i]=signUidBytes[i];
		}
		byte[] worldUidBytes=UUIDHelper.getBytesFromUUID(frames.get(0).getLocation().getWorld().getUID());
		for(int i=0; i<worldUidBytes.length; i++) {
			totalBytes[i+16]=worldUidBytes[i];
		}
		int facing=0;
		BlockFace face=frames.get(0).getFacing();
		for(BlockFace check:BlockFace.values()) {
			if(face.equals(check)) break;
			facing++;
		}
		totalBytes[32]=(byte)facing;
		int frameAmount=frames.size();
		totalBytes[33]=((byte)((frameAmount >> 24) & 0xff));
		totalBytes[34]=((byte)((frameAmount >> 16) & 0xff));
		totalBytes[35]=((byte)((frameAmount >> 8) & 0xff));
		totalBytes[36]=((byte)((frameAmount >> 0) & 0xff));
		
		totalBytes[37]=((byte)((width >> 24) & 0xff));
		totalBytes[38]=((byte)((width >> 16) & 0xff));
		totalBytes[39]=((byte)((width >> 8) & 0xff));
		totalBytes[40]=((byte)((width >> 0) & 0xff));
		
		totalBytes[41]=((byte)((height >> 24) & 0xff));
		totalBytes[42]=((byte)((height >> 16) & 0xff));
		totalBytes[43]=((byte)((height >> 8) & 0xff));
		totalBytes[44]=((byte)((height >> 0) & 0xff));
		
		for(int frameIndex=0; frameIndex<frameAmount; frameIndex++) {
			BetterFrame frame=frames.get(frameIndex);
			int fileIndex = FILE_HEADER_LENGTH + frameIndex * (16 + frame.getImages().length*l);
			
		    byte[][] images=frame.getImages();
			Location loc=frame.getLocation();
			
			int x=loc.getBlockX();
			int y=loc.getBlockY();
			int z=loc.getBlockZ();
			int imageAmount=images.length;
			totalBytes[fileIndex]=((byte)((x >> 24) & 0xff));
			totalBytes[fileIndex+1]=((byte)((x >> 16) & 0xff));
			totalBytes[fileIndex+2]=((byte)((x >> 8) & 0xff));
			totalBytes[fileIndex+3]=((byte)((x >> 0) & 0xff));
			
			totalBytes[fileIndex+4]=((byte)((y >> 24) & 0xff));
			totalBytes[fileIndex+5]=((byte)((y >> 16) & 0xff));
			totalBytes[fileIndex+6]=((byte)((y >> 8) & 0xff));
			totalBytes[fileIndex+7]=((byte)((y >> 0) & 0xff));
			
			totalBytes[fileIndex+8]=((byte)((z >> 24) & 0xff));
			totalBytes[fileIndex+9]=((byte)((z >> 16) & 0xff));
			totalBytes[fileIndex+10]=((byte)((z >> 8) & 0xff));
			totalBytes[fileIndex+11]=((byte)((z >> 0) & 0xff));
			
			totalBytes[fileIndex+12]=((byte)((imageAmount >> 24) & 0xff));
			totalBytes[fileIndex+13]=((byte)((imageAmount >> 16) & 0xff));
			totalBytes[fileIndex+14]=((byte)((imageAmount >> 8) & 0xff));
			totalBytes[fileIndex+15]=((byte)((imageAmount >> 0) & 0xff));
			
	    	for(int i=0; i<images.length; i++) {
	    		int index=fileIndex+16+i*l;
	    		
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
		while(new File(WorldUtil.getSignFolder().getPath()+"/"+number+".gsign").exists()) {
			number++;
		}
		FileOutputStream fos = new FileOutputStream(WorldUtil.getSignFolder().getPath()+"/"+number+".gsign");
		
		// Compress the data
		byte[] buf = new byte[totalBytes.length];
		while (!compressor.finished()) {
		      int count = compressor.deflate(buf);
		      fos.write(buf, 0, count);
		}
    	fos.close();
	}
	
}
