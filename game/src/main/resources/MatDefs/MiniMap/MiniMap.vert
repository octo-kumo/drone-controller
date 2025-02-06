#version 150
attribute vec3 inPosition;
attribute vec2 inTexCoord;

out vec2 texCoord;
uniform mat4 jme_ModelViewProjection;
void main() {
    texCoord = inTexCoord;
    vec4 modelSpacePos = vec4(inPosition, 1.0);
    gl_Position = jme_ModelViewProjection * modelSpacePos;

}