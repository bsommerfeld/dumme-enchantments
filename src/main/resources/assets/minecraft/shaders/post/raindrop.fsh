#version 150

// Raindrop Post-Processing Shader
// Ported from Shadertoy: https://www.shadertoy.com/view/ldSBWW
// Author: Élie Michel (Original), Ported for Minecraft 1.21.5
// License: CC BY 3.0

uniform sampler2D InSampler;      // Main game render (iChannel0) 
uniform sampler2D NoiseSampler;   // Noise texture for displacement (iChannel1)

uniform vec2 InSize;              // Screen resolution (iResolution.xy)
uniform float Time;               // Game time in seconds (iTime)

in vec2 texCoord;                 // Screen coordinates

out vec4 fragColor;

void main() {
    vec2 u = texCoord;  // Already normalized in Minecraft
    
    // Get displacement from noise texture
    vec2 n = texture(NoiseSampler, u * 0.1).rg;
    
    // Base color with slight blur/mip level
    fragColor = texture(InSampler, u);
    
    // Loop through the different inverse sizes of drops
    for (float r = 4.0; r > 0.0; r--) {
        // Number of potential drops (in a grid)
        vec2 x = InSize * r * 0.015;
        
        // Phase calculation with noise displacement
        vec2 p = 6.28318530718 * u * x + (n - 0.5) * 2.0;
        vec2 s = sin(p);
        
        // Current drop properties. Coordinates are rounded to ensure a
        // consistent value among the fragments of a given drop.
        vec4 d = texture(NoiseSampler, round(u * x - 0.25) / x);
        
        // Drop shape and fading
        float t = (s.x + s.y) * max(0.0, 1.0 - fract(Time * (d.b + 0.1) + d.g) * 2.0);
        
        // d.r -> only x% of drops are kept on, with x depending on the size of drops
        if (d.r < (5.0 - r) * 0.08 && t > 0.5) {
            // Drop normal calculation
            vec3 v = normalize(-vec3(cos(p), mix(0.2, 2.0, t - 0.5)));
            
            // Poor man's refraction (no visual need to do more)
            fragColor = texture(InSampler, u - v.xy * 0.3);
        }
    }
} 