package paul05.de.QuestMaker;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.craftbukkit.libs.jline.internal.InputStreamReader;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.google.common.collect.Lists;

import assets.Assets;
import net.milkbowl.vault.Vault;
import net.milkbowl.vault.VaultEco;
import net.milkbowl.vault.VaultEco.VaultAccount;
import net.milkbowl.vault.economy.Economy;
import paul05.de.QuestMaker.Commands.Giver;
import paul05.de.QuestMaker.Commands.Path;
import paul05.de.QuestMaker.Events.PacketReader;
import paul05.de.QuestMaker.Exceptions.AttributeMissingException;
import paul05.de.QuestMaker.Exceptions.JSONSyntaxException;
import paul05.de.QuestMaker.Exceptions.PluginMissingException;
import paul05.de.QuestMaker.Quest.Quest;
import paul05.de.QuestMaker.Quest.Player.RunningQuest;
import paul05.de.QuestMaker.Quest.QuestGiver.FakePlayer;
import paul05.de.QuestMaker.Quest.QuestGiver.QuestGiver;

public class main extends JavaPlugin {
	
	private static main instance;
	public static Config config;
	public static Config data;
	public static Config Errorconfig;
	
	public static File LogFolder;
	public static File QuestFolder;
	public static File PathFolder;
	public static File InfoFolder;
	
	public static int MAXQUESTVALUE;
	public static boolean USEQUESTBOOK;
	
	private static HashMap<Player, PacketReader> PacketReaders = new HashMap<>();
	private static ArrayList<Quest> quests = new ArrayList<>();
	private static ArrayList<paul05.de.QuestMaker.Quest.QuestGiver.Path> paths = new ArrayList<>();
	
	public static HashMap<Player, PacketReader> getPacketReaders() {
		return PacketReaders;
	}
	
	public static ArrayList<paul05.de.QuestMaker.Quest.QuestGiver.Path> getPaths() {
		return paths;
	}
	
	@Override
	public void onEnable() {
		instance = this;
		
		logg("*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~*");
		try {
			Errorconfig = toConfig("ErrorConfig.err", "ErrorConfig.err");
			config = toConfig("config.conf", "config.conf");
			data = toConfig("data.conf", "data.conf");
			SkinLoader.ini(toConfig("PlayerData.conf", null));
			logg(" >>>>>>>>>>>>>> Config Data <<<<<<<<<<<<<<");
			MAXQUESTVALUE = ((Long) load(config, "maxquestvalue")).intValue();
			logg("	MaxQuestValue = "+MAXQUESTVALUE);
			USEQUESTBOOK = (boolean) load(config, "usequestbook");
			logg("	ShowAll = "+USEQUESTBOOK);
		} catch (JSONSyntaxException | AttributeMissingException e) {
			safeError(e);
		} catch (IOException e) {
			safeError(e);
		}
		logg("------------------------------------------");
		
		LogFolder = new File(this.getDataFolder().getAbsolutePath()+"/Logs");
		if (!LogFolder.exists()) {
			LogFolder.mkdirs();
		}
		PathFolder = new File(this.getDataFolder().getAbsolutePath()+"/Paths");
		if (!PathFolder.exists()) {
			PathFolder.mkdirs();
		}
		InfoFolder= new File(this.getDataFolder().getAbsolutePath()+"/Infos");
		if (!InfoFolder.exists()) {
			InfoFolder.mkdirs();
		}
		
		/*
		//Test Exceptions
		JSONSyntaxException e1 = new JSONSyntaxException(new File("Test.quest"), 4, "if (test = 1) {", 4);
		safeError(e1);
		AttributeMissingException e2 = new AttributeMissingException(new File("TEst.quest"), "Name");
		safeError(e2);
		*/
		
		Bukkit.getPluginManager().registerEvents(new EventListener(), getInstance());
		loadQuests();
		
		for (Player p : Bukkit.getOnlinePlayers()) {
			Bukkit.getPluginManager().callEvent(new PlayerJoinEvent(p, ""));
		}
		
		for (Object o : (JSONArray) data.get("entitys")) {
			JSONObject ob = (JSONObject) o;
			for (Object u : ob.keySet()) {
				int id = Integer.valueOf((String) u);
				for (Quest q : quests) {
					if (q.getId() == id) {
						q.spawn(Config.getLocation((JSONObject) ob.get(id+""), id), true);
					}
				}
			}
		}
	}
	
	public static Economy setupEconomy() {
		RegisteredServiceProvider<Economy> economyProvider = Bukkit.getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
		if (economyProvider != null) {
            return economyProvider.getProvider();
        }

        return null;
    }
	
	private static void loadQuests() {
		QuestFolder = new File(instance.getDataFolder().getAbsolutePath()+"/Quests");
		if (!QuestFolder.exists()) {
			QuestFolder.mkdirs();
		}
		for (File f : QuestFolder.listFiles()) {
			if (f.getName().replace('.', '#').split("#")[1].equalsIgnoreCase("quest")) {
				Quest q = new Quest(f);
				quests.add(q);
			}
		}
		logg("*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~*");
	}
	
	@Override
	public void onDisable() {
		for (PacketReader r : PacketReaders.values()) {
			r.uninject();
		}
		for (FakePlayer fp : QuestGiver.getFakePlayers().keySet()) {
			fp.destroy();
		}
		for (LivingEntity ent : QuestGiver.getAllEntities().keySet()) {
			ent.remove();
		}
	}

	public static void safeError(Exception e) {
		if (!(e instanceof JSONSyntaxException || e instanceof AttributeMissingException || e instanceof PluginMissingException)) {
			Timestamp time = new Timestamp(System.currentTimeMillis());
			File log = new File(LogFolder.getAbsolutePath()+"/"+time.toString().substring(0, 16).replace(':', '.')+".log");
			try {
				int id = 1;
				while (true) {
					if (log.exists()) {
						log = new File(LogFolder.getAbsolutePath()+"/"+time.toString().substring(0, 16).replace(':', '.')+" ("+id+").log");
					} else {
						log.createNewFile();
						break;
					}
					id++;
				}
				PrintWriter w = new PrintWriter(log);
				e.printStackTrace(w);
				w.flush();
				w.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			//Send
			JSONArray text = (JSONArray) main.Errorconfig.get("intern");
			for (Object o : text) {
				String st = (String) o;
				st = st.replace("%file%", instance.getDataFolder().getName()+"/"+LogFolder.getName()+"/"+log.getName());
				main.logg(st);
			}
		} else {
			e.printStackTrace();
		}
	}
	
	public static void logg(String s) {
		System.out.println("[QuestMaker] "+s);
	}
	
	public static main getInstance() {
		return instance;
	}
	
	public static Config toConfig(String name, String source) throws JSONSyntaxException, IOException {
		if (!instance.getDataFolder().exists()) {
			instance.getDataFolder().mkdirs();
		}
		File c = new File(instance.getDataFolder().getAbsolutePath()+"/"+name);
		if (!c.exists()) {
			try {
				c.createNewFile();
				BufferedWriter w = new BufferedWriter(new FileWriter(c));
				if (source != null) {
					InputStream i = new Assets().getFile("configs/"+source);
					BufferedReader r = new BufferedReader(new InputStreamReader(i));
					while (r.ready()) {
						w.write(r.readLine());
						w.newLine();
					}
					r.close();
				} else {
					w.write("{");
					w.newLine();
					w.newLine();
					w.write("}");
				}
				w.flush();
				w.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			Config co = new Config(c);
			logg("	"+name+" is loaded.");
			return co;
		} catch (IOException e) {
			logg("	"+name+" is loaded with error.");
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
			throw new AttributeMissingException(c.getFile(), s, from);
		}
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (label.equalsIgnoreCase("quest")  || label.equalsIgnoreCase("questmaker:quest")) {
			if (sender.isPermissionSet("QuestMaker.Op") || sender.isOp()) {
				if (args.length >= 1) {
					cmd.setName(args[0]);
					String[] newArgs = new String[args.length-1];
					for (int i = 0; i < args.length; i++) {
						if (i > 0) {
							newArgs[i-1] = args[i];
						}
					}
					if (args[0].equalsIgnoreCase("giver")) {
						new Giver().onCommand(sender, cmd, args[0], newArgs);
					} else if (args[0].equalsIgnoreCase("player")) {
						
					} else if (args[0].equalsIgnoreCase("path")) {
						if (sender instanceof Player) {
							new Path().onCommand(sender, cmd, args[0], newArgs);
						} else {
							sender.sendMessage("§cThis command can just run from player.");
						}
					} else {
						sender.sendMessage("§cAvaible subcommands are: giver, player, path");
					}
				} else {
					sender.sendMessage("§cUse sub command.");
				}
			} else {
				sender.sendMessage("§cYou have no permission to do that.");
			}
		}
		return true;
	}
	
	public static ArrayList<Quest> getQuests() {
		return quests;
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		if (command.getLabel().equalsIgnoreCase("quest") || command.getLabel().equalsIgnoreCase("questmaker:quest")) {
			if (sender.isPermissionSet("QuestMaker.Op") || sender.isOp()) {
				if (args.length >= 2) {
					command.setName(args[0]);
					String[] newArgs = new String[args.length-1];
					for (int i = 0; i < args.length; i++) {
						if (i > 0) {
							newArgs[i-1] = args[i];
						}
					}
					if (args[0].equalsIgnoreCase("giver")) {
						return new Giver().onTabComplete(sender, command, args[0], newArgs);
					}
					if (args[0].equalsIgnoreCase("player")) {
						return new Giver().onTabComplete(sender, command, args[0], newArgs);
					}
					if (args[0].equalsIgnoreCase("path")) {
						return new Path().onTabComplete(sender, command, args[0], newArgs);
					}
				} else {
					return Lists.newArrayList(new String[] {"giver", "player", "path"});
				}
			}
		}
		return null;
	}
}
