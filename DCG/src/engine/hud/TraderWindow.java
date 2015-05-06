/**
 * This class represents the vendor or trader window, used for buying and selling items.
 * 
 * It extends Pane and implements GuiMouseListener.
 */

package engine.hud;

import entities.actors.Player;
import entities.items.Item;
import gui.MouseEvent;
import gui.elements.AbstractElement;
import gui.elements.Button;
import gui.elements.Pane;
import gui.elements.ScrollingPane;
import gui.elements.TextArea;
import gui.implementables.GuiMouseListener;

import java.util.ArrayList;

import map.Sanctuary;

import com.jmex.angelfont.BitmapFont.Align;

public class TraderWindow extends Pane implements GuiMouseListener {

	private static Player player;

	private enum ShopTab {
		WEAPON, ARMOR, CONSUMABLE
	}

	private ShopTab selTab;

	private ScrollingPane spnItems;
	private Button btnClose;
	private Button btnWeapons, btnArmors, btnConsumables;

	private Sanctuary sanc;

	private boolean requiresUpdate;

	public TraderWindow(Player player) {
		super("wndTrader", 1, 11, 48, 88);
		TraderWindow.player = player;
		this.setMouseListener(this);
		this.selTab = ShopTab.CONSUMABLE;
	}

	@Override
	public void rebuild() {
		this.removeAllElements();

		this.sanc = TraderWindow.player.getWorld().getSanctuary();

		if (this.sanc == null) {
			System.out.println("Sanctuary is null");
			return;
		}

		// Tree selection buttons
		btnWeapons = new Button("btnWeapons", 1, this.height - 7, 14, 5, "Weapons");
		if (this.selTab.equals(ShopTab.WEAPON)) {
			btnWeapons.setBgTex("pane3.png");
		}
		this.addElement(btnWeapons);

		btnArmors = new Button("btnArmors", 17, this.height - 7, 14, 5, "Armors");
		if (this.selTab.equals(ShopTab.ARMOR)) {
			btnArmors.setBgTex("pane3.png");
		}
		this.addElement(btnArmors);

		btnConsumables = new Button("btnConsumables", 33, this.height - 7, 14, 5, "Consumables");
		if (this.selTab.equals(ShopTab.CONSUMABLE)) {
			btnConsumables.setBgTex("pane3.png");
		}
		this.addElement(btnConsumables);

		// Skill scroll pane
		spnItems = new ScrollingPane("spnItems", 1, 8, this.width - 2, this.height - 16);
		spnItems.setMouseListener(this);
		this.addElement(spnItems);

		// Skills
		ArrayList<Item> items = new ArrayList<Item>();
		switch (this.selTab) {
			case WEAPON :
				items.addAll(sanc.getWeapons());
				break;
			case ARMOR :
				items.addAll(sanc.getArmors());
				break;
			case CONSUMABLE :
				items.addAll(sanc.getConsumables());
				break;
		}

		TextArea dummytext = new TextArea("dummytext", 0, 0, 100, 0, "");
		this.addElement(dummytext);
		float lineHeightC = dummytext.getLineHeight();
		dummytext.setSize(13);
		dummytext.rebuild();
		float lineHeightT = dummytext.getLineHeight();
		dummytext.hide();

		for (Item item : items) {
			String text = item.getInfoString();
			String caption = item.getCaption();
			float height = text.split("\r\n|\r|\n").length * lineHeightT + 1 * lineHeightC + 1f;

			Pane pane = new Pane("itemPane" + item.getId(), 0, 0, spnItems.getWidth() - 2, height);
			pane.setTexs("pane2.png", "pane2.png");
			pane.setAttachment(item);
			spnItems.addContent(pane);

			TextArea tc = new TextArea("itemCaption" + item.getId(), 0, height, pane.getWidth(), lineHeightC, caption);
			tc.setColor(item.getQualityColor());
			tc.setAlign(Align.Left);
			pane.addElement(tc);

			TextArea ta = new TextArea("itemText" + item.getId(), 0, height - lineHeightC, pane.getWidth(), height,
					text);
			ta.setAlign(Align.Left);
			ta.setSize(13);
			pane.addElement(ta);

			pane.setMouseListener(this);
		}

		// Close button
		btnClose = new Button("btnSkillClose", 39, 1, 8, 5, "Close");
		btnClose.setMouseListener(this);
		this.addElement(btnClose);

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
	public void show() {
		this.requiresUpdate = true;
		super.show();
	}

	@Override
	public void guiMouseClickEvent(AbstractElement element, MouseEvent e) {
		if (element.equals(this.btnClose)) {
			this.hide();
		} else if (element.equals(this.btnArmors)) {
			this.selTab = ShopTab.ARMOR;
			this.rebuild();
		} else if (element.equals(this.btnWeapons)) {
			this.selTab = ShopTab.WEAPON;
			this.rebuild();
		} else if (element.equals(this.btnConsumables)) {
			this.selTab = ShopTab.CONSUMABLE;
			this.rebuild();
		} else if (element.getId().startsWith("itemPane") && element.getAttachment() != null) {
			TraderWindow.player.getWorld().getSanctuary().buyItem(TraderWindow.player, (Item) element.getAttachment());
			this.requiresUpdate = true;
		}
	}

	@Override
	public void guiMouseMoveEvent(AbstractElement element, MouseEvent e) {

	}

	public void requireUpdate() {
		this.requiresUpdate = true;
	}
}
