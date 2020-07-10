package paul05.de.QuestMaker.Quest.Player;

import java.io.File;
import java.util.ArrayList;

import javax.swing.text.html.parser.Entity;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.google.gson.JsonArray;

import net.minecraft.server.v1_14_R1.IFluidContainer;
import paul05.de.QuestMaker.Config;
import paul05.de.QuestMaker.main;
import paul05.de.QuestMaker.Exceptions.AttributeMissingException;
import paul05.de.QuestMaker.Quest.BlockQuest;
import paul05.de.QuestMaker.Quest.Quest;
import paul05.de.QuestMaker.Quest.Task;
import paul05.de.QuestMaker.Quest.Types.TaskType;

public class RunningQuest implements Listener {

	private Player p;
	private Quest q;
	private ArrayList<Task> complete = new ArrayList<>();
	private ArrayList<BlockQuest> completeBlock = new ArrayList<>();
	private ArrayList<ItemStack> completeItem = new ArrayList<>();
	private boolean complet = false;
	private boolean collected = false;

	public RunningQuest(Quest q, Player p) {
		Bukkit.getPluginManager().registerEvents(this, main.getInstance());
		this.q = q;
		this.p = p;
		save();
	}
	
	@EventHandler
	private void testQuestCompletion(PlayerMoveEvent e) {
		if (!isComplet()) {
			Player p = e.getPlayer();
			if (isComplete()) {
				setComplete();
			} else {
				for (Task task : q.getTasks()) {
					if (!complete.contains(task)) {
						switch (task.getType()) {
						case GO:
							if (testLocation(p.getLocation(), task.getMiddle(), task.getRadius())) {
								completeTask(task);
							}
							break;
						case GET:
							if (completeItem.size() < task.getItems().size()) {
								for (ItemStack item : task.getItems()) {
									if (!completeItem.contains(item)) {
										for (ItemStack invItem : p.getInventory().all(item.getType()).values()) {
											if (invItem.getAmount() >= item.getAmount()) {
												if (item.hasItemMeta()) {
													if (item.getItemMeta().equals(invItem.getItemMeta())) {
														completeItem.add(item);
													}
												} else {
													completeItem.add(item);
												}
											}
										}
									}
								}
							} else {
								completeTask(task);
							}
							break;
						case MONEY:
							double balance = main.setupEconomy().getBalance(Bukkit.getOfflinePlayer(p.getUniqueId()));
							if (balance >= task.getValue()) {
								completeTask(task);
							}
							break;
						}
					}
				}
				save();
			}
		}
	}
	
	@EventHandler
	private void onBlockBreak(BlockBreakEvent e) {
		if (!isComplet()) {
			Player p = e.getPlayer();
			Block b = e.getBlock();
			for (Task task : q.getTasks()) {
				if (!complete.contains(task)) {
					if (task.getType() == TaskType.BREAK) {
						for (BlockQuest bq : task.getBlocks()) {
							if (!completeBlock.contains(bq)) {
								if (bq.getType() == b.getType()) {
									if (bq.getLoc() != null) {
										if (testLocation(b.getLocation(), bq.getLoc(), 1)) {
											completeBlock.add(bq);
										}
									} else {
										completeBlock.add(bq);
									}
								}
							}
						}
						if (completeBlock.size() == task.getBlocks().size()) {
							completeTask(task);
						}
					}
				}
			}
		}
	}
	
	@EventHandler
	private void onEntityDeath(EntityDeathEvent e) {
		if (!isComplet()) {
			LivingEntity ent = e.getEntity();
			if (ent.getKiller() instanceof Player) {
				Player p = ent.getKiller();
				if (p.equals(this.p)) {
					for (Task task : q.getTasks()) {
						if (!complete.contains(task)) {
							if (task.getType() == TaskType.KILL) {
								if (task.isPlayer()) {
									if (ent instanceof Player) {
										if (task.getEntityName() != null) {
											if (task.getEntityName().equals(((Player) ent).getName())) {
												completeTask(task);
											}
										} else {
											completeTask(task);
										}
									}
								} else {
									if (task.getEntityType() == ent.getType()) {
										if (task.getEntityName() != null) {
											if (task.getEntityName().equals(ent.getName())) {
												completeTask(task);
											}
										} else {
											completeTask(task);
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}
	
	public boolean isCollected() {
		return collected;
	}
	
	public void setCollected() {
		this.collected = true;
		save();
	}
	
	public int getComplete() {
		return complete.size();
	}
	
	private void completeTask(Task task) {
		complete.add(task);
		p.sendMessage("§aYou have completed: "+task.toString());
	}
	
	private boolean testLocation(Location testLoc, Location middle, int radius) {
		if (testLoc.getX() < middle.getBlockX()+radius && testLoc.getX() > middle.getBlockX()-radius) {
			if (testLoc.getY() < middle.getBlockY()+radius && testLoc.getY() > middle.getBlockY()-radius) {
				if (testLoc.getZ() < middle.getBlockZ()+radius && testLoc.getZ() > middle.getBlockZ()-radius) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean isComplete() {
		if (complete.size() >= q.getTasks().size()) {
			return true;
		}
		return false;
	}
	
	public void setComplete() {
		p.sendMessage("§aQuest: §2"+q.getName()+"§a is completed.");
		this.complet = true;
		save();
	}
	
	private void save() {
		JSONObject qs = (JSONObject) main.data.get("quests");
		if (qs != null) {
			qs.put(p.getUniqueId().toString(), asJSON());
			main.data.set("quests", qs);
			main.data.save();
		}
	}
	
	public static RunningQuest RunningQuestfromJSON(Config c, Player p) {
		try {
			Quest q = Quest.getQuestbyId(((Long) load(c, "id")).intValue());
			if (!Quest.isActive(p, q)) {
				RunningQuest r = q.startQuest(p);
				if ((boolean) load(c, "complete")) {
					r.setComplete();
					if ((boolean) load(c, "collected")) {
						r.setCollected();
					}
				} else {
					Config data = new Config((JSONObject) load(c, "data"));
					ArrayList<Task> complete = new ArrayList<>();
					ArrayList<BlockQuest> block = new ArrayList<>();
					ArrayList<ItemStack> item = new ArrayList<>();
					for (Object o : (JSONArray) load(data, "complete")) {
						Task t = r.getTask((JSONObject) o);
						if (t != null) {
							complete.add(t);
						}
					}
					for (Object o : (JSONArray) load(data, "blocks")) {
						BlockQuest b = r.getBlock((JSONObject) o);
						if (b != null) {
							block.add(b);
						}
					}
					for (Object o : (JSONArray) load(data, "items")) {
						ItemStack i = r.getItem(loadItem(new Config((JSONObject) o)));
						if (i != null) {
							item.add(i);
						}
					}
					p.sendMessage(complete.toString());
					p.sendMessage(block.toString());
					p.sendMessage(item.toString());
					r.complete = complete;
					r.completeBlock = block;
					r.completeItem = item;
				}
				return r;
			}
		} catch (Exception e) {
			main.safeError(e);
		}
		return null;
	}

	private ItemStack getItem(ItemStack itemStack) {
		for (Task task : q.getTasks()) {
			if (task.getType() == TaskType.GET) {
				for (ItemStack item : task.getItems()) {
					if (item.equals(itemStack)) {
						return item;
					}
				}
			}
		}
		return null;
	}

	private BlockQuest getBlock(JSONObject o) {
		for (Task task : q.getTasks()) {
			if (task.getType() == TaskType.BREAK) {
				for (BlockQuest block : task.getBlocks()) {
					if (block.asByte().equals(o)) {
						return block;
					}
				}
			}
		}
		return null;
	}

	private static ItemStack loadItem(Config c) throws AttributeMissingException {
		ItemStack item = new ItemStack(Material.valueOf(((String) load(c, "type")).toUpperCase()), ((Long) load(c, "amount")).intValue());
		if (c.get("damage") != null) {
			short damage = ((Long) c.get("damage")).shortValue();
			if (damage != 0) {
				item.setDurability(damage);
			}
		}
		ItemMeta meta = item.getItemMeta();
		String name = (String) c.get("name");
		if (name != null) {
			meta.setDisplayName(name);
		}
		ArrayList<String> lore = new ArrayList<>();
		if (c.get("lore") != null) {
			for (Object o : (JSONArray) c.get("lore")) {
				lore.add((String) o);
			}
			if (!lore.isEmpty()) {
				meta.setLore(lore);
			}
		}
		item.setItemMeta(meta);
		if (c.get("enchanments") != null) {
			for (Object o : (JSONArray) c.get("enchantments")) {
				Config nc = new Config((JSONObject) o);
				item.addEnchantment(Enchantment.getByName(((String) load(nc, "type")).toUpperCase()), ((Long) load(nc, "level")).intValue());
				
			}
		}
		return item;
	}
	
	private static Object load(Config c, String s) throws AttributeMissingException {
		Object o = c.get(s);
		if (o != null) {
			return o;
		} else {
			String from;
			if (c.getFile() == null) {
				from = c.toJSONString();
			} else {
				from = c.getFile().getName();
			}
			throw new AttributeMissingException(main.data.getFile(), s, from);
		}
	}
	
	private Task getTask(JSONObject o) {
		for (Task task : q.getTasks()) {
			if (task.asByte().equals(o)) {
				return task;
			}
		}
		return null;
	}

	private JSONObject asJSON() {
		JSONObject o = new JSONObject();
		o.put("id", q.getId());
		JSONObject data = new JSONObject();
		if (!complet) {
			data.put("complete", getCompleteTasksasJSON());
			data.put("blocks", getCompleteBlocksasJSON());
			data.put("items", getCompleteItemsasJSON());
		}
		o.put("data", data);
		o.put("complete", complet);
		o.put("collected", collected);
		return o;
	}

	private JSONArray getCompleteItemsasJSON() {
		JSONArray a = new JSONArray();
		for (ItemStack item : completeItem) {
			JSONObject o = new JSONObject();
			o.put("type", item.getType().name());
			o.put("amount", item.getAmount());
			o.put("damage", item.getDurability());
			if (item.hasItemMeta()) {
				if (item.getItemMeta().hasDisplayName()) {
					o.put("name", item.getItemMeta().getDisplayName());
				}
				if (item.getItemMeta().hasLore()) {
					JSONArray l = new JSONArray();
					for (String s : item.getItemMeta().getLore()) {
						l.add(s);
					}
					o.put("lore", l);
				}
			}
			JSONArray en = new JSONArray();
			for (Enchantment e : item.getEnchantments().keySet()) {
				JSONObject ec = new JSONObject();
				ec.put("type", e.getName());
				en.add(ec);
			}
			o.put("enchantments", en);
			a.add(o);
		}
		return a;
	}

	private JSONArray getCompleteBlocksasJSON() {
		JSONArray a = new JSONArray();
		for (BlockQuest b : completeBlock) {
			a.add(b.asByte());
		}
		return a;
	}

	private JSONArray getCompleteTasksasJSON() {
		JSONArray a = new JSONArray();
		for (Task t : complete) {
			a.add(t.asByte());
		}
		return a;
	}

	public boolean isComplet() {
		return complet;
	}
	
	public Player getPlayer() {
		return p;
	}
	
	public Quest getQuest() {
		return q;
	}
}
