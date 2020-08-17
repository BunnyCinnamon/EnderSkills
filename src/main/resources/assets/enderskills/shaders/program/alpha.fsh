#version 120

uniform sampler2D DiffuseSampler;
uniform float alpha;

void main()
{
    vec4 sample = texture2D(DiffuseSampler, vec2(gl_TexCoord[0]));
    gl_FragColor = (sample * gl_Color + vec4(0, 0, 0, 0)) * vec4(1.0, 1.0, 1.0, alpha);
}