package paul05.de.QuestMaker.Quest;

import java.io.File;
import java.util.ArrayList;
import java.util.spi.CurrencyNameProvider;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import net.milkbowl.vault.Vault;
import net.milkbowl.vault.VaultEco;
import net.milkbowl.vault.economy.Economy;
import paul05.de.QuestMaker.Config;
import paul05.de.QuestMaker.main;
import paul05.de.QuestMaker.Exceptions.AttributeMissingException;
import paul05.de.QuestMaker.Exceptions.PluginMissingException;
import paul05.de.QuestMaker.Quest.Types.TaskType;

public class Task {
	private File f;
	private JSONObject o;
	
	private TaskType type;
	
	//GO
	private int rad;
	private Location middle;
	//BREAK
	private ArrayList<BlockQuest> blocks = new ArrayList<>();
	//KILL
	private boolean isPlayer;
	private EntityType enttype;
	private String entname;
	//GET
	private ArrayList<ItemStack> items = new ArrayList<>();
	//MONEY
	private double value;
	
	public Task(File f, Config c) throws PluginMissingException {
		this.f = f;
		this.o = c.toJSON();
		load(c);
	}
	
	public TaskType getType() {
		return type;
	}
	
	public int getRadius() {
		return rad;
	}
	
	public Location getMiddle() {
		return middle;
	}
	
	public ArrayList<BlockQuest> getBlocks() {
		return blocks;
	}
	
	public boolean isPlayer() {
		return isPlayer;
	}
	
	public EntityType getEntityType() {
		return enttype;
	}
	
	public String getEntityName() {
		return entname;
	}
	
	public ArrayList<ItemStack> getItems() {
		return items;
	}
	
	public double getValue() {
		return value;
	}
	
	private void load(Config c) {
		try {
			type = TaskType.valueOf(((String) load(c, "type")).toUpperCase());
			switch (type) {
			case GO:
				rad = ((Long) load(c, "radius")).intValue();
				middle = getLoc(new Config((JSONObject) load(c, "loc")));
				break;
			case BREAK:
				for (Object o : (JSONArray) load(c, "blocks")) {
					blocks.add(new BlockQuest(f, new Config((JSONObject) o)));
				}
				break;
			case KILL:
				Config ent = new Config((JSONObject) load(c, "entity"));
				isPlayer = (boolean) load(ent, "isplayer");
				if (!isPlayer) {
					enttype = EntityType.fromName((String) load(ent, "type"));
				}
				if (ent.get("name") != null) {
					entname = (String) ent.get("name");
				}
				break;
			case GET:
				for (Object o : (JSONArray) load(c, "items")) {
					items.add(loadItem(new Config((JSONObject) o)));
				}
				break;
			case MONEY:
				if (hasPlugin("Vault") != null) {
					value = (double) load(c, "value");
				} else {
					main.safeError(new PluginMissingException("Vault"));
				}
				break;
			}
		} catch (Exception e) {
			main.safeError(e);
		}
	}
	
	private Plugin hasPlugin(String name) throws PluginMissingException {
		Plugin p = Bukkit.getPluginManager().getPlugin(name);
		if (p == null) {
			throw new PluginMissingException(name);
		}
		return p;
	}

	@Override
	public String toString() {
		switch (type) {
		case GO:
			return "GO: "+middle.toString()+", R: "+rad;
		case BREAK:
			return "BREAK: "+blocks.toString();
		case GET:
			return "GET: "+items.toString();
		case KILL:
			return "KILL: Player:"+isPlayer+", Type: "+enttype+", Name: "+entname;
		case MONEY:
			return "MONEY: Value:"+value;
		}
		return null;
	}
	
	private Location getLoc(Config c) throws AttributeMissingException {
		try {
			World world = Bukkit.getWorld((String) load(c, "world"));
			double x = (double) load(c, "x");
			double y = (double) load(c, "y");
			double z = (double) load(c, "z");
			return new Location(world, x, y, z);
		} catch (AttributeMissingException e) {
			throw e;
		}
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
	
	public static Task fromByte(String o) throws PluginMissingException {
		return new Task(new File(""), new Config(o));
	}

	public JSONObject asByte() {
		return o;
	}

}
