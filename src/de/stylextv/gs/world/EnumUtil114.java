package de.stylextv.gs.world;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

public class EnumUtil114 extends EnumUtil {
	
	@SuppressWarnings("deprecation")
	@Override
	public ItemFrame spawnItemFrame(int id, World world, Location loc, BlockFace dir, MapRenderer renderer) {
		loc.add(0, 0, -1);
		BlockData backup=null;
		Block b=loc.getBlock();
		if(!b.getType().isSolid()) {
			backup=b.getBlockData();
			b.setType(Material.COBBLESTONE);
		}
		loc.add(0, 0, 1);
		ItemFrame frame=(ItemFrame) world.spawnEntity(loc, EntityType.ITEM_FRAME);
		frame.setFacingDirection(dir);
		if(backup!=null) b.setBlockData(backup);
		
		MapView view=Bukkit.getMap(id);
		view.getRenderers().clear();
		for(MapRenderer r:view.getRenderers()) view.removeRenderer(r);
		view.addRenderer(renderer);
		ItemStack item = new ItemStack(Material.FILLED_MAP, 1);
		MapMeta meta=(MapMeta) item.getItemMeta();
		meta.setMapView(view);
		item.setItemMeta(meta);
		
		frame.setItem(item);
		return frame;
	}
	
	@Override
	public ItemFrame spawnItemFrame(World world, Location loc, BlockFace dir, MapRenderer renderer) {
		loc.add(0, 0, -1);
		BlockData backup=null;
		Block b=loc.getBlock();
		if(!b.getType().isSolid()) {
			backup=b.getBlockData();
			b.setType(Material.COBBLESTONE);
		}
		loc.add(0, 0, 1);
		ItemFrame frame=(ItemFrame) world.spawnEntity(loc, EntityType.ITEM_FRAME);
		frame.setFacingDirection(dir);
		if(backup!=null) b.setBlockData(backup);
		
		MapView view = Bukkit.createMap(world);
		view.getRenderers().clear();
		for(MapRenderer r:view.getRenderers()) view.removeRenderer(r);
		view.addRenderer(renderer);
		ItemStack item = new ItemStack(Material.FILLED_MAP, 1);
		MapMeta meta=(MapMeta) item.getItemMeta();
		meta.setMapView(view);
		item.setItemMeta(meta);
		
		frame.setItem(item);
		return frame;
	}
	
	@Override
	public MapView getMapView(ItemStack item) {
		if(item==null) return null;
		MapMeta meta=(MapMeta) item.getItemMeta();
		if(meta!=null) return meta.getMapView();
		return null;
	}
	
	@Override
	public int getMapId(MapView view) {
		return view.getId();
	}
	
}
