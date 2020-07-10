package paul05.de.QuestMaker.Quest;

import java.io.File;
import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.google.common.collect.Lists;

import paul05.de.QuestMaker.Config;
import paul05.de.QuestMaker.main;
import paul05.de.QuestMaker.Exceptions.AttributeMissingException;
import paul05.de.QuestMaker.Quest.Types.RewardType;

public class Reward {
	
	private File f;
	
	private RewardType type;
	
	//XP
	private boolean level;
	private int value;
	//QUEST
	private int id;
	private Quest q;
	//ITEMS
	private ArrayList<ItemStack> items = new ArrayList<>();
	//MONEY
	private double moneyValue;
	
	public Reward(File f, Config c) {
		this.f = f;
		load(c);
	}
	
	private void load(Config c) {
		try {
			type = RewardType.valueOf(((String) load(c, "type")).toUpperCase());
			switch (type) {
			case XP:
				level = (boolean) load(c, "level");
				value = ((Long) load(c, "value")).intValue();
				break;
			case QUEST:
				id = ((Long) load(c, "id")).intValue();
				q = Quest.getQuestbyId(id);
				break;
			case ITEMS:
				for (Object o : (JSONArray) load(c, "items")) {
					items.add(loadItem(new Config((JSONObject) o)));
				}
				break;
			case MONEY:
				moneyValue = (double) load(c, "value");
				break;
			}
		} catch (AttributeMissingException e) {
			main.safeError(e);
		}
	}
	
	@Override
	public String toString() {
		switch (type) {
		case QUEST:
			return "Quest: "+q.getId();
		case ITEMS:
			return "Items: "+items.toString();
		case XP:
			return "XP: LEVEL:"+level+", Value: "+value;
		case MONEY:
			return "MONEY: Value: "+moneyValue;
		}
		return null;
	}

	private ItemStack loadItem(Config c) throws AttributeMissingException {
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
	
	public void reward(Player p) {
		switch (type) {
		case QUEST:
			if (q != null) {
				q.startQuest(p);
			} else {
				p.sendMessage("§cCan't find the quest with id: "+id);
			}
			break;
		case ITEMS:
			for (ItemStack item : items) {
				p.getInventory().addItem(item);
			}
			break;
		case XP:
			if (level) {
				p.giveExpLevels(value);
			} else {
				p.giveExp(value);
			}
			break;
		case MONEY:
			main.setupEconomy().depositPlayer(Bukkit.getOfflinePlayer(p.getUniqueId()), moneyValue);
			break;
		}
	}

	private Object load(Config c, String s) throws AttributeMissingException {
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
			throw new AttributeMissingException(f, s, from);
		}
	}
	
}
