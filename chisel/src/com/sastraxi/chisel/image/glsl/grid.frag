#ifdef GL_ES
precision mediump float;
#endif

const float SCALE = 1.0;

const float majorSpacing = 4.0;
const float majorWidth = 0.06;
const vec4  majorColour = vec4(0.3, 0.9, 1.0, 1.0);

const float minorSpacing = 1.0;
const float minorWidth = 0.06;
const vec4 minorColour = vec4(0.3, 0.9, 1.0, 0.4);

varying vec2 v_texCoords;
varying vec4 v_position;

void main(void)
{

	vec2 coord = v_position.xz;
	float x1 = mod(coord.x * SCALE + 0.5*majorWidth, majorSpacing);
	float y1 = mod(coord.y * SCALE + 0.5*majorWidth, majorSpacing);

	if (x1 < majorWidth || y1 < majorWidth) {
		gl_FragColor = majorColour;
	} else {
		float x2 = mod(coord.x * SCALE + 0.5*minorWidth, minorSpacing);
		float y2 = mod(coord.y * SCALE + 0.5*minorWidth, minorSpacing);
		if (x2 < minorWidth || y2 < minorWidth) {
			gl_FragColor = minorColour;
		} else {
			discard;
		}
	}

}
