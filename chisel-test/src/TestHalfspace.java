import com.badlogic.gdx.math.Plane;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.sastraxi.chisel.map.Brush;
import com.sastraxi.chisel.math.Face;
import com.sastraxi.chisel.math.HalfspacePolygon;
import com.sastraxi.chisel.math.LocalMath;
import org.hamcrest.core.IsEqual;
import org.junit.Assert;
import org.junit.Test;

import static org.hamcrest.core.IsEqual.*;
import static org.junit.Assert.assertThat;

public class TestHalfspace {

	final float SCALE = 5f; // absolute
	final float CUT = 0.3f; // assume 1..1 cube

	@Test
	public void testCube() {

		// a simple box.
		Array<Vector3> vertices = new Array<Vector3>();
		vertices.add(new Vector3(-SCALE, -SCALE, -SCALE));  // left-bottom-front
		vertices.add(new Vector3( SCALE, -SCALE, -SCALE));  // right-bottom-front
		vertices.add(new Vector3( SCALE,  SCALE, -SCALE));  // right-top-front
		vertices.add(new Vector3(-SCALE,  SCALE, -SCALE));  // left-top-front
		vertices.add(new Vector3(-SCALE, -SCALE,  SCALE));  // left-bottom-back
		vertices.add(new Vector3( SCALE, -SCALE,  SCALE));  // right-bottom-back
		vertices.add(new Vector3( SCALE,  SCALE,  SCALE));  // right-top-back
		vertices.add(new Vector3(-SCALE,  SCALE,  SCALE));  // left-top-back

		Array<Face> faces = new Array<Face>();
		faces.add(new Face(new int[]{ 0, 1, 2, 3 }));   // front
		faces.add(new Face(new int[]{ 4, 5, 6, 7 }));   // back
		faces.add(new Face(new int[]{ 0, 4, 7, 3 }));   // left
		faces.add(new Face(new int[]{ 1, 5, 6, 2 }));   // right
		faces.add(new Face(new int[]{ 0, 4, 5, 1 }));   // bottom
		faces.add(new Face(new int[]{ 2, 6, 7, 3 }));   // top

		// the same in terms of half-spaces
		Array<Plane> planes = new Array<Plane>();
		planes.add(new Plane(new Vector3(0f,  1f, 0f), new Vector3(0f,  SCALE, 0f)));
		planes.add(new Plane(new Vector3(0f, -1f, 0f), new Vector3(0f, -SCALE, 0f)));
		planes.add(new Plane(new Vector3( 1f, 0f, 0f), new Vector3( SCALE, 0f, 0f)));
		planes.add(new Plane(new Vector3(-1f, 0f, 0f), new Vector3(-SCALE, 0f, 0f)));
		planes.add(new Plane(new Vector3(0f, 0f,  1f), new Vector3(0f, 0f,  SCALE)));
		planes.add(new Plane(new Vector3(0f, 0f, -1f), new Vector3(0f, 0f, -SCALE)));

		Brush expected = new Brush(vertices, faces);
		Brush brush = HalfspacePolygon.toConvex(planes);
		assertThat(expected, equalTo(brush));

	}

	/**
	 * Degenerate case; 4 planes emit top vertex.
	 */
	@Test
	public void testPyramid() {

		// a simple pyramid (contained in a box).
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

		// the same in terms of half-spaces
		Array<Plane> planes = new Array<Plane>();
		planes.add(LocalMath.safePlane(new Vector3( 0f, 1f,  2f), new Vector3(0f, SCALE, 0f)));
		planes.add(LocalMath.safePlane(new Vector3( 0f, 1f, -2f), new Vector3(0f, SCALE, 0f)));
		planes.add(LocalMath.safePlane(new Vector3( 2f, 1f,  0f), new Vector3(0f, SCALE, 0f)));
		planes.add(LocalMath.safePlane(new Vector3(-2f, 1f,  0f), new Vector3(0f, SCALE, 0f)));
		planes.add(LocalMath.safePlane(new Vector3( 0f, -1f, 0f), new Vector3(0f,  -SCALE, 0f)));

		Brush expected = new Brush(vertices, faces);
		Brush brush = HalfspacePolygon.toConvex(planes);
		assertThat(expected, equalTo(brush));

	}

	@Test
	public void testTriangularPrism() {

		// triangular prism (contained in a box).
		Array<Vector3> vertices = new Array<Vector3>();
		vertices.add(new Vector3(-SCALE,  SCALE,  SCALE));  // left-top-back
		vertices.add(new Vector3( SCALE,  SCALE,  SCALE));  // right-top-back
		vertices.add(new Vector3(-SCALE, -SCALE, -SCALE));  // left-bottom-front
		vertices.add(new Vector3( SCALE, -SCALE, -SCALE));  // right-bottom-front
		vertices.add(new Vector3( SCALE, -SCALE,  SCALE));  // right-bottom-back
		vertices.add(new Vector3(-SCALE, -SCALE,  SCALE));  // left-bottom-back

		Array<Face> faces = new Array<Face>();
		faces.add(new Face(new int[]{ 0, 5, 4, 1 }));   // back
		faces.add(new Face(new int[]{ 0, 5, 2 }));      // left
		faces.add(new Face(new int[]{ 1, 3, 4 }));      // right
		faces.add(new Face(new int[]{ 5, 4, 3, 2 }));   // bottom
		faces.add(new Face(new int[]{ 0, 2, 3, 1 }));   // slope

		// the same in terms of half-spaces
		Array<Plane> planes = new Array<Plane>();
		planes.add(new Plane(new Vector3(0f, -1f, 0f), new Vector3(0f, -SCALE, 0f))); // bottom
		planes.add(new Plane(new Vector3( 1f, 0f, 0f), new Vector3( SCALE, 0f, 0f))); // right
		planes.add(new Plane(new Vector3(-1f, 0f, 0f), new Vector3(-SCALE, 0f, 0f))); // left
		planes.add(new Plane(new Vector3(0f, 0f,  1f), new Vector3(0f, 0f,  SCALE))); // back
		planes.add(LocalMath.safePlane(new Vector3(0f, 1f, -1f), new Vector3(0f, 0f, 0f))); // slope

		Brush expected = new Brush(vertices, faces);
		Brush brush = HalfspacePolygon.toConvex(planes);
		assertThat(expected, equalTo(brush));
	}

	@Test
	public void testCubeCutLeftTopFront() {

		// a simple box.
		Array<Vector3> vertices = new Array<Vector3>();
		vertices.add(new Vector3(-SCALE, -SCALE, -SCALE));  // left-bottom-front
		vertices.add(new Vector3( SCALE, -SCALE, -SCALE));  // right-bottom-front
		vertices.add(new Vector3( SCALE,  SCALE, -SCALE));  // right-top-front

		// left-top-front cut
		vertices.add(new Vector3(-SCALE*(1.0f - CUT),  SCALE, -SCALE));  // x-
		vertices.add(new Vector3(-SCALE,  SCALE*(1.0f - CUT), -SCALE));  // y-
		vertices.add(new Vector3(-SCALE,  SCALE, -SCALE*(1.0f - CUT)));  // z

		vertices.add(new Vector3(-SCALE, -SCALE,  SCALE));  // left-bottom-back
		vertices.add(new Vector3( SCALE, -SCALE,  SCALE));  // right-bottom-back
		vertices.add(new Vector3( SCALE,  SCALE,  SCALE));  // right-top-back
		vertices.add(new Vector3(-SCALE,  SCALE,  SCALE));  // left-top-back

		Array<Face> faces = new Array<Face>();
		faces.add(new Face(new int[]{ 0, 1, 2, 4, 3 }));        // front
		faces.add(new Face(new int[]{ 4+2, 5+2, 6+2, 7+2 }));   // back
		faces.add(new Face(new int[]{ 0, 4+2, 7+2, 5, 4 }));    // left
		faces.add(new Face(new int[]{ 1, 5+2, 6+2, 2 }));       // right
		faces.add(new Face(new int[]{ 0, 4+2, 5+2, 1 }));       // bottom
		faces.add(new Face(new int[]{ 2, 6+2, 7+2, 5, 3 }));    // top
		faces.add(new Face(new int[]{ 3, 4, 5 }));              // cut

		// the same in terms of half-spaces
		Array<Plane> planes = new Array<Plane>();
		planes.add(new Plane(new Vector3(0f,  1f, 0f), new Vector3(0f,  SCALE, 0f)));
		planes.add(new Plane(new Vector3(0f, -1f, 0f), new Vector3(0f, -SCALE, 0f)));
		planes.add(new Plane(new Vector3( 1f, 0f, 0f), new Vector3( SCALE, 0f, 0f)));
		planes.add(new Plane(new Vector3(-1f, 0f, 0f), new Vector3(-SCALE, 0f, 0f)));
		planes.add(new Plane(new Vector3(0f, 0f,  1f), new Vector3(0f, 0f,  SCALE)));
		planes.add(new Plane(new Vector3(0f, 0f, -1f), new Vector3(0f, 0f, -SCALE)));
		planes.add(LocalMath.safePlane(new Vector3(-1f, 1f, -1f), vertices.get(3))); // left-top-front cut

		System.out.println(planes.get(6));

		Brush expected = new Brush(vertices, faces);
		Brush brush = HalfspacePolygon.toConvex(planes);
		assertThat(expected, equalTo(brush));

	}


}
