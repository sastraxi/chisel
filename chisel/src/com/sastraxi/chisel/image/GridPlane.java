package com.sastraxi.chisel.image;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.sastraxi.chisel.image.glsl.GridShader;

public class GridPlane implements RenderableProvider {

	public final float EXTENT = 10000.0f; // the size of the "infinite" plane. hint: it's not

	private final float originSize, majorSize, minorSize;
	private final Color originColour, majorColour, minorColour;
	private final Shader shader;
	private int majorSpacing, minorSpacing;

	private final ModelInstance instance;
	private Camera camera = null;

	public GridPlane(float originSize, Color originColour,
	                 float majorSize, int majorSpacing, Color majorColour,
	                 float minorSize, int minorSpacing, Color minorColour) {

		// todo fade to major colour at horizon (glancing angle?)
		// todo pass arguments to shader

		this.originSize = originSize;
		this.originColour = originColour;
		this.majorSize = majorSize;
		this.majorSpacing = majorSpacing;
		this.majorColour = majorColour;
		this.minorSize = minorSize;
		this.minorSpacing = minorSpacing;
		this.minorColour = minorColour;

		Model plane = new ModelBuilder().createRect(
				-EXTENT, 0f, -EXTENT,
				-EXTENT, 0f,  EXTENT,
			 	 EXTENT, 0f,  EXTENT,
				 EXTENT, 0f, -EXTENT,
				0f,      1f,  0f,
				new Material(),
				VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates);
		this.instance = new ModelInstance(plane);

		this.shader = new GridShader();
		this.shader.init();
	}

	public GridPlane() {
		this(2.5f, new Color(0.3f, 0.7f, 1.0f, 1.0f),
				1.0f, 128, new Color(0.3f, 0.7f, 1.0f, 0.5f),
				0.5f, 32, new Color(0.3f, 0.7f, 1.0f, 0.3f));
	}

	public void setMajorSpacing(int majorSpacing) {
		this.majorSpacing = majorSpacing;
		updateUniforms();
	}

	public void setMinorSpacing(int minorSpacing) {
		this.minorSpacing = minorSpacing;
		updateUniforms();
	}

	private void updateUniforms() {

	}

	public void trackPosition(Camera camera) {
		this.camera = camera;
	}

	/**
	 * Mostly delegate to the plane instance.
	 */
	@Override
	public void getRenderables(Array<Renderable> renderables, Pool<Renderable> pool) {
		assert(camera != null);
		instance.transform.setToTranslation(camera.position.x, 0f, camera.position.z);
		instance.getRenderables(renderables, pool);
	}

	public Shader getShader() {
		return shader;
	}
}
