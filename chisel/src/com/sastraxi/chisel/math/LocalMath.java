package com.sastraxi.chisel.math;

import com.badlogic.gdx.math.Plane;
import com.badlogic.gdx.math.Vector3;

public class LocalMath {

	public static final float EPSILON = 0.00001f; // 10e-5
	public static final int EDGE_START = 0, EDGE_END = 1;

	/**
	 * Returns a plane with an unscaled normal.
	 * @param normal
	 * @param point
	 * @return
	 */
	public static Plane safePlane(Vector3 normal, Vector3 point) {
		Plane p = new Plane(normal, point);
		p.set(normal.x, normal.y, normal.z, -normal.dot(point));
		return p;
	}


}
