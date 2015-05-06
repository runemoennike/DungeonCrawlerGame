/**
 * The Weapon clas extensind the WearableItem contains informaiton on the weapons
 * damage, cooldown and range type, as well as constructors and getters.
 */

package entities.items;

import misc.Stats;
import infostore.DataManager;
import infostore.DataNode;
import infostore.DataManager.DataType;
import engine.World;

public class Weapon extends WearableItem {

	private static final long serialVersionUID = -3711299331078819668L;

	public enum RangeType {
		MELEE, RANGED
	};

	private RangeType rangeType;
	private int minDmg;
	private int maxDmg;
	private float cooldown;

	public Weapon(World world, String name, DataNode model, WearableType type, Quality quality, int level, Stats stats,
			int minDmg, int maxDmg, float cooldown, RangeType weaponType) {
		super(world, name, model, type, quality, level, stats, null);
		this.minDmg = minDmg;
		this.maxDmg = maxDmg;
		this.cooldown = cooldown;
		this.rangeType = weaponType;
	}

	public Weapon(World world, DataNode n, int level) {
		super(world, "?", DataManager.findByNameAndType(DataType.MODEL_ID, n.getProp("modelID")), WearableItem
				.identToType(n.getProp("attachPoint")), Quality.COMMON, 1, new Stats(), n);

		this.minDmg = n.getChild("attack").getPropI("minDmg") + level
				* n.getChild("attack").getChild("scale").getPropI("minDmg");
		this.maxDmg = n.getChild("attack").getPropI("maxDmg") + level
				* n.getChild("attack").getChild("scale").getPropI("maxDmg");
		this.cooldown = n.getChild("attack").getPropF("cooldown") + (float) level
				* n.getChild("attack").getChild("scale").getPropF("cooldown");
		this.rangeType = RangeType.valueOf(n.getProp("rangeType").toUpperCase());
	}

	public int getMaxDmg() {
		return this.maxDmg;
	}

	public int getMinDmg() {
		return this.minDmg;
	}

	public float getCooldown() {
		return this.cooldown;
	}

	public RangeType getRangeType() {
		return this.rangeType;
	}

	@Override
	public String toString() {
		return "Lvl " + this.getLevel() + " " + this.getCaption() + ": " + this.getItemQuality() + ", dmg "
				+ this.minDmg + "-" + this.maxDmg + ", " + this.getStats();
	}

	public String getInfoString() {
		return "(Level " + this.getLevel() + " " + this.getItemQuality().toString().toLowerCase() + ")\n" + "Value: "
				+ this.getMonetaryValue() + " Gold\n" + "Damage: " + this.minDmg + " - " + this.maxDmg + "\n"
				+ (this.getStats() != null ? this.getStats().getInfoString() : "");
	}
}
