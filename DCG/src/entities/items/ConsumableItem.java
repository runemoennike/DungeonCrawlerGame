/**
 * This class is used by potions and book, and all other items which can be consumed by an actor.
 * It contains a list of ConsumeEffects, which are apply on the Actor class consuming
 * this object. Note that health and mana potions are treated specially.
 */

package entities.items;

import infostore.DataManager;
import infostore.DataNode;
import infostore.DataManager.DataType;

import java.util.LinkedList;

import misc.ConsumeEffect;

import engine.World;
import entities.actors.Player;

public class ConsumableItem extends Item {

	private static final long serialVersionUID = 6203101097121654480L;
	private LinkedList<ConsumeEffect> effects;

	public ConsumableItem(World world, int monetaryValue, String name, DataNode model) {
		super(world, ItemType.CONSUMABLE, monetaryValue, name, model, Quality.COMMON, null);
		this.addSubtype(EntitySubtype.CONSUMABLE);
		this.effects = new LinkedList<ConsumeEffect>();
	}

	public ConsumableItem(World world, DataNode n, int level) {
		super(world, ItemType.CONSUMABLE, 0, n.getProp("name"), DataManager.findByNameAndType(DataType.MODEL_ID, n
				.getProp("modelID")), Quality.COMMON, n);
		this.effects = new LinkedList<ConsumeEffect>();
		if (n.getList("consumeEffect") != null) {
			for (DataNode cen : n.getList("consumeEffect")) {
				this.effects.add(new ConsumeEffect(cen, level));
			}
		}
	}

	public void addConsumeEffect(DataNode n) {
		ConsumeEffect c = new ConsumeEffect(n);
		this.effects.add(c);
		if (c.hasMagic()) {
			this.setCaption(this.getCaption() + " of " + n.getProp("magic"));
		}
	}

	public void addConsumeEffect(DataNode n, int level) {
		this.effects.add(new ConsumeEffect(n, level));
	}

	public LinkedList<ConsumeEffect> getEffects() {
		return this.effects;
	}

	public void consume(Player p) {
		p.getInventory().removeItem(this);
		for (ConsumeEffect c : this.effects) {
			c.apply(p);
		}
	}

	public String dumpEffects() {
		String r = "";

		for (ConsumeEffect ce : this.effects) {
			r += ce + ", ";
		}

		return r;
	}

	@Override
	public void update(float t) {
		super.update(t);
	}

	@Override
	public String toString() {
		return "Lvl " + this.getLevel() + " " + this.getCaption() + ": " + this.getItemQuality() + ", "
				+ this.dumpEffects();
	}
}
