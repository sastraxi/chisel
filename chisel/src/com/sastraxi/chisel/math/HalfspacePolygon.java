package com.sastraxi.chisel.math;

import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.Array;
import com.sastraxi.chisel.map.Brush;

import java.util.*;


public class HalfspacePolygon {

	// TODO: somehow let static classes like this be "tasks" to be taken on by worker threads.
	// idea guy: Tasks.ConcurrentLinkedQueue<Runnable>

	/**
	 * Create a Matrix3 as if (a,b,c) were its columns.
	 * @param a the first column (a.x is top-left, a.y is middle-left, a.z is bottom-left)
	 * @param b the second column
	 * @param c the third column
	 * @return a new Matrix3
	 */
	private static Matrix3 createSideBySide(Vector3 a, Vector3 b, Vector3 c) {
		Matrix3 mat = new Matrix3();
		mat.val[Matrix3.M00] = a.x; mat.val[Matrix3.M01] = b.x; mat.val[Matrix3.M02] = c.x;
		mat.val[Matrix3.M10] = a.y; mat.val[Matrix3.M11] = b.y; mat.val[Matrix3.M12] = c.y;
		mat.val[Matrix3.M20] = a.z; mat.val[Matrix3.M21] = b.z; mat.val[Matrix3.M22] = c.z;
		return mat;
	}

	/**
	 * Take a list of planes that denote half-spaces and turn it into a list of vertices and faces.
	 *
	 *
	 * @param planes
	 * @return
	 */
	public static Brush toConvex(Array<Plane> planes) {

		// 3 planes intersect at a point (vertex), as long as they are not visible from an outside face.
		// 2 planes intersect at a line segment.
		// 2 vertices that share 2 planes are an edge
		// 1 face per plane

		// todo optimize out inner-loop object creation

		Array<Vector3> vertices = new Array<Vector3>();
		Array<Array<Integer>> vertexList = new Array<Array<Integer>>();
		Array<Array<int[]>> edgeList = new Array<Array<int[]>>();

		// cache a point on each plane
		Vector3[] pointOn = new Vector3[planes.size];
		for (int x = 0; x < planes.size; ++x) {
			Intersector.intersectLinePlane(0.0f, 0.0f, 0.0f,
					0.0f, 0.0f, 1.0f, planes.get(x), pointOn[x]);
		}

		// gather [plane_index, plane_index] -> ArrayList<Integer>
		for (int a = 0; a < planes.size; ++a) {
			Plane plane_a = planes.get(a);

			for (int b = 0; b < a; ++b) {
				Plane plane_b = planes.get(b);

				for (int c = 0; c < b; ++c) {
					Plane plane_c = planes.get(c);

					float determinant = createSideBySide(plane_a.normal, plane_b.normal, plane_c.normal).det();
					if (determinant < LocalMath.EPSILON) {
						// the planes do not intersect.
						continue;
					}

					// the point where all planes intersect
					Vector3 intersection =
							(plane_b.normal.crs(plane_c.normal).scl(pointOn[a].dot(plane_a.normal)))
						.add(plane_c.normal.crs(plane_a.normal).scl(pointOn[b].dot(plane_b.normal)))
					    .add(plane_a.normal.crs(plane_b.normal).scl(pointOn[c].dot(plane_c.normal)))
					    .scl(1f / determinant);

					// add this vertex to the master list
					int v_i = vertices.size;
					vertices.add(intersection);

					// add to per-edge vertex lists
					vertexList.get(a * planes.size + b).add(v_i);
					vertexList.get(a * planes.size + c).add(v_i);
					vertexList.get(b * planes.size + c).add(v_i);
				}
			}
		}

		// try to discard each vertex with each plane
		for (int a = 0; a < planes.size; ++a) {
			Plane plane_a = planes.get(a);
			for (int i = 0; i < vertices.size; ++i) {
				Vector3 vertex = vertices.get(i);
				if (vertex == null) continue;

				if (plane_a.distance(vertex) > LocalMath.EPSILON) {
					// vertex on plane side; that is, an outward face on the polyhedron can see this point.
					// however, this would make the polyhedron concave, so discard the point.
					vertices.set(i, null);
				}
			}
		}

		// gather edges
		for (int a = 0; a < planes.size; ++a) {
			Plane plane_a = planes.get(a);

			for (int b = 0; b < a; ++b) {
				Plane plane_b = planes.get(b);

				Array<Integer> lineVertices = vertexList.get(a * planes.size + b);

				// remove all vertices that have been discarded in the previous nested loop (impl.: null)
				// todo could do this a lot faster by adding to edge[] things that aren't null and asserting there are no more non-nulls in the list
				for (Iterator<Integer> x = lineVertices.iterator(); x.hasNext();) {
					int v_i = x.next();
					if (vertices.get(v_i) == null) {
						x.remove();;
					}
				}

				if (lineVertices.size == 0) continue; // the two planes don't meet (are parallel)
				assert(lineVertices.size == 2);

				int[] edge = new int[] {lineVertices.get(LocalMath.EDGE_START),
						                lineVertices.get(LocalMath.EDGE_END)};

				// add to per-plane edge lists
				edgeList.get(a).add(edge);
				edgeList.get(b).add(edge);

			}
		}

		// gather faces
		Array<Face> faces = new Array<Face>();
		for (int a = 0; a < planes.size; ++a) {

			// todo sort edges; pick whatever is first to start with and add adjoining until we get back to the start.
			Array<int[]> sortedEdges = new Array<int[]>();
			// populate sortedEdges from edgeList.get(a)

			Face candidate = new Face(sortedEdges);
			assert(candidate.isClosed());
			assert(candidate.isConvex(vertices));
			faces.add(candidate);
		}

		return new Brush(vertices, faces);
	}

	public static ArrayList<Plane> fromConvex(Brush brush) {
		return null;
	}

}
