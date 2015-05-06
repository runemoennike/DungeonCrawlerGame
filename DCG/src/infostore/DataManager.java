/**
 * The DataManager first retrieves all xml data, and stores it in DataNode. It 
 * then supplies methods for retrieving single or lists of DataNodes with different filters.
 */

package infostore;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.LinkedList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import engine.hud.ConsoleLog;

public class DataManager {

	private static DataNode root = new DataNode();

	public enum DataType {
		DOODAD("doodad"), ITEM("item"), LOOT_TABLE("lootTable"), MODEL_ID("modelID"), MONSTER("monster"), PREFIX(
				"prefix"), SURFIX("postfix"), MAGIC("magic"), OFFENSIVE_SKILL("offensiveskill"), DEFENSIVE_SKILL(
				"defensiveskill"), STATS_SKILL("statsskill"), MAGIC_SKILL("magicskill"), BOOKFIX("bookfix"), STORY(
				"story"), ;

		public String ident;
		private DataType(String ident) {
			this.ident = ident;
		}
	}

	public static LinkedList<DataNode> findAllByType(DataType type) {
		return root.getList(type.ident);
	}

	public static DataNode findByNameAndType(DataType type, String name) {
		LinkedList<DataNode> list = root.getList(type.ident);
		for (DataNode n : list) {
			if (name.equals(n.getProp("name"))) {
				return n;
			}
		}

		return null;
	}

	public static DataNode findByName(String name) {
		for (String k : root.getKeys()) {
			if (!root.isProp(k)) {
				LinkedList<DataNode> list = root.getList(k);
				for (DataNode n : list) {
					if (name.equals(n.getProp("name"))) {
						return n;
					}
				}
			}
		}

		return null;
	}

	public static LinkedList<DataNode> findAllByTypeWhereContains(DataType type, String attr, String contains) {
		LinkedList<DataNode> all = null;
		LinkedList<DataNode> result = new LinkedList<DataNode>();

		all = root.getList(type.ident);

		for (DataNode n : all) {
			if (n.getProp(attr).contains(contains)) {
				result.add(n);
			}
		}

		return result;
	}

	public static void load() {
		try {
			URL masterfile = Thread.currentThread().getContextClassLoader().getResource("dcg/data/info/files.lst");
			BufferedReader in = new BufferedReader(new InputStreamReader(masterfile.openStream()));
			String str;

			while ((str = in.readLine()) != null) {
				loadXML(Thread.currentThread().getContextClassLoader().getResource("dcg/data/info/" + str));
			}

			in.close();
		} catch (Exception e) {
			System.out.println("Failed loading files. " + e.toString());
			e.printStackTrace();
		}

		System.out.println("Database size: " + DataManager.root.bytesize() + " bytes.");
	}

	public static void dump() {
		System.out.println(root);
	}

	private static void loadXML(URL url) {
		System.out.println("DataManager: Loading " + url.toString());

		DocumentBuilder builder = null;
		try {
			builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		} catch (ParserConfigurationException e1) {
			e1.printStackTrace();
		}

		Document document = null;
		try {
			document = builder.parse(url.openStream());
		} catch (Exception e) {
			e.printStackTrace();
		}

		NodeList nodes_i = document.getDocumentElement().getChildNodes();
		for (int i = 0; i < nodes_i.getLength(); i++) {
			Node node_i = nodes_i.item(i);
			parseXMLnode(node_i, root);
		}

		ConsoleLog.addLine("DataManager: Loaded " + url.toString());

	}

	private static void parseXMLnode(Node xmlNode, DataNode dataNode) {
		switch (xmlNode.getNodeType()) {
			case Node.ELEMENT_NODE :
				DataNode elemDataNode = new DataNode();
				elemDataNode.setProp("_self", xmlNode.getNodeName());
				dataNode.addChild(xmlNode.getNodeName(), elemDataNode);
				for (int i = 0; i < xmlNode.getChildNodes().getLength(); i++) {
					parseXMLnode(xmlNode.getChildNodes().item(i), elemDataNode);
				}
				for (int i = 0; i < xmlNode.getAttributes().getLength(); i++) {
					parseXMLnode(xmlNode.getAttributes().item(i), elemDataNode);
				}

				break;
			case Node.ATTRIBUTE_NODE :
				dataNode.setProp(xmlNode.getNodeName(), xmlNode.getNodeValue());
				break;
		}

	}
}
