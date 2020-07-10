package paul05.de.QuestMaker.Quest.QuestGiver;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.craftbukkit.libs.org.apache.commons.lang3.Validate;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import paul05.de.QuestMaker.Config;
import paul05.de.QuestMaker.main;
import paul05.de.QuestMaker.Exceptions.AttributeMissingException;
import paul05.de.QuestMaker.Exceptions.JSONSyntaxException;

public class Path {

	private File f;
	private int index = 0;
	
	private ArrayList<Location> locs = new ArrayList<>();
	private World world;

	public Path(File file) {
		try {
			this.f = file;
			Config c = new Config(file);
			load(c);
		} catch (JSONSyntaxException | IOException e) {
			main.safeError(e);
		}
	}
	
	public Path(Path p) {
		locs = p.getAllLocs();
		f = p.getFile();
	}

	public Location next() {
		Location loc = locs.get(index);
		index++;
		if (locs.size() <= index) {
			index = 0;
		}
		return loc;
	}
	
	public File getFile() {
		return f;
	}
	
	private void load(Config c) {
		try {
			world = Bukkit.getWorld((String) load(c, "world"));
			for (Object o : (JSONArray) load(c, "path")) {
				locs.add(getLoc(new Config((JSONObject)o)));
			}
			main.logg("	"+f.getName()+" was loaded.");
		} catch (AttributeMissingException e) {
			main.logg("	"+f.getName()+" was loaded with error.");
			main.safeError(e);
		}
	}
	
	public ArrayList<Location> getAllLocs() {
		return locs;
	}
	
	public Location getLoc(Config c) throws AttributeMissingException {
		try {
			double x = (double) load(c, "x");
			double y = (double) load(c, "y");
			double z = (double) load(c, "z");
			double yaw = (double) load(c, "yaw");
			double pitch = (double) load(c, "pitch");
			return new Location(world, x, y, z, (float) yaw, (float) pitch);
		} catch (AttributeMissingException e) {
			throw e;
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

	public Path getChild() {
		return new Path(this);
	}
	
	private int t;
	private int i;
	private Location last;
	
	public void show(Player p) {
		i = 1;
		last = next();
		t = Bukkit.getScheduler().runTaskTimerAsynchronously(main.getInstance(), new Runnable() {
			
			@Override
			public void run() {
				Location loc = next();
				drawLine(p, loc, last, 0.1);
				if (i >= 2*5*30) {
					Bukkit.getScheduler().cancelTask(t);
					return;
				}
				last = loc;
				i++;
			}
		}, 2, 0).getTaskId();
	}
	
	public void drawLine(Player p, Location point1, Location point2, double space) {
	    World world = point1.getWorld();
	    Validate.isTrue(point2.getWorld().equals(world), "Lines cannot be in different worlds!");
	    double distance = point1.distance(point2);
	    Vector p1 = point1.toVector();
	    Vector p2 = point2.toVector();
	    Vector vector = p2.clone().subtract(p1).normalize().multiply(space);
	    double length = 0;
	    for (; length < distance; p1.add(vector)) {
			p.spawnParticle(Particle.FLAME, p1.getX(), p1.getY()+0.2, p1.getZ(), 0);
	        length += space;
	    }
	}
}
