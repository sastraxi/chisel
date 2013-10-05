package com.sastraxi.chisel.image;

import com.badlogic.gdx.graphics.Color;

public class ColourManager {

	private static final Color[] CYCLE_COLOURS = new Color[] {
		new Color(1.0f, 0.8f, 0.8f, 1.0f),
		new Color(0.9f, 0.7f, 1.0f, 1.0f),
		new Color(0.7f, 1.0f, 0.6f, 1.0f),
	};
	private static Integer i = 0;

	public static Color next() {
		synchronized(i) {
			i = (i + 1) % CYCLE_COLOURS.length;
		}
		return CYCLE_COLOURS[i];
	}
}
