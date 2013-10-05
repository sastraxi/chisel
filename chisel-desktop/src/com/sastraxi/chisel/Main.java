package com.sastraxi.chisel;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

public class Main {
	public static void main(String[] args) {
		LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();

		cfg.title = "Chisel Editor";
		cfg.useGL20 = true;
		cfg.width = 1280;
		cfg.height = 720;
		cfg.resizable = true;

		new LwjglApplication(new ChiselApp(), cfg);
	}
}
