package de.stylextv.gs.packet;

import com.google.common.collect.Lists;
import com.google.common.collect.MapMaker;

import de.stylextv.gs.main.Main;
import io.netty.channel.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class TinyProtocol {

    private static final AtomicInteger ID = new AtomicInteger(0);

    // Used in order to lookup a channel
    private static final Reflections.MethodInvoker getPlayerHandle = Reflections.getMethod("{obc}.entity.CraftPlayer", "getHandle");
    private static final Reflections.FieldAccessor<Object> getConnection = Reflections.getField("{nms}.EntityPlayer", "playerConnection", Object.class);
    private static final Reflections.FieldAccessor<Object> getManager = Reflections.getField("{nms}.PlayerConnection", "networkManager", Object.class);
    private static final Reflections.FieldAccessor<Channel> getChannel = Reflections.getField("{nms}.NetworkManager", Channel.class, 0);

    // Looking up ServerConnection
    private static final Class<Object> minecraftServerClass = Reflections.getUntypedClass("{nms}.MinecraftServer");
    private static final Class<Object> serverConnectionClass = Reflections.getUntypedClass("{nms}.ServerConnection");
    private static final Reflections.FieldAccessor<Object> getMinecraftServer = Reflections.getField("{obc}.CraftServer", minecraftServerClass, 0);
    private static final Reflections.FieldAccessor<Object> getServerConnection = Reflections.getField(minecraftServerClass, serverConnectionClass, 0);
    private static final Reflections.MethodInvoker getNetworkMarkers = Reflections.getTypedMethod(serverConnectionClass, null, List.class, serverConnectionClass);

    // Packets we have to intercept
    private static final Class<?> PACKET_LOGIN_IN_START = Reflections.getMinecraftClass("PacketLoginInStart");
    @SuppressWarnings("rawtypes")
	private static final Reflections.FieldAccessor getGameProfile = Reflections.getField(PACKET_LOGIN_IN_START,
            Reflections.getClass("com.mojang.authlib.GameProfile"), 0);

    // Speedup channel lookup
    private Map<String, Channel> channelLookup = new MapMaker().weakValues().makeMap();
    private Listener listener;

    // Channels that have already been removed
    private Set<Channel> uninjectedChannels = Collections.newSetFromMap(new MapMaker().weakKeys().makeMap());

    // List of network markers
    private List<Object> networkManagers;

    // Injected channel handlers
    private List<Channel> serverChannels = Lists.newArrayList();
    private ChannelInboundHandlerAdapter serverChannelHandler;
    private ChannelInitializer<Channel> beginInitProtocol;
    private ChannelInitializer<Channel> endInitProtocol;

    // Current handler name
    private String handlerName;

    private volatile boolean closed;
    protected Plugin plugin;

    protected TinyProtocol() {
    	plugin=Main.getPlugin();

        // Compute handler name
        this.handlerName = "tiny-" + plugin.getName() + "-" + ID.incrementAndGet();

        // Prepare existing players
        registerBukkitEvents();

        try {
            registerChannelHandler();
            registerPlayers(plugin);
        } catch (IllegalArgumentException exception) {
            // Damn you, late bind
            new BukkitRunnable() {
                @Override
                public void run() {
                    registerChannelHandler();
                    registerPlayers(plugin);
                }
            }.runTask(plugin);
        }
    }
    
    private void createServerChannelHandler() {
        // Handle connected channels
        endInitProtocol = new ChannelInitializer<Channel>() {

            @SuppressWarnings("all")
            @Override
            protected void initChannel(Channel channel) throws Exception {
                try {
                    // This can take a while, so we need to stop the main thread from interfering
                    synchronized (networkManagers) {
                        // Stop injecting channels
                        if (!closed) {
                            channel.eventLoop().submit(() -> injectChannelInternal(channel));
                        }
                    }
                } catch (Exception exception) {
                }
            }

        };

        // This is executed before Minecraft's channel handler
        beginInitProtocol = new ChannelInitializer<Channel>() {

            @SuppressWarnings("all")
            @Override
            protected void initChannel(Channel channel) throws Exception {
                channel.pipeline().addLast(endInitProtocol);
            }

        };

        serverChannelHandler = new ChannelInboundHandlerAdapter() {

            @SuppressWarnings("all")
            @Override
            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                Channel channel = (Channel) msg;

                // Prepare to initialize ths channel
                channel.pipeline().addFirst(beginInitProtocol);
                ctx.fireChannelRead(msg);
            }

        };
    }

    private void registerBukkitEvents() {
        listener = new Listener() {

            @EventHandler(priority = EventPriority.LOWEST)
            public final void onPlayerLogin(PlayerLoginEvent e) {
                if (closed)
                    return;

                Channel channel = getChannel(e.getPlayer());

                // Don't inject players that have been explicitly uninjected
                if (!uninjectedChannels.contains(channel)) {
                    injectPlayer(e.getPlayer());
                }
            }

            @EventHandler
            public final void onPluginDisable(PluginDisableEvent e) {
                if (e.getPlugin().equals(plugin)) {
                    close();
                }
            }

        };

        plugin.getServer().getPluginManager().registerEvents(listener, plugin);
    }

    @SuppressWarnings("unchecked")
    private void registerChannelHandler() {
        Object mcServer = getMinecraftServer.get(Bukkit.getServer());
        Object serverConnection = getServerConnection.get(mcServer);
        boolean looking = true;

        // We need to synchronize against this list
        networkManagers = (List<Object>) getNetworkMarkers.invoke(null, serverConnection);
        createServerChannelHandler();

        // Find the correct list, or implicitly throw an exception
        for (int i = 0; looking; i++) {
            List<Object> list = Reflections.getField(serverConnection.getClass(), List.class, i).get(serverConnection);

            for (Object item : list) {
                if (!(item instanceof ChannelFuture))
                    break;

                // Channel future that contains the server connection
                Channel serverChannel = ((ChannelFuture) item).channel();

                serverChannels.add(serverChannel);
                serverChannel.pipeline().addFirst(serverChannelHandler);
                looking = false;
            }
        }
    }

    private void unregisterChannelHandler() {
        if (serverChannelHandler == null)
            return;

        for (Channel serverChannel : serverChannels) {
            final ChannelPipeline pipeline = serverChannel.pipeline();

            // Remove channel handler
            serverChannel.eventLoop().execute(() -> {
                try {
                    pipeline.remove(serverChannelHandler);
                } catch (NoSuchElementException exception) {
                    // That's fine
                }
            });
        }
    }

    private void registerPlayers(Plugin plugin) {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            injectPlayer(player);
        }
    }

    public Object onPacketInAsync(Player sender, Object packet) {
        return packet;
    }

    private void injectPlayer(Player player) {
    	PacketInterceptor pi=injectChannelInternal(getChannel(player));
        if(pi!=null) pi.player = player;
    }

    private PacketInterceptor injectChannelInternal(Channel channel) {
        try {
            PacketInterceptor interceptor = (PacketInterceptor) channel.pipeline().get(handlerName);

            // Inject our packet interceptor
            if (interceptor == null) {
                interceptor = new PacketInterceptor();
                channel.pipeline().addBefore("packet_handler", handlerName, interceptor);
                uninjectedChannels.remove(channel);
            }

            return interceptor;
        } catch (Exception exception) {
            // Try again
        	if(channel==null) return null;
            return (PacketInterceptor) channel.pipeline().get(handlerName);
        }
    }

    private Channel getChannel(Player player) {
        Channel channel = channelLookup.get(player.getName());

        // Lookup channel again
        int c=0;
        while (channel == null && c<25) {
        	try {
        		Object connection = getConnection.get(getPlayerHandle.invoke(player));
                Object manager = getManager.get(connection);

                channelLookup.put(player.getName(), channel = getChannel.get(manager));
        	} catch(Exception ex) {}
        	c++;
        }

        return channel;
    }

    private void close() {
        if (!closed) {
            closed = true;

            // Remove our handlers
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                // No need to guard against this if we're closing
                Channel channel = getChannel(player);
                if (!closed) {
                    uninjectedChannels.add(channel);
                }

                // See ChannelInjector in ProtocolLib, line 590
                channel.eventLoop().execute(() -> channel.pipeline().remove(handlerName));
            }

            // Clean up Bukkit
            HandlerList.unregisterAll(listener);
            unregisterChannelHandler();
        }
    }

    private final class PacketInterceptor extends ChannelDuplexHandler {
        // Updated by the login event
        public volatile Player player;

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            // Intercept channel
            final Channel channel = ctx.channel();
            handleLoginStart(channel, msg);

            try {
                msg = onPacketInAsync(player, msg);
            } catch (Exception exception) {}

            if (msg != null) {
                super.channelRead(ctx, msg);
            }
        }

        private void handleLoginStart(Channel channel, Object packet) {
            if (PACKET_LOGIN_IN_START.isInstance(packet)) {
                Object profile = getGameProfile.get(packet);
                channelLookup.put((String) Reflections.getMethod(profile.getClass(), "getName").invoke(profile), channel);
            }
        }
    }
}