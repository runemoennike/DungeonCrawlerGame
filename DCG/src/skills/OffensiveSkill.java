/**
 * This class represents offensive skills, thus extends Skill and has a lot
 * of fields used to manipulate outgoing attacks.
 */

package skills;

import infostore.DataNode;
import misc.Attack;

import com.jme.math.FastMath;

import entities.items.Weapon.RangeType;

public class OffensiveSkill extends Skill {

	private RangeType rangeType = null;
	private float minSquaredRangeReq = 0.0f;
	private float maxSquaredRangeReq = 1000000.0f;
	private int chance = 0;
	private int bonusDmg = 0;
	private int dmgGoesToMana = 0;
	private int dmgGoesToHp = 0;
	private int magicBonusDmg = 0;
	private int physicalBonusDmg = 0;
	private boolean hit = false;
	// private Magic magic;

	protected OffensiveSkill(DataNode node, int level) {
		super(node, level);
		DataNode stats = node.getChild("stats");
		if (!stats.getProp("rangetype").isEmpty()) {
			this.rangeType = RangeType.valueOf(stats.getProp("rangetype").toUpperCase());
		}
		this.minSquaredRangeReq = stats.getPropF("minsquaredrangereq");
		this.maxSquaredRangeReq = stats.getPropF("maxsquaredrangereq") == 0.0f ? this.maxSquaredRangeReq : node
				.getPropF("maxsquaredrangereq");
		this.chance = stats.getPropI("chance");
		this.bonusDmg = stats.getPropI("bonusdmg");
		this.dmgGoesToMana = stats.getPropI("dmggoestomana");
		this.dmgGoesToHp = stats.getPropI("dmggoestohp");
		if (!stats.getProp("hit").isEmpty()) {
			this.hit = Boolean.parseBoolean(stats.getProp("hit"));
		}

		for (int i = 2; i <= level; i++) {
			this.addFields(stats.getChild("scale"));
		}
	}

	private void addFields(DataNode node) {
		this.minSquaredRangeReq += node.getPropF("minsquaredrangereq");
		this.maxSquaredRangeReq += node.getPropF("maxsquaredrangereq");
		this.chance += node.getPropI("chance");
		this.bonusDmg += node.getPropI("bonusDmg");
		this.dmgGoesToMana += node.getPropI("dmggoestomana");
		this.dmgGoesToHp += node.getPropI("dmggoestohp");
	}

	public void apply(Attack attack) {
		if (this.rangeType == null || this.rangeType.equals(attack.getAttacker().getAttackRangeType())) {
			if (attack.getAttacker().getMap().getWorld().roll100(this.chance)) {
				if (attack.getRangeSquared() >= this.minSquaredRangeReq
						&& attack.getRangeSquared() <= this.maxSquaredRangeReq) {
					if (this.bonusDmg > 0) {
						attack.setMinDmg(attack.getMinDmg() * (100 + this.bonusDmg) / 100);
						attack.setMaxDmg(attack.getMaxDmg() * (100 + this.bonusDmg) / 100);
						attack.setFinalDmg(attack.getFinalDmg() * (100 + this.bonusDmg) / 100);
					}
					if (this.dmgGoesToMana > 0) {
						attack.addAttackerMana(attack.getFinalDmg() * this.dmgGoesToMana / 100);
					}
					if (this.dmgGoesToHp > 0) {
						attack.addAttackerHp(attack.getFinalDmg() * this.dmgGoesToHp / 100);
					}
					if (this.hit) {
						attack.setHitRoll(-1);
					}
				}
			}
		}
	}

	@Override
	public String toString() {
		String r = super.toString() + " (Requires level " + this.playerLevelRequirement() + ")\n";
		if (rangeType != null)
			r += "Effects " + rangeType.toString().toLowerCase() + " attacks only.\n";
		if (minSquaredRangeReq != 0f)
			r += "Mininum range " + FastMath.sqr(minSquaredRangeReq) + ".\n";
		if (maxSquaredRangeReq != 1000000.0f)
			r += "Maximum range " + FastMath.sqr(maxSquaredRangeReq) + ".\n";
		if (chance != 0)
			r += chance + "% chance of:\n";
		if (bonusDmg != 0)
			r += "Doing " + bonusDmg + "% more damage.\n";
		if (magicBonusDmg != 0)
			r += "Doing " + bonusDmg + "% more physical damage.\n";
		if (physicalBonusDmg != 0)
			r += "Doing " + bonusDmg + "% more magic damage.\n";
		if (dmgGoesToMana != 0)
			r += dmgGoesToMana + "% damage done goes to mana.\n";
		if (dmgGoesToHp != 0)
			r += dmgGoesToHp + "% damage done goes to health.\n";
		return r;
	}
}
