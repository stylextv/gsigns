package de.stylextv.gs.event;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;

import de.stylextv.gs.gui.GuiManager;

public class EventGui implements Listener {
	
	@EventHandler
	public void onClick(InventoryClickEvent e) {
		GuiManager.onClick(e);
	}
	@EventHandler
	public void onClose(InventoryCloseEvent e) {
		GuiManager.onClose(e);
	}
	@EventHandler
	public void onOpen(InventoryOpenEvent e) {
		GuiManager.onOpen(e);
	}
	
}
