/**
 * This class extends Entity and represents the marker used to mark the target of the mouse.
 */

package entities;

import map.Map;

import com.jme.bounding.BoundingBox;
import com.jme.image.Texture;
import com.jme.renderer.ColorRGBA;
import com.jme.renderer.Renderer;
import com.jme.scene.Node;
import com.jme.scene.Spatial;
import com.jme.scene.shape.Disk;
import com.jme.scene.state.BlendState;
import com.jme.scene.state.MaterialState;
import com.jme.scene.state.TextureState;
import com.jme.system.DisplaySystem;
import com.jme.util.TextureManager;

import engine.Game;
import engine.Settings;
import engine.Settings.Fields;

public class Marker extends Entity {

	private static final long serialVersionUID = -1291306011487157648L;

	public Marker(Map map) {
		super(map.getWorld(), null);
		this.map = map;
		this.noPathing = true;
		this.noPicking = true;
		this.addSubtype(EntitySubtype.DUMMY);

		createModel();
	}

	private void createModel() {
		Disk b = new Disk("Picking Marker", 10, 20, 1.5f);

		TextureState ts = DisplaySystem.getDisplaySystem().getRenderer().createTextureState();
		ts.setEnabled(true);
		ts.setTexture(TextureManager.loadTexture(Game.class.getClassLoader().getResource(
				"dcg/data/textures/" + Settings.get(Fields.TEXTURE_QUALITY).str + "/marker.png"),
				Texture.MinificationFilter.NearestNeighborLinearMipMap, Texture.MagnificationFilter.Bilinear));

		b.setRenderState(ts);

		MaterialState ms = DisplaySystem.getDisplaySystem().getRenderer().createMaterialState();
		ms.setEmissive(new ColorRGBA(0f, 0.2f, 0.5f, 1));

		b.setRenderState(ms);

		BlendState tpState = DisplaySystem.getDisplaySystem().getRenderer().createBlendState();
		tpState.setEnabled(true);
		tpState.setSourceFunction(BlendState.SourceFunction.SourceAlpha);
		tpState.setDestinationFunction(BlendState.DestinationFunction.OneMinusSourceAlpha);
		tpState.setBlendEnabled(true);
		tpState.setTestEnabled(true);
		tpState.setTestFunction(BlendState.TestFunction.GreaterThan);
		tpState.setReference(0.1f);
		b.setRenderState(tpState);
		b.setRenderQueueMode(Renderer.QUEUE_TRANSPARENT);
		b.setLightCombineMode(Spatial.LightCombineMode.Replace);

		b.setModelBound(new BoundingBox());
		b.updateModelBound();

		Node n2 = new Node();
		n2.attachChild(b);

		this.n.attachChild(n2);
	}

}
