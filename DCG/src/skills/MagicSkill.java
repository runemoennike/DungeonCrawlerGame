/**
 * This class represents magic skills, thus extends Skill and has a lot
 * of fields used to manipulate the spells know to the actor having the skill.
 */

package skills;

import infostore.DataNode;

import java.util.LinkedList;

import misc.Magic;
import misc.Magic.MagicType;

public class MagicSkill extends Skill {

	private LinkedList<String> affectedMagics = new LinkedList<String>();;
	private MagicType magicType;
	private int durationBonus = 0;
	private int dmgHealBonus = 0;
	private int rangeBonus = 0;
	private int aoeRangeBonus = 0;
	private int reduceManaCostBonus = 0;

	protected MagicSkill(DataNode node, int level) {
		super(node, level);
		DataNode stats = node.getChild("stats");
		if (!stats.getProp("magictype").isEmpty()) {
			this.magicType = MagicType.valueOf(stats.getProp("magictype").toUpperCase());
		}
		if (!stats.getProp("affectedMagics").isEmpty()) {
			for (String s : stats.getProp("affectedMagics").split(",")) {
				this.affectedMagics.add(s.trim());
			}
		}
		this.durationBonus = stats.getPropI("durationbonus");
		this.dmgHealBonus = stats.getPropI("dmghealbonus");
		this.rangeBonus = stats.getPropI("rangebonus");
		this.aoeRangeBonus = stats.getPropI("aoerangebonus");
		this.reduceManaCostBonus = stats.getPropI("reducemanacostbonus");

		for (int i = 2; i <= level; i++) {
			this.addFields(stats.getChild("scale"));
		}
	}

	private void addFields(DataNode node) {
		this.durationBonus += node.getPropI("durationbonus");
		this.dmgHealBonus += node.getPropI("dmghealbonus");
		this.rangeBonus += node.getPropI("rangebonus");
		this.aoeRangeBonus += node.getPropI("aoerangebonus");
		this.reduceManaCostBonus += node.getPropI("reducemanacostbonus");
	}

	public void apply(Magic magic) {
		if (this.affectedMagics.isEmpty() || this.affectedMagics.contains(magic.getName())) {
			if (this.magicType == null || this.magicType.equals(magic.getMagicType())) {
				if (this.durationBonus > 0) {
					magic.setDuration(magic.getDuration() * (this.durationBonus + 100) / 100);
				}
				if (this.dmgHealBonus > 0) {
					magic.setMaxDmgHeal(magic.getMaxDmgHeal() * (this.dmgHealBonus + 100) / 100);
					magic.setMinDmgHeal(magic.getMinDmgHeal() * (this.dmgHealBonus + 100) / 100);
				}
				if (this.rangeBonus > 0) {
					magic.setRangeSquared(magic.getRangeSquared() * (this.rangeBonus * this.rangeBonus + 100) / 100);
				}
				if (this.aoeRangeBonus > 0) {
					magic.setAoeRangeSquared(magic.getAoeRangeSquared()
							* (this.aoeRangeBonus * this.aoeRangeBonus + 100) / 100);
				}
				if (this.reduceManaCostBonus > 0) {
					int cost = magic.getManaCost();
					magic.setAoeRangeSquared(cost - (cost * this.reduceManaCostBonus) / 100);
				}
			}
		}
	}

	@Override
	public String toString() {
		String r = super.toString() + " (Requires level " + this.playerLevelRequirement() + ")\n";
		if (!affectedMagics.isEmpty())
			r += "Effects " + affectedMagics + " attacks only.\n";
		if (magicType != null)
			r += "Effects" + magicType.toString().toLowerCase() + ".\n";
		if (durationBonus != 0)
			r += "Duration of spells increased by" + durationBonus + "%.\n";
		if (dmgHealBonus != 0)
			r += "Damage and healing increased by " + dmgHealBonus + "%\n";
		if (rangeBonus != 0)
			r += "Range increased by " + rangeBonus + "%\n";
		if (aoeRangeBonus != 0)
			r += "Area of effect range increased by " + rangeBonus + "%\n";
		if (reduceManaCostBonus != 0)
			r += "Mana cost reduved by " + reduceManaCostBonus + "%\n";
		return r;
	}
}
