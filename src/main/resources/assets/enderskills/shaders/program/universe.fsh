#version 120

#define iterations 17
#define formuparam 0.53

#define volsteps 20
#define stepsize 0.1

#define zoom 0.800
#define tile   0.850
#define speed  0.00005

#define saturation 0
#define brightness 0.0015
#define darkmatter 0.300
#define distfading 0.730

uniform sampler2D DiffuseSampler;
uniform vec2 dimensions;
uniform vec3 color;
uniform float ticks;
uniform float alpha;

uniform float yaw;
uniform float pitch;

varying vec3 position;

void main()
{
    // get ray from camera to fragment
    vec3 dir = normalize(vec3( -position));

	// rotate the ray to show the right bit of the sphere for the angle
	float sb = sin(pitch);
	float cb = cos(pitch);
	dir = normalize(vec3(dir.x, dir.y * cb - dir.z * sb, dir.y * sb + dir.z * cb));

	float sa = sin(-yaw);
	float ca = cos(-yaw);
	dir = normalize(vec3(dir.z * sa + dir.x * ca, dir.y, dir.z * ca - dir.x * sa));

    //get sample
    vec4 sample = texture2D(DiffuseSampler, vec2(dir.x,dir.y));
    //get coords and direction
	vec2 uv=vec2(dir.x, dir.y);
	float time=ticks*speed+.25;

	//mouse rotation
	float a1=111.5+time/dimensions.x*2.;
	float a2=111.8+time/dimensions.y*2.;
	mat2 rot1=mat2(cos(a1),sin(a1),-sin(a1),cos(a1));
	mat2 rot2=mat2(cos(a2),sin(a2),-sin(a2),cos(a2));
	dir.xz*=rot1;
	dir.xy*=rot2;
	vec3 from=vec3(1.,.5,0.5);
	from+=vec3(time*2.,time,-2.);
	from.xz*=rot1;
	from.xy*=rot2;

    //volumetric rendering
    float s=0.1,fade=1.;
    vec4 v=vec4(0.);
    for (int r=0; r<volsteps; r++) {
        vec3 p=from+s*dir*.5;
        p = abs(vec3(tile)-mod(p,vec3(tile*2.))); // tiling fold
        float pa,a=pa=0.;
        for (int i=0; i<iterations; i++) {
            p=abs(p)/dot(p,p)-formuparam; // the magic formula
            a+=abs(length(p)-pa); // absolute sum of average change
            pa=length(p);
        }
        float dm=max(0.,darkmatter-a*a*.001); //dark matter
        a*=a*a; // add contrast
        if (r>6) fade*=1.-dm; // dark matter, don't render near
        v+=fade;
        v+=vec4(s,s*s,s*s*s*s,s*s*s*s*s*s)*a*brightness*fade; // coloring based on distance
        fade*=distfading; // distance fading
        s+=stepsize;
    }
    v=mix(vec4(length(v)),v,saturation); //color adjust
    gl_FragColor = sample;
    gl_FragColor.rgb = mix(sample.rgb, vec4(v*.01).rgb * color, vec4(v*.01).a);
    gl_FragColor.a *= alpha;
}