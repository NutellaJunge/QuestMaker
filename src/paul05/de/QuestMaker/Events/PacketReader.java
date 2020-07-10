package paul05.de.QuestMaker.Events;


import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import net.minecraft.server.v1_14_R1.EnumHand;
import net.minecraft.server.v1_14_R1.Packet;
import paul05.de.QuestMaker.main;
import paul05.de.QuestMaker.Quest.QuestGiver.FakePlayer;
import paul05.de.QuestMaker.Quest.QuestGiver.QuestGiver;

import java.lang.reflect.Field;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class PacketReader {

	Player player;
	Channel channel;
	
	public PacketReader(Player player) {
		this.player = player;
	}
	
	public void inject(){
		CraftPlayer cPlayer = (CraftPlayer)this.player;
		channel = cPlayer.getHandle().playerConnection.networkManager.channel;
		channel.pipeline().addAfter("decoder", "PacketInjector", new MessageToMessageDecoder<Packet<?>>() {
			@Override 
			protected void decode(ChannelHandlerContext arg0, Packet<?> packet,List<Object> arg2)
				throws Exception {
					arg2.add(packet);
					readPacket(packet);
				}
			}
		);
	}
	
	public void uninject(){
		if(channel.pipeline().get("PacketInjector") != null){
			channel.pipeline().remove("PacketInjector");
		}
	}
	

	public void readPacket(Packet<?> packet) {
		if (packet.getClass().getSimpleName().equalsIgnoreCase("PacketPlayInUseEntity")){
			int id = (Integer)getValue(packet, "a");
			if (getValue(packet, "action").toString().toUpperCase().equals("INTERACT")) {
				if (getValue(packet, "d") == EnumHand.MAIN_HAND) {
					for (FakePlayer fp : QuestGiver.getFakePlayers().keySet()) {
						if (fp.getEntityID() == id) {
							for (FakePlayerClickListener listener : fp.getAllClickListeners()) {
								Bukkit.getScheduler().runTaskLater(main.getInstance(), new Runnable() {
									
									@Override
									public void run() {
										listener.rightClickMain(player);										
									}
								}, 1);
							}
						}
					}
				}
			}
		}
	}
	

	public void setValue(Object obj,String name,Object value){
		try{
		Field field = obj.getClass().getDeclaredField(name);
		field.setAccessible(true);
		field.set(obj, value);
		}catch(Exception e){}
	}
	
	public Object getValue(Object obj,String name){
		try{
		Field field = obj.getClass().getDeclaredField(name);
		field.setAccessible(true);
		return field.get(obj);
		}catch(Exception e){}
		return null;
	}
	
}