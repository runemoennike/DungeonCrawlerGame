/**
 * This class in used by the DataManager to store the data loaded from the xml-files.
 * A DataNode can has any number of children, but just one parent. The general structure
 * resembles the xml structure.
 */

package infostore;

import java.util.HashMap;
import java.util.LinkedList;

public class DataNode {
	private HashMap<String, String> props = new HashMap<String, String>();
	private HashMap<String, DataNode> children = new HashMap<String, DataNode>();
	private HashMap<String, LinkedList<DataNode>> lists = new HashMap<String, LinkedList<DataNode>>();
	private DataNode parent = null;
	private boolean ignoreErrors = true;

	public boolean isProp(String child) {
		child = child.toLowerCase();
		return this.props.containsKey(child);
	}

	public boolean isList(String child) {
		child = child.toLowerCase();
		return this.lists.containsKey(child);
	}

	public boolean isChild(String child) {
		child = child.toLowerCase();
		return this.children.containsKey(child);
	}

	public int bytesize() {
		int result = 0;

		for (String s : this.props.keySet()) {
			result += s.length() * 2 + this.props.get(s).length() * 2;
		}

		for (String s : this.children.keySet()) {
			result += s.length() * 2 + this.children.get(s).bytesize();
		}

		for (String s : this.lists.keySet()) {
			result += s.length() * 2;
			for (DataNode n : this.lists.get(s)) {
				result += s.length() * 2 + n.bytesize();
			}
		}

		return result;
	}

	public String getProp(String key) {
		key = key.toLowerCase();
		if (isProp(key)) {
			return this.props.get(key);
		}
		if (!this.ignoreErrors) {
			System.out.println("DataNode: No prop " + key + " (Tree: " + dumpParent() + ")");
		}
		return "";
	}

	private String getPropNoFail(String key) {
		if (isProp(key)) {
			return this.props.get(key);
		}
		return "";
	}

	public String dumpParent() {
		if (this.parent != null) {
			return this.getPropNoFail("_self") + "/" + this.getPropNoFail("name") + ", " + this.parent.dumpParent();
		} else {
			return this.getPropNoFail("name");
		}
	}

	public Float getPropF(String key) {
		if (this.ignoreErrors && !isProp(key)) {
			return 0f;
		}
		return Float.parseFloat(getProp(key));
	}

	public Integer getPropI(String key) {
		if (this.ignoreErrors && !isProp(key)) {
			return 0;
		}
		return Integer.parseInt(getProp(key));
	}

	public Boolean getPropB(String key) {
		if (this.ignoreErrors && !isProp(key)) {
			return false;
		}
		return Boolean.parseBoolean(getProp(key));
	}

	public LinkedList<String> getKeys() {
		LinkedList<String> result = new LinkedList<String>();

		result.addAll(props.keySet());
		result.addAll(children.keySet());
		result.addAll(lists.keySet());

		return result;
	}

	public DataNode getChild(String key) {
		key = key.toLowerCase();
		if (isChild(key)) {
			return this.children.get(key);
		}
		return null;
	}

	public LinkedList<DataNode> getList(String key) {
		key = key.toLowerCase();
		if (isList(key)) {
			return this.lists.get(key);
		} else if (isChild(key)) {
			LinkedList<DataNode> r = new LinkedList<DataNode>();
			r.add(this.children.get(key));
			return r;
		}
		return null;
	}

	public void setProp(String key, String value) {
		key = key.toLowerCase();
		this.props.put(key, value);
	}

	public void addChild(String key, DataNode value) {
		key = key.toLowerCase();
		value.parent = this;
		if (isChild(key)) {
			this.lists.put(key, new LinkedList<DataNode>());
			this.lists.get(key).add(this.children.remove(key));
			this.lists.get(key).add(value);
		} else if (isList(key)) {
			this.lists.get(key).add(value);
		} else {
			this.children.put(key, value);
		}
	}

	public void addList(String key, LinkedList<DataNode> values) {
		key = key.toLowerCase();
		for (DataNode n : values) {
			n.parent = this;
		}

		if (this.isList(key)) {
			this.lists.get(key).addAll(values);
		} else {
			this.lists.put(key, new LinkedList<DataNode>());
			this.lists.get(key).addAll(values);

			if (isChild(key)) {
				this.lists.get(key).add(this.children.remove(key));
			}
		}
	}

	public String toString() {
		return this.dump(0);
	}

	public String dump(int level) {
		String result = "";
		String tab = "";

		for (int i = 0; i < level; i++) {
			tab += "\t";
		}

		for (String key : this.props.keySet()) {
			result += tab + key + ": " + this.props.get(key) + "\n";
		}

		for (String key : this.children.keySet()) {
			result += tab + key + "\n" + this.children.get(key).dump(level + 1);
		}

		for (String key : this.lists.keySet()) {
			for (DataNode dn : this.lists.get(key)) {
				result += tab + key + "\n" + dn.dump(level + 1);
			}
		}

		return result;
	}

	public DataNode getParent() {
		return this.parent;
	}

	public void setIgnoreErrors(boolean b) {
		this.ignoreErrors = b;
	}
}
