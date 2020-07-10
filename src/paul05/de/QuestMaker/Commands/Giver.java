package paul05.de.QuestMaker.Commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import com.google.common.collect.Lists;

import paul05.de.QuestMaker.main;
import paul05.de.QuestMaker.Quest.Quest;
import paul05.de.QuestMaker.Quest.QuestGiver.QuestGiver;

public class Giver implements CommandExecutor, TabCompleter {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (sender instanceof Player) {
			Player p = (Player) sender;
			if (args.length >= 1) {
				if (args[0].equalsIgnoreCase("add")) {
					if (args.length >= 2) {
						for (Quest q : main.getQuests()) {
							if (args[1].equalsIgnoreCase(q.getName())) {
								q.spawn(p.getLocation());
							}
						}
					}
				}
			}
		} else {
			sender.sendMessage("§cThis command can just run from player.");
		}
		return false;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		if (sender instanceof Player) {
			if (args.length >= 2) {
				if (args[0].equalsIgnoreCase("add")) {
					if (args.length == 2) {
						ArrayList<String> quests = new ArrayList<>();
						for (Quest q : main.getQuests()) {
							quests.add(q.getName());
						}
						return quests;
					}
				}
			} else {
				return Lists.newArrayList(new String[] {"add", "remove"});
			}
		}
		return new ArrayList<>();
	}
	
}
