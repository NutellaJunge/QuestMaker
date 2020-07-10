package paul05.de.QuestMaker.Commands;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.google.common.collect.Lists;

import net.minecraft.server.v1_14_R1.IScoreboardCriteria;
import paul05.de.QuestMaker.Config;
import paul05.de.QuestMaker.main;
import paul05.de.QuestMaker.Exceptions.JSONSyntaxException;
import paul05.de.QuestMaker.Quest.Quest;

public class Path implements CommandExecutor, TabCompleter {
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		Player p = (Player) sender;
		if (args.length >= 1) {
			if (args[0].equalsIgnoreCase("add")) {
				if (args.length >= 2) {
					try {
						Config path = new Config(new File(main.PathFolder.getAbsolutePath()+"/"+args[1]));
						path.set("world", p.getWorld().getName());
						JSONArray a = (JSONArray) path.get("path");
						if (a == null) {
							a = new JSONArray();
						}
						a.add(toLocation(p.getLocation()));
						path.set("path", a);
						path.save();
						for (Quest q : main.getQuests()) {
							if (q.isGiverWalk()) {
								q.reloadPath();
							}
						}						
					} catch (IOException e) {
						p.sendMessage("§cThe path file "+args[1]+" can't find.");
					} catch (JSONSyntaxException e) {
						main.safeError(e);
					}
				}
			}
			if (args[0].equalsIgnoreCase("create")) {
				if (args.length >= 2) {
					try {
						Config path = main.toConfig(main.PathFolder.getName()+"/"+args[1], null);
					} catch (JSONSyntaxException | IOException e) {
						main.safeError(e);
					}
				}
			}
			if (args[0].equalsIgnoreCase("show")) {
				if (args.length >= 2) {
					for (paul05.de.QuestMaker.Quest.QuestGiver.Path paths : main.getPaths()) {
						if (paths.getFile().getName().replace('.', '#').split("#")[0].equalsIgnoreCase(args[1])) {
							paths.show(p);
						}
					}
				}
			}
		}
		return true;
	}

	private JSONObject toLocation(Location l) {
		JSONObject o = new JSONObject();
		o.put("x", l.getX());
		o.put("y", l.getY());
		o.put("z", l.getZ());
		o.put("yaw", l.getYaw());
		o.put("pitch", l.getPitch());
		return o;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		if (sender instanceof Player) {
			if (args.length >= 2) {
				if (args[0].equalsIgnoreCase("add")) {
					if (args.length == 2) {
						ArrayList<String> paths = new ArrayList<>();
						for (File f : main.PathFolder.listFiles()) {
							paths.add(f.getName());
						}
						return paths;
					}
				}
				if (args[0].equalsIgnoreCase("show")) {
					if (args.length == 2) {
						ArrayList<String> paths = new ArrayList<>();
						for (paul05.de.QuestMaker.Quest.QuestGiver.Path p : main.getPaths()) {
							paths.add(p.getFile().getName().replace('.', '#').split("#")[0]);
						}
						return paths;
					}
				}
			} else {
				return Lists.newArrayList(new String[] {"add", "create", "show"});
			}
		}
		return new ArrayList<>();
	}
}
