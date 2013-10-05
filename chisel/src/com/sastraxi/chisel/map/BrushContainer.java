package com.sastraxi.chisel.map;

import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.RenderableProvider;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;

public class BrushContainer implements RenderableProvider {

	public Array<Brush> brushes;

	public BrushContainer() {
		this.brushes = new Array<Brush>();
	}

	public void add(Brush brush) {
		this.brushes.add(brush);
	}

	public void addAll(Array<Brush> brushes) {
		this.brushes.addAll(brushes);
	}

	@Override
	public void getRenderables(Array<Renderable> renderables, Pool<Renderable> pool) {
		for (Brush brush: brushes) {
			Renderable renderable = pool.obtain();
			brush.populate(renderable);
			renderables.add(renderable);
		}
	}
}
