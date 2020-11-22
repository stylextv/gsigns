package de.stylextv.gs.event;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingBreakEvent.RemoveCause;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;

import de.stylextv.gs.player.PlayerManager;
import de.stylextv.gs.world.BetterFrame;
import de.stylextv.gs.world.WorldUtil;

public class EventItemFrame implements Listener {
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onItemFrameBreak(HangingBreakEvent e) {
		if(e.getEntity() instanceof ItemFrame) {
			ItemFrame itemFrame = (ItemFrame) e.getEntity();
			BetterFrame frame=WorldUtil.getFrame(itemFrame.getLocation(), itemFrame.getFacing());
			if(frame!=null) {
				e.setCancelled(true);
			}
		}
	}
	@EventHandler(priority = EventPriority.LOWEST)
	public void onItemFrameBreak(HangingBreakByEntityEvent e) {
		if(e.getRemover() instanceof Player && e.getEntity() instanceof ItemFrame && e.getCause()==RemoveCause.ENTITY) {
			ItemFrame itemFrame = (ItemFrame) e.getEntity();
			BetterFrame frame=WorldUtil.getFrame(itemFrame.getLocation(), itemFrame.getFacing());
			if(frame!=null) {
				Player p=(Player) e.getRemover();
				PlayerManager.onFrameBreak(p,frame);
			}
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onItemFrameInteract(PlayerInteractEntityEvent e) {
		if(e.getRightClicked() instanceof ItemFrame) {
			ItemFrame itemFrame = (ItemFrame) e.getRightClicked();
			BetterFrame frame=WorldUtil.getFrame(itemFrame.getLocation(), itemFrame.getFacing());
			if(frame!=null) {
				e.setCancelled(true);
			}
		}
	}
	@EventHandler(priority = EventPriority.LOWEST)
	public void onItemFrameInteract(PlayerInteractAtEntityEvent e) {
		if(e.getRightClicked() instanceof ItemFrame) {
			ItemFrame itemFrame = (ItemFrame) e.getRightClicked();
			BetterFrame frame=WorldUtil.getFrame(itemFrame.getLocation(), itemFrame.getFacing());
			if(frame!=null) {
				e.setCancelled(true);
			}
		}
	}
	@EventHandler(priority = EventPriority.LOWEST)
	public void onItemFrameInteract(HangingPlaceEvent e) {
		if(e.getEntity().getType()==EntityType.ITEM_FRAME) {
			ItemFrame itemFrame = (ItemFrame) e.getEntity();
			BetterFrame frame=WorldUtil.getFrame(itemFrame.getLocation(), itemFrame.getFacing());
			if(frame!=null) {
				e.setCancelled(true);
			}
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onItemFrameDamage(EntityDamageEvent e) {
		if(e.getEntity().getType()==EntityType.ITEM_FRAME) {
			ItemFrame itemFrame = (ItemFrame) e.getEntity();
			BetterFrame frame=WorldUtil.getFrame(itemFrame.getLocation(), itemFrame.getFacing());
			if(frame!=null) {
				e.setCancelled(true);
			}
		}
	}
	
}
