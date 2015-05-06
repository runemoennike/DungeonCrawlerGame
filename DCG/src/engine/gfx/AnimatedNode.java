/**
 * Wraps the jME model-loading mess for the Ogre-xml format.
 * Has a node that can be attached to other nodes, and provides
 * methods for controlling skeletal animation.
 * Should not be used directly but instead via ModelFactory to
 * provide caching. 
 */
package engine.gfx;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Level;

import com.jme.bounding.BoundingBox;
import com.jme.math.FastMath;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.scene.Node;
import com.jme.util.Timer;
import com.jme.util.resource.RelativeResourceLocator;
import com.jme.util.resource.ResourceLocatorTool;
import com.jmex.model.ModelFormatException;
import com.jmex.model.ogrexml.MaterialLoader;
import com.jmex.model.ogrexml.MeshCloner;
import com.jmex.model.ogrexml.OgreLoader;
import com.jmex.model.ogrexml.anim.MeshAnimationController;

import engine.Game;
import engine.Settings;
import engine.Settings.Fields;

public class AnimatedNode {
	private Node n;
	private boolean isReusable = false;
	private MeshAnimationController animControl;
	private String curAnimationName;
	private RepeatMode repeatMode;
	private boolean hasAnimations = false;
	private float startTime;
	private static Timer timer = Timer.getTimer();

	public enum RepeatMode {
		NONE(MeshAnimationController.RT_CLAMP), LOOPED(MeshAnimationController.RT_WRAP);

		int value;
		private RepeatMode(int value) {
			this.value = value;
		}
	}

	public enum BoneType {
		RIGHT_HAND("Bip01 R Hand"), LEFT_HAND("Bip01 L Hand");

		String name;
		private BoneType(String name) {
			this.name = name;
		}
	}

	public AnimatedNode copy() {
		AnimatedNode result = new AnimatedNode();
		result.n = MeshCloner.cloneMesh(this.n);
		result.hasAnimations = this.hasAnimations;
		if (this.hasAnimations) {
			result.animControl = (MeshAnimationController) result.n.getController(0);
		}
		return result;
	}

	public AnimatedNode(String model) {
		OgreLoader loader = new OgreLoader();
		MaterialLoader matLoader = new MaterialLoader();

		String ninjaMeshUrlString = "/dcg/data/models/" + model + ".mesh.xml";
		String matUrlString = "/dcg/data/textures/" + Settings.get(Fields.TEXTURE_QUALITY).str + "/" + model
				+ ".material";

		try {
			URL matURL = ResourceLocatorTool.locateResource(ResourceLocatorTool.TYPE_TEXTURE, matUrlString);
			URL meshURL = ResourceLocatorTool.locateResource(ResourceLocatorTool.TYPE_MODEL, ninjaMeshUrlString);

			if (meshURL == null)
				throw new IllegalStateException("Required runtime resource missing: " + ninjaMeshUrlString);
			if (matURL == null)
				throw new IllegalStateException("Required runtime resource missing: " + matUrlString);
			try {
				ResourceLocatorTool.addResourceLocator(ResourceLocatorTool.TYPE_TEXTURE, new RelativeResourceLocator(
						matURL));
				// This causes relative references in the .material file to
				// resolve to the same dir as the material file.
				// Don't have to set up a relative locator for TYPE_MODEL
				// here, because OgreLoader.loadModel() takes care of that.
			} catch (URISyntaxException use) {
				// Since we're generating the URI from a URL we know to be
				// good, we won't get here. This is just to satisfy the
				// compiler.
				throw new RuntimeException(use);
			}
			matLoader.load(matURL.openStream());
			if (matLoader.getMaterials().size() > 0)
				loader.setMaterials(matLoader.getMaterials());

			this.n = (Node) loader.loadModel(meshURL);
		} catch (IOException ex) {
			Game.logger.log(Level.SEVERE, null, ex);
		} catch (ModelFormatException mfe) {
			Game.logger.log(Level.SEVERE, null, mfe);
		}

		if (this.n.getControllerCount() > 0) {
			this.animControl = (MeshAnimationController) this.n.getController(0);
			this.animControl.setRepeatType(MeshAnimationController.RT_WRAP);
			this.hasAnimations = true;
			System.out.println(model + " has animations.");
		} else {
			System.out.println(model + " has no animations.");
		}

		Quaternion q = new Quaternion();
		q.fromAngleAxis(FastMath.HALF_PI, new Vector3f(1, 0, 0));
		this.n.setLocalRotation(q);

		this.n.setModelBound(new BoundingBox());
		this.n.updateModelBound();
	}

	public AnimatedNode() {
	}

	public void attachToBone(BoneType bone, Node child) {
		if (bone == null || this.animControl == null || this.animControl.getBone(bone.name) == null) {
			return;
		}
		this.animControl.getBone(bone.name).getAttachmentsNode().attachChild(child);
		this.n.attachChild(this.animControl.getBone(bone.name).getAttachmentsNode());
	}

	public Node getNode() {
		return n;
	}

	public void doAnimation(String name, float speed) {
		if (this.animControl == null)
			return;
		if (!name.equals(this.curAnimationName)) {
			this.animControl.setSpeed(speed);
			this.animControl.setAnimation(name);
			this.curAnimationName = name;
			this.startTime = timer.getTimeInSeconds();
		}

	}

	public void doAnimation(String name) {
		doAnimation(name, 1.0f);
	}

	public void setRepeat(RepeatMode rm) {
		if (this.animControl == null)
			return;
		this.animControl.setRepeatType(rm.value);
	}

	public MeshAnimationController getAnimControl() {
		return animControl;
	}

	public boolean hasAnimation(String name) {
		return this.animControl.getAnimationNames().contains(name);
	}

	public float animPercentage() {
		if (this.animControl == null)
			return 0;
		return ((AnimatedNode.timer.getTimeInSeconds() - this.startTime) / this.animControl
				.getAnimationLength(this.curAnimationName));
	}

	public void setScaling(float scaling) {
		this.n.setLocalScale(scaling);
	}

	public void setReusable(boolean isReusable) {
		this.isReusable = isReusable;
	}

	public boolean isReusable() {
		return isReusable;
	}
}