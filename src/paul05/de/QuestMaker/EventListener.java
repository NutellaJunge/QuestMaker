package paul05.de.QuestMaker;

import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.json.simple.JSONObject;

import paul05.de.QuestMaker.Events.PacketReader;
import paul05.de.QuestMaker.Quest.Quest;
import paul05.de.QuestMaker.Quest.Player.RunningQuest;
import paul05.de.QuestMaker.Quest.QuestGiver.FakePlayer;
import paul05.de.QuestMaker.Quest.QuestGiver.QuestGiver;

public class EventListener implements Listener {
	
	@EventHandler
	private void onJoin(PlayerJoinEvent e) {
		PacketReader reader = new PacketReader(e.getPlayer());
		reader.inject();
		main.getPacketReaders().put(e.getPlayer(), reader);
		for (FakePlayer fp : QuestGiver.getFakePlayers().keySet()) {
			fp.spawn(e.getPlayer());
		}
		if (((JSONObject)main.data.get("quests")).containsKey(e.getPlayer().getUniqueId().toString())) {
			RunningQuest r = RunningQuest.RunningQuestfromJSON(new Config((JSONObject) ((JSONObject) main.data.get("quests")).get(e.getPlayer().getUniqueId().toString())), e.getPlayer());
		}
	}
	
	@EventHandler
	private void onDeath(PlayerRespawnEvent e) {
		Bukkit.getScheduler().runTaskLater(main.getInstance(), new Runnable() {
			
			@Override
			public void run() {
				for (FakePlayer fp : QuestGiver.getFakePlayers().keySet()) {
					fp.spawn(e.getPlayer());
				}
			}
		}, 10);
	}
	
	@EventHandler
	private void QuestGiverRightClick(PlayerInteractAtEntityEvent e) {
		UUID uuid = e.getRightClicked().getUniqueId();
		if (e.getHand() == EquipmentSlot.HAND) {
			for (LivingEntity ent : QuestGiver.getAllEntities().keySet()) {
				if (uuid.equals(ent.getUniqueId())) {
					QuestGiver.getAllEntities().get(ent).rightClick(e.getPlayer());
				}
			}
		}
	}
	
	@EventHandler
	private void onAttack(EntityDamageEvent e) {
		if (e.getEntity() instanceof LivingEntity) {
			if (QuestGiver.getAllEntities().keySet().contains(e.getEntity())) {
				e.setCancelled(true);
			}
		}
	}
	
}
