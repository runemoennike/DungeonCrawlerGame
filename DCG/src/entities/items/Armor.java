/**
 * The Armor class extends the WearableItem by adding an armor value, constructors and
 * a static function for reducing damage by an armor value.
 */

package entities.items;

import misc.Stats;
import infostore.DataManager;
import infostore.DataNode;
import infostore.DataManager.DataType;
import engine.World;

public class Armor extends WearableItem {

	private static final long serialVersionUID = -1119349624626484092L;

	private int armor;

	public Armor(World world, String name, DataNode model, WearableType type, Quality quality, int level, Stats stats,
			int armor) {
		super(world, name, model, type, quality, level, stats, null);
		this.armor = armor;
	}

	public Armor(World world, DataNode n, int level) {
		super(world, "?", DataManager.findByNameAndType(DataType.MODEL_ID, n.getProp("modelID")), WearableItem
				.identToType(n.getProp("attachPoint")), Quality.COMMON, 1, null, n);

		this.armor = n.getChild("defense").getPropI("armor") + level
				* n.getChild("defense").getChild("scale").getPropI("armor");
	}

	public int getArmor() {
		return this.armor;
	}

	public static int reduceDmgByArmor(int dmg, int armor) {
		return (int) (dmg / (Math.sqrt(armor) / 10.0 + 1.0) + 0.5);
	}

	@Override
	public String toString() {
		return this.getCaption() + ": " + this.getItemQuality() + ", armor " + this.armor + ", " + this.getStats();
	}

	public String getInfoString() {
		return "(Level " + this.getLevel() + " " + this.getItemQuality().toString().toLowerCase() + ")\n" + "Value: "
				+ this.getMonetaryValue() + " Gold\n" + "Armor: " + this.armor + "\n" + this.getStats().getInfoString();
	}
}
