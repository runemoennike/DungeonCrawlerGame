/**
 * Gold is a simple extension of Item, which contains the quantity of gold. Gold is 
 * treated specially in the inventory.
 */

package entities.items;

import infostore.DataManager;
import infostore.DataNode;
import infostore.DataManager.DataType;

import com.jme.math.FastMath;

import engine.World;

public class Gold extends Item {

	private static final long serialVersionUID = -5732814141580831533L;
	private int quantity;

	public Gold(World world, int quantity) {
		super(world, ItemType.GOLD, quantity, "Pile of Gold", DataManager.findByNameAndType(DataType.MODEL_ID,
				"modelID_gold"), Quality.COMMON, null);

		this.noPathing = true;
		this.addSubtype(EntitySubtype.GOLD);
		this.setQuantity(quantity);
	}

	public Gold(World world, DataNode n, int level) {
		super(world, ItemType.GOLD, (int) ((FastMath.pow(level, 3) + world.getRndInt(0, 20))), n.getProp("caption"),
				DataManager.findByNameAndType(DataType.MODEL_ID, n.getProp("modelID")), Quality.COMMON, n);
		this.quantity = this.getMonetaryValue();
	}

	@Override
	public void update(float t) {
		super.update(t);
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
		this.setMonetaryValue(this.quantity);
		this.setCaption(this.getCaption() + " (" + this.quantity + " pieces)");
	}

	public int getQuantity() {
		return quantity;
	}

	@Override
	public String toString() {
		return "Gold, " + this.quantity + " pieces (lvl " + this.getLevel() + ")";
	}

}
