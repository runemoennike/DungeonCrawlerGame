/**
 * This class represents the character window, used for browsing and placing stats..
 * 
 * It extends Pane and implements GuiMouseListener.
 */

package engine.hud;

import entities.actors.Player;
import entities.items.Armor;
import gui.MouseEvent;
import gui.elements.AbstractElement;
import gui.elements.Button;
import gui.elements.Pane;
import gui.elements.TextArea;
import gui.implementables.GuiMouseListener;

public class CharacterWindow extends Pane implements GuiMouseListener {

	private Player player;

	Pane paneStr, paneAgi, paneVit, paneMag, paneHP, paneMP, paneDmg, paneArmor, paneHit, paneMagicHit, paneEva;
	Pane paneName, paneLvl, paneXP, paneAvailStatPoints;
	TextArea txtStr, txtAgi, txtVit, txtMag, txtHP, txtMP, txtDmg, txtArmor, txtHit, txtMagicHit, txtEva;
	TextArea txtName, txtLvl, txtXP, txtAvailStatPoint;
	Button btnClose, btnStrPlus, btnAgiPlus, btnVitPlus, btnMagPlus;

	private String checksum;

	public CharacterWindow(Player player) {
		super("wndCharacter", 1, 11, 48, 88);
		this.player = player;
		this.setMouseListener(this);
	}

	@Override
	public void rebuild() {
		this.removeAllElements();

		// Name
		paneName = new Pane("paneCsName", 2, 80, 44, 5);
		paneName.setTexs("border2.png", "pane2.png");
		paneName.setBorderWidth(2);
		this.addElement(paneName);

		txtName = new TextArea("txtCsName", 2, 84, 44, 5, "Pjaskebamse of Doom");
		this.addElement(txtName);

		// Level/experience
		paneLvl = new Pane("paneCsLvl", 2, 73, 12, 5);
		paneLvl.setTexs("border2.png", "pane2.png");
		paneLvl.setBorderWidth(2);
		this.addElement(paneLvl);

		txtLvl = new TextArea("txtCsLvl", 2, 77, 12, 5, "Level: " + this.player.getLevel());
		this.addElement(txtLvl);

		paneXP = new Pane("paneCsXP", 16, 73, 30, 5);
		paneXP.setTexs("border2.png", "pane2.png");
		paneXP.setBorderWidth(2);
		this.addElement(paneXP);

		txtXP = new TextArea("txtCsXP", 16, 77, 30, 5, "XP: " + this.player.getXp() + " / "
				+ this.player.getXpForNextLevel());
		this.addElement(txtXP);

		// Strength
		paneStr = new Pane("paneCsStr", 2, 66, 15, 5);
		paneStr.setTexs("border2.png", "pane2.png");
		paneStr.setBorderWidth(2);
		this.addElement(paneStr);

		txtStr = new TextArea("txtCsStr", 2, 70, 15, 5, "Strength: " + this.player.getCurStats().getStr());
		this.addElement(txtStr);

		// Damage
		paneDmg = new Pane("paneCsDmg", 22, 66, 24, 5);
		paneDmg.setTexs("border2.png", "pane2.png");
		paneDmg.setBorderWidth(2);
		this.addElement(paneDmg);

		txtDmg = new TextArea("txtCsDmg", 22, 70, 24, 5, "Damage: " + this.player.getPhysicalMinDmg() + "-"
				+ this.player.getPhysicalMaxDmg());
		this.addElement(txtDmg);

		// Armor
		paneArmor = new Pane("paneCsArmor", 22, 58, 24, 5);
		paneArmor.setTexs("border2.png", "pane2.png");
		paneArmor.setBorderWidth(2);
		this.addElement(paneArmor);

		int armor = this.player.getArmor();
		txtArmor = new TextArea("txtCsArmor", 22, 62, 24, 5, "Armor: " + armor + " ("
				+ (100 - Armor.reduceDmgByArmor(100, armor)) + "%)");
		this.addElement(txtArmor);

		// hit
		paneHit = new Pane("paneCsHit", 22, 50, 24, 5);
		paneHit.setTexs("border2.png", "pane2.png");
		paneHit.setBorderWidth(2);
		this.addElement(paneHit);

		txtHit = new TextArea("txtCsHit", 22, 54, 24, 5, "Hit Chance: " + this.player.getCurAttack().getHitChance()
				+ "%");
		this.addElement(txtHit);

		// evasion
		paneEva = new Pane("paneCsEva", 22, 42, 24, 5);
		paneEva.setTexs("border2.png", "pane2.png");
		paneEva.setBorderWidth(2);
		this.addElement(paneEva);

		txtEva = new TextArea("txtCsEva", 22, 46, 24, 5, "Evasion Chance: "
				+ this.player.getCurStats().getEvasionChance() + "%");
		this.addElement(txtEva);

		// Agility
		paneAgi = new Pane("paneCsAgi", 2, 50, 15, 5);
		paneAgi.setTexs("border2.png", "pane2.png");
		paneAgi.setBorderWidth(2);
		this.addElement(paneAgi);

		txtAgi = new TextArea("txtCsAgi", 2, 54, 15, 5, "Agility: " + this.player.getCurStats().getAgi());
		this.addElement(txtAgi);

		// ???

		// Vitality
		paneVit = new Pane("paneCsVit", 2, 34, 15, 5);
		paneVit.setTexs("border2.png", "pane2.png");
		paneVit.setBorderWidth(2);
		this.addElement(paneVit);

		txtVit = new TextArea("txtCsVit", 2, 38, 15, 5, "Vitality: " + this.player.getCurStats().getVit());
		this.addElement(txtVit);

		// HP
		paneHP = new Pane("paneCsHP", 22, 34, 24, 5);
		paneHP.setTexs("border2.png", "pane2.png");
		paneHP.setBorderWidth(2);
		this.addElement(paneHP);

		txtHP = new TextArea("txtCsHP", 22, 38, 24, 5, "HP: " + this.player.getCurStats().getTotalHp() + "/"
				+ this.player.getTotalStats().getTotalHp());
		this.addElement(txtHP);

		// Magic
		paneMag = new Pane("paneCsMag", 2, 18, 15, 5);
		paneMag.setTexs("border2.png", "pane2.png");
		paneMag.setBorderWidth(2);
		this.addElement(paneMag);

		txtMag = new TextArea("txtCsMag", 2, 22, 15, 5, "Magic: " + this.player.getCurStats().getMag());
		this.addElement(txtMag);

		// MP
		paneMP = new Pane("paneCsMP", 22, 18, 24, 5);
		paneMP.setTexs("border2.png", "pane2.png");
		paneMP.setBorderWidth(2);
		this.addElement(paneMP);

		txtMP = new TextArea("txtCsMP", 22, 22, 24, 5, "Mana: " + this.player.getCurStats().getTotalMana() + "/"
				+ this.player.getTotalStats().getTotalMana());
		this.addElement(txtMP);

		// cast hit
		paneMagicHit = new Pane("paneCsMagicHit", 22, 10, 24, 5);
		paneMagicHit.setTexs("border2.png", "pane2.png");
		paneMagicHit.setBorderWidth(2);
		this.addElement(paneMagicHit);

		txtMagicHit = new TextArea("txtCsMagicHit", 22, 14, 24, 5, "Cast Hit Chance: "
				+ this.player.getCurStats().getMagicHitChance() + "%");
		this.addElement(txtMagicHit);

		// Close button
		btnClose = new Button("btnCsClose", 2, 2, 8, 5, "Close");
		btnClose.setMouseListener(this);
		this.addElement(btnClose);

		// Unused stats
		if (this.player.getAvailableStatPoints() > 0) {
			btnStrPlus = new Button("strPlus", 17, 66, 5, 5, "+");
			btnStrPlus.setMouseListener(this);
			this.addElement(btnStrPlus);

			btnAgiPlus = new Button("agiPlus", 17, 50, 5, 5, "+");
			btnAgiPlus.setMouseListener(this);
			this.addElement(btnAgiPlus);

			btnVitPlus = new Button("vitPlus", 17, 34, 5, 5, "+");
			btnVitPlus.setMouseListener(this);
			this.addElement(btnVitPlus);

			btnMagPlus = new Button("magPlus", 17, 18, 5, 5, "+");
			btnMagPlus.setMouseListener(this);
			this.addElement(btnMagPlus);

			paneAvailStatPoints = new Pane("paneAvailStatPoints", 12, 10, 5, 5);
			paneAvailStatPoints.setTexs("border2.png", "pane2.png");
			paneAvailStatPoints.setBorderWidth(2);
			this.addElement(paneAvailStatPoints);

			txtAvailStatPoint = new TextArea("txtAvailStatPoint", 12, 14, 5, 5, ""
					+ this.player.getAvailableStatPoints());
			this.addElement(txtAvailStatPoint);
		}

		super.rebuild();

		this.checksum = calcChecksum();
	}

	private String calcChecksum() {
		return this.player.getCurStats().toString() + this.player.getXp() + this.player.getPhysicalMinDmg()
				+ this.player.getPhysicalMaxDmg() + this.player.getCurAttack().getHitChance()
				+ this.player.getCurStats().getMagicHitChance() + this.player.getArmor()
				+ this.player.getCurStats().getEvasionChance();
	}

	@Override
	public boolean handleMouse(MouseEvent e) {
		return super.handleMouse(e);
	}

	@Override
	public void update(float t) {
		if (!this.checksum.equals(calcChecksum())) {
			this.rebuild();

			this.checksum = calcChecksum();
		}
	}

	@Override
	public void guiMouseClickEvent(AbstractElement element, MouseEvent e) {
		if (element.equals(this.btnClose)) {
			this.hide();
		} else if (element.equals(this.btnStrPlus)) {
			this.player.getBaseStats().addStr();
			this.player.removeAvailableStatPoint();
			this.player.updateStats();
			this.rebuild();
		} else if (element.equals(this.btnAgiPlus)) {
			this.player.getBaseStats().addAgi();
			this.player.removeAvailableStatPoint();
			this.player.updateStats();
			this.rebuild();
		} else if (element.equals(this.btnVitPlus)) {
			this.player.getBaseStats().addVit();
			this.player.removeAvailableStatPoint();
			this.player.updateStats();
			this.rebuild();
		} else if (element.equals(this.btnMagPlus)) {
			this.player.getBaseStats().addMag();
			this.player.removeAvailableStatPoint();
			this.player.updateStats();
			this.rebuild();
		}
	}

	@Override
	public void guiMouseMoveEvent(AbstractElement element, MouseEvent e) {

	}

}
