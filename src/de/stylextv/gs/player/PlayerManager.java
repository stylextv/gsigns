package de.stylextv.gs.player;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import de.stylextv.gs.decode.BetterGifDecoder.GifImage;
import de.stylextv.gs.gui.GuiManager;
import de.stylextv.gs.image.ImageGenerator;
import de.stylextv.gs.lang.LanguageManager;
import de.stylextv.gs.main.Main;
import de.stylextv.gs.main.Variables;
import de.stylextv.gs.permission.PermissionUtil;
import de.stylextv.gs.world.BetterFrame;
import de.stylextv.gs.world.BetterSign;
import de.stylextv.gs.world.Direction;
import de.stylextv.gs.world.WorldUtil;

public class PlayerManager {
	
	private static final Direction[] DIRECTIONS = new Direction[]{
			new Direction(1,  0,  0, BlockFace.WEST),
			new Direction(0,  0,  1, BlockFace.NORTH),
			new Direction(-1, 0,  0, BlockFace.EAST),
			new Direction(0,  0, -1, BlockFace.SOUTH),
			new Direction(0, -1,  0, BlockFace.UP),
			new Direction(0,  1,  0, BlockFace.DOWN)
	};
	
	private static ConcurrentHashMap<Player, Order> playerTasks=new ConcurrentHashMap<Player, Order>();
	private static ArrayList<Player> playersInRemove=new ArrayList<Player>();
	
	public static void startPlacingPhase(Player p, Order order) {
		playerTasks.put(p, order);
		p.sendMessage(Variables.PREFIX+LanguageManager.parseMsg("trans.create.firstcorner"));
	}
	public static void cancelPlacingPhase(Player p) {
		Order o=playerTasks.remove(p);
		if(o!=null) p.sendMessage(Variables.PREFIX+LanguageManager.parseMsg("trans.create.cancel"));
		else p.sendMessage(Variables.PREFIX+LanguageManager.parseMsg("trans.create.error.noprocess"));
	}
	public static void toggleRemovingPhase(Player p) {
		if(playersInRemove.remove(p)) {
			p.sendMessage(Variables.PREFIX+LanguageManager.parseMsg("trans.remove.cancel"));
		} else {
			playersInRemove.add(p);
			p.sendMessage(Variables.PREFIX+LanguageManager.parseMsg("trans.remove.punch"));
		}
	}
	
	public static void onPlayerQuit(PlayerQuitEvent e) {
		Player p=e.getPlayer();
		GuiManager.removePlayer(p);
		WorldUtil.removeAllDrewEntries(p);
		ConnectionManager.removePlayer(p);
		
		playerTasks.remove(p);
		playersInRemove.remove(p);
	}
	
	public static void onPlayerInteract(PlayerInteractEvent e) {
		if(e.getAction() == Action.LEFT_CLICK_BLOCK) {
			Block b=e.getClickedBlock();
			BlockFace face=e.getBlockFace();
			BetterFrame frame=WorldUtil.getFrame(b.getRelative(face).getLocation(), face);
			if(frame != null) {
				e.setCancelled(true);
				onFrameBreak(e.getPlayer(), frame);
				return;
			}
		}
		
		if(e.getAction()==Action.LEFT_CLICK_BLOCK || e.getAction()==Action.RIGHT_CLICK_BLOCK) {
			Block b=e.getClickedBlock();
			Player p=e.getPlayer();
			Order order=playerTasks.get(p);
			if(order!=null) {
				
				if(order.getLastSelect()!=0) {
					long now=System.currentTimeMillis();
					if(now-order.getLastSelect()>500) {
						order.setLastSelect(now);
					} else {
						e.setCancelled(true);
						return;
					}
				} else {
					order.setLastSelect(System.currentTimeMillis());
				}
				
				if(order.getFirstCorner()!=null) {
					Location first=order.getFirstCorner();
					Location second=b.getRelative(e.getBlockFace()).getLocation();
					placeSign(order, first, second, p, e, false);
				} else {
					order.setFirstCorner(b.getRelative(e.getBlockFace()).getLocation());
					p.sendMessage(Variables.PREFIX+LanguageManager.parseMsg("trans.create.secondcorner"));
					e.setCancelled(true);
				}
				
			}
			
		}
	}
	public static UUID placeSign(Order order, Location first, Location second, Player p, PlayerInteractEvent e, boolean isApi) {
		UUID uid = null;
		
		if(!isApi) e.setCancelled(true);
		
		if(first.getWorld().equals(second.getWorld())) {
			Location top=new Location(first.getWorld(), Math.max(first.getBlockX(), second.getBlockX()), Math.max(first.getBlockY(), second.getBlockY()), Math.min(first.getBlockZ(), second.getBlockZ()));
			Location bottom=new Location(first.getWorld(), Math.min(first.getBlockX(), second.getBlockX()), Math.min(first.getBlockY(), second.getBlockY()), Math.max(first.getBlockZ(), second.getBlockZ()));
			
			boolean placed=false;
			boolean validFound=false;
			boolean itemFramesDetected=false;
			for(Direction dir : DIRECTIONS) {
				boolean valid=true;
				if(dir.getX()!=0) {
					valid=top.getBlockX()==bottom.getBlockX();
				} else if(dir.getZ()!=0) {
					valid=top.getBlockZ()==bottom.getBlockZ();
				} else valid=top.getBlockY()==bottom.getBlockY();
				
				if(valid) {
					validFound=true;
					boolean save=true;
					for(int x=bottom.getBlockX(); x<=top.getBlockX(); x++) {
						for(int y=bottom.getBlockY(); y<=top.getBlockY(); y++) {
							for(int z=top.getBlockZ(); z<=bottom.getBlockZ(); z++) {
								Block block=top.getWorld().getBlockAt(x+dir.getX(), y+dir.getY(), z+dir.getZ());
								if(!block.getType().isSolid()) {
									save=false;
									break;
								} else {
									Collection<Entity> list=top.getWorld().getNearbyEntities(new Location(top.getWorld(), x+dir.getX()*0.5+0.5, y+dir.getY()*0.5+0.5, z+dir.getZ()*0.5+0.5), 0.1, 0.1, 0.1);
									for(Entity entity:list) {
										if(entity instanceof ItemFrame) {
											save=false;
											itemFramesDetected=true;
											break;
										}
									}
									if(!save) break;
								}
							}
							if(!save) break;
						}
					}
					if(save) {
						BetterSign sign=WorldUtil.createSign();
						if(dir.getX()!=0) {
							int minZ;
							int maxZ;
							BlockFace face = dir.getFace();
							minZ=Math.min(top.getBlockZ(),bottom.getBlockZ());
							maxZ=Math.max(top.getBlockZ(),bottom.getBlockZ());
							int imgHeight=top.getBlockY()-bottom.getBlockY()+1;
							int imgWidth=maxZ-minZ+1;
							sign.setSize(imgWidth, imgHeight);
							
							if(order.getBackgroundGif()!=null) {
								new BukkitRunnable() {
									@Override
									public void run() {
										GifImage gif=order.getBackgroundGif();
										int amount=gif.getFrameCount();
										byte[][] frames=new byte[amount][];
										for(int i=0; i<amount; i++) {
											frames[i]=ImageGenerator.generate(order,imgWidth,imgHeight,true,i);
										}
										
										long startTime=System.currentTimeMillis();
										int[] delays=gif.getDelays();
										for(int z=minZ; z<=maxZ; z++) {
											for(int y=top.getBlockY(); y>=bottom.getBlockY(); y--) {
												Location loc=new Location(top.getWorld(), top.getBlockX(), y, z);
												int imgY=top.getBlockY()-y;
												int imgX;
												if(dir.getX()==-1) imgX=maxZ-z;
												else imgX=z-minZ;
												byte[][] individualFrames=new byte[amount][];
												for(int i=0; i<amount; i++) {
													individualFrames[i]=ImageGenerator.getSubimage(frames[i],imgWidth, imgX*128, imgY*128, 128, 128);
												}
												WorldUtil.spawnItemFrame(sign, loc, individualFrames,delays,startTime, face);
											}
										}
										WorldUtil.registerSign(sign);
										System.gc();
										if(!isApi) p.sendMessage(Variables.PREFIX+LanguageManager.parseMsg("trans.create.success"));
									}
								}.runTaskAsynchronously(Main.getPlugin());
							} else {
								byte[] image=ImageGenerator.generate(order,imgWidth,imgHeight,false,0);
								
								for(int z=minZ; z<=maxZ; z++) {
									for(int y=top.getBlockY(); y>=bottom.getBlockY(); y--) {
										Location loc=new Location(top.getWorld(), top.getBlockX(), y, z);
										loc.getBlock().setType(Material.AIR);
										int imgY=top.getBlockY()-y;
										int imgX;
										if(dir.getX()==-1) imgX=maxZ-z;
										else imgX=z-minZ;
										WorldUtil.spawnItemFrame(sign, loc, ImageGenerator.getSubimage(image,imgWidth, imgX*128, imgY*128, 128, 128), face);
									}
								}
								WorldUtil.registerSign(sign);
							}
						} else if(dir.getZ()!=0) {
							int minX;
							int maxX;
							BlockFace face = dir.getFace();
							maxX=Math.max(top.getBlockX(),bottom.getBlockX());
							minX=Math.min(top.getBlockX(),bottom.getBlockX());
							int imgHeight=top.getBlockY()-bottom.getBlockY()+1;
							int imgWidth=maxX-minX+1;
							sign.setSize(imgWidth, imgHeight);
							
							if(order.getBackgroundGif()!=null) {
								new BukkitRunnable() {
									@Override
									public void run() {
										GifImage gif=order.getBackgroundGif();
										int amount=gif.getFrameCount();
										byte[][] frames=new byte[amount][];
										for(int i=0; i<amount; i++) {
											frames[i]=ImageGenerator.generate(order,imgWidth,imgHeight,true,i);
										}
										
										long startTime=System.currentTimeMillis();
										int[] delays=gif.getDelays();
										for(int x=minX; x<=maxX; x++) {
											for(int y=top.getBlockY(); y>=bottom.getBlockY(); y--) {
												Location loc=new Location(top.getWorld(), x, y, top.getBlockZ());
												int imgY=top.getBlockY()-y;
												int imgX;
												if(dir.getZ()==-1) imgX=x-minX;
												else imgX=maxX-x;
												byte[][] individualFrames=new byte[amount][];
												for(int i=0; i<amount; i++) {
													individualFrames[i]=ImageGenerator.getSubimage(frames[i],imgWidth, imgX*128, imgY*128, 128, 128);
												}
												WorldUtil.spawnItemFrame(sign, loc, individualFrames,delays,startTime, face);
											}
										}
										WorldUtil.registerSign(sign);
										System.gc();
										if(!isApi) p.sendMessage(Variables.PREFIX+LanguageManager.parseMsg("trans.create.success"));
									}
								}.runTaskAsynchronously(Main.getPlugin());
							} else {
								byte[] image=ImageGenerator.generate(order,imgWidth,imgHeight,false,0);
								
								for(int x=minX; x<=maxX; x++) {
									for(int y=top.getBlockY(); y>=bottom.getBlockY(); y--) {
										Location loc=new Location(top.getWorld(), x, y, top.getBlockZ());
										loc.getBlock().setType(Material.AIR);
										int imgY=top.getBlockY()-y;
										int imgX;
										if(dir.getZ()==-1) imgX=x-minX;
										else imgX=maxX-x;
										WorldUtil.spawnItemFrame(sign, loc, ImageGenerator.getSubimage(image,imgWidth, imgX*128, imgY*128, 128, 128), face);
									}
								}
								WorldUtil.registerSign(sign);
							}
						} else {
							if(WorldUtil.getMcVersion()<WorldUtil.MCVERSION_1_13) {
								if(isApi) throw new InvalidParameterException("Item frames can only be placed on the floor/ceiling when using minecraft version 1.13 or higher.");
								placed=true;
								p.sendMessage(Variables.PREFIX+LanguageManager.parseMsg("trans.create.error.wrongversion"));
								playerTasks.remove(p);
								break;
							}
							
							int minX;
							int maxX;
							int minZ;
							int maxZ;
							BlockFace face = dir.getFace();
							maxX=Math.max(top.getBlockX(),bottom.getBlockX());
							minX=Math.min(top.getBlockX(),bottom.getBlockX());
							maxZ=Math.max(top.getBlockZ(),bottom.getBlockZ());
							minZ=Math.min(top.getBlockZ(),bottom.getBlockZ());
							int h=maxZ-minZ+1;
							int w=maxX-minX+1;
							float playerYaw=p.getLocation().getYaw()+45;
							while(playerYaw<-180) playerYaw+=360;
							while(playerYaw>180) playerYaw-=360;
							playerYaw+=180;
							int rot=(int)playerYaw/90;
							if(face==BlockFace.DOWN&&rot%2==1) {
								rot+=2;
							}
							if(rot%2==1) {
								int temp=h;
								h=w;
								w=temp;
							}
							final int imgWidth=w;
							final int imgHeight=h;
							sign.setSize(imgWidth, imgHeight);
							final int imgRotation=rot%4;
							
							if(order.getBackgroundGif()!=null) {
								new BukkitRunnable() {
									@Override
									public void run() {
										GifImage gif=order.getBackgroundGif();
										int amount=gif.getFrameCount();
										byte[][] frames=new byte[amount][];
										for(int i=0; i<amount; i++) {
											frames[i]=ImageGenerator.generate(order,imgWidth,imgHeight,true,i);
										}
										
										long startTime=System.currentTimeMillis();
										int[] delays=gif.getDelays();
										for(int x=minX; x<=maxX; x++) {
											for(int z=minZ; z<=maxZ; z++) {
												Location loc=new Location(top.getWorld(), x, top.getBlockY(), z);
												int imgY;
												if(dir.getY()==-1) imgY=z-minZ;
												else imgY=maxZ-z;
												int imgX=x-minX;
												if(imgRotation%2==1) {
													int temp=imgY;
													imgY=imgX;
													imgX=temp;
												}
												if(imgRotation==2) {
													imgX=(imgWidth-1)-imgX;
													imgY=(imgHeight-1)-imgY;
												} else if(imgRotation==1) {
													imgY=(imgHeight-1)-imgY;
												} else if(imgRotation==3) {
													imgX=(imgWidth-1)-imgX;
												}
												byte[][] individualFrames=new byte[amount][];
												for(int i=0; i<amount; i++) {
													individualFrames[i]=ImageGenerator.rotateImage(ImageGenerator.getSubimage(frames[i],imgWidth, imgX*128, imgY*128, 128, 128), 128,128, imgRotation);
												}
												WorldUtil.spawnItemFrame(sign, loc, individualFrames,delays,startTime, face);
											}
										}
										WorldUtil.registerSign(sign);
										System.gc();
										if(!isApi) p.sendMessage(Variables.PREFIX+LanguageManager.parseMsg("trans.create.success"));
									}
								}.runTaskAsynchronously(Main.getPlugin());
							} else {
								byte[] image=ImageGenerator.generate(order,imgWidth,imgHeight,false,0);
								
								for(int x=minX; x<=maxX; x++) {
									for(int z=minZ; z<=maxZ; z++) {
										Location loc=new Location(top.getWorld(), x, top.getBlockY(), z);
										loc.getBlock().setType(Material.AIR);
										int imgY;
										if(dir.getY()==-1) imgY=z-minZ;
										else imgY=maxZ-z;
										int imgX=x-minX;
										if(imgRotation%2==1) {
											int temp=imgY;
											imgY=imgX;
											imgX=temp;
										}
										if(imgRotation==2) {
											imgX=(imgWidth-1)-imgX;
											imgY=(imgHeight-1)-imgY;
										} else if(imgRotation==1) {
											imgY=(imgHeight-1)-imgY;
										} else if(imgRotation==3) {
											imgX=(imgWidth-1)-imgX;
										}
										WorldUtil.spawnItemFrame(sign, loc, ImageGenerator.rotateImage(ImageGenerator.getSubimage(image,imgWidth, imgX*128, imgY*128, 128, 128), 128,128, imgRotation), face);
									}
								}
								WorldUtil.registerSign(sign);
							}
						}
						
						placed=true;
						if(!isApi) {
							if(order.getBackgroundGif()!=null) p.sendMessage(Variables.PREFIX+LanguageManager.parseMsg("trans.create.wait"));
							else p.sendMessage(Variables.PREFIX+LanguageManager.parseMsg("trans.create.success"));
							playerTasks.remove(p);
						}
						uid = sign.getUid();
						break;
					}
				}
			}
			if(!placed) {
				if(isApi) throw new InvalidParameterException("No valid location found.");
				
				if(!validFound) p.sendMessage(Variables.PREFIX+LanguageManager.parseMsg("trans.create.error.novalidpos"));
				else if(itemFramesDetected) {
					p.sendMessage(Variables.PREFIX+LanguageManager.parseMsg("trans.create.error.itemframes"));
				} else p.sendMessage(Variables.PREFIX+LanguageManager.parseMsg("trans.create.error.nosolid"));
			}
		} else {
			if(isApi) throw new InvalidParameterException("The two corners have to be in the same world.");
			
			p.sendMessage(Variables.PREFIX+LanguageManager.parseMsg("trans.create.error.wrongworld"));
		}
		return uid;
	}
	public static UUID placeSign(Order order, Location first, Location second) {
		return placeSign(order, first, second, null, null, true);
	}
	
	public static void onFrameBreak(Player p, BetterFrame frame) {
		if(playersInRemove.remove(p)) {
			
			if(PermissionUtil.hasRemovePermission(p)) {
				WorldUtil.removeSign(frame.getSign());
				p.sendMessage(Variables.PREFIX+LanguageManager.parseMsg("trans.remove.success"));
			} else {
				p.sendMessage(Variables.PREFIX+LanguageManager.parseMsg("trans.remove.error.noperm"));
			}
			
		} else {
			
			if(PermissionUtil.hasRemovePermission(p)) {
				p.sendMessage(Variables.PREFIX+LanguageManager.parseMsg("trans.remove.use"));
				if(WorldUtil.getMcVersion()<=WorldUtil.MCVERSION_1_8) p.playSound(p.getLocation(), "gui.button.press", 0.5f,0.75f);
				else if(WorldUtil.getMcVersion()<=WorldUtil.MCVERSION_1_10) p.playSound(p.getLocation(), "minecraft:block.wood_button.click_off", 1,1);
				else if(WorldUtil.getMcVersion()<=WorldUtil.MCVERSION_1_12) p.playSound(p.getLocation(), "minecraft:block.wood_button.click_off", SoundCategory.BLOCKS, 1,1);
				else p.playSound(p.getLocation(), Sound.BLOCK_WOODEN_BUTTON_CLICK_OFF, SoundCategory.BLOCKS, 1,1);
			}
			
		}
	}
	
}
