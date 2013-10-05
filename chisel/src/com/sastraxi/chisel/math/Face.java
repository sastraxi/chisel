package com.sastraxi.chisel.math;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

import java.util.Arrays;

/**
 * Light-weight edges structure for a face.
 * Each edge is an int[] {index_start_vertex, index_end_vertex}.
 *
 * Vertex indices are always >= 0; this is an implementation detail.
 */
public class Face {
	private static final int INITIAL_CAPACITY = 3;

	Face(Array<int[]> edges) {
		this.edges = edges;
	}

	private Array<int[]> edges;
	private Integer largest = 0;

	public boolean isClosed() {
		// count how many times each vertex is linked to.
		int[] counts = new int[largest+1];
		Arrays.fill(counts, 0);
		for (int[] edge: edges) {
			counts[edge[LocalMath.EDGE_START]]++;
			counts[edge[LocalMath.EDGE_END]]++;
		}
		// the only valid face has a ring of edges around it.
		// in such a ring, each vertex is visited exactly twice.
		int num_twos = 0;
		for (int count: counts) {
			if (count == 2) num_twos++;
		}
		// in such a ring, there are as many edges as vertices.
		return num_twos == edges.size;
	}

	/**
	 * Does this face form a
	 * Not optimised for speed; intended for debug-mode assertions.
	 */
	public boolean isConvex(Array<Vector3> vertices) {
		// todo implement Face.isConvex
	   	return false;
	}

	// XXX: assumes counter-clockwise polygon winding
	public Vector3 getNormal(Array<Vector3> vertices) {
	   	assert(arity() >= 3);
		assert(isConvex(vertices));

		Vector3 p_a = vertices.get(edges.get(0)[LocalMath.EDGE_START]);
		Vector3 p_b = vertices.get(edges.get(1)[LocalMath.EDGE_START]);
		Vector3 p_c = vertices.get(edges.get(2)[LocalMath.EDGE_START]);

		Vector3 l1 = p_b.cpy().sub(p_a);
		Vector3 l2 = p_c.cpy().sub(p_a);
		return l2.crs(l1);
	}

	public int arity() {
		return this.edges.size;
	}

	public Array<int[]> getEdges() {
		return edges;
	}
}
