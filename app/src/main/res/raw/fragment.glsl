#version 300 es

precision mediump float;

uniform sampler2D uTextureId;

in vec2 vCoord;
out vec4 fragColor;

void main(){
    fragColor = texture(uTextureId , vCoord);
}