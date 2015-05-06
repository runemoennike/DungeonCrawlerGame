/**
 * This class represents stats skills, thus extends Skill and has a instance
 * of Stats used to manipulate the stats of the actor having this skill.
 */

package skills;

import misc.Stats;
import infostore.DataNode;
import entities.actors.Actor;

public class StatsSkill extends Skill {

	private Stats statModifiers;

	protected StatsSkill(DataNode node, int level) {
		super(node, level);
		this.statModifiers = new Stats(node.getChild("stats"));
		for (int i = 2; i <= level; i++) {
			this.statModifiers.addStatsToThis(new Stats(node.getChild("stats").getChild("scale")));
		}
	}

	public void apply(Actor actor) {
		if (this.statModifiers != null) {
			actor.getTotalStats().applyModifierStats(this.statModifiers);
		}
	}

	@Override
	public String toString() {
		String r = super.toString() + " (Requires level " + this.playerLevelRequirement() + ")\n";
		r += this.statModifiers.toString();
		// TODO: Add statsskill explanation
		return r;
	}
}
