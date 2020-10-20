package de.stylextv.gs.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import de.stylextv.gs.world.BetterSign;
import de.stylextv.gs.world.WorldUtil;

public class ItemUtil {
	
	private static Field PROFILE_FIELD;
	
	public static ItemStack BLANK;
	public static ItemStack EMPTY;
	
	public static ItemStack HEAD_LEFT;
	public static ItemStack HEAD_RIGHT;
	public static ItemStack HEAD_BACK;
	
	public static ItemStack[] SORTED_BY;
	
	public static ItemStack SIGN_REMOVE;
	public static ItemStack SIGN_TP;
	public static ItemStack SIGN_PLAY;
	public static ItemStack SIGN_PAUSE;
	
	static {
		if(WorldUtil.getMcVersion() < WorldUtil.MCVERSION_1_13) {
			BLANK = createItemStack(Material.valueOf("STAINED_GLASS_PANE"), 15, "§r");
			SIGN_PLAY = createItemStack(Material.valueOf("INK_SACK"), 8, "§7State: §e§lPaused", "§7Click here to play", "§7this sign.");
			SIGN_PAUSE = createItemStack(Material.valueOf("INK_SACK"), 10, "§7State: §e§lPlaying", "§7Click here to pause", "§7this sign.");
		} else {
			BLANK = createItemStack(Material.BLACK_STAINED_GLASS_PANE, "§r");
			SIGN_PLAY = createItemStack(Material.GRAY_DYE, "§7State: §e§lPaused", "§7Click here to play", "§7this sign.");
			SIGN_PAUSE = createItemStack(Material.LIME_DYE, "§7State: §e§lPlaying", "§7Click here to pause", "§7this sign.");
		}
		EMPTY = createItemStack(Material.AIR);
		
		HEAD_LEFT = createItemStack("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmQ2OWUwNmU1ZGFkZmQ4NGU1ZjNkMWMyMTA2M2YyNTUzYjJmYTk0NWVlMWQ0ZDcxNTJmZGM1NDI1YmMxMmE5In19fQ==", "§e§lLEFT", "§7Click here to go 1 page", "§7to the left.");
		HEAD_RIGHT = createItemStack("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTliZjMyOTJlMTI2YTEwNWI1NGViYTcxM2FhMWIxNTJkNTQxYTFkODkzODgyOWM1NjM2NGQxNzhlZDIyYmYifX19", "§e§lRIGHT", "§7Click here to go 1 page", "§7to the right.");
		HEAD_BACK = createItemStack("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNWE2Nzg3YmEzMjU2NGU3YzJmM2EwY2U2NDQ5OGVjYmIyM2I4OTg0NWU1YTY2YjVjZWM3NzM2ZjcyOWVkMzcifX19", "§e§lBACK", "§7Click here to go to the", "§7previous menu.");
		
		SORTED_BY = new ItemStack[3];
		SORTED_BY[0] = createItemStack(Material.NETHER_STAR, "§7Sorted by: §e§lTypes", "","§7The signs are sorted by their","§7type (GIF/image).","","§7Click here to sort by worlds.");
		SORTED_BY[1] = createItemStack(Material.NETHER_STAR, "§7Sorted by: §e§lWorlds", "","§7The signs are sorted by their","§7world (World type and name).","","§7Click here to sort by sizes.");
		SORTED_BY[2] = createItemStack(Material.NETHER_STAR, "§7Sorted by: §e§lSizes", "","§7The signs are sorted by their","§7size (Largest -> Smallest).","","§7Click here to sort by types.");
		
		SIGN_REMOVE = createItemStack(Material.BARRIER, "§c§lREMOVE", "§7Click here to remove", "§7this sign.");
		SIGN_TP = createItemStack(Material.ENDER_PEARL, "§e§lTELEPORT", "§7Click here to teleport", "§7to this sign.");
	}
	
	public static ItemStack createItemStack(BetterSign sign, int sortingType) {
		if(WorldUtil.getMcVersion() < WorldUtil.MCVERSION_1_13) {
			Material m = null;
			int data = 0;
			if(sortingType == 0) {
				if(sign.isGif()) m=Material.ENCHANTED_BOOK;
				else m=Material.BOOK;
			} else if(sortingType == 1) {
				World w=sign.getWorld();
				if(w.getEnvironment().equals(Environment.NORMAL)) m=Material.SLIME_BALL;
				else if(w.getEnvironment().equals(Environment.NETHER)) m=Material.MAGMA_CREAM;
				else {
					if(WorldUtil.getMcVersion() <= WorldUtil.MCVERSION_1_8) m=Material.valueOf("EYE_OF_ENDER");
					else m=Material.END_CRYSTAL;
				}
			} else if(sortingType == 2) {
				int size = sign.getTotalSize();
				m=Material.valueOf("INK_SACK");
				if(size<9) data=10;
				else if(size<16) data=11;
				else data=1;
			}
			return createItemStack(m, data, "§7UUID: §b"+sign.getUid().toString().substring(0,6)+"...", "§7World: §e"+sign.getWorld().getName(),"§7Size: §e"+sign.getSize(),"","§7Click here to open the menu","§7for this sign.");
		} else if(WorldUtil.getMcVersion() < WorldUtil.MCVERSION_1_14) {
			Material m = null;
			if(sortingType == 0) {
				if(sign.isGif()) m=Material.ENCHANTED_BOOK;
				else m=Material.BOOK;
			} else if(sortingType == 1) {
				World w=sign.getWorld();
				if(w.getEnvironment().equals(Environment.NORMAL)) m=Material.SLIME_BALL;
				else if(w.getEnvironment().equals(Environment.NETHER)) m=Material.MAGMA_CREAM;
				else m=Material.END_CRYSTAL;
			} else if(sortingType == 2) {
				int size = sign.getTotalSize();
				if(size<9) m=Material.LIME_DYE;
				else if(size<16) m=Material.valueOf("DANDELION_YELLOW");
				else m=Material.valueOf("ROSE_RED");
			}
			return createItemStack(m, "§7UUID: §b"+sign.getUid().toString().substring(0,6)+"...", "§7World: §e"+sign.getWorld().getName(),"§7Size: §e"+sign.getSize(),"","§7Click here to open the menu","§7for this sign.");
		} else {
			Material m = null;
			if(sortingType == 0) {
				if(sign.isGif()) m=Material.ENCHANTED_BOOK;
				else m=Material.BOOK;
			} else if(sortingType == 1) {
				World w=sign.getWorld();
				if(w.getEnvironment().equals(Environment.NORMAL)) m=Material.SLIME_BALL;
				else if(w.getEnvironment().equals(Environment.NETHER)) m=Material.MAGMA_CREAM;
				else m=Material.END_CRYSTAL;
			} else if(sortingType == 2) {
				int size = sign.getTotalSize();
				if(size<9) m=Material.LIME_DYE;
				else if(size<16) m=Material.YELLOW_DYE;
				else m=Material.RED_DYE;
			}
			return createItemStack(m, "§7UUID: §b"+sign.getUid().toString().substring(0,6)+"...", "§7World: §e"+sign.getWorld().getName(),"§7Size: §e"+sign.getSize(),"","§7Click here to open the menu","§7for this sign.");
		}
	}
	
	public static ItemStack createItemStack(Material m) {
		ItemStack itemStack = new ItemStack(m);
		return itemStack;
	}
	public static ItemStack createItemStack(Material m, String name) {
		ItemStack itemStack = new ItemStack(m);
		ItemMeta meta = itemStack.getItemMeta();
		meta.setDisplayName(name);
		itemStack.setItemMeta(meta);
		return itemStack;
	}
	public static ItemStack createItemStack(Material m, String name, String... lore) {
		ItemStack itemStack = new ItemStack(m);
		ItemMeta meta = itemStack.getItemMeta();
		meta.setDisplayName(name);
		ArrayList<String> list = new ArrayList<String>();
		for(String s : lore) list.add(s);
		meta.setLore(list);
		itemStack.setItemMeta(meta);
		return itemStack;
	}
	@SuppressWarnings("deprecation")
	public static ItemStack createItemStack(Material m, int data, String name, String... lore) {
		ItemStack itemStack = new ItemStack(m, 1, (short) data);
		ItemMeta meta = itemStack.getItemMeta();
		meta.setDisplayName(name);
		ArrayList<String> list = new ArrayList<String>();
		for(String s : lore) list.add(s);
		meta.setLore(list);
		itemStack.setItemMeta(meta);
		return itemStack;
	}
	@SuppressWarnings("deprecation")
	public static ItemStack createItemStack(Material m, int data, String name) {
		ItemStack itemStack = new ItemStack(m, 1, (short) data);
		ItemMeta meta = itemStack.getItemMeta();
		meta.setDisplayName(name);
		itemStack.setItemMeta(meta);
		return itemStack;
	}
	@SuppressWarnings("deprecation")
	public static ItemStack createItemStack(String texture, String name, String... lore) {
		ItemStack itemStack;
		if(WorldUtil.getMcVersion() < WorldUtil.MCVERSION_1_13) {
			itemStack = new ItemStack(Material.valueOf("SKULL_ITEM"), 1, (short) 3);
		} else {
			itemStack = new ItemStack(Material.PLAYER_HEAD);
		}
		SkullMeta meta = (SkullMeta) itemStack.getItemMeta();
		meta.setDisplayName(name);
		
		ArrayList<String> list = new ArrayList<String>();
		for(String s : lore) list.add(s);
		meta.setLore(list);
		
		GameProfile profile = new GameProfile(UUID.randomUUID(), null);
        profile.getProperties().put("textures", new Property("textures", texture));
        try {
            if(PROFILE_FIELD == null) {
            	PROFILE_FIELD = meta.getClass().getDeclaredField("profile");
            	PROFILE_FIELD.setAccessible(true);
            }
            PROFILE_FIELD.set(meta, profile);
        } catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException ex) {
            ex.printStackTrace();
        }
        
		itemStack.setItemMeta(meta);
		return itemStack;
	}
	
}
