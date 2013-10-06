import com.badlogic.gdx.math.Plane;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.sastraxi.chisel.map.Brush;
import com.sastraxi.chisel.math.Face;
import com.sastraxi.chisel.math.HalfspacePolygon;
import org.hamcrest.core.IsEqual;
import org.junit.Assert;
import org.junit.Test;

import static org.hamcrest.core.IsEqual.*;
import static org.junit.Assert.assertThat;

public class TestHalfspace {

	final float SCALE = 5f;

	// Overall testing method:
	// Create a Brush b.
	// From that brush, pick 3 points on each face, make that into a plane. Add the plane.
	// Brush b2 = HalfspacePolygon.toConvex(planes)
	// assert(b.equals(b2));

	@Test
	public void testCube() {

		// a simple box.
		Array<Vector3> vertices = new Array<Vector3>();
		vertices.add(new Vector3(-SCALE, -SCALE, -SCALE));  // left-bottom-front
		vertices.add(new Vector3( SCALE, -SCALE, -SCALE));  // right-bottom-front
		vertices.add(new Vector3(-SCALE,  SCALE, -SCALE));  // left-top-front
		vertices.add(new Vector3( SCALE,  SCALE, -SCALE));  // right-top-front
		vertices.add(new Vector3(-SCALE, -SCALE,  SCALE));  // left-bottom-back
		vertices.add(new Vector3( SCALE, -SCALE,  SCALE));  // right-bottom-back
		vertices.add(new Vector3(-SCALE,  SCALE,  SCALE));  // left-top-back
		vertices.add(new Vector3( SCALE,  SCALE,  SCALE));  // right-top-back

		Array<Face> faces = new Array<Face>();
		faces.add(new Face(new int[]{ 0, 1, 2, 3 }));   // left
		faces.add(new Face(new int[]{ 4, 5, 6, 7 }));   // right
		faces.add(new Face(new int[]{ 0, 4, 7, 3 }));   // front
		faces.add(new Face(new int[]{ 1, 5, 6, 2 }));   // back
		faces.add(new Face(new int[]{ 0, 4, 5, 1 }));   // top
		faces.add(new Face(new int[]{ 2, 6, 7, 3 }));   // bottom

		// the same box, as made by our vertex thing
		Array<Plane> planes = new Array<Plane>();
		planes.add(new Plane(Vector3.Y,          new Vector3(0f,  SCALE, 0f)));
		planes.add(new Plane(Vector3.Y.scl(-1f), new Vector3(0f, -SCALE, 0f)));
		planes.add(new Plane(Vector3.X,          new Vector3( SCALE, 0f, 0f)));
		planes.add(new Plane(Vector3.X.scl(-1f), new Vector3(-SCALE, 0f, 0f)));
		planes.add(new Plane(Vector3.Z,          new Vector3(0f, 0f,  SCALE)));
		planes.add(new Plane(Vector3.Z.scl(-1f), new Vector3(0f, 0f, -SCALE)));

		Brush expected = new Brush(vertices, faces);
		Brush brush = HalfspacePolygon.toConvex(planes);
		assertThat(expected, equalTo(brush));

	}

	/**
	 * Degenerate case; 4 planes emit top vertex.
	 */
	public void testPyramid() {

		// a simple box.
		Array<Vector3> vertices = new Array<Vector3>();
		vertices.add(new Vector3(    0f,  SCALE,  0f   ));  // top of pyramid
		vertices.add(new Vector3(-SCALE, -SCALE, -SCALE));  // left-bottom-front
		vertices.add(new Vector3( SCALE, -SCALE, -SCALE));  // right-bottom-front
		vertices.add(new Vector3( SCALE, -SCALE,  SCALE));  // right-bottom-back
		vertices.add(new Vector3(-SCALE, -SCALE,  SCALE));  // left-bottom-back

		Array<Face> faces = new Array<Face>();
		faces.add(new Face(new int[]{ 0, 1, 2 }));      // front
		faces.add(new Face(new int[]{ 0, 2, 3 }));      // right
		faces.add(new Face(new int[]{ 0, 3, 4 }));      // back
		faces.add(new Face(new int[]{ 0, 4, 1 }));      // left
		faces.add(new Face(new int[]{ 1, 2, 3, 4 }));   // bottom

		// the same box, as made by our vertex thing
		Array<Plane> planes = new Array<Plane>();
		planes.add(new Plane(vertices.get(0), vertices.get(1), vertices.get(2)));
		planes.add(new Plane(vertices.get(0), vertices.get(2), vertices.get(3)));
		planes.add(new Plane(vertices.get(0), vertices.get(3), vertices.get(4)));
		planes.add(new Plane(vertices.get(0), vertices.get(4), vertices.get(1)));
		planes.add(new Plane(vertices.get(1), vertices.get(2), vertices.get(3)));

		Brush expected = new Brush(vertices, faces);
		Brush brush = HalfspacePolygon.toConvex(planes);
		assertThat(expected, equalTo(brush));

	}

}
