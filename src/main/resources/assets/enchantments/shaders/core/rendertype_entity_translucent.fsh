#version 150

#moj_import <fog.glsl>

uniform sampler2D Sampler0;
uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;
uniform float GameTime;// Zeit-Uniform für den animierten Chroma-Effekt

in float vertexDistance;
in vec4 vertexColor;
in vec4 lightMapColor;
in vec4 overlayColor;
in vec2 texCoord0;

out vec4 fragColor;

// Funktion zur Umwandlung von HSV zu RGB
vec3 hsvToRgb(vec3 c) {
    vec4 K = vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);
    vec3 p = abs(fract(c.xxx + K.xyz) * 6.0 - K.www);
    return c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y);
}

void main() {
    vec4 baseColor = texture(Sampler0, texCoord0);
    if (baseColor.a < 0.1) {
        discard;
    }

    // Chroma-Effekt-Farbe berechnen (Bewegung entlang der y-Koordinate und GameTime)
    // Der Farbton (hue) wird von der y-Koordinate und GameTime beeinflusst.
    // GameTime sorgt dafür, dass der Effekt animiert wird.
    float hue = 0.002 * gl_FragCoord.y - GameTime * 1500.0;// Hier bewegt sich der Effekt nach oben entlang der y-Koordinate

    // Chroma-Farbe mit festem Farbton, Sättigung und Helligkeit
    vec3 chromaColor = hsvToRgb(vec3(hue, 0.7, 1.0));// Sättigung auf 0.7 und Helligkeit auf 1.0

    // Chroma-Effekt-Alpha festlegen
    float chromaAlpha = 0.7;// Alpha-Wert für den Chroma-Effekt

    // Berechne nur die RGB-Komponenten des Chroma-Effekts mit transparentem Alpha
    vec4 chromaEffect = vec4(chromaColor, chromaAlpha);// Der Alpha-Wert des Chroma-Effekts bleibt 0.7

    // Mische den Chroma-Effekt in die Basisfarbe, aber bewahre den Alpha-Wert der Basisfarbe bei
    vec4 color = mix(baseColor, chromaEffect, chromaAlpha);
    color.a = baseColor.a;// Setze den Alpha-Wert zurück auf den ursprünglichen Alpha-Wert der Basisfarbe

    // Weitere Modifikatoren anwenden
    color *= vertexColor * ColorModulator;
    color.rgb = mix(overlayColor.rgb, color.rgb, overlayColor.a);
    color *= lightMapColor;

    // Nebel anwenden
    fragColor = linear_fog(color, vertexDistance, FogStart, FogEnd, FogColor);
}
