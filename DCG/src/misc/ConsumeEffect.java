/**
 * ConsumeEffects are contained by ConsumableItems and can apply stats to Actors
 * or teach them spells using the apply method.
 */

package misc;

import engine.hud.ConsoleLog;
import entities.actors.Player;
import infostore.DataManager;
import infostore.DataNode;
import infostore.DataManager.DataType;

public class ConsumeEffect {
	public enum ConsumeTargetStat {
		CURRENT, TOTAL
	};

	private Stats stats;
	private ConsumeTargetStat target;
	private DataNode magicNode;

	public ConsumeEffect(DataNode n, int level) {
		this.magicNode = DataManager.findByNameAndType(DataType.MAGIC, n.getProp("magic"));
		if (n.isChild("stats")) {
			this.stats = new Stats(n.getChild("stats"));
			Stats scale = new Stats(n.getChild("scale"), level);
			this.stats.addStatsToThis(scale);
		}
		if (!n.getProp("target").isEmpty()) {
			this.target = ConsumeTargetStat.valueOf(n.getProp("target").toUpperCase());
		}
	}

	public ConsumeEffect(DataNode n) {
		this.magicNode = DataManager.findByNameAndType(DataType.MAGIC, n.getProp("magic"));
		if (n.isChild("stats")) {
			this.stats = new Stats(n.getChild("stats"));
		}
		if (!n.getProp("target").isEmpty()) {
			this.target = ConsumeTargetStat.valueOf(n.getProp("target").toUpperCase());
		}
	}

	public boolean hasMagic() {
		return this.magicNode != null;
	}

	public Stats getStats() {
		return this.stats;
	}

	public Magic getMagic(int level) {
		if (this.magicNode != null) {
			return new Magic(this.magicNode, level);
		}
		return null;
	}

	public ConsumeTargetStat getTarget() {
		return this.target;
	}

	public void apply(Player p) {
		if (this.magicNode != null) {
			Magic newM;
			for (Magic m : p.getMagics()) {
				if (m.getName().equals(this.magicNode.getProp("name"))) {
					p.removeMagic(m);
					newM = this.getMagic(m.getLevel() + 1);
					p.addMagic(newM);
					ConsoleLog.addLine(p.getName() + " learned " + newM.toString() + ".");
					return;
				}
			}
			newM = this.getMagic(1);
			p.addMagic(newM);
			ConsoleLog.addLine(p.getName() + " learned " + newM.toString() + ".");
		}
	}
	@Override
	public String toString() {
		return target + ": " + stats;
	}
}
