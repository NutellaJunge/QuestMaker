package paul05.de.QuestMaker.Quest.QuestGiver;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftCreature;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftPlayer;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import net.minecraft.server.v1_14_R1.EntityInsentient;
import net.minecraft.server.v1_14_R1.EnumMoveType;
import net.minecraft.server.v1_14_R1.Navigation;
import net.minecraft.server.v1_14_R1.NavigationAbstract;
import net.minecraft.server.v1_14_R1.PathEntity;
import net.minecraft.server.v1_14_R1.Vec3D;
import paul05.de.QuestMaker.main;
import paul05.de.QuestMaker.EntityUtils.EntityUtils;
import paul05.de.QuestMaker.Events.FakePlayerClickListener;
import paul05.de.QuestMaker.Quest.Quest;
import paul05.de.QuestMaker.Quest.Types.QuestGiverInventoryType;

public class QuestGiver {
	
	private static HashMap<LivingEntity, QuestGiver> entities = new HashMap<>();
	private static HashMap<FakePlayer, QuestGiver> fakePlayers = new HashMap<>();
	private static ArrayList<Integer> moves = new ArrayList<>();
	
	private int t;
	private Path p;
	private Quest q;
	private FakePlayer fp;
	private LivingEntity ent;
	
	public QuestGiver(Location loc, Quest q) {
		this.q = q;
		p = q.getPath().getChild();
		if (q.isGiverisPlayer()) {
			fp = new FakePlayer(q.getGiverSkinPlayerName(), q.getGiverName(), loc);
			main.data.saveEntity(loc, q);
			fp.addClickListener(new FakePlayerClickListener() {
				
				@Override
				public void rightClickMain(Player p) {
					rightClick(p);
				}
			});
			if (q.isGiverWalk()) {
				t = Bukkit.getScheduler().runTaskTimerAsynchronously(main.getInstance(), new Runnable() {
					
					@Override
					public void run() {
						if (fp.isLiving()) {
							if (!fp.isMove()) {
								fp.walk(p.next());
							}
						} else {
							Bukkit.getScheduler().cancelTask(t);
						}
					}
				}, 0, 20).getTaskId();
				moves.add(t);
			}
			fakePlayers.put(fp, this);
		} else {
			ent = (LivingEntity) loc.getWorld().spawnEntity(loc, q.getGiverType());
			ent.setAI(false);
			ent.setSilent(true);
			main.data.saveEntity(loc, q);
			if (q.isGiverWalk()) {
				EntityUtils.walk(ent, p.next());
				t = Bukkit.getScheduler().runTaskTimer(main.getInstance(), new Runnable() {
					
					@Override
					public void run() {
						if (!ent.isDead()) {
							if (!EntityUtils.getWalker(ent).isMove()) {
								EntityUtils.walk(ent, p.next());
							}
						} else {
							Bukkit.getScheduler().cancelTask(t);
						}
					}
				}, 0, 20).getTaskId();
				moves.add(t);
			}
			entities.put(ent, this);
		}
	}
	
	public boolean isStop() {
		if (fp != null) {
			return fp.isStop();
		} else {
			return EntityUtils.getWalker(ent).isStop();
		}
	}
	
	public Quest getQest() {
		return q;
	}
	
	public LivingEntity getEntity() {
		return ent;
	}
	
	public void setStop(boolean stop) {
		if (fp != null) {
			fp.setStop(stop);
		} else {
			EntityUtils.getWalker(ent).setStop(stop);
		}
	}
	
	public void rightClick(Player p) {
		if (!Quest.isActive(p, q)) {
			new QuestGiverInventory(this, QuestGiverInventoryType.Quest).open(p);
		} else {
			if (!Quest.getActive(p, q).isComplet()) {
				new QuestGiverInventory(this, QuestGiverInventoryType.Progress).open(p);
			} else {
				if (!Quest.getActive(p, q).isCollected()) {
					new QuestGiverInventory(this, QuestGiverInventoryType.Reward).open(p);
				} else {
					new QuestGiverInventory(this, QuestGiverInventoryType.Collected).open(p);
				}
			}
		}
	}
	
	public FakePlayer getFakePlayer() {
		return fp;
	}
	
	public static HashMap<LivingEntity, QuestGiver> getAllEntities() {
		return entities;
	}
	
	public static HashMap<FakePlayer, QuestGiver> getFakePlayers() {
		return fakePlayers;
	}
	
	public QuestGiver(Location loc, Quest q, boolean b) {
		this.q = q;
		p = q.getPath().getChild();
		if (q.isGiverisPlayer()) {
			fp = new FakePlayer(q.getGiverSkinPlayerName(), q.getGiverName(), loc);
			fp.addClickListener(new FakePlayerClickListener() {
				
				@Override
				public void rightClickMain(Player p) {
					rightClick(p);
				}
			});
			if (q.isGiverWalk()) {
				t = Bukkit.getScheduler().runTaskTimerAsynchronously(main.getInstance(), new Runnable() {
					
					@Override
					public void run() {
						if (fp.isLiving()) {
							if (!fp.isMove()) {
								fp.walk(p.next());
							}
						} else {
							Bukkit.getScheduler().cancelTask(t);
						}
					}
				}, 0, 20).getTaskId();
				moves.add(t);
			}
			fakePlayers.put(fp, this);
		} else {
			ent = (LivingEntity) loc.getWorld().spawnEntity(loc, q.getGiverType());
			ent.setAI(false);
			ent.setSilent(true);
			if (q.isGiverWalk()) {
				EntityUtils.walk(ent, p.next());
				t = Bukkit.getScheduler().runTaskTimer(main.getInstance(), new Runnable() {
					
					@Override
					public void run() {
						if (!ent.isDead()) {
							if (!EntityUtils.getWalker(ent).isMove()) {
								EntityUtils.walk(ent, p.next());
							}
						} else {
							Bukkit.getScheduler().cancelTask(t);
						}
					}
				}, 0, 20).getTaskId();
				moves.add(t);
			}
			entities.put(ent, this);
		}
	}
}
