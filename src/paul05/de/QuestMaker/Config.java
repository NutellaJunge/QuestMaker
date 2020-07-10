package paul05.de.QuestMaker;

import java.awt.image.BufferedImageFilter;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.xml.transform.sax.TransformerHandler;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.stream.JsonReader;

import paul05.de.QuestMaker.Exceptions.AttributeMissingException;
import paul05.de.QuestMaker.Exceptions.JSONSyntaxException;
import paul05.de.QuestMaker.Quest.Quest;

public class Config {
	
	private JSONObject c;
	private File f;

	public Config(File f) throws JSONSyntaxException, IOException {
		c = readJSON(f);
	}
	
	public Config(JSONObject o) {
		c = o;
	}
	
	public void print(String name) {
		System.out.println(name+": ");
		System.out.println(c.toJSONString());
	}
	
	@Override
	public String toString() {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		return gson.toJson(c);
	}
	
	public String toJSONString() {
		return c.toJSONString();
	}
	
	public void set(String st, Object o) {
		c.put(st.toLowerCase(), o);
		save();
	}
	
	public Config(String string) {
		try {
			JSONParser p = new JSONParser();
			c = (JSONObject) p.parse(string);
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
	
	public boolean contains(String s) {
		return c.containsKey(s);
	}
	
	public void save() {
		try {
			BufferedWriter w = new BufferedWriter(new FileWriter(f));
			w.write(toString());
			w.flush();
			w.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public File getFile() {
		return f;
	}
	
	public Object get(String st) {
		return c.get(st.toLowerCase());
	}

	private JSONObject readJSON(File f) throws JSONSyntaxException, IOException {
		try {
			FileReader r = new FileReader(f);
			JSONParser p = new JSONParser();
			JSONObject o = (JSONObject) p.parse(r);
			this.f = f;
			return o;
		} catch (ParseException e) {
			throw new JSONSyntaxException(f, e.getUnexpectedObject(), e.getPosition());
		}
	}

	public void saveEntity(Location locc, Quest q) {
		JSONArray entitys;
		if (!c.containsKey("entitys")) {
			entitys = new JSONArray();
		} else {
			entitys = (JSONArray) get("entitys");
		}
		JSONObject e = new JSONObject();
		JSONObject loc = new JSONObject();
		loc.put("world", locc.getWorld().getName());
		loc.put("x", locc.getX());
		loc.put("y", locc.getY());
		loc.put("z", locc.getZ());
		loc.put("yaw", locc.getYaw());
		loc.put("pitch", locc.getPitch());
		e.put(q.getId(), loc);
		entitys.add(e);
		save();
	}

	public static Location getLocation(JSONObject ob, Object name) {
		World world = Bukkit.getWorld((String) ob.get("world"));
		double x = (double) ob.get("x");
		double y = (double) ob.get("y");
		double z = (double) ob.get("z");
		double yaw = (double) ob.get("yaw");
		double pitch = (double) ob.get("pitch");
		return new Location(world, x, y, z, (float) yaw, (float) pitch);
	}

	public JSONObject toJSON() {
		return c;
	}

}
