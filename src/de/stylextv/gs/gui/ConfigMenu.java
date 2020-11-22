package de.stylextv.gs.gui;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;

import de.stylextv.gs.config.ConfigManager;
import de.stylextv.gs.config.ConfigValue;
import de.stylextv.gs.permission.PermissionUtil;
import de.stylextv.gs.util.ItemUtil;

public class ConfigMenu extends Menu {
	
	private HashMap<Player, MainMenu> mainMenues = new HashMap<Player, MainMenu>();
	
	@Override
	public void createInventory() {
		inv = Bukkit.createInventory(null, 9*6, GuiManager.getDefaultTitle());
	}
	@Override
	public void fillConstants() {
		for(int x=0; x<9; x++) {
			setItem(x, 0, ItemUtil.BLANK);
			if(x!=4) setItem(x, getLastY(), ItemUtil.BLANK);
		}
		
		setItem(4, getLastY(), ItemUtil.HEAD_BACK);
		
		setItem(4, 1, ItemUtil.CONFIG_DEFAULT);
		setPlusMinusButtons(2, 2, ConfigManager.VALUE_VIEW_DISTANCE);
		setPlusMinusButtons(6, 2, ConfigManager.VALUE_MAP_SENDS_PER_3TICKS);
		setPlusMinusButtons(2, 3, ConfigManager.VALUE_RESERVED_VANILLA_MAPS);
		setPlusMinusButtons(6, 3, ConfigManager.VALUE_LANGUAGE);
	}
	private void setPlusMinusButtons(int x, int y, ConfigValue<?> value) {
		setItem(x-1, y, value.getLeftButton());
		setItem(x+1, y, value.getRightButton());
	}
	
	@Override
	public void updateDynamicContent() {
		setItem(2, 2, ConfigManager.VALUE_VIEW_DISTANCE.getItemStack());
		setItem(6, 2, ConfigManager.VALUE_MAP_SENDS_PER_3TICKS.getItemStack());
		setItem(2, 3, ConfigManager.VALUE_RESERVED_VANILLA_MAPS.getItemStack());
		setItem(6, 3, ConfigManager.VALUE_LANGUAGE.getItemStack());
	}
	
	public void openFor(Player p, MainMenu mainMenu) {
		mainMenues.put(p, mainMenu);
		openFor(p);
	}
	
	@Override
	public void onClick(Player p, InventoryClickEvent e) {
		if(e.getClickedInventory()!=null && e.getClickedInventory().equals(inv)) {
			e.setCancelled(true);
			
			int slot=e.getSlot();
			if(slot==13) {
				if(PermissionUtil.hasConfigPermission(p) && PermissionUtil.hasGuiPermission(p)) {
					playClickSound(p, true);
					ConfigManager.restoreDefaultConfig();
				} else {
					kickPlayerForNoPerm(p);
				}
			} else if(slot == getLastY()*9+4) {
				if(PermissionUtil.hasGuiPermission(p)) {
					playClickSound(p, true);
					kickPlayerToSettingsMenu(p);
				} else {
					kickPlayerForNoPerm(p);
				}
			} else {
				if(handleButtonPresses(p, slot, e, ConfigManager.VALUE_VIEW_DISTANCE, 2*9+2)) return;
				if(handleButtonPresses(p, slot, e, ConfigManager.VALUE_MAP_SENDS_PER_3TICKS, 2*9+6)) return;
				if(handleButtonPresses(p, slot, e, ConfigManager.VALUE_RESERVED_VANILLA_MAPS, 3*9+2)) return;
				if(handleButtonPresses(p, slot, e, ConfigManager.VALUE_LANGUAGE, 3*9+6)) return;
			}
			
		}
	}
	private boolean handleButtonPresses(Player p, int slot, InventoryClickEvent e, ConfigValue<?> value, int valueSlot) {
		if(Math.abs(valueSlot-slot) == 1) {
			if(PermissionUtil.hasConfigPermission(p) && PermissionUtil.hasGuiPermission(p)) {
				int dir = slot - valueSlot;
				boolean shift = e.getAction().equals(InventoryAction.MOVE_TO_OTHER_INVENTORY);
				
				playClickSound(p, value.handleButtonPress(dir, shift));
				
			} else {
				kickPlayerForNoPerm(p);
			}
		}
		return false;
	}
	@Override
	public void onClose(Player p) {
		mainMenues.remove(p);
	}
	
	public void kickPlayerToSettingsMenu(Player p) {
		MainMenu menu = mainMenues.get(p);
		if(menu != null) GuiManager.openSettingsMenu(p, menu);
	}
	
}
