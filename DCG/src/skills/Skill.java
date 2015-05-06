/**
 * This abstract class represent the foundation of all skills in the game.
 * It contains level, name, type and tier of the skill.
 */

package skills;

import infostore.DataNode;

abstract public class Skill implements Cloneable {

	public enum SkillType {
		OFFENSIVE, DEFENSIVE, MAGIC
	};

	public final static int LEVELS_PER_TIER = 5;

	protected String name;
	protected int level;
	protected int tier;
	protected SkillType type;

	protected Skill(DataNode node, int level) {
		this.name = node.getProp("name");
		this.level = level;
		this.tier = node.getPropI("tier");
		this.type = SkillType.valueOf(node.getProp("skillclass").toUpperCase());
	}

	@Override
	public boolean equals(Object s) {
		if (!this.getClass().equals(s.getClass())) {
			return false;
		}
		return s.toString().equals(this.toString());
	}

	@Override
	public String toString() {
		return this.name + " " + this.level;
	}

	public String getName() {
		return this.name;
	}

	public int getLevel() {
		return this.level;
	}

	public int getTier() {
		return this.tier;
	}

	public int playerLevelRequirement() {
		return this.tier * LEVELS_PER_TIER;
	}

	public SkillType getSkillType() {
		return this.type;
	}
}
