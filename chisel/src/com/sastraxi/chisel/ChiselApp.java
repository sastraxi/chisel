package com.sastraxi.chisel;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Plane;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;
import com.sastraxi.chisel.map.Brush;
import com.sastraxi.chisel.math.HalfspacePolygon;

public class ChiselApp implements ApplicationListener {

	public static float FIELD_OF_VIEW = 90.0f;

	private CameraInputController camController;
	private PerspectiveCamera camera;
	private Environment environment;

	private Model box;
	private ModelInstance boxInstance;

	private ModelBatch batch;
	private DefaultShaderProvider shaderProvider;

	private Stage stage;

	@Override
	public void create() {
		Gdx.gl.glClearColor(0, 0, 0, 1);

		camera = new PerspectiveCamera(FIELD_OF_VIEW, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		camera.position.set(10f, 10f, 10f);
		camera.lookAt(0f, 0f, 0f);
		camera.near = 0.1f;
		camera.far = 300f;
		camera.update();

		camController = new CameraInputController(camera);
		Gdx.input.setInputProcessor(camController);

		environment = new Environment();
		environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
		environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));

		// the box, as made by libGDX
		ModelBuilder modelBuilder = new ModelBuilder();
		box = modelBuilder.createBox(5f, 5f, 5f,
			  new Material(ColorAttribute.createDiffuse(Color.GREEN)),
			  VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);
		boxInstance = new ModelInstance(box);
		//boxInstance.transform.setToTranslation(-10.0f, 0f, 0f);

		// the same box, as made by our vertex thing
		final float SCALE = 5f;
		Array<Plane> planes = new Array<Plane>();
		planes.add(new Plane(Vector3.Y,          new Vector3(0f,  SCALE, 0f)));
		planes.add(new Plane(Vector3.Y.scl(-1f), new Vector3(0f, -SCALE, 0f)));
		planes.add(new Plane(Vector3.X,          new Vector3( SCALE, 0f, 0f)));
		planes.add(new Plane(Vector3.X.scl(-1f), new Vector3(-SCALE, 0f, 0f)));
		planes.add(new Plane(Vector3.Z,          new Vector3(0f, 0f,  SCALE)));
		planes.add(new Plane(Vector3.Z.scl(-1f), new Vector3(0f, 0f, -SCALE)));
		Brush boxBrush = HalfspacePolygon.toConvex(planes);

		shaderProvider = new DefaultShaderProvider();
		batch = new ModelBatch(shaderProvider);

		createUI();
	}

	private void createUI() {

		stage = new Stage();
		Gdx.input.setInputProcessor(stage);

		Table table = new Table();
		table.setFillParent(true);
		stage.addActor(table);

		// TODO Add widgets to the table here.

	}

	@Override
	public void dispose() {
		batch.dispose();
		stage.dispose();
		box.dispose();
	}

	@Override
	public void render() {
		camController.update();

		Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

		batch.begin(camera);
		batch.render(boxInstance, environment);
		batch.end();

		//stage.act(Gdx.graphics.getDeltaTime());
		//stage.draw();

		//Table.drawDebug(stage); // This is optional, but enables debug lines for tables.
	}

	@Override
	public void resize(int width, int height) {
		stage.setViewport(width, height, true);
		camera.viewportWidth = (float) width;
		camera.viewportHeight = (float) height;
		camera.update();
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}
}
