#version 300 es

precision mediump float;

layout(location = 0) in vec2 aPos;
layout(location = 1) in vec2 aCoord;

out vec2 vCoord;

void main(){
    gl_Position = vec4(aPos ,0.0f , 1.0f);
    vCoord = aCoord;
}