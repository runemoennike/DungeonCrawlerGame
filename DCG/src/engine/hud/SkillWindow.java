/**
 * This class represents the skills window. It is used to train skills, as well, as 
 * browsing the players current skills.
 * 
 * It extends Pane and implements GuiMouseListener.
 */

package engine.hud;

import entities.actors.Player;
import gui.MouseEvent;
import gui.elements.AbstractElement;
import gui.elements.Button;
import gui.elements.Pane;
import gui.elements.ScrollingPane;
import gui.elements.TextArea;
import gui.implementables.GuiMouseListener;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

import skills.DefensiveSkill;
import skills.MagicSkill;
import skills.OffensiveSkill;
import skills.Skill;
import skills.SkillTree;
import skills.StatsSkill;
import skills.Skill.SkillType;

import com.jme.renderer.ColorRGBA;
import com.jmex.angelfont.BitmapFont.Align;

public class SkillWindow extends Pane implements GuiMouseListener {

	private static Player player;
	private static LinkedList<Skill> allPlayerSkills;

	private SkillType selTree;

	private ScrollingPane spnSkills;
	private Button btnClose;
	private Button btnOffensive, btnDefensive, btnMagic;

	private boolean requiresUpdate;

	public SkillWindow(Player player) {
		super("wndSkills", 51, 11, 48, 88);
		SkillWindow.player = player;
		this.setMouseListener(this);
		this.selTree = SkillType.OFFENSIVE;
	}

	@Override
	public void rebuild() {
		this.removeAllElements();

		SkillWindow.allPlayerSkills = SkillWindow.player.getAllSkills();

		// Tree selection buttons
		btnOffensive = new Button("btnOffensive", 1, this.height - 7, 14, 5, "Offensive");
		if (this.selTree.equals(SkillType.OFFENSIVE)) {
			btnOffensive.setBgTex("pane3.png");
		}
		this.addElement(btnOffensive);

		btnDefensive = new Button("btnDefensive", 17, this.height - 7, 14, 5, "Defensive");
		if (this.selTree.equals(SkillType.DEFENSIVE)) {
			btnDefensive.setBgTex("pane3.png");
		}
		this.addElement(btnDefensive);

		btnMagic = new Button("btnMagic", 33, this.height - 7, 14, 5, "Magic");
		if (this.selTree.equals(SkillType.MAGIC)) {
			btnMagic.setBgTex("pane3.png");
		}
		this.addElement(btnMagic);

		// Skill scroll pane
		spnSkills = new ScrollingPane("spnSkills", 1, 8, this.width - 2, this.height - 16);
		spnSkills.setMouseListener(this);
		this.addElement(spnSkills);

		// Skills
		LinkedList<Skill> skills = SkillTree.getSkillsByType(this.selTree);
		for (Skill s1 : allPlayerSkills) {
			skills.removeAll(SkillTree.getLowerLevelsOfSkill(s1));
		}
		Collections.sort(skills, new SkillSorter());

		TextArea dummytext = new TextArea("dummytext", 0, 0, 100, 0, "");
		this.addElement(dummytext);
		float lineHeight = dummytext.getLineHeight();
		dummytext.hide();

		for (Skill s : skills) {
			ColorRGBA color = ColorRGBA.red;
			if (SkillWindow.allPlayerSkills.contains(s)) {
				color = ColorRGBA.blue;
			} else if (SkillTree.skillAvailable(SkillWindow.player, s)) {
				color = ColorRGBA.green;
			}

			String text = s.toString();
			float height = text.split("\r\n|\r|\n").length * lineHeight + 1f;

			Pane pane = new Pane("skillPane" + s, 0, 0, spnSkills.getWidth() - 2, height);
			pane.setTexs("pane2.png", "pane2.png");
			pane.setAttachment(s);
			spnSkills.addContent(pane);

			TextArea ta = new TextArea("skillText" + s, 0, height, pane.getWidth(), height, text);
			ta.setAlign(Align.Left);
			ta.setColor(color);
			pane.addElement(ta);

			pane.setMouseListener(this);
		}

		// Close button
		btnClose = new Button("btnSkillClose", 39, 1, 8, 5, "Close");
		btnClose.setMouseListener(this);
		this.addElement(btnClose);

		// Available skill points
		Pane paneSkillPoints = new Pane("paneSSpoints", 1, 1, 20, 5);
		paneSkillPoints.setTexs("border2.png", "pane2.png");
		paneSkillPoints.setBorderWidth(2);
		this.addElement(paneSkillPoints);

		TextArea txtSkillPoints = new TextArea("txtSSpoints", 3, 5, 20, 5, "Skill points: "
				+ Integer.toString(SkillWindow.player.getAvailableSkillPoints()));
		txtSkillPoints.setAlign(Align.Left);
		this.addElement(txtSkillPoints);

		super.rebuild();
	}

	@Override
	public void update(float t) {
		if (this.requiresUpdate) {
			this.rebuild();
			this.requiresUpdate = false;
		}
	}

	@Override
	public void guiMouseClickEvent(AbstractElement element, MouseEvent e) {
		if (element.equals(this.btnClose)) {
			this.hide();
		} else if (element.equals(this.btnOffensive)) {
			this.selTree = SkillType.OFFENSIVE;
			this.rebuild();
		} else if (element.equals(this.btnDefensive)) {
			this.selTree = SkillType.DEFENSIVE;
			this.rebuild();
		} else if (element.equals(this.btnMagic)) {
			this.selTree = SkillType.MAGIC;
			this.rebuild();
		} else if (player.getAvailableSkillPoints() > 0 && element.getAttachment() != null) {
			if (element.getAttachment().getClass().equals(OffensiveSkill.class)) {
				OffensiveSkill s = (OffensiveSkill) element.getAttachment();
				if (SkillTree.skillAvailable(SkillWindow.player, s)) {
					SkillWindow.player.addOffensiveSkill(s);
					SkillWindow.player.removeAvailableSkillPoint();
				}
			} else if (element.getAttachment().getClass().equals(DefensiveSkill.class)) {
				DefensiveSkill s = (DefensiveSkill) element.getAttachment();
				if (SkillTree.skillAvailable(SkillWindow.player, s)) {
					SkillWindow.player.addDefensiveSkill(s);
					SkillWindow.player.removeAvailableSkillPoint();
				}
			} else if (element.getAttachment().getClass().equals(StatsSkill.class)) {
				StatsSkill s = (StatsSkill) element.getAttachment();
				if (SkillTree.skillAvailable(SkillWindow.player, s)) {
					SkillWindow.player.addStatsSkill(s);
					SkillWindow.player.removeAvailableSkillPoint();
				}
			} else if (element.getAttachment().getClass().equals(MagicSkill.class)) {
				MagicSkill s = (MagicSkill) element.getAttachment();
				if (SkillTree.skillAvailable(SkillWindow.player, s)) {
					SkillWindow.player.addMagicSkill(s);
					SkillWindow.player.removeAvailableSkillPoint();
				}
			}
			this.requiresUpdate = true;
		}
	}

	@Override
	public void guiMouseMoveEvent(AbstractElement element, MouseEvent e) {

	}

	private class SkillSorter implements Comparator<Skill> {

		public int compare(Skill s1, Skill s2) {
			if (allPlayerSkills.contains(s1) || allPlayerSkills.contains(s2)) {
				if (allPlayerSkills.contains(s1) && !allPlayerSkills.contains(s2)) {
					return -1;
				} else if (allPlayerSkills.contains(s2) && !allPlayerSkills.contains(s1)) {
					return 1;
				} else {
					return ((Integer) s1.playerLevelRequirement()).compareTo(s2.playerLevelRequirement());
				}
			} else if (SkillTree.skillAvailable(SkillWindow.player, s1)
					|| SkillTree.skillAvailable(SkillWindow.player, s2)) {
				if (SkillTree.skillAvailable(SkillWindow.player, s1)
						&& !SkillTree.skillAvailable(SkillWindow.player, s2)) {
					return -1;
				} else if (SkillTree.skillAvailable(SkillWindow.player, s2)
						&& !SkillTree.skillAvailable(SkillWindow.player, s1)) {
					return 1;
				} else {
					return ((Integer) s1.playerLevelRequirement()).compareTo(s2.playerLevelRequirement());
				}
			} else {
				return ((Integer) s1.playerLevelRequirement()).compareTo(s2.playerLevelRequirement());
			}
		}
	}

	public void requireUpdate() {
		this.requiresUpdate = true;
	}
}
