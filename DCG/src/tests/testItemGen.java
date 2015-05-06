/**
 * Used for testing the Item generation with generateFromNode().
 */

package tests;

import infostore.DataManager;
import infostore.DataNode;
import infostore.DataManager.DataType;

import java.util.LinkedList;

import map.Map;
import engine.Settings;
import engine.World;
import entities.items.Item;

public class testItemGen {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		DataManager.load();
		Settings.NOGFX = true;
		System.out.println("--------------------");

		World w = new World();
		Map m = new Map(w, 1);
		DataNode n = DataManager.findByNameAndType(DataType.ITEM, "item_sword");

		for (int i = 0; i < 10; i++) {
			Item t = Item.generateFromNode(n, w.getRndInt(1, 10), 5, w);
			System.out.println(t);
		}

		for (int i = 0; i < 10; i++) {
			Item t = Item.generateFromNode(n, w.getRndInt(1, 10), 50, w);
			System.out.println(t);
		}

		DataNode ng = DataManager.findByNameAndType(DataType.ITEM, "item_gold");
		for (int i = 0; i < 10; i++) {
			System.out.println(Item.generateFromNode(ng, w.getRndInt(1, 10), 0, w));
		}

		System.out.println("--------------------");

		DataNode lootTable = DataManager.findByNameAndType(DataType.LOOT_TABLE, "lootTable_all");
		LinkedList<Item> t = Item.generateFromLootTable(lootTable, 0, m);

		System.out.println(t);
	}

}
