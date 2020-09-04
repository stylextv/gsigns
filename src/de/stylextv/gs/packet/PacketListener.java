package de.stylextv.gs.packet;

import org.bukkit.entity.Player;

import de.stylextv.gs.packet.Reflections.FieldAccessor;
import de.stylextv.gs.world.WorldUtil;

/**
 * @author Jitse Boonstra
 */
public class PacketListener {
	
    // Classes:
    private final Class<?> packetPlayInUseEntityClazz = Reflections.getMinecraftClass("PacketPlayInUseEntity");
    private final Class<?> enumEntityUseActionClazz = Reflections.getMinecraftClass("PacketPlayInUseEntity$EnumEntityUseAction");
    
    // Fields:
    private final FieldAccessor<Integer> entityIdField = Reflections.getField(packetPlayInUseEntityClazz, "a", int.class);
    private final FieldAccessor<?> interactTypeField = Reflections.getField(packetPlayInUseEntityClazz, "action", enumEntityUseActionClazz);
    
    public void start() {
        new TinyProtocol() {
            @Override
            public Object onPacketInAsync(Player player, Object packet) {
                return handleInteractPacket(packet) ? super.onPacketInAsync(player, packet) : null;
            }
        };
    }
    
    private boolean handleInteractPacket(Object packet) {
        if (!packetPlayInUseEntityClazz.isInstance(packet))
            return true; // We aren't handling the packet.
        
        int packetEntityId = (int) entityIdField.get(packet);
        boolean attack = interactTypeField.get(packet).toString().equals("ATTACK");
        
        return !(!attack&&WorldUtil.isFrame(packetEntityId));
    }
    
}
