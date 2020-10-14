package de.stylextv.gs.packet;

import org.bukkit.entity.Player;

import de.stylextv.gs.main.Main;
import de.stylextv.gs.packet.Reflections.FieldAccessor;
import de.stylextv.gs.world.WorldUtil;
import io.netty.channel.Channel;

public class PacketListener {
	
    private final Class<?> packetPlayInUseEntityClazz = Reflections.getMinecraftClass("PacketPlayInUseEntity");
    private final Class<?> enumEntityUseActionClazz = Reflections.getMinecraftClass("PacketPlayInUseEntity$EnumEntityUseAction");
    private final Class<?> packetPlayOutMapClazz = Reflections.getMinecraftClass("PacketPlayOutMap");
    
    private final FieldAccessor<Integer> entityIdField = Reflections.getField(packetPlayInUseEntityClazz, "a", int.class);
    private final FieldAccessor<?> interactTypeField = Reflections.getField(packetPlayInUseEntityClazz, "action", enumEntityUseActionClazz);
    private final FieldAccessor<Integer> mapIdField = Reflections.getField(packetPlayOutMapClazz, "a", int.class);
    
    public void start() {
        new TinyProtocol(Main.getPlugin()) {
            @Override
            public Object onPacketInAsync(Player sender, Channel channel, Object packet) {
            	return handlePacketIn(packet) ? super.onPacketInAsync(sender, channel, packet) : null;
            }
            @Override
            public Object onPacketOutAsync(Player receiver, Channel channel, Object packet) {
            	return handlePacketOut(receiver, packet) ? super.onPacketOutAsync(receiver, channel, packet) : null;
            }
        };
    }
    
    private boolean handlePacketIn(Object packet) {
        if (!packetPlayInUseEntityClazz.isInstance(packet))
            return true; // We aren't handling the packet.
        
        int packetEntityId = (int) entityIdField.get(packet);
        boolean attack = interactTypeField.get(packet).toString().equals("ATTACK");
        
        return !(!attack&&WorldUtil.isFrame(packetEntityId));
    }
    private boolean handlePacketOut(Player p, Object packet) {
    	if(packetPlayOutMapClazz.isInstance(packet)) {
	        int id = (int) mapIdField.get(packet);
	        
	        if (id < 0) {
				int newId = -id;
				mapIdField.set(packet, newId);
			} else {
				boolean isPluginMap = WorldUtil.isIdUsedBy(p, (short)id);
				return !isPluginMap;
			}
		}
    	return true;
    }
    
}
