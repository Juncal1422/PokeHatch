#ifdef GL_ES
precision mediump float;
#endif
varying vec4 v_color;
varying vec2 v_uv;
uniform sampler2D u_texture;
uniform float u_flash;
void main() {
    vec4 texel = texture2D(u_texture, v_uv);
    gl_FragColor = vec4(mix(texel.rgb, vec3(1.0,0.97,0.78), u_flash), texel.a) * v_color;
}
