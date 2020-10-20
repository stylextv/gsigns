package de.stylextv.gs.gui;

import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import de.stylextv.gs.command.CommandHandler;
import de.stylextv.gs.world.WorldUtil;

public abstract class Menu {
	
	protected Inventory inv;
	
	public void create() {
		createInventory();
		fillConstants();
		updateDynamicContent();
	}
	
	public abstract void createInventory();
	public abstract void fillConstants();
	
	public abstract void updateDynamicContent();
	
	public abstract void onClick(Player p, InventoryClickEvent e);
	public abstract void onClose(Player p);
	
	protected void openFor(Player p) {
		p.openInventory(inv);
	}
	public void setItem(int x, int y, ItemStack item) {
		inv.setItem(y*9+x, item);
	}
	public int getLastY() {
		return inv.getSize()/9-1;
	}
	
	protected static void playClickSound(Player p, boolean success) {
		if(success) {
			if(WorldUtil.getMcVersion()<=WorldUtil.MCVERSION_1_8) p.playSound(p.getLocation(), "gui.button.press", 1,2);
			else if(WorldUtil.getMcVersion()<=WorldUtil.MCVERSION_1_10) p.playSound(p.getLocation(), "minecraft:block.stone_button.click_off", 1,2);
			else if(WorldUtil.getMcVersion()<=WorldUtil.MCVERSION_1_12) p.playSound(p.getLocation(), "minecraft:block.stone_button.click_off", SoundCategory.AMBIENT, 1,2);
			else p.playSound(p.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_OFF, SoundCategory.AMBIENT, 1,2);
		} else {
			if(WorldUtil.getMcVersion()<=WorldUtil.MCVERSION_1_8) p.playSound(p.getLocation(), "gui.button.press", 0.5f,0.75f*2);
			else if(WorldUtil.getMcVersion()<=WorldUtil.MCVERSION_1_10) p.playSound(p.getLocation(), "minecraft:block.wood_button.click_off", 1,2);
			else if(WorldUtil.getMcVersion()<=WorldUtil.MCVERSION_1_12) p.playSound(p.getLocation(), "minecraft:block.wood_button.click_off", SoundCategory.AMBIENT, 1,2);
			else p.playSound(p.getLocation(), Sound.BLOCK_WOODEN_BUTTON_CLICK_OFF, SoundCategory.AMBIENT, 1,2);
		}
	}
	protected static void kickPlayerForNoPerm(Player p) {
		playClickSound(p, false);
		p.closeInventory();
		CommandHandler.sendNoPermission(p);
	}
	
}
