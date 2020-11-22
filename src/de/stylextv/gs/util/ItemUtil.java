package de.stylextv.gs.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import de.stylextv.gs.lang.LanguageManager;
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
	public static ItemStack SETTINGS;
	
	public static ItemStack SIGN_REMOVE;
	public static ItemStack SIGN_TP;
	public static ItemStack SIGN_PLAY;
	public static ItemStack SIGN_PAUSE;
	
	public static ItemStack SETTINGS_NUKE;
	public static ItemStack SETTINGS_CONFIG;
	public static ItemStack SETTINGS_UPDATE;
	
	public static ItemStack CONFIG_DEFAULT;
	
	static {
		create();
	}
	public static void create() {
		if(WorldUtil.getMcVersion() < WorldUtil.MCVERSION_1_13) {
			BLANK = createItemStack(Material.valueOf("STAINED_GLASS_PANE"), 15, "§r");
			SIGN_PLAY = createItemStack(Material.valueOf("INK_SACK"), 8, LanguageManager.parseMsg("trans.item.play.name"), LanguageManager.parseMsg("trans.item.play.desc1"), LanguageManager.parseMsg("trans.item.play.desc2"));
			SIGN_PAUSE = createItemStack(Material.valueOf("INK_SACK"), 10, LanguageManager.parseMsg("trans.item.pause.name"), LanguageManager.parseMsg("trans.item.pause.desc1"), LanguageManager.parseMsg("trans.item.pause.desc2"));
		} else {
			BLANK = createItemStack(Material.BLACK_STAINED_GLASS_PANE, "§r");
			SIGN_PLAY = createItemStack(Material.GRAY_DYE, LanguageManager.parseMsg("trans.item.play.name"), LanguageManager.parseMsg("trans.item.play.desc1"), LanguageManager.parseMsg("trans.item.play.desc2"));
			SIGN_PAUSE = createItemStack(Material.LIME_DYE, LanguageManager.parseMsg("trans.item.pause.name"), LanguageManager.parseMsg("trans.item.pause.desc1"), LanguageManager.parseMsg("trans.item.pause.desc2"));
		}
		EMPTY = createItemStack(Material.AIR);
		
		HEAD_LEFT = createItemStack("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmQ2OWUwNmU1ZGFkZmQ4NGU1ZjNkMWMyMTA2M2YyNTUzYjJmYTk0NWVlMWQ0ZDcxNTJmZGM1NDI1YmMxMmE5In19fQ==", LanguageManager.parseMsg("trans.item.left.name"), LanguageManager.parseMsg("trans.item.left.desc1"), LanguageManager.parseMsg("trans.item.left.desc2"));
		HEAD_RIGHT = createItemStack("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTliZjMyOTJlMTI2YTEwNWI1NGViYTcxM2FhMWIxNTJkNTQxYTFkODkzODgyOWM1NjM2NGQxNzhlZDIyYmYifX19", LanguageManager.parseMsg("trans.item.right.name"), LanguageManager.parseMsg("trans.item.right.desc1"), LanguageManager.parseMsg("trans.item.right.desc2"));
		HEAD_BACK = createItemStack("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNWE2Nzg3YmEzMjU2NGU3YzJmM2EwY2U2NDQ5OGVjYmIyM2I4OTg0NWU1YTY2YjVjZWM3NzM2ZjcyOWVkMzcifX19", LanguageManager.parseMsg("trans.item.back.name"), LanguageManager.parseMsg("trans.item.back.desc1"), LanguageManager.parseMsg("trans.item.back.desc2"));
		
		SORTED_BY = new ItemStack[3];
		for(int i=0; i<SORTED_BY.length; i++) {
			int j = i+1;
			SORTED_BY[i] = createItemStack(Material.NETHER_STAR, LanguageManager.parseMsg("trans.item.sorting"+j+".name"), "",LanguageManager.parseMsg("trans.item.sorting"+j+".desc1"),LanguageManager.parseMsg("trans.item.sorting"+j+".desc2"),"",LanguageManager.parseMsg("trans.item.sorting"+j+".desc3"));
		}
		SETTINGS = createItemStack(Material.COMPARATOR, LanguageManager.parseMsg("trans.item.settings.name"), LanguageManager.parseMsg("trans.item.settings.desc1"), LanguageManager.parseMsg("trans.item.settings.desc2"));
		
		SIGN_REMOVE = createItemStack(Material.BARRIER, LanguageManager.parseMsg("trans.item.remove.name"), LanguageManager.parseMsg("trans.item.remove.desc1"), LanguageManager.parseMsg("trans.item.remove.desc2"));
		SIGN_TP = createItemStack(Material.ENDER_PEARL, LanguageManager.parseMsg("trans.item.teleport.name"), LanguageManager.parseMsg("trans.item.teleport.desc1"), LanguageManager.parseMsg("trans.item.teleport.desc2"));
		
		SETTINGS_NUKE = createItemStack("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWU0MzUyNjgwZDBiYjI5YjkxMzhhZjc4MzMwMWEzOTFiMzQwOTBjYjQ5NDFkNTJjMDg3Y2E3M2M4MDM2Y2I1MSJ9fX0=", LanguageManager.parseMsg("trans.item.removeall.name"), LanguageManager.parseMsg("trans.item.removeall.desc1"), LanguageManager.parseMsg("trans.item.removeall.desc2"));
		SETTINGS_CONFIG = createItemStack(Material.BOOKSHELF, LanguageManager.parseMsg("trans.item.config.name"), LanguageManager.parseMsg("trans.item.config.desc1"), LanguageManager.parseMsg("trans.item.config.desc2"));
		String updateTexture = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzA0MGZlODM2YTZjMmZiZDJjN2E5YzhlYzZiZTUxNzRmZGRmMWFjMjBmNTVlMzY2MTU2ZmE1ZjcxMmUxMCJ9fX0=";
		String msg = null;
		Calendar cal = Calendar.getInstance();
		int month = cal.get(Calendar.MONTH)+1;
		int day = cal.get(Calendar.DAY_OF_MONTH);
		if(month == 10) {
			msg = "* Happy Halloween *";
			updateTexture = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDVhZGI2ZmZhMmM1YzBlMzUwYzI4NDk5MTM4YTU1NjY0NDFkN2JjNTczZGUxOTg5ZmRlMjEyZmNiMTk2NjgyNiJ9fX0=";
		} else if(month == 12 && day <= 26) {
			msg = "* Happy Christmas *";
			updateTexture = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNmNlZjlhYTE0ZTg4NDc3M2VhYzEzNGE0ZWU4OTcyMDYzZjQ2NmRlNjc4MzYzY2Y3YjFhMjFhODViNyJ9fX0=";
		} else if(month == 12 && day == 31) {
			updateTexture = null;
			SETTINGS_UPDATE = createItemStack(Material.FIREWORK_ROCKET, LanguageManager.parseMsg("trans.item.update.name"), LanguageManager.parseMsg("trans.item.update.desc1"), LanguageManager.parseMsg("trans.item.update.desc2"), LanguageManager.parseMsg("trans.item.update.desc3"), LanguageManager.parseMsg("trans.item.update.desc4"), "", "§8* Happy New Year *", "");
		}
		if(updateTexture!=null) {
			if(msg == null) SETTINGS_UPDATE = createItemStack(updateTexture, LanguageManager.parseMsg("trans.item.update.name"), LanguageManager.parseMsg("trans.item.update.desc1"), LanguageManager.parseMsg("trans.item.update.desc2"), LanguageManager.parseMsg("trans.item.update.desc3"), LanguageManager.parseMsg("trans.item.update.desc4"));
			else SETTINGS_UPDATE = createItemStack(updateTexture, LanguageManager.parseMsg("trans.item.update.name"), LanguageManager.parseMsg("trans.item.update.desc1"), LanguageManager.parseMsg("trans.item.update.desc2"), LanguageManager.parseMsg("trans.item.update.desc3"), LanguageManager.parseMsg("trans.item.update.desc4"), "", "§8"+msg, "");
		}
		
		CONFIG_DEFAULT = createItemStack("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZmE4NTk3MWZiMTNiZjBiNzhlYjlmOTZiMmJkY2UxYTExMzMxMzczZGUzMGQ5MjM5ZThiYzA2YTI5MTJjNGE0In19fQ==", LanguageManager.parseMsg("trans.item.restoreconfig.name"), LanguageManager.parseMsg("trans.item.restoreconfig.desc1"), LanguageManager.parseMsg("trans.item.restoreconfig.desc2"));
	}
	
	public static ItemStack createSignItemStack(BetterSign sign, int sortingType) {
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
			return createItemStack(m, data, LanguageManager.parseMsg("trans.menu.sign.uuid", sign.getUid().toString().substring(0,6)+"..."), LanguageManager.parseMsg("trans.menu.sign.world", sign.getWorld().getName()),LanguageManager.parseMsg("trans.menu.sign.size", sign.getSize()),"",LanguageManager.parseMsg("trans.menu.sign.desc1"),LanguageManager.parseMsg("trans.menu.sign.desc2"));
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
			return createItemStack(m, LanguageManager.parseMsg("trans.menu.sign.uuid", sign.getUid().toString().substring(0,6)+"..."), LanguageManager.parseMsg("trans.menu.sign.world", sign.getWorld().getName()),LanguageManager.parseMsg("trans.menu.sign.size", sign.getSize()),"",LanguageManager.parseMsg("trans.menu.sign.desc1"),LanguageManager.parseMsg("trans.menu.sign.desc2"));
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
			return createItemStack(m, LanguageManager.parseMsg("trans.menu.sign.uuid", sign.getUid().toString().substring(0,6)+"..."), LanguageManager.parseMsg("trans.menu.sign.world", sign.getWorld().getName()),LanguageManager.parseMsg("trans.menu.sign.size", sign.getSize()),"",LanguageManager.parseMsg("trans.menu.sign.desc1"),LanguageManager.parseMsg("trans.menu.sign.desc2"));
		}
	}
	public static ItemStack createConfigMinusItemStack() {
		return createItemStack("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmQ4YTk5ZGIyYzM3ZWM3MWQ3MTk5Y2Q1MjYzOTk4MWE3NTEzY2U5Y2NhOTYyNmEzOTM2Zjk2NWIxMzExOTMifX19", LanguageManager.parseMsg("trans.item.minus.name"), LanguageManager.parseMsg("trans.item.minus.desc1"),LanguageManager.parseMsg("trans.item.minus.desc2"),"",LanguageManager.parseMsg("trans.item.plusminus.desc1","-"),LanguageManager.parseMsg("trans.item.plusminus.desc2","-"));
	}
	public static ItemStack createConfigPlusItemStack() {
		return createItemStack("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvM2VkZDIwYmU5MzUyMDk0OWU2Y2U3ODlkYzRmNDNlZmFlYjI4YzcxN2VlNmJmY2JiZTAyNzgwMTQyZjcxNiJ9fX0=", LanguageManager.parseMsg("trans.item.plus.name"), LanguageManager.parseMsg("trans.item.plus.desc1"),LanguageManager.parseMsg("trans.item.plus.desc2"),"",LanguageManager.parseMsg("trans.item.plusminus.desc1","+"),LanguageManager.parseMsg("trans.item.plusminus.desc2","+"));
	}
	public static ItemStack createConfigOptionLeftItemStack() {
		return createItemStack("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmQ2OWUwNmU1ZGFkZmQ4NGU1ZjNkMWMyMTA2M2YyNTUzYjJmYTk0NWVlMWQ0ZDcxNTJmZGM1NDI1YmMxMmE5In19fQ==", LanguageManager.parseMsg("trans.item.optionleft.name"), LanguageManager.parseMsg("trans.item.optionleft.desc1"),LanguageManager.parseMsg("trans.item.optionleft.desc2"));
	}
	public static ItemStack createConfigOptionRightItemStack() {
		return createItemStack("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTliZjMyOTJlMTI2YTEwNWI1NGViYTcxM2FhMWIxNTJkNTQxYTFkODkzODgyOWM1NjM2NGQxNzhlZDIyYmYifX19", LanguageManager.parseMsg("trans.item.optionright.name"), LanguageManager.parseMsg("trans.item.optionright.desc1"),LanguageManager.parseMsg("trans.item.optionright.desc2"));
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
	public static ItemStack createItemStack(Material m, String name, ArrayList<String> lore) {
		ItemStack itemStack = new ItemStack(m);
		ItemMeta meta = itemStack.getItemMeta();
		meta.setDisplayName(name);
		meta.setLore(lore);
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
	public static ItemStack createItemStack(String texture, String name, String... lore) {
		ArrayList<String> list = new ArrayList<String>();
		for(String s : lore) list.add(s);
		return createItemStack(texture, name, list);
	}
	@SuppressWarnings("deprecation")
	public static ItemStack createItemStack(String texture, String name, ArrayList<String> lore) {
		ItemStack itemStack;
		if(WorldUtil.getMcVersion() < WorldUtil.MCVERSION_1_13) {
			itemStack = new ItemStack(Material.valueOf("SKULL_ITEM"), 1, (short) 3);
		} else {
			itemStack = new ItemStack(Material.PLAYER_HEAD);
		}
		SkullMeta meta = (SkullMeta) itemStack.getItemMeta();
		meta.setDisplayName(name);
		
		meta.setLore(lore);
		
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
