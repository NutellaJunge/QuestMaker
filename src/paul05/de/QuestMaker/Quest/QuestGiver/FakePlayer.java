package paul05.de.QuestMaker.Quest.QuestGiver;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.craftbukkit.libs.org.apache.commons.lang3.ThreadUtils;
import org.bukkit.craftbukkit.v1_14_R1.CraftServer;
import org.bukkit.craftbukkit.v1_14_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftPlayer;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import net.minecraft.server.v1_14_R1.DataWatcher;
import net.minecraft.server.v1_14_R1.DataWatcherRegistry;
import net.minecraft.server.v1_14_R1.Entity;
import net.minecraft.server.v1_14_R1.EntityInsentient;
import net.minecraft.server.v1_14_R1.EntityLiving;
import net.minecraft.server.v1_14_R1.EntityPlayer;
import net.minecraft.server.v1_14_R1.EnumMoveType;
import net.minecraft.server.v1_14_R1.IChatBaseComponent;
import net.minecraft.server.v1_14_R1.MinecraftServer;
import net.minecraft.server.v1_14_R1.PacketPlayOutEntityHeadRotation;
import net.minecraft.server.v1_14_R1.PacketPlayOutEntityMetadata;
import net.minecraft.server.v1_14_R1.PacketPlayOutEntitySound;
import net.minecraft.server.v1_14_R1.PacketPlayOutEntity.PacketPlayOutEntityLook;
import net.minecraft.server.v1_14_R1.PacketPlayOutEntity.PacketPlayOutRelEntityMove;
import net.minecraft.server.v1_14_R1.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_14_R1.PacketPlayOutEntityTeleport;
import net.minecraft.server.v1_14_R1.PacketPlayOutNamedEntitySpawn;
import net.minecraft.server.v1_14_R1.PacketPlayOutPlayerInfo;
import net.minecraft.server.v1_14_R1.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;
import net.minecraft.server.v1_14_R1.PacketPlayOutPlayerListHeaderFooter;
import paul05.de.QuestMaker.SkinLoader;
import paul05.de.QuestMaker.main;
import paul05.de.QuestMaker.Events.FakePlayerClickListener;
import net.minecraft.server.v1_14_R1.PlayerConnection;
import net.minecraft.server.v1_14_R1.PlayerInteractManager;
import net.minecraft.server.v1_14_R1.Vec3D;
import net.minecraft.server.v1_14_R1.WorldServer;

public class FakePlayer {
	
	private EntityPlayer ep;
	private boolean isMove = false;
	private boolean stop = false;
	private boolean live;
	private GameProfile gp;
	
	private ArrayList<FakePlayerClickListener> clickListeners = new ArrayList<>();
	
	private int t;
	
	public FakePlayer(String playername, String name, Location loc) {
		gp = new GameProfile(UUID.randomUUID(), name);
		setSkin(playername);
		MinecraftServer minecraftserver = ((CraftServer)Bukkit.getServer()).getServer();
		WorldServer worldserver = ((CraftWorld)loc.getWorld()).getHandle();
		EntityPlayer ep = new EntityPlayer(minecraftserver, worldserver, gp, new PlayerInteractManager(worldserver));
		ep.sentListPacket = false;
		this.ep = ep;
		
		spawn();
		
		teleport(loc);
	}
	
	public int getEntityID() {
		return ep.getId();
	}
	
	public void addClickListener(FakePlayerClickListener l) {
		clickListeners.add(l);
	}
	
	public ArrayList<FakePlayerClickListener> getAllClickListeners() {
		return clickListeners;
	}
	
	private void setSkin(String playername) {
		gp.getProperties().put("textures", SkinLoader.getSkin(playername));
	}
	
	private void spawn() {
		for (Player p : Bukkit.getOnlinePlayers()) {
			PlayerConnection con = ((CraftPlayer) p).getHandle().playerConnection;
			con.sendPacket(new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.REMOVE_PLAYER, ((CraftPlayer)p).getHandle()));
			con.sendPacket(new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.ADD_PLAYER, ep));
			con.sendPacket(new PacketPlayOutNamedEntitySpawn(ep));
			con.sendPacket(new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.ADD_PLAYER, ((CraftPlayer)p).getHandle()));
		}
		live = true;
	}
	
	public void spawn(Player p) {
		PlayerConnection con = ((CraftPlayer) p).getHandle().playerConnection;
		DataWatcher watcher = ep.getDataWatcher();
        Byte b = 0x01 | 0x02 | 0x04 | 0x08 | 0x10 | 0x20 | 0x40;
        watcher.set(DataWatcherRegistry.a.a(15), (byte) b);
		con.sendPacket(new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.ADD_PLAYER, ep));
		con.sendPacket(new PacketPlayOutNamedEntitySpawn(ep));
		con.sendPacket(new PacketPlayOutEntityMetadata(ep.getId(), watcher, true));
	}
	
	public void teleport(Location loc) {
		ep.setLocation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
		for (Player gp : Bukkit.getOnlinePlayers()) {
			PlayerConnection c = ((CraftPlayer)gp).getHandle().playerConnection;
			c.sendPacket(new PacketPlayOutEntityTeleport(ep));
			c.sendPacket(new PacketPlayOutEntityLook(ep.getId(), getFixRotation(loc.getYaw()), getFixRotation(loc.getPitch()), true));
			c.sendPacket(new PacketPlayOutEntityHeadRotation(ep, getFixRotation(loc.getYaw())));
		}
	}
	
	public void walk(Location loc) {
		Bukkit.getScheduler().cancelTask(t);
		t = Bukkit.getScheduler().runTaskTimerAsynchronously(main.getInstance(), new Runnable() {
			
			@Override
			public void run() {
				if (!stop) {
					Location origin = new Location(ep.getWorld().getWorld(), ep.locX, ep.locY, ep.locZ);
					Vector target = loc.toVector();
					origin.setDirection(target.subtract(origin.toVector()));
					Vector increase = origin.getDirection();
					
					float yaw = getLookAtYaw(increase)+100;
	
					ep.move(EnumMoveType.SELF, new Vec3D(increase.getX() / 6, increase.getY() / 6, increase.getZ() / 6));
					teleport(new Location(ep.getWorld().getWorld(), ep.locX, ep.locY, ep.locZ, yaw, ep.pitch));
					if (((ep.locX >= loc.getX()-0.6 && ep.locX <= loc.getX()+0.6) && (ep.locY >= loc.getY()-0.6 && ep.locY <= loc.getY()+0.02) && (ep.locZ >= loc.getZ()-0.6 && ep.locZ <= loc.getZ()+0.6))) {
						Bukkit.getScheduler().cancelTask(t);
						isMove = false;
					}
				}
			}
		}, 0, 1).getTaskId();
		isMove = true;
	}
	
	public boolean isStop() {
		return stop;
	}
	
	public void setStop(boolean stop) {
		this.stop = stop;
	}
	
	public boolean isMove() {
		return isMove;
	}
	
	public boolean isLiving() {
		return live;
	}
	
	public void destroy() {
		live = false;
		for (Player p : Bukkit.getOnlinePlayers()) {
			PlayerConnection con = ((CraftPlayer) p).getHandle().playerConnection;
			con.sendPacket(new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.REMOVE_PLAYER, ep));
			con.sendPacket(new PacketPlayOutEntityDestroy(ep.getId()));
		}
	}
	
	public static float getLookAtYaw(Vector motion) {
	        double dx = motion.getX();
	        double dz = motion.getZ();
	        double yaw = 0;
	        // Set yaw
	        if (dx != 0) {
	            // Set yaw start value based on dx
	            if (dx < 0) {
	                yaw = 1.5 * Math.PI;
	            } else {
	                yaw = 0.5 * Math.PI;
	            }
	            yaw -= Math.atan(dz / dx);
	        } else if (dz < 0) {
	            yaw = Math.PI;
	        }
	        return (float) (-yaw * 180 / Math.PI - 90);
	    }
	
	public byte getFixRotation(float yawpitch){
        return (byte) ((int) (yawpitch * 256.0F / 360.0F));
	}
}
