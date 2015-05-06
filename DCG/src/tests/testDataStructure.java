/**
 * Used for testing the DataNode.
 */

package tests;

import infostore.DataNode;

public class testDataStructure {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		DataNode trunk = new DataNode();

		trunk.setProp("win", "yes");
		trunk.setProp("cool", "indeed");

		DataNode subnode = new DataNode();

		subnode.setProp("asd", "lj");
		subnode.setProp("eqw", "wgere");
		subnode.setProp("gsa", "fsakjl asd");

		trunk.addChild("SUBNODE!", subnode);

		DataNode listnode = new DataNode();
		listnode.setProp("asl", "opi");
		listnode.setProp("rqesdf", "tewi");

		DataNode listnode2 = new DataNode();
		listnode2.setProp("efd", "regf");
		listnode2.setProp("æjl", "bnp");

		subnode.addChild("randomlist", listnode);
		subnode.addChild("randomlist", listnode2);

		DataNode root = new DataNode();
		root.addChild("data", trunk);

		System.out.println(root);

	}

}
