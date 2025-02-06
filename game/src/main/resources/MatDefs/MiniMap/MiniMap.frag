#version 150
uniform sampler2D m_ColorMap;
uniform sampler2D m_Mask;
uniform sampler2D m_Overlay;

in vec2 texCoord;
out vec4 fragColor;

void main() {
    vec4 color = texture(m_ColorMap, texCoord);
    vec4 mask = texture(m_Mask, texCoord);
    vec4 overlay = texture(m_Overlay, texCoord);

    color = mix(color, overlay, 1.0 - mask.a);

    if (overlay.a < 0.5) {
        discard;
    }

    fragColor = color;
}
