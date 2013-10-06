package com.sastraxi.chisel.map;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.RenderableProvider;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.math.Plane;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.sastraxi.chisel.image.ColourManager;
import com.sastraxi.chisel.math.Face;
import com.sastraxi.chisel.math.LocalMath;

public class Brush implements RenderableProvider {

	public static float RESOLUTION = 0.00001f;

	// actual data
	private final Array<Face> faces;
	private final Array<Vector3> vertices;
	private final Color colour;

	public Brush(Array<Vector3> vertices, Array<Face> faces) {
		this.vertices = vertices;
		this.faces = faces;
		this.colour = ColourManager.next();

		// assert face correctness: planar + convex
		for (Face face: this.faces) {
			assert(face.arity() >= 3);
			assert(face.isConvex(this.vertices));
		}
	}

	// cache
	private Mesh _mesh;
	private boolean validMesh = false;

	private Mesh getMesh() {
		if (!validMesh) {
			if (_mesh != null) _mesh.dispose();
			_mesh = generateMesh();
			validMesh = true;
		}
		return _mesh;
	}

	private Mesh generateMesh() {

		// each face has (edges - 2) * 3 triangles.
		int n_triangles = 0;
		for (Face face: faces) {
			n_triangles += (face.arity() - 2) * 3;
		}

		int i = 0, v = 0;
		short[] indices = new short[n_triangles * 3]; // each triangle has 3 vertices.
		float[] verts = new float[indices.length * 6]; // each vertex has a number of attributes; see below*
		for (Face face: faces) {
			for (int t = 0; t < face.arity() - 2; ++t) {

				// triangle fan-type generator
				indices[i  ] = (short) face.getEdges().get(0  )[LocalMath.EDGE_START];
				indices[i+1] = (short) face.getEdges().get(t+1)[LocalMath.EDGE_START];
				indices[i+2] = (short) face.getEdges().get(t+2)[LocalMath.EDGE_START];

				Vector3 normal = face.getNormal(this.vertices);
				Vector3 p1 = this.vertices.get(indices[i  ]);
				Vector3 p2 = this.vertices.get(indices[i+1]);
				Vector3 p3 = this.vertices.get(indices[i+2]);

				verts[v++] = p1.x; verts[v++] = p1.y; verts[v++] = p1.z;
				verts[v++] = normal.x; verts[v++] = normal.y; verts[v++] = normal.z;
				verts[v++] = p2.x; verts[v++] = p2.y; verts[v++] = p2.z;
				verts[v++] = normal.x; verts[v++] = normal.y; verts[v++] = normal.z;
				verts[v++] = p3.x; verts[v++] = p3.y; verts[v++] = p3.z;
				verts[v++] = normal.x; verts[v++] = normal.y; verts[v++] = normal.z;

				i += 3;
			}
		}

		// the total number of components in the VertexAttributes attached
		// must match the multiplier in the verts[] definition above*
		Mesh mesh = new Mesh(true, verts.length, indices.length,
				new VertexAttribute(VertexAttributes.Usage.Position, 3, "a_position"),
				new VertexAttribute(VertexAttributes.Usage.Normal, 3, "a_normal"));

		mesh.setVertices(verts);
		mesh.setIndices(indices);

		return mesh;
	}

	private Mesh generateWireframeMesh() {
		return null;
	}

	public void populate(Renderable r) {
		r.mesh = getMesh();
		r.material = new Material(new Material(ColorAttribute.createDiffuse(colour)));
	}

	public void invalidateMesh() {
		this.validMesh = false;
	}

	@Override
	public void getRenderables(Array<Renderable> renderables, Pool<Renderable> pool) {
		Renderable r = pool.obtain();
		populate(r);
		renderables.add(r);
	}

	/**
	 *
	 * @param plane the splitting plane.
	 * @return
	 */
	public Brush[] split(Plane plane) {
		// xxx for each face, the plane either:
		// a) misses the face;
		// b) glances the face (some edge of the face is on the plane)
		// c) splits the face.
		return null;
	}

	public Array<Face> getFaces() {
		return faces;
	}

	public Array<Vector3> getVertices() {
		return vertices;
	}

	/**
	 * Compares the list of Faces and vertices differently (topologically).
	 * 1. Find a mapping that is one-to-one and onto from the vertex
	 *    indices of this brush to the vertex indices of the other.
	 * 2. Re-write our Face edge lists in terms of the other brush's vertex indices.
	 * 3. Require that each re-written Face from our faces topologically
	 *    equals some face in the other object.
	 * @param o the object to compare to.
	 * @return true, if both Brush objects represent the same geometry, false otherwise.
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Brush other  = (Brush) o;

		if (other.vertices.size != vertices.size) return false;
		if (other.faces.size != faces.size) return false;

		// first map vertices, _cnv[i] -> the index of i in other
		int[] _cnv = new int[vertices.size];
		for (int i = 0; i < vertices.size; ++i) {
			Vector3 v_this = this.vertices.get(i);

			for (int j = 0; j < vertices.size; ++j) {
				Vector3 v_other = other.vertices.get(j);

				if (v_this.equals(v_other)) {
					_cnv[i] = j;
				}
			}
		}

		// create expected faces and look for them in the other object
		for (int i = 0; i < faces.size; ++i) {

			Array<int[]> expectedFaceEdges = new Array<int[]>();
			for (int[] edge: faces.get(i).getEdges()) {
				expectedFaceEdges.add(new int[] {
					_cnv[edge[LocalMath.EDGE_START]],
					_cnv[edge[LocalMath.EDGE_END]]
				});
			}

			Face expectedFace = new Face(expectedFaceEdges);
			if (!other.faces.contains(expectedFace, false)) {
				return false;
			}

		}

		return true;
	}

	@Override
	public int hashCode() {
		int result = faces.hashCode();
		result = 31 * result + vertices.hashCode();
		return result;
	}
}
