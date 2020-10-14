package de.stylextv.gs.player;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import de.stylextv.gs.decode.BetterGifDecoder.GifImage;
import de.stylextv.gs.image.ImageGenerator;
import de.stylextv.gs.main.Main;
import de.stylextv.gs.main.Variables;
import de.stylextv.gs.permission.PermissionUtil;
import de.stylextv.gs.world.BetterFrame;
import de.stylextv.gs.world.Direction;
import de.stylextv.gs.world.WorldUtil;

public class PlayerManager {
	
	private static Direction[] directions = new Direction[]{
			new Direction(1,  0,  0),
			new Direction(0,  0,  1),
			new Direction(-1, 0,  0),
			new Direction(0,  0, -1),
			new Direction(0, -1,  0),
			new Direction(0,  1,  0)
	};
	
	private static ConcurrentHashMap<Player, Order> playerTasks=new ConcurrentHashMap<Player, Order>();
	private static ArrayList<Player> playersInRemove=new ArrayList<Player>();
	
	public static void startPlacingPhase(Player p, Order order) {
		playerTasks.put(p, order);
		p.sendMessage(Variables.PREFIX+"Your sign was created §asuccessfully§7. Please click one of the §ecorners§7 of the frame.");
	}
	public static void cancelPlacingPhase(Player p) {
		Order o=playerTasks.remove(p);
		if(o!=null) p.sendMessage(Variables.PREFIX+"The placement process has been §ccanceled§7.");
		else p.sendMessage(Variables.PREFIX+"You are currently not in a placement §cprocess§7.");
	}
	public static void toggleRemovingPhase(Player p) {
		if(playersInRemove.remove(p)) {
			p.sendMessage(Variables.PREFIX+"The removal process has been §ccanceled§7.");
		} else {
			playersInRemove.add(p);
			p.sendMessage(Variables.PREFIX+"§aPunch§7 a sign to remove it or use §e/gs remove§7 again to cancel the process.");
		}
	}
	
	public static void onPlayerQuit(PlayerQuitEvent e) {
		Player p=e.getPlayer();
		WorldUtil.removeAllDrewEntries(p);
		ConnectionManager.removePlayer(p);
		
		playerTasks.remove(p);
		playersInRemove.remove(p);
	}
	
	public static void onPlayerInteract(PlayerInteractEvent e) {
		if(e.getAction()==Action.LEFT_CLICK_BLOCK||e.getAction()==Action.RIGHT_CLICK_BLOCK) {
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
					p.sendMessage(Variables.PREFIX+"The first corner has been §aset§7. Now please click on the §eopposite§7 corner.");
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
			for(Direction dir:directions) {
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
								}
							}
							if(!save) break;
						}
					}
					if(save) {
						UUID signUid=WorldUtil.randomSignUid();
						if(dir.getX()!=0) {
							int minZ;
							int maxZ;
							BlockFace face;
							minZ=Math.min(top.getBlockZ(),bottom.getBlockZ());
							maxZ=Math.max(top.getBlockZ(),bottom.getBlockZ());
							if(dir.getX()==-1) {
								face=BlockFace.EAST;
							} else {
								face=BlockFace.WEST;
							}
							int imgHeight=top.getBlockY()-bottom.getBlockY()+1;
							int imgWidth=maxZ-minZ+1;
							
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
												WorldUtil.spawnItemFrame(signUid, loc, individualFrames,delays,startTime, face);
											}
										}
										System.gc();
										if(!isApi) p.sendMessage(Variables.PREFIX+"Your sign has been §aplaced§7 successfully.");
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
										WorldUtil.spawnItemFrame(signUid, loc, ImageGenerator.getSubimage(image,imgWidth, imgX*128, imgY*128, 128, 128), face);
									}
								}
							}
						} else if(dir.getZ()!=0) {
							int minX;
							int maxX;
							BlockFace face;
							maxX=Math.max(top.getBlockX(),bottom.getBlockX());
							minX=Math.min(top.getBlockX(),bottom.getBlockX());
							if(dir.getZ()==-1) {
								face=BlockFace.SOUTH;
							} else {
								face=BlockFace.NORTH;
							}
							int imgHeight=top.getBlockY()-bottom.getBlockY()+1;
							int imgWidth=maxX-minX+1;
							
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
												WorldUtil.spawnItemFrame(signUid, loc, individualFrames,delays,startTime, face);
											}
										}
										System.gc();
										if(!isApi) p.sendMessage(Variables.PREFIX+"Your sign has been §aplaced§7 successfully.");
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
										WorldUtil.spawnItemFrame(signUid, loc, ImageGenerator.getSubimage(image,imgWidth, imgX*128, imgY*128, 128, 128), face);
									}
								}
							}
						} else {
							if(WorldUtil.getMcVersion()<WorldUtil.MCVERSION_1_13) {
								if(isApi) throw new InvalidParameterException("Item frames can only be placed on the §cfloor/ceiling§7 when using minecraft version §c1.13§7 or higher.");
								placed=true;
								p.sendMessage(Variables.PREFIX+"Item frames can only be placed on the §cfloor/ceiling§7 when using minecraft version §c1.13§7 or higher.");
								playerTasks.remove(p);
								break;
							}
							
							int minX;
							int maxX;
							int minZ;
							int maxZ;
							BlockFace face;
							maxX=Math.max(top.getBlockX(),bottom.getBlockX());
							minX=Math.min(top.getBlockX(),bottom.getBlockX());
							maxZ=Math.max(top.getBlockZ(),bottom.getBlockZ());
							minZ=Math.min(top.getBlockZ(),bottom.getBlockZ());
							if(dir.getY()==-1) {
								face=BlockFace.UP;
							} else {
								face=BlockFace.DOWN;
							}
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
													individualFrames[i]=ImageGenerator.rotateImage(ImageGenerator.getSubimage(frames[i],imgWidth, imgX*128, imgY*128, 128, 128), 128,128, imgRotation*90);
												}
												WorldUtil.spawnItemFrame(signUid, loc, individualFrames,delays,startTime, face);
											}
										}
										System.gc();
										if(!isApi) p.sendMessage(Variables.PREFIX+"Your sign has been §aplaced§7 successfully.");
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
										WorldUtil.spawnItemFrame(signUid, loc, ImageGenerator.rotateImage(ImageGenerator.getSubimage(image,imgWidth, imgX*128, imgY*128, 128, 128), 128,128, imgRotation*90), face);
									}
								}
								System.gc();
							}
						}
						
						placed=true;
						if(!isApi) {
							if(order.getBackgroundGif()!=null) p.sendMessage(Variables.PREFIX+"Please §ewait§7 until the sign has been placed. This may take a bit...");
							else p.sendMessage(Variables.PREFIX+"Your sign has been §aplaced§7 successfully.");
							playerTasks.remove(p);
						}
						uid = signUid;
						break;
					}
				}
			}
			if(!placed) {
				if(isApi) throw new InvalidParameterException("No valid location found.");
				
				if(!validFound) p.sendMessage(Variables.PREFIX+"This is not a §cvalid§7 position for a sign.");
				else p.sendMessage(Variables.PREFIX+"There must be §csolid §7blocks to hang a sign.");
			}
		} else {
			if(isApi) throw new InvalidParameterException("The two corners have to be in the same world.");
			
			p.sendMessage(Variables.PREFIX+"The two corners have to be in the same §cworld§7.");
		}
		return uid;
	}
	public static UUID placeSign(Order order, Location first, Location second) {
		return placeSign(order, first, second, null, null, true);
	}
	
	public static void onFrameBreak(Player p, BetterFrame frame) {
		if(playersInRemove.remove(p)) {
			
			if(PermissionUtil.hasRemovePermission(p)) {
				WorldUtil.removeSign(frame.getSignUid());
				p.sendMessage(Variables.PREFIX+"§7The sign has been §aremoved§7.");
			} else {
				p.sendMessage(Variables.PREFIX+"§7You no longer have the §cpermission§7 to remove signs.");
			}
			
		} else {
			
			if(PermissionUtil.hasRemovePermission(p)) {
				p.sendMessage(Variables.PREFIX+"§7Use §e/gs remove§7 to remove a sign.");
				if(WorldUtil.getMcVersion()<=WorldUtil.MCVERSION_1_8) p.playSound(p.getLocation(), "random.click", 0.5f,0.75f);
				else if(WorldUtil.getMcVersion()<=WorldUtil.MCVERSION_1_12) p.playSound(p.getLocation(), "minecraft:block.wood_button.click_off", SoundCategory.BLOCKS, 1,1);
				else p.playSound(p.getLocation(), Sound.BLOCK_WOODEN_BUTTON_CLICK_OFF, SoundCategory.BLOCKS, 1,1);
			}
			
		}
	}
	
}
