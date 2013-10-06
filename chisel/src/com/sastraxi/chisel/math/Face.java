package com.sastraxi.chisel.math;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

import java.util.Arrays;
import java.util.Iterator;

/**
 * Light-weight edges structure for a face.
 * Each edge is an int[] {index_start_vertex, index_end_vertex}.
 *
 * Vertex indices are always >= 0; this is an implementation detail.
 */
public class Face {
	private static final int INITIAL_CAPACITY = 3;

	public Face(Array<int[]> edges) {
		this.edges = edges;
	}

	private Array<int[]> edges;
	private Integer largest = 32; // FIXME: fix this!! this caps the max # of vertices/face!

	/**
	 * Faces must be wound counter-clockwise.
	 */
	public Face(int[] faces) {
		this.edges = new Array<int[]>();
		for (int i = 0; i < faces.length; ++i) {
			edges.add(new int[] {
				faces[i],
				faces[(i+1)%faces.length]
			});
		}
	}

	/**
	 * Counts how many time each vertex is linked to.
	 * @return an array of counts, addressed by vertex index.
	 */
	private int[] generateCounts() {
		int[] counts = new int[largest+1];
		Arrays.fill(counts, 0);
		for (int[] edge: edges) {
			counts[edge[LocalMath.EDGE_START]]++;
			counts[edge[LocalMath.EDGE_END]]++;
		}
		return counts;
	}

	public boolean isClosed() {
		// the only valid face has a ring of edges around it.
		// in such a ring, each vertex is visited exactly twice.
		int num_twos = 0;
		int counts[] = generateCounts();
		for (int count: counts) {
			if (count == 2) num_twos++;
		}
		// in such a ring, there are as many edges as vertices.
		return num_twos == edges.size;
	}

	/**
	 * Does this face form a loop?
	 */
	public boolean isOrdered() {
		Integer last = null;
		for (int[] edge: edges) {
			if (last != null && last != edge[LocalMath.EDGE_START]) {
				return false;
			}
			last = edge[LocalMath.EDGE_END];
		}
		return (edges.get(0)[LocalMath.EDGE_START] == edges.get(edges.size - 1)[LocalMath.EDGE_END]);
	}

	public boolean isConvex(Array<Vector3> vertices) {
		// todo implement Face.isConvex
		return true;
	}

	/**
	 * Assumes counter-clockwise polygon winding.
	 *
	 * @param vertices
	 * @return a newly-allocated Vector3 normal (not normalized).
	 */
	public Vector3 getNormal(Array<Vector3> vertices) {
	   	assert(arity() >= 3);

		Vector3 p_a = vertices.get(edges.get(0)[LocalMath.EDGE_START]);
		Vector3 p_b = vertices.get(edges.get(1)[LocalMath.EDGE_START]);
		Vector3 p_c = vertices.get(edges.get(2)[LocalMath.EDGE_START]);

		Vector3 l1 = p_b.cpy().sub(p_a);
		Vector3 l2 = p_c.cpy().sub(p_a);
		return l1.crs(l2);
	}

	public int arity() {
		return this.edges.size;
	}

	public Array<int[]> getEdges() {
		return edges;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Face face = (Face) o;

		// the two must mention the same vertices the same number of times.
		// as convexity is a requirement, we won't equal anything wacky.
		// XXX: do we check for winding here?
		if (!Arrays.equals(generateCounts(), face.generateCounts())) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		return edges.hashCode();
	}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		b.append("Face(" + arity() + "): ");
		for (int[] edge: edges) {
			b.append("(");
			b.append(edge[LocalMath.EDGE_START]);
			b.append(")-");
		}
		return b.toString();
	}

	public String toString(Array<Vector3> vertices) {
		StringBuilder b = new StringBuilder();
		b.append("Face(" + arity() + "): ");
		for (int[] edge: edges) {
			b.append("(");
			b.append(vertices.get(edge[LocalMath.EDGE_START]));
			b.append(")-");
		}
		return b.toString();
	}

	public String toString2(Array<Vector3> vertices) {
		StringBuilder b = new StringBuilder();
		b.append("Face(" + arity() + "):\n");
		for (int[] edge: edges) {
			b.append(" ~ (" + vertices.get(edge[LocalMath.EDGE_START]) + ")");
			b.append("-(" + vertices.get(edge[LocalMath.EDGE_END]) + ")\n");
		}
		return b.toString();
	}
}
