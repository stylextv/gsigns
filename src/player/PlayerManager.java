package de.stylextv.gs.player;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import de.stylextv.gs.image.ImageGenerator;
import de.stylextv.gs.main.Vars;
import de.stylextv.gs.world.WorldUtil;

public class PlayerManager {
	
	public static double[] matrix;
	public static int n=8;
	private static Point[] directions=new Point[]{new Point(1, 0),new Point(0, 1),new Point(-1, 0),new Point(0, -1)};
	
	private static ConcurrentHashMap<Player, Task> playerTasks=new ConcurrentHashMap<Player, Task>();
	
	public static void init() {
		matrix = new double[] {
				0,48,12,60,3,51,15,63,
				32,16,44,28,35,19,47,31,
				8,56,4,52,11,59,7,55,
				40,24,36,20,43,27,39,23,
				2,50,14,62,1,49,13,61,
				34,18,46,30,33,17,45,29,
				10,58,6,54,9,57,5,53,
				42,26,38,22,41,25,37,21
		};
		for(int j=0; j<matrix.length; j++) {
			matrix[j]=(matrix[j]+1)/(double)(n*n) - 0.5;
		}
	}
	
	public static void startPlacingPhase(Player p, Order order) {
		BufferedImage image=ImageGenerator.generate(order);
		
		playerTasks.put(p, new Task(image));
		p.sendMessage(Vars.PREFIX+"Your sign was created §asuccessfully§7. Please click with the left mouse button on one of the two §estone blocks§7 to place it.");
	}
	public static void cancelPlacingPhase(Player p) {
		Task t=playerTasks.remove(p);
		if(t!=null) p.sendMessage(Vars.PREFIX+"The placement process has been §ccanceled§7.");
		else p.sendMessage(Vars.PREFIX+"You are currently not in a placement §cprocess§7.");
	}
	
	public static void onPlayerQuit(PlayerQuitEvent e) {
		WorldUtil.removeAllDrewEntries(e.getPlayer());
	}
	
	public static void onPlayerInteract(PlayerInteractEvent e) {
		if(e.getAction()==Action.LEFT_CLICK_BLOCK) {
			Block b=e.getClickedBlock();
			if(b.getType().equals(Material.STONE)) {
				Player p=e.getPlayer();
				Task task=playerTasks.get(p);
				if(task!=null) {
					Location loc=b.getLocation();
					for(Point dir:directions) {
						loc.add(dir.x, 0, dir.y);
						if(loc.getBlock().getType().equals(Material.STONE)) break;
						loc.add(-dir.x, 0, -dir.y);
					}
					Location base=b.getLocation();
					if(!loc.equals(base)) {
						BufferedImage image=task.getImage();
						
						Point dirToLookIn=new Point(-(base.getBlockZ()-loc.getBlockZ()),(base.getBlockX()-loc.getBlockX()));
						World world=loc.getWorld();
						if(loc.clone().add(dirToLookIn.x, 0, dirToLookIn.y).getBlock().getType().isSolid()&&base.clone().add(dirToLookIn.x, 0, dirToLookIn.y).getBlock().getType().isSolid()) {
							b.setType(Material.AIR);
							loc.getBlock().setType(Material.AIR);
							BlockFace dir;
							if(dirToLookIn.x!=0) {
								int i=-dirToLookIn.x;
								if(i>0) dir=BlockFace.EAST;
								else dir=BlockFace.WEST;
							} else {
								int i=-dirToLookIn.y;
								if(i>0) dir=BlockFace.SOUTH;
								else dir=BlockFace.NORTH;
							}
							WorldUtil.spawnItemFrame(world, base, image.getSubimage(0, 0, 128, 128), dir);
							WorldUtil.spawnItemFrame(world, loc, image.getSubimage(128, 0, 128, 128), dir);
							p.sendMessage(Vars.PREFIX+"Your sign has been §aplaced§7 successfully.");
							playerTasks.remove(p);
						} else if(loc.clone().add(-dirToLookIn.x, 0, -dirToLookIn.y).getBlock().getType().isSolid()&&base.clone().add(-dirToLookIn.x, 0, -dirToLookIn.y).getBlock().getType().isSolid()) {
							b.setType(Material.AIR);
							loc.getBlock().setType(Material.AIR);
							BlockFace dir;
							if(dirToLookIn.x!=0) {
								int i=dirToLookIn.x;
								if(i>0) dir=BlockFace.EAST;
								else dir=BlockFace.WEST;
							} else {
								int i=dirToLookIn.y;
								if(i>0) dir=BlockFace.SOUTH;
								else dir=BlockFace.NORTH;
							}
							WorldUtil.spawnItemFrame(world, loc, image.getSubimage(0, 0, 128, 128), dir);
							WorldUtil.spawnItemFrame(world, base, image.getSubimage(128, 0, 128, 128), dir);
							p.sendMessage(Vars.PREFIX+"Your sign has been §aplaced§7 successfully.");
							playerTasks.remove(p);
						} else {
							p.sendMessage(Vars.PREFIX+"There must be §csolid §7blocks to hang a sign");
						}
					} else p.sendMessage(Vars.PREFIX+"Could not find another §cstone§7 block nearby.");
					e.setCancelled(true);
				}
			}
		}
	}
	
}
