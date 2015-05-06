/**
 * The SkillTree contains all the Skills in the game. These are accessible through a number
 * of different static methods, lists and hashmaps.
 */

package skills;

import infostore.DataManager;
import infostore.DataNode;
import infostore.DataManager.DataType;

import java.util.HashMap;
import java.util.LinkedList;

import skills.Skill.SkillType;

import entities.actors.Player;

public class SkillTree {

	private static LinkedList<StatsSkill> statsSkills = new LinkedList<StatsSkill>();
	private static LinkedList<OffensiveSkill> offensiveSkills = new LinkedList<OffensiveSkill>();
	private static LinkedList<DefensiveSkill> defensiveSkills = new LinkedList<DefensiveSkill>();
	private static LinkedList<MagicSkill> magicSkills = new LinkedList<MagicSkill>();
	private static HashMap<Skill, LinkedList<Skill>> prerequisites = new HashMap<Skill, LinkedList<Skill>>();
	private static HashMap<String, Skill> nameToSkill = new HashMap<String, Skill>();

	public static void init() {
		HashMap<Skill, String[]> prerequisiteStrings = new HashMap<Skill, String[]>();
		for (DataNode statsskill : DataManager.findAllByType(DataType.STATS_SKILL)) {
			for (int level = 1; level <= statsskill.getPropI("maxlevel"); level++) {
				StatsSkill skill = new StatsSkill(statsskill, level);
				statsSkills.add(skill);
				prerequisites.put(skill, null);
				prerequisiteStrings.put(skill, statsskill.getProp("prerequisites").split(","));
				nameToSkill.put(skill.getName() + " " + skill.getLevel(), skill);
			}
		}
		for (DataNode offensiveskill : DataManager.findAllByType(DataType.OFFENSIVE_SKILL)) {
			for (int level = 1; level <= offensiveskill.getPropI("maxlevel"); level++) {
				OffensiveSkill skill = new OffensiveSkill(offensiveskill, level);
				offensiveSkills.add(skill);
				prerequisites.put(skill, null);
				prerequisiteStrings.put(skill, offensiveskill.getProp("prerequisites").split(","));
				nameToSkill.put(skill.getName() + " " + skill.getLevel(), skill);
			}
		}
		for (DataNode defensiveskill : DataManager.findAllByType(DataType.DEFENSIVE_SKILL)) {
			for (int level = 1; level <= defensiveskill.getPropI("maxlevel"); level++) {
				DefensiveSkill skill = new DefensiveSkill(defensiveskill, level);
				defensiveSkills.add(skill);
				prerequisites.put(skill, null);
				prerequisiteStrings.put(skill, defensiveskill.getProp("prerequisites").split(","));
				nameToSkill.put(skill.getName() + " " + skill.getLevel(), skill);
			}
		}
		for (DataNode magicskill : DataManager.findAllByType(DataType.MAGIC_SKILL)) {
			for (int level = 1; level <= magicskill.getPropI("maxlevel"); level++) {
				MagicSkill skill = new MagicSkill(magicskill, level);
				magicSkills.add(skill);
				prerequisites.put(skill, null);
				prerequisiteStrings.put(skill, magicskill.getProp("prerequisites").split(","));
				nameToSkill.put(skill.getName() + " " + skill.getLevel(), skill);
			}
		}

		for (Skill skill1 : prerequisiteStrings.keySet()) {
			LinkedList<Skill> list = new LinkedList<Skill>();
			for (String string : prerequisiteStrings.get(skill1)) {
				for (Skill skill2 : prerequisites.keySet()) {
					if (string.trim().equals(skill2.getName())) {
						list.add(skill2);
					}
				}
			}
			prerequisites.put(skill1, list);
		}
	}

	public static boolean skillAvailable(Player p, Skill s) {
		if (p.getLevel() < s.playerLevelRequirement()) {
			return false;
		}
		if (p.getAllSkills().contains(s)) {
			return false;
		}
		LinkedList<Skill> pskills = p.getAllSkills();
		if (s.getLevel() > 1) {
			for (Skill skill : pskills) {
				if (s.getName().equals(skill.getName()) && s.getLevel() == skill.getLevel() + 1) {
					if (pskills.containsAll(prerequisites.get(s))) {
						return true;
					}
				}
			}
		} else if (pskills.containsAll(prerequisites.get(s))) {
			return true;
		}

		return false;
	}

	public static LinkedList<StatsSkill> getStatsSkills() {
		return statsSkills;
	}

	public static LinkedList<OffensiveSkill> getOffensiveSkills() {
		return offensiveSkills;
	}

	public static LinkedList<DefensiveSkill> getDefensiveSkills() {
		return defensiveSkills;
	}

	public static LinkedList<MagicSkill> getMagicSkills() {
		return magicSkills;
	}

	public static LinkedList<Skill> getPrerequisites(Skill s) {
		return prerequisites.get(s);
	}

	public static LinkedList<Skill> getAllSkills() {
		LinkedList<Skill> all = new LinkedList<Skill>();
		all.addAll(statsSkills);
		all.addAll(defensiveSkills);
		all.addAll(offensiveSkills);
		all.addAll(magicSkills);
		return all;
	}

	public static Skill getSkillByString(String nameAndLevel) {
		return nameToSkill.get(nameAndLevel.trim());
	}

	public static LinkedList<Skill> getAllLevelsOfSkill(Skill s) {
		LinkedList<Skill> list = new LinkedList<Skill>();
		int i = 1;
		Skill o = getSkillByString(s.name + " " + i);
		while (o != null) {
			list.add(o);
			i++;
			o = getSkillByString(s.name + " " + i);
		}
		return list;
	}

	public static LinkedList<Skill> getLowerLevelsOfSkill(Skill s) {
		LinkedList<Skill> list = new LinkedList<Skill>();
		for (int i = 1; i < s.getLevel(); i++) {
			list.add(getSkillByString(s.name + " " + i));
		}
		return list;
	}

	public static LinkedList<Skill> getSkillsByType(SkillType type) {
		LinkedList<Skill> result = new LinkedList<Skill>();
		for (Skill s : getAllSkills()) {
			if (s.getSkillType().equals(type)) {
				result.add(s);
			}
		}
		return result;
	}
}
