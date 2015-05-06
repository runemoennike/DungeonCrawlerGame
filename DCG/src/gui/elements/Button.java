/**
 * A simple button. Uses a pane and a label. Has a caption and textures.
 */
package gui.elements;

public class Button extends AbstractElement {

	private String bgTex = "button.png";
	private String borderTex = "border.png";
	private String caption;
	private boolean depressed;

	public Button(String id, float left, float top, float width, float height, String caption) {
		super(id, left, top, width, height);

		this.setCaption(caption);
	}

	@Override
	public void rebuild() {
		this.removeAllElements();

		Pane pane = new Pane(this.getId(), 0, 0, this.getWidth(), this.getHeight());
		pane.setTexs(borderTex, bgTex);
		pane.setBorderWidth(2);
		this.addElement(pane);

		Label label = new Label(this.getId(), 0, 0, this.caption);
		this.addElement(label);
		label.setLeft(this.getWidth() / 2 - label.getWidth() / 2);
		label.setTop(this.getHeight() / 2 - label.getHeight() / 2);

		this.getNode().detachAllChildren();
		super.rebuild();
	}

	public void setCaption(String caption) {
		this.caption = caption;
	}

	public String getCaption() {
		return caption;
	}

	public void setDepressed(boolean depressed) {
		this.depressed = depressed;
	}

	public boolean isDepressed() {
		return depressed;
	}

	public void setBgTex(String bgTex) {
		this.bgTex = bgTex;
	}

	public void setBorderTex(String borderTex) {
		this.borderTex = borderTex;
	}

	public void setTexs(String border, String bg) {
		setBorderTex(border);
		setBgTex(bg);
	}
}
