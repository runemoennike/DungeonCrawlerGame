package thirdParty;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

import com.jme.image.Image;

public abstract class PaintableImage extends Image {
	private BufferedImage backImg;
	private ByteBuffer scratch;
	
	public PaintableImage(int width, int height, boolean hasAlpha) {
		super();
		try {
			backImg = new BufferedImage(width, height, hasAlpha
					? BufferedImage.TYPE_4BYTE_ABGR 
					: BufferedImage.TYPE_3BYTE_BGR);
			setFormat(hasAlpha ? com.jme.image.Image.Format.RGBA8 : com.jme.image.Image.Format.RGB8);
			setWidth(backImg.getWidth());
			setHeight(backImg.getHeight());
			scratch = ByteBuffer.allocateDirect(4 * backImg.getWidth() * backImg.getHeight());
		} catch (IllegalArgumentException e) {
			System.out.println("Problem creating buffered Image: " + e.getMessage());
		}
	}
	
	public void refreshImage() {
		Graphics2D g = backImg.createGraphics();
		paint(g);
		g.dispose();

		/* get the image data */
		byte data[] = (byte[]) backImg.getRaster().getDataElements(0, 0, backImg.getWidth(), backImg.getHeight(), null);
		scratch.clear();
		scratch.put(data, 0, data.length);
		scratch.rewind();
		setData(scratch);
	}
	
	/**
	 * Implement this method so that it contains your drawing calls. The image
	 * state is preserved so if you want to clear the image to completely redraw
	 * it, you must provide the function yourself.
	 * 
	 * @param graphicsContext
	 *            Tha Graphics2D context in which the painting can be performed.
	 */
	public abstract void paint(Graphics2D graphicsContext);
}
