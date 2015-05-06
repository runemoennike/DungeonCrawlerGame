/**
 * This class extends the Map class, and just adds some list of items,
 * which are sold by the vendor in the sanctuary.
 * 
 * Also there are methods for selling buying and generating items for the vendor.
 */

package map;

import infostore.DataManager;
import infostore.DataNode;
import infostore.DataManager.DataType;

import java.util.ArrayList;
import java.util.LinkedList;

import engine.Game;
import engine.World;
import entities.actors.Player;
import entities.items.Armor;
import entities.items.ConsumableItem;
import entities.items.Item;
import entities.items.Weapon;
import entities.items.Item.Quality;

public class Sanctuary extends Map {

	private ArrayList<Weapon> weapons;
	private ArrayList<Armor> armors;
	private ArrayList<ConsumableItem> consumables;

	private long sellTime;

	public Sanctuary(World world) {
		super(world, 0);
		this.consumables = new ArrayList<ConsumableItem>();
		this.weapons = new ArrayList<Weapon>();
		this.armors = new ArrayList<Armor>();
	}

	public ArrayList<Weapon> getWeapons() {
		return this.weapons;
	}

	public ArrayList<Armor> getArmors() {
		return this.armors;
	}

	public ArrayList<ConsumableItem> getConsumables() {
		return this.consumables;
	}

	public void sellItem(Player p, Item i) {
		if (i.getClass().equals(ConsumableItem.class)) {
			p.getInventory().removeItem(i);
			this.consumables.add((ConsumableItem) i);
			i.setIsInShop(true);
			p.getInventory().addGold(i.getMonetaryValue());
		} else if (i.getClass().equals(Weapon.class)) {
			p.getInventory().removeItem(i);
			this.weapons.add((Weapon) i);
			i.setIsInShop(true);
			p.getInventory().addGold(i.getMonetaryValue());
		} else if (i.getClass().equals(Armor.class)) {
			p.getInventory().removeItem(i);
			this.armors.add((Armor) i);
			i.setIsInShop(true);
			p.getInventory().addGold(i.getMonetaryValue());
		} else {
			Game.getInstance().getHUD().showInfoMessage("Item cannot be sold!");
		}
		this.sellTime = System.currentTimeMillis();
	}

	public void buyItem(Player p, Item i) {
		if (System.currentTimeMillis() > this.sellTime + 500) {
			if (p.getInventory().canAfford(i.getMonetaryValue())) {
				if (p.giveItem(i)) {
					p.getInventory().subtractGold(i.getMonetaryValue());
					if (this.armors.remove(i) || this.weapons.remove(i)) {
						i.setIsInShop(false);
					}
				} else {
					Game.getInstance().getHUD().showInfoMessage("Not enough space in inventory!");
				}
			} else {
				Game.getInstance().getHUD().showInfoMessage("Not enough gold!");
			}
		}
	}

	public void generateVendorItems(Player p) {
		this.consumables.clear();
		this.armors.clear();
		this.weapons.clear();
		LinkedList<DataNode> allItems = DataManager.findAllByType(DataType.ITEM);
		for (DataNode node : allItems) {
			if (node.getProp("name").equals("item_hpot") || node.getProp("name").equals("item_hpot_large")
					|| node.getProp("name").equals("item_mpot") || node.getProp("name").equals("item_mpot_large")) {
				this.consumables.add((ConsumableItem) Item.generateFromNode(node, 1, Quality.COMMON, this.world));
			}
		}

		for (DataNode node : allItems) {
			if (node.getProp("class").equals("Weapon")) {
				int roll = this.world.getRndInt(0, 2);
				for (int i = 1; i <= roll; i++) {
					this.weapons.add((Weapon) Item.generateFromNode(node, p.getLevel(), Quality.COMMON, this.world));
				}
				roll = this.world.getRndInt(0, 2);
				for (int i = 1; i <= roll; i++) {
					this.weapons.add((Weapon) Item.generateFromNode(node, p.getLevel(), Quality.MAGIC, this.world));
				}
			}
			if (node.getProp("class").equals("Armor")) {
				int roll = this.world.getRndInt(0, 1);
				for (int i = 1; i <= roll; i++) {
					this.armors.add((Armor) Item.generateFromNode(node, p.getLevel(), Quality.COMMON, this.world));
				}
				roll = this.world.getRndInt(0, 1);
				for (int i = 1; i <= roll; i++) {
					this.armors.add((Armor) Item.generateFromNode(node, p.getLevel(), Quality.MAGIC, this.world));
				}
			}
		}

		for (Item i : this.weapons) {
			i.setIsInShop(true);
		}
		for (Item i : this.armors) {
			i.setIsInShop(true);
		}
		for (Item i : this.consumables) {
			i.setIsInShop(true);
		}
	}
}
