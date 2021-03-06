attribute vec4 a_position;
attribute vec2 a_texCoord0;
uniform mat4 u_projTrans;
uniform mat4 u_worldTrans;

varying vec2 v_texCoords;
varying vec4 v_position;

void main()
{
	v_texCoords = a_texCoord0;
	gl_Position = u_projTrans * u_worldTrans * a_position;
	v_position = u_worldTrans * a_position;
}
