/**
 * Provides caching and preloading of models. When asked to create
 * a new model (i.e. an AnimatedNode), it will first check if it already
 * has a free one of that type in its pool and then return that one 
 * instead. Otherwise it will load a new one and add it to the pool for
 * later reuse. It checks for reusability by checking the isReusable flag
 * in AnimatedNode. 
 */
package engine.gfx;

import infostore.DataManager;
import infostore.DataNode;
import infostore.DataManager.DataType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import com.jme.math.FastMath;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.renderer.Renderer;
import com.jme.scene.state.BlendState;
import com.jme.system.DisplaySystem;

import engine.Settings;

public class ModelFactory {
	// TODO: use SharedNode/SharedMesh as per
	// http://www.jmonkeyengine.com/forum/index.php?topic=13404.msg97645#msg97645
	// update: only for some models, like barrels.

	private static HashMap<String, AnimatedNode> loadCache = new HashMap<String, AnimatedNode>();
	private static HashMap<String, ArrayList<AnimatedNode>> reusePool = new HashMap<String, ArrayList<AnimatedNode>>();

	public static void init() {
		LinkedList<DataNode> modelIDdata = DataManager.findAllByType(DataType.MODEL_ID);
		System.out.println(modelIDdata);
		for (DataNode n : modelIDdata) {
			ModelFactory.reusePool.put(n.getProp("name"), new ArrayList<AnimatedNode>());
		}
	}

	public static AnimatedNode getModel(DataNode m) {
		if (Settings.NOGFX)
			return null;

		for (AnimatedNode n : ModelFactory.reusePool.get(m.getProp("name"))) {
			if (n.isReusable()) {
				return n;
			}
		}
		AnimatedNode newNode = load(m);
		ModelFactory.reusePool.get(m.getProp("name")).add(newNode);
		return newNode;
	}

	public static void preload(DataNode m, int count) {
		for (int i = 0; i < count; i++) {
			AnimatedNode trash = load(m);
			// ModelFactory.rootNode.attachChild(trash.getNode());
			trash.setReusable(true);
			trash.getNode().updateGeometricState(0, true);
			trash.getNode().updateModelBound();
			trash.getNode().updateRenderState();
			ModelFactory.reusePool.get(m.getProp("name")).add(trash);
		}
	}

	private static AnimatedNode load(DataNode m) {
		if (!loadCache.containsKey(m.getProp("name"))) {
			loadModel(m);
		}
		AnimatedNode newNode = loadCache.get(m.getProp("name")).copy();
		return newNode;
	}

	private static void loadModel(DataNode m) {
		loadCache.put(m.getProp("name"), new AnimatedNode(m.getProp("file")));

		if (m.getPropB("alpha")) {
			BlendState tpState = DisplaySystem.getDisplaySystem().getRenderer().createBlendState();
			tpState.setEnabled(true);
			tpState.setSourceFunction(BlendState.SourceFunction.SourceAlpha);
			tpState.setDestinationFunction(BlendState.DestinationFunction.OneMinusSourceAlpha);
			tpState.setBlendEnabled(true);
			tpState.setTestEnabled(true);
			tpState.setTestFunction(BlendState.TestFunction.GreaterThan);
			tpState.setReference(0.1f);

			loadCache.get(m.getProp("name")).getNode().setRenderState(tpState);
			loadCache.get(m.getProp("name")).getNode().setRenderQueueMode(Renderer.QUEUE_TRANSPARENT);

			loadCache.get(m.getProp("name")).getNode().updateRenderState();
		}

		loadCache.get(m.getProp("name")).getNode().setLocalScale(m.getPropF("scale"));
		loadCache.get(m.getProp("name")).getNode().setLocalTranslation(0f, 0f, m.getPropF("zOffset"));

		float yaw = 0, roll = 0, pitch = 0;
		if (m.isProp("xRotPi")) {
			yaw = m.getPropF("xRotPi") * FastMath.PI;
		}
		if (m.isProp("yRotPi")) {
			roll = m.getPropF("yRotPi") * FastMath.PI;
		}
		if (m.isProp("zRotPi")) {
			pitch = m.getPropF("zRotPi") * FastMath.PI;
		}
		if (yaw != 0 || roll != 0 || pitch != 0) {
			Quaternion qtot = new Quaternion();
			qtot.fromAngles(yaw, roll, pitch);
			loadCache.get(m.getProp("name")).getNode().setLocalRotation(qtot);
		}
	}
}
