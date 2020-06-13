#version 120

uniform sampler2D DiffuseSampler;
uniform float intensity;

vec3 color = vec3(0.54, 0.01176, 0.01176);

void main()
{
    vec4 sample = texture2D(DiffuseSampler, vec2(gl_TexCoord[0]));
	gl_FragColor = vec4(mix(sample.rgb, color, intensity), sample.a);
}