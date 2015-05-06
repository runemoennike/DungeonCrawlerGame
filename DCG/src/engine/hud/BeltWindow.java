/**
 * This class represents the belt window in the gui.
 * 
 * It extends AbstractElement and implements GuiMouseListener.
 */

package engine.hud;

import misc.Inventory.BELTSPOT;
import entities.actors.Player;
import gui.MouseEvent;
import gui.elements.AbstractElement;
import gui.elements.Image;
import gui.elements.Pane;
import gui.elements.TextArea;
import gui.implementables.GuiMouseListener;

public class BeltWindow extends AbstractElement implements GuiMouseListener {

	private Player player;
	private TextArea txtCountHP, txtCountHPL, txtCountMP, txtCountMPL;
	private int checksum;

	public BeltWindow(Player player) {
		super("wndBelt", 32, 0, 30, 10);
		this.player = player;
		this.setMouseListener(this);
	}

	@Override
	public void rebuild() {
		this.removeAllElements();

		Pane bg = new Pane("paneBeltBG", 1, 1, 28, 8);
		bg.setBorderWidth(6);
		this.addElement(bg);

		// HP

		Pane paneSlot1 = new Pane("paneBeltSlot1", 2, 1.5f, (InventoryWindow.bagSlotW + 1),
				(InventoryWindow.bagSlotH + 1));
		paneSlot1.setTexs("border2.png", "pane2.png");
		paneSlot1.setBorderWidth(2);
		this.addElement(paneSlot1);
		Image imgHP = new Image("imgBeltHP", "bag_hpot.png", 2.5f, 2f, InventoryWindow.bagSlotW,
				InventoryWindow.bagSlotH);
		this.addElement(imgHP);
		this.txtCountHP = new TextArea("txtBeltHP", 2f, 5f, InventoryWindow.bagSlotW + 1, 5, "0");
		this.addElement(this.txtCountHP);

		Pane paneSlot2 = new Pane("paneBeltSlot2", 2 + (InventoryWindow.bagSlotW + 2) * 1, 1.5f,
				(InventoryWindow.bagSlotW + 1), (InventoryWindow.bagSlotH + 1));
		paneSlot2.setTexs("border2.png", "pane2.png");
		paneSlot2.setBorderWidth(2);
		this.addElement(paneSlot2);
		Image imgHPL = new Image("imgBeltHP", "bag_hpot_large.png", 2.5f + (InventoryWindow.bagSlotW + 2) * 1, 2f,
				InventoryWindow.bagSlotW, InventoryWindow.bagSlotH);
		this.addElement(imgHPL);
		this.txtCountHPL = new TextArea("txtBeltHPL", 2f + (InventoryWindow.bagSlotW + 2) * 1, 5f,
				InventoryWindow.bagSlotW + 1, 5, "0");
		this.addElement(this.txtCountHPL);

		// Mana

		Pane paneSlot3 = new Pane("paneBeltSlot3", 2 + (InventoryWindow.bagSlotW + 2) * 2, 1.5f,
				(InventoryWindow.bagSlotW + 1), (InventoryWindow.bagSlotH + 1));
		paneSlot3.setTexs("border2.png", "pane2.png");
		paneSlot3.setBorderWidth(2);
		this.addElement(paneSlot3);
		Image imgMP = new Image("imgBeltMP", "bag_mpot.png", 2.5f + (InventoryWindow.bagSlotW + 2) * 2, 2f,
				InventoryWindow.bagSlotW, InventoryWindow.bagSlotH);
		this.addElement(imgMP);
		this.txtCountMP = new TextArea("txtBeltMP", 2f + (InventoryWindow.bagSlotW + 2) * 2, 5f,
				InventoryWindow.bagSlotW + 1, 5, "0");
		this.addElement(this.txtCountMP);

		Pane paneSlot4 = new Pane("paneBeltSlot4", 2 + (InventoryWindow.bagSlotW + 2) * 3, 1.5f,
				(InventoryWindow.bagSlotW + 1), (InventoryWindow.bagSlotH + 1));
		paneSlot4.setTexs("border2.png", "pane2.png");
		paneSlot4.setBorderWidth(2);
		this.addElement(paneSlot4);
		Image imgMPL = new Image("imgBeltMPL", "bag_mpot_large.png", 2.5f + (InventoryWindow.bagSlotW + 2) * 3, 2f,
				InventoryWindow.bagSlotW, InventoryWindow.bagSlotH);
		this.addElement(imgMPL);
		this.txtCountMPL = new TextArea("txtBeltMPL", 2f + (InventoryWindow.bagSlotW + 2) * 3, 5f,
				InventoryWindow.bagSlotW + 1, 5, "0");
		this.addElement(this.txtCountMPL);

		this.checksum = calcChecksum();
	}

	private int calcChecksum() {
		int checksum = this.player.getInventory().getBeltSpotCount(BELTSPOT.HPOT)
				+ this.player.getInventory().getBeltSpotCount(BELTSPOT.HPOT_LARGE)
				+ this.player.getInventory().getBeltSpotCount(BELTSPOT.MPOT)
				+ this.player.getInventory().getBeltSpotCount(BELTSPOT.MPOT_LARGE);
		return checksum;
	}

	@Override
	public boolean handleMouse(MouseEvent e) {
		return super.handleMouse(e);
	}

	@Override
	public void show() {
		super.show();
	}

	@Override
	public void update(float t) {
		if (this.checksum != calcChecksum()) {
			this.checksum = calcChecksum();
			this.txtCountHP.setText(Integer.toString(this.player.getInventory().getBeltSpotCount(BELTSPOT.HPOT)));
			this.txtCountHP.rebuild();
			this.txtCountHPL
					.setText(Integer.toString(this.player.getInventory().getBeltSpotCount(BELTSPOT.HPOT_LARGE)));
			this.txtCountHPL.rebuild();
			this.txtCountMP.setText(Integer.toString(this.player.getInventory().getBeltSpotCount(BELTSPOT.MPOT)));
			this.txtCountMP.rebuild();
			this.txtCountMPL
					.setText(Integer.toString(this.player.getInventory().getBeltSpotCount(BELTSPOT.MPOT_LARGE)));
			this.txtCountMPL.rebuild();
		}
	}

	@Override
	public void guiMouseClickEvent(AbstractElement element, MouseEvent e) {
		if (element.getId().equals("paneBeltSlot1")) {
			this.player.useBeltSlot(BELTSPOT.HPOT);
		} else if (element.getId().equals("paneBeltSlot2")) {
			this.player.useBeltSlot(BELTSPOT.HPOT_LARGE);
		} else if (element.getId().equals("paneBeltSlot3")) {
			this.player.useBeltSlot(BELTSPOT.MPOT);
		} else if (element.getId().equals("paneBeltSlot4")) {
			this.player.useBeltSlot(BELTSPOT.MPOT_LARGE);
		}
	}

	@Override
	public void guiMouseMoveEvent(AbstractElement element, MouseEvent e) {

	}

}
