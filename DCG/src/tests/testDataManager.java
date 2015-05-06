/**
 * Used for testing the DataManager.
 */

package tests;

import infostore.DataManager;
import infostore.DataNode;
import infostore.DataManager.DataType;

import java.util.LinkedList;

public class testDataManager {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		DataManager.load();

		dumpList(DataManager.findAllByType(DataType.DOODAD));

		System.out.println(DataManager.findByNameAndType(DataType.MODEL_ID, "modelID_zombie"));

		System.out.println("----------------------------------------");

		System.out.println(DataManager.findByName("monster_zombie"));
	}

	private static void dumpList(LinkedList<DataNode> list) {
		System.out.println("--------------------------------------");
		for (DataNode n : list) {
			System.out.println(n.toString());
			System.out.println("--------------------------------------");
		}

	}

}
