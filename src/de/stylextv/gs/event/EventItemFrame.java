package de.stylextv.gs.event;

import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingBreakEvent.RemoveCause;

import de.stylextv.gs.player.PlayerManager;
import de.stylextv.gs.world.BetterFrame;
import de.stylextv.gs.world.WorldUtil;

public class EventItemFrame implements Listener {
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onItemFrameBreak(HangingBreakEvent e) {
		if(e.getEntity() instanceof ItemFrame) {
			BetterFrame frame=WorldUtil.getFrame((ItemFrame) e.getEntity());
			if(frame!=null) {
				e.setCancelled(true);
			}
		}
	}
	@EventHandler(priority = EventPriority.LOWEST)
	public void onItemFrameBreak(HangingBreakByEntityEvent e) {
		if(e.getRemover() instanceof Player && e.getEntity() instanceof ItemFrame && e.getCause()==RemoveCause.ENTITY) {
			BetterFrame frame=WorldUtil.getFrame((ItemFrame) e.getEntity());
			if(frame!=null) {
				Player p=(Player) e.getRemover();
				PlayerManager.onFrameBreak(p,frame);
			}
		}
	}
	
//	@EventHandler(priority = EventPriority.LOWEST)
//	public void onItemFrameInteract(PlayerInteractEntityEvent e) {
//		if(e.getRightClicked() instanceof ItemFrame) {
//			BetterFrame frame=WorldUtil.getFrame((ItemFrame) e.getRightClicked());
//			if(frame!=null) {
//				e.setCancelled(true);
//			}
//		}
//	}
	
}
