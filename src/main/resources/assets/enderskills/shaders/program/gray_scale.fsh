#version 120

uniform sampler2D DiffuseSampler;

void main()
{
    vec4 sample = texture2D(DiffuseSampler, vec2(gl_TexCoord[0]));
    float gray = dot(sample.xyz, vec3(0.299, 0.587, 0.114));
    gl_FragColor = vec4(gray, gray, gray, sample.a);
}