package de.stylextv.gs.world;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import de.stylextv.gs.main.Main;
import de.stylextv.gs.packet.Reflections;
import de.stylextv.gs.packet.Reflections.ConstructorInvoker;
import de.stylextv.gs.packet.Reflections.FieldAccessor;
import de.stylextv.gs.packet.TinyProtocol;
import de.stylextv.gs.player.ConnectionManager;

public class BetterFrame {
	
	private static final int VIEW_DISTANCE_SQ=30*30;
	
    private static final Class<Object> packetClass = Reflections.getUntypedClass("{nms}.Packet");
    private static final Reflections.MethodInvoker sendPacket = Reflections.getMethod("{nms}.PlayerConnection", "sendPacket", packetClass);
    private static final Class<Object> packetPlayOutEntityMetadataClass = Reflections.getUntypedClass("{nms}.PacketPlayOutEntityMetadata");
    private static final ConstructorInvoker packetPlayOutEntityMetadataConstructor = Reflections.getConstructor(packetPlayOutEntityMetadataClass);
    private static final FieldAccessor<Integer> packetPlayOutEntityMetadataFieldA = Reflections.getField(packetPlayOutEntityMetadataClass, "a", int.class);
	@SuppressWarnings("rawtypes")
	private static final FieldAccessor<List> packetPlayOutEntityMetadataFieldB = Reflections.getField(packetPlayOutEntityMetadataClass, "b", List.class);
	
    private static final Class<Object> packetPlayOutMapClass = Reflections.getUntypedClass("{nms}.PacketPlayOutMap");
	private static ConstructorInvoker packetPlayOutMapConstructor;
	
	private static ConstructorInvoker dataWatcherItemConstructor;
    private static Class<Object> entityItemFrameClass;
    private static Class<Object> dataWatcherObjectClass;
	private static FieldAccessor<?> entityItemFrameItemField;
	
	private static ConstructorInvoker watchableObjectConstructor;
    
    private static final Class<Object> craftItemStackClass = Reflections.getUntypedClass("{obc}.inventory.CraftItemStack");
    private static final Class<Object> nmsItemStackClass = Reflections.getUntypedClass("{nms}.ItemStack");
    private static final Reflections.MethodInvoker asNMSCopy = Reflections.getMethod(craftItemStackClass, "asNMSCopy", ItemStack.class);
    private static Reflections.MethodInvoker getOrCreateTag;
    private static Reflections.MethodInvoker setShort;
    
	private ItemFrame itemFrame;
	private int entityId;
	private byte[][] images;
	
	private final ConcurrentHashMap<UUID, CopyOnWriteArrayList<Short>> playerMapIds = new ConcurrentHashMap<>();
	private HashMap<Player, Integer> playersSentProgress=new HashMap<Player, Integer>();
	private ArrayList<Player> playersInRadius=new ArrayList<Player>();
	private int stillRefreshTimer;
	
	private UUID signUid;
	private long startTime;
	private int currentItemIndex=-1;
	private int[] delays;
	private int totalTime=0;
	
	public BetterFrame(UUID signUid, Location loc, BlockFace dir, byte[][] images, long startTime, int[] delays) {
		this.images=images;
		this.signUid=signUid;
		this.startTime=startTime;
		this.delays=delays;
		if(delays!=null) for(int i:delays) totalTime+=i;
		
		World w=loc.getWorld();
		itemFrame=(ItemFrame) w.spawnEntity(loc, EntityType.ITEM_FRAME);
		itemFrame.setFacingDirection(dir);
	}
	public BetterFrame(UUID signUid, ItemFrame frame, byte[][] images, long startTime, int[] delays) {
		this.images=images;
		this.signUid=signUid;
		this.startTime=startTime;
		this.delays=delays;
		if(delays!=null) for(int i:delays) totalTime+=i;
		
		this.itemFrame=frame;
	}
	
	public boolean update(long currentTime) {
		if(itemFrame.isDead()) return true;
		
		refreshEntityId();
		
		if(images!=null) {
			int prevFrame=currentItemIndex;
			
			int a=images.length;
			if(a>1) {
				int msIntoGif=(int) ((currentTime-startTime)%totalTime);
				int j=0;
				for(int i=0; i<delays.length; i++) {
					int delay=delays[i];
					if(msIntoGif<j+delay) {
						currentItemIndex=i;
						break;
					}
					j+=delay;
				}
			} else currentItemIndex=0;
			
			if(currentItemIndex!=prevFrame) {
				
				for(Player all:Bukkit.getOnlinePlayers()) {
					if(all.getWorld()==itemFrame.getWorld()) {
						Location loc1=all.getLocation();
						Location loc2=itemFrame.getLocation();
						double dis=(loc1.getX()-loc2.getX())*(loc1.getX()-loc2.getX()) + (loc1.getZ()-loc2.getZ())*(loc1.getZ()-loc2.getZ());
						if(dis<BetterFrame.VIEW_DISTANCE_SQ) {
							sendContent(all);
							showInFrame(all, currentItemIndex);
						}
					} else removePlayer(all);
				}
				
			} else if(a==1) {
				
				stillRefreshTimer--;
				if(stillRefreshTimer<=0) stillRefreshTimer=60;
				
				for(Player all:Bukkit.getOnlinePlayers()) {
					if(all.getWorld()==itemFrame.getWorld()) {
						Location loc1=all.getLocation();
						Location loc2=itemFrame.getLocation();
						double dis=(loc1.getX()-loc2.getX())*(loc1.getX()-loc2.getX()) + (loc1.getZ()-loc2.getZ())*(loc1.getZ()-loc2.getZ());
						if(dis<BetterFrame.VIEW_DISTANCE_SQ) {
							sendContent(all);
							if(!playersInRadius.contains(all)) {
								playersInRadius.add(all);
								
								showInFrame(all, 0);
							} else if(stillRefreshTimer == 1) {
								showInFrame(all, 0);
							}
						} else {
							playersInRadius.remove(all);
						}
					} else removePlayer(all);
				}
				
			}
		}
		return false;
	}
	private void refreshEntityId() {
		for(Entity e : itemFrame.getWorld().getChunkAt(itemFrame.getLocation()).getEntities()) {
			if(e.getUniqueId().equals(itemFrame.getUniqueId())) {
				entityId = e.getEntityId();
				break;
			}
		}
	}
	
	public void removePlayer(Player p) {
		playersSentProgress.remove(p);
		playersInRadius.remove(p);
		playerMapIds.remove(p.getUniqueId());
	}
	private void sendContent(Player p) {
		UUID uid=p.getUniqueId();
		if(!playerMapIds.containsKey(uid)) {
			CopyOnWriteArrayList<Short> ids = new CopyOnWriteArrayList<Short>();
			playerMapIds.put(uid, ids);
			for(int i=0; i<images.length; i++) {
				ids.add(WorldUtil.getMapManager().getNextFreeIdFor(p));
			}
		}
		
		Integer got=playersSentProgress.get(p);
		if(got==null) got=0;
		
		int allowedToSend;
		if(got!=images.length&&(allowedToSend=ConnectionManager.canSend(p,images.length-got))!=0) {
			playersSentProgress.put(p, got+allowedToSend);
			int gotF=got;
			int allowedToSendF=allowedToSend;
			new BukkitRunnable() {
				@Override
				public void run() {
					Object connection = TinyProtocol.getConnection.get(TinyProtocol.getPlayerHandle.invoke(p));
					for(int i=0; i<allowedToSendF; i++) {
						int mapIndex=gotF+i;
						CopyOnWriteArrayList<Short> playerIdList = playerMapIds.get(uid);
						if(playerIdList != null && playerIdList.size() > mapIndex) {
							int mapId = playerIdList.get(mapIndex);
							mapId = -mapId;
							
							sendPacket.invoke(connection, constructMapDataPacket(mapId, images[mapIndex]));
						}
					}
				}
			}.runTaskAsynchronously(Main.getPlugin());
		}
	}
	private Object constructMapDataPacket(int mapId, byte[] data) {
		if(WorldUtil.getMcVersion() >= WorldUtil.MCVERSION_1_14) {
			if(packetPlayOutMapConstructor == null) packetPlayOutMapConstructor = Reflections.getConstructor(packetPlayOutMapClass, int.class, byte.class, boolean.class, boolean.class, Collection.class, byte[].class, int.class, int.class, int.class, int.class);
			return packetPlayOutMapConstructor.invoke(mapId, (byte)0, false, false, new ArrayList<>(), data, 0, 0, 128, 128);
		} else if(WorldUtil.getMcVersion() >= WorldUtil.MCVERSION_1_9) {
			if(packetPlayOutMapConstructor == null) packetPlayOutMapConstructor = Reflections.getConstructor(packetPlayOutMapClass, int.class, byte.class, boolean.class, Collection.class, byte[].class, int.class, int.class, int.class, int.class);
			return packetPlayOutMapConstructor.invoke(mapId, (byte)0, false, new ArrayList<>(), data, 0, 0, 128, 128);
		} else if(WorldUtil.getMcVersion() >= WorldUtil.MCVERSION_1_8) {
			if(packetPlayOutMapConstructor == null) packetPlayOutMapConstructor = Reflections.getConstructor(packetPlayOutMapClass, int.class, byte.class, Collection.class, byte[].class, int.class, int.class, int.class, int.class);
			return packetPlayOutMapConstructor.invoke(mapId, (byte)0, new ArrayList<>(), data, 0, 0, 128, 128);
		}
		return null;
	}
	@SuppressWarnings("deprecation")
	private void showInFrame(Player p, int imageIndex) {
		Integer got=playersSentProgress.get(p);
		if(got==null) got=0;
		CopyOnWriteArrayList<Short> playerIdList = playerMapIds.get(p.getUniqueId());
		
		Object craftItemStack;
		if(playerIdList != null && playerIdList.size() > imageIndex && got > imageIndex) {
			short mapId = playerIdList.get(imageIndex);
			if(WorldUtil.getMcVersion() >= WorldUtil.MCVERSION_1_13) {
				ItemStack itemStack = new ItemStack(Material.FILLED_MAP);
				craftItemStack = asNMSCopy.invoke(craftItemStackClass, itemStack);
				if(setShort == null) setShort = Reflections.getMethod(Reflections.getUntypedClass("{nms}.NBTTagCompound"), "setShort", String.class, short.class);
				if(getOrCreateTag == null) getOrCreateTag = Reflections.getMethod(nmsItemStackClass, "getOrCreateTag");
				setShort.invoke(getOrCreateTag.invoke(craftItemStack), "map", mapId);
			} else {
				ItemStack itemStack = new ItemStack(Material.MAP, 1, mapId);
				craftItemStack = asNMSCopy.invoke(craftItemStackClass, itemStack);
			}
		} else {
			ItemStack itemStack = new ItemStack(Material.BARRIER);
			craftItemStack = asNMSCopy.invoke(craftItemStackClass, itemStack);
		}
		
		Object connection = TinyProtocol.getConnection.get(TinyProtocol.getPlayerHandle.invoke(p));
		Object metaDataPacket = packetPlayOutEntityMetadataConstructor.invoke();
		
		packetPlayOutEntityMetadataFieldA.set(metaDataPacket, entityId);
		
		ArrayList<Object> list = new ArrayList<>();
		
		
		if(WorldUtil.getMcVersion() <= WorldUtil.MCVERSION_1_8) {
			if(watchableObjectConstructor == null) {
				try {
					watchableObjectConstructor = Reflections.getConstructor("{nms}.DataWatcher$WatchableObject", int.class, int.class, Object.class);
				} catch(Exception ex) {
					try {
						watchableObjectConstructor = Reflections.getConstructor("{nms}.WatchableObject", int.class, int.class, Object.class);
					} catch(Exception ex2) {
						ex2.printStackTrace();
					}
				}
			}
			list.add(watchableObjectConstructor.invoke(5, 8, craftItemStack));
		} else {
			Object dataWatcherObject;
			if(dataWatcherObjectClass == null) dataWatcherObjectClass = Reflections.getUntypedClass("{nms}.DataWatcherObject");
			if(entityItemFrameClass == null) entityItemFrameClass = Reflections.getUntypedClass("{nms}.EntityItemFrame");
			if(WorldUtil.getMcVersion() >= WorldUtil.MCVERSION_1_13) {
				if(entityItemFrameItemField == null) {
					try {
						entityItemFrameItemField = Reflections.getField(entityItemFrameClass, "ITEM", dataWatcherObjectClass);
					} catch(Exception ex) {
						entityItemFrameItemField = Reflections.getField(entityItemFrameClass, "e", dataWatcherObjectClass);
					}
				}
			} else {
				if(entityItemFrameItemField == null) entityItemFrameItemField = Reflections.getField(entityItemFrameClass, "c", dataWatcherObjectClass);
			}
			dataWatcherObject = entityItemFrameItemField.get(null);
			
			if(dataWatcherItemConstructor == null) dataWatcherItemConstructor = Reflections.getFirstConstructor("{nms}.DataWatcher$Item");
			Object dataWatcherItem;
			if(WorldUtil.getMcVersion() >= WorldUtil.MCVERSION_1_11) {
				dataWatcherItem = dataWatcherItemConstructor.invoke(dataWatcherObject, craftItemStack);
			} else {
				dataWatcherItem = dataWatcherItemConstructor.invoke(dataWatcherObject, com.google.common.base.Optional.fromNullable(craftItemStack));
			}
			
			list.add(dataWatcherItem);
		}
		
		packetPlayOutEntityMetadataFieldB.set(metaDataPacket, list);
		
		
		sendPacket.invoke(connection, metaDataPacket);
	}
	
	public boolean isDead() {
		return itemFrame.isDead();
	}
	public ItemFrame getItemFrame() {
		return itemFrame;
	}
	public int getEntityId() {
		return entityId;
	}
	public BlockFace getFacing() {
		return itemFrame.getFacing();
	}
	public Location getLocation() {
		return itemFrame.getLocation();
	}
	
	public UUID getSignUid() {
		return signUid;
	}
	public int getCurrentItemIndex() {
		return currentItemIndex;
	}
	public void setCurrentItemIndex(int currentItemIndex) {
		this.currentItemIndex = currentItemIndex;
	}
	public int getDelay(int index) {
		if(delays==null) return 0;
		return delays[index];
	}
	
	public void getOccupiedIdsFor(OfflinePlayer p, Set<Short> set) {
		UUID uid=p.getUniqueId();
		if(playerMapIds.containsKey(uid)) {
			CopyOnWriteArrayList<Short> ids = playerMapIds.get(uid);
			if(!ids.isEmpty()) for(short s : ids) {
				set.add(s);
			}
		}
	}
	public boolean isIdUsedBy(OfflinePlayer p, short id) {
		UUID uid=p.getUniqueId();
		if(playerMapIds.containsKey(uid)) {
			CopyOnWriteArrayList<Short> ids = playerMapIds.get(uid);
			if(!ids.isEmpty()) for(short s : ids) {
				if(s == id) return true;
			}
		}
		return false;
	}
	public byte[][] getImages() {
		return images;
	}
	
}