package de.stylextv.gs.api;

import java.util.UUID;

import org.bukkit.Location;

import de.stylextv.gs.player.CodeParser;
import de.stylextv.gs.player.Order;
import de.stylextv.gs.player.PlayerManager;
import de.stylextv.gs.world.WorldUtil;

public class PublicGSignsAPI implements GSignsAPI {
	
	@Override
	public UUID createSign(String code, Location corner1, Location corner2) {
		Order order = CodeParser.parseCode(code);
		
		return PlayerManager.placeSign(order, corner1, corner2);
	}
	
	@Override
	public void removeSign(UUID uid) {
		WorldUtil.removeSign(uid);
	}
	
}
