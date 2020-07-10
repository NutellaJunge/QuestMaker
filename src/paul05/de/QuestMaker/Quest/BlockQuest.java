package paul05.de.QuestMaker.Quest;

import java.io.File;
import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import paul05.de.QuestMaker.Config;
import paul05.de.QuestMaker.main;
import paul05.de.QuestMaker.Exceptions.AttributeMissingException;

public class BlockQuest {
	
	private File f;
	private JSONObject o;
	
	private Material type;
	private Location loc;

	public BlockQuest(File f, Config c) {
		this.f = f;
		this.o = c.toJSON();
		try {
			type = Material.valueOf(((String) load(c, "type")).toUpperCase());
			if (c.get("loc") != null) {
				loc = getLoc(new Config((JSONObject) c.get("loc")));
			}
		} catch (AttributeMissingException e) {
			main.safeError(e);
		}
	}
	
	public Material getType() {
		return type;
	}
	
	public Location getLoc() {
		return loc;
	}
	
	@Override
	public String toString() {
		return "BlockQuest:{Type: "+type+", Loc: "+loc+"}";
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
	
	public JSONObject asByte() {
		return o;
	}
	
	public static BlockQuest fromByte(String o) {
		return new BlockQuest(new File(""), new Config(o));
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
