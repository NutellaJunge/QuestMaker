package paul05.de.QuestMaker.Quest;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import paul05.de.QuestMaker.Config;
import paul05.de.QuestMaker.main;
import paul05.de.QuestMaker.Exceptions.AttributeMissingException;
import paul05.de.QuestMaker.Exceptions.JSONSyntaxException;
import paul05.de.QuestMaker.Exceptions.PluginMissingException;
import paul05.de.QuestMaker.Quest.Player.RunningQuest;
import paul05.de.QuestMaker.Quest.QuestGiver.Path;
import paul05.de.QuestMaker.Quest.QuestGiver.QuestGiver;

public class Quest {
	
	public static HashMap<UUID, ArrayList<RunningQuest>> playerQuests = new HashMap<>();
	
	private String name;
	private int id;
	private EntityType giverType;
	private boolean giverisPlayer;
	private String giverName;
	private boolean giverWalk;
	private Path path;
	private ArrayList<Reward> rewards = new ArrayList<>();
	private ArrayList<Task> tasks = new ArrayList<>();
	private QuestInfo info;
	
	private File f;
	private Config c;
	private String giverSkinPlayerName;
	private boolean hasInfo;

	public Quest(File f) {
		try {
			Config c = new Config(f);
			this.f = f;
			this.c = c;
			load(c);
		} catch (JSONSyntaxException | IOException e) {
			main.logg(f.getName()+" was loaded with error.");
			main.safeError(e);
		}
	}
	
	public int getId() {
		return id;
	}
	
	public EntityType getGiverType() {
		return giverType;
	}
	
	public Path getPath() {
		return path;
	}
	
	public String getGiverName() {
		return giverName;
	}
	
	public QuestInfo getInfo() {
		return info;
	}
	
	public void updateInfo() {
		if (hasInfo) {
			info.registerInfo();
		}
	}
	
	public RunningQuest startQuest(Player p) {
		ArrayList<RunningQuest> quests = new ArrayList<>();
		if (playerQuests.containsKey(p.getUniqueId())) {
			quests = playerQuests.get(p.getUniqueId());
		}
		
		RunningQuest r = new RunningQuest(this, p);
		quests.add(r);
		p.playSound(p.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 70, 1.3f);
		
		playerQuests.put(p.getUniqueId(), quests);
		return r;
	}
	
	public String getName() {
		return name;
	}
	
	public ArrayList<Reward> getRewards() {
		return rewards;
	}
	
	public ArrayList<Task> getTasks() {
		return tasks;
	}

	private void load(Config c) {
		try {
			name = (String) load(c, "name");
			id = ((Long) load(c, "id")).intValue();
			hasInfo = (boolean) load(c, "hasinfo");
			if (hasInfo) {
				info = new QuestInfo(this, new File(main.getInstance().InfoFolder.getAbsolutePath()+"/"+(String) load(c, "info")));
			}
			Config giver = new Config((JSONObject)load(c, "giver"));
			giverisPlayer = (boolean) load(giver, "isplayer");
			if (!giverisPlayer) {
				giverType = EntityType.fromName((String) load(giver, "type"));
			} else {
				giverSkinPlayerName = (String) load(giver, "skinplayername");
			}
			giverName = (String) load(giver, "name");
			giverWalk = (boolean) load(giver, "walk");
			if (giverWalk) {
				path = new Path(new File(main.getInstance().PathFolder.getAbsolutePath()+"/"+load(giver, "path")));
				main.getPaths().add(path);
			}
			for (Object o : (JSONArray) load(c, "rewards")) {
				rewards.add(new Reward(f, new Config((JSONObject) o)));
			}
			for (Object o : (JSONArray) load(c, "quests")) {
				try {
					tasks.add(new Task(f, new Config((JSONObject) o)));
				} catch (PluginMissingException e) {
					main.safeError(e);
				}
			}
			main.logg("	"+f.getName()+" was loaded.");
		} catch (AttributeMissingException e) {
			main.logg("	"+f.getName()+" was loaded with error.");
			main.safeError(e);
		}
	}
	
	public boolean isGiverisPlayer() {
		return giverisPlayer;
	}
	
	public boolean isGiverWalk() {
		return giverWalk;
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

	public QuestGiver spawn(Location location) {
		return new QuestGiver(location, this);
	}

	public void reloadPath() {
		try {
			Config giver = new Config((JSONObject)load(c, "giver"));
			if (giverWalk) {
				path = new Path(new File(main.getInstance().PathFolder.getAbsolutePath()+"/"+load(giver, "path")));
			}
		} catch (Exception e) {
			main.safeError(e);
		}
	}

	public String getGiverSkinPlayerName() {
		return giverSkinPlayerName;
	}

	public QuestGiver spawn(Location location, boolean b) {
		return new QuestGiver(location, this, b);
	}

	public static boolean isActive(Player p, Quest q) {
		if (playerQuests.containsKey(p.getUniqueId())) {
			for (RunningQuest rq : playerQuests.get(p.getUniqueId())) {
				if (rq.getQuest().equals(q)) {
					return true;
				}
			}
		}
		return false;
	}
	
	public static void reward(Quest q, Player p) {
		for (Reward r : q.getRewards()) {
			r.reward(p);
		}
	}
	
	public static RunningQuest getActive(Player p, Quest q) {
		if (playerQuests.containsKey(p.getUniqueId())) {
			for (RunningQuest rq : playerQuests.get(p.getUniqueId())) {
				if (rq.getQuest().equals(q)) {
					return rq;
				}
			}
		}
		return null;
	}

	public static Quest getQuestbyId(int id) {
		for (Quest q : main.getQuests()) {
			if (q.getId() == id) {
				return q;
			}
		}
		return null;
	}

}
