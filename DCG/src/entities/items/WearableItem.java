/**
 * This extension of the Item class is used for all items which can be equipped by an actor.
 * It contains information on where the item can be equipped and stats of the item.
 */

package entities.items;

import misc.Stats;
import infostore.DataNode;
import engine.World;

public abstract class WearableItem extends Item {

	private static final long serialVersionUID = 236529545852918511L;

	public enum WearableType {
		FEET, CHEST, HEAD, FINGER, MAINHAND, OFFHAND, HANDS
	}

	private Stats stats;
	private WearableType wearableType;

	public WearableItem(World world, String name, DataNode model, WearableType type, Quality quality, int level,
			Stats stats, DataNode n) {
		super(world, ItemType.WEARABLE, (int) (10 * Stats.qualityMultiplier(quality) * level), name, model, quality, n);
		this.setLevel(level);
		this.setWearableType(type);
		this.addSubtype(EntitySubtype.WEARABLE);
		this.stats = stats;
	}

	@Override
	public void update(float t) {
		super.update(t);
	}

	public void setWearableType(WearableType wearableType) {
		this.wearableType = wearableType;
	}

	public WearableType getWearableType() {
		return wearableType;
	}

	public Stats getStats() {
		return this.stats;
	}

	public void setStats(Stats stats) {
		this.stats = stats;
	}

	public static WearableType identToType(String ident) {
		return WearableType.valueOf(ident.toUpperCase());
	}

	public String getInfoString() {
		return "(Level " + this.getLevel() + " " + this.getItemQuality().toString().toLowerCase() + ")\n" + "Value: "
				+ this.getMonetaryValue() + " Gold\n" + this.getStats().getInfoString();
	}
}
