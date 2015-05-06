/**
 * This class represents defensive skills, thus extends Skill and has a lot
 * of fields used to manipulate incomming attacks.
 */

package skills;

import misc.Attack;
import misc.Attack.DamageType;

import com.jme.math.FastMath;

import infostore.DataNode;
import entities.actors.Actor.AttachPoint;
import entities.items.Armor;
import entities.items.Weapon.RangeType;

public class DefensiveSkill extends Skill implements Cloneable {

	private RangeType rangeType = null;
	private float minSquaredRangeReq = 0.0f;
	private float maxSquaredRangeReq = 1000000.0f;
	private int chance = 0;
	private int returnDmg = 0;
	private int dmgGoesToMana = 0;
	private int magicDmgReduction = 0;
	private int physicalDmgReduction = 0;
	private boolean evasion = false;
	private boolean block = false;
	// private Magic magic;

	protected DefensiveSkill(DataNode node, int level) {
		super(node, level);
		DataNode stats = node.getChild("stats");

		if (!stats.getProp("rangetype").isEmpty()) {
			this.rangeType = RangeType.valueOf(stats.getProp("rangetype").toUpperCase());
		}
		this.minSquaredRangeReq = stats.getPropF("minsquaredrangereq");
		this.maxSquaredRangeReq = stats.getPropF("maxsquaredrangereq") == 0.0f ? this.maxSquaredRangeReq : node
				.getPropF("maxsquaredrangereq");
		this.chance = stats.getPropI("chance");
		this.returnDmg = stats.getPropI("returndmg");
		this.dmgGoesToMana = stats.getPropI("dmggoestomana");
		this.magicDmgReduction = stats.getPropI("magicdmgreduction");
		this.physicalDmgReduction = stats.getPropI("physicaldmgreduction");
		if (!stats.getProp("evasion").isEmpty()) {
			this.evasion = Boolean.parseBoolean(stats.getProp("evasion"));
		}
		if (!stats.getProp("block").isEmpty()) {
			this.block = Boolean.parseBoolean(stats.getProp("block"));
		}

		for (int i = 2; i <= level; i++) {
			this.addFields(stats.getChild("scale"));
		}
	}

	private void addFields(DataNode node) {
		this.minSquaredRangeReq += node.getPropF("minsquaredrangereq");
		this.maxSquaredRangeReq += node.getPropF("maxsquaredrangereq");
		this.chance += node.getPropI("chance");
		this.returnDmg += node.getPropI("returndmg");
		this.dmgGoesToMana += node.getPropI("dmggoestomana");
		this.magicDmgReduction += node.getPropI("magicdmgreduction");
		this.physicalDmgReduction += node.getPropI("physicaldmgreduction");
	}

	public void apply(Attack attack) {
		if (this.rangeType == null || this.rangeType.equals(attack.getAttacker().getAttackRangeType())) {
			if (attack.getTarget().getMap().getWorld().roll100(this.chance)) {
				if (attack.getRangeSquared() >= this.minSquaredRangeReq
						&& attack.getRangeSquared() <= this.maxSquaredRangeReq) {
					if (this.returnDmg > 0) {
						attack.subAttackerHp(attack.getFinalDmg() * this.returnDmg / 100);
					}
					if (this.dmgGoesToMana > 0) {
						int lost = attack.getFinalDmg() * this.dmgGoesToMana / 100;
						attack.setMaxDmg(attack.getFinalDmg() - lost);
						attack.subTargetMana(lost);
					}
					if (this.magicDmgReduction > 0 && attack.getDamageType().equals(DamageType.MAGIC)) {
						attack.setFinalDmg(attack.getFinalDmg() * this.magicDmgReduction / 100);
					}
					if (this.physicalDmgReduction > 0 && attack.getDamageType().equals(DamageType.PHYSICAL)) {
						attack.setFinalDmg(attack.getFinalDmg() * this.physicalDmgReduction / 100);
					}
					if (this.block && attack.getDamageType().equals(DamageType.PHYSICAL)
							&& attack.getTarget().getWearItem(AttachPoint.HAND_L).getClass().equals(Armor.class)) {
						attack.setFinalDmg(0);
					}
					if (this.evasion) {
						attack.setHitRoll(0x0FFFFFFF);
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
		if (returnDmg != 0)
			r += "Return " + returnDmg + " to attacker.\n";
		if (dmgGoesToMana != 0)
			r += dmgGoesToMana + "% damage taken goes to mana.\n";
		if (magicDmgReduction != 0)
			r += "Magic damage reduced by " + magicDmgReduction + "%.\n";
		if (physicalDmgReduction != 0)
			r += "Physical damage reduced by " + physicalDmgReduction + "%.\n";
		if (evasion)
			r += evasion + " evade attack.\n";
		if (block)
			r += block + " block attack.\n";
		return r;
	}
}
