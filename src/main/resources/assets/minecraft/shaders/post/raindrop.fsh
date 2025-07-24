#version 150

// Raindrop Post-Processing Shader
// Ported from Shadertoy: https://www.shadertoy.com/view/ldSBWW
// Author: Élie Michel (Original), Ported for Minecraft 1.21.5

uniform sampler2D InSampler;      // Main game render (iChannel0)
uniform sampler2D NoiseSampler;   // Noise texture for displacement (iChannel1)

uniform vec2 InSize;              // Screen resolution (iResolution.xy)
uniform float GameTime;           // Game time in seconds (iTime)
uniform float WiperActive = 1.0;        // 1.0 = wiper active, 0.0 = full rain

in vec2 texCoord;                 // Screen coordinates

out vec4 fragColor;

void main() {
    vec2 u = texCoord;  // Already normalized in Minecraft
    
    // Get displacement from noise texture
    vec2 n = texture(NoiseSampler, u * 0.1).rg;
    
    // Base color
    fragColor = texture(InSampler, u);
    
    // WIPER CONTROL FLAG
    if (WiperActive > 0.5) {  // Wiper is active
        // WINDSHIELD WIPER EFFECT
        // Wiper center position (bottom center - like a car windshield)
        vec2 wiperCenter = vec2(0.5, 0.0);
        
        // Calculate angle from wiper center to current pixel
        vec2 toPixel = u - wiperCenter;
        float pixelAngle = atan(toPixel.x, toPixel.y); // Swapped x,y for vertical orientation
        
        // Wiper animation - sweeps from -80° to +80° (160° total)
        float wiperTime = GameTime * 5000.0; // Match updated Kotlin code speed
        float wiperAngle = sin(wiperTime) * 1.396263402; // ±80 degrees in radians (toRadians(80f))
        
        // Wiper width (in radians)
        float wiperWidth = 0.174532925; // ~10 degrees
        
        // Distance from wiper center
        float pixelDistance = length(toPixel);
        
        // Check if pixel is in wiped area (centered around 0° = upward direction)
        bool inWipedArea = abs(pixelAngle - wiperAngle) < wiperWidth && pixelDistance < 0.9 && toPixel.y > 0.0;
        
        // Calculate "dry time" after wiper passed
        // Find when the wiper was last at this pixel's angle
        float timeSinceWiped = 999.0; // Default: very long time ago
        
        if (pixelDistance < 0.9 && toPixel.y > 0.0) { // Only for areas the wiper can reach
            // Calculate how long ago the wiper was at this angle
            float angleProgress = pixelAngle / 1.396263402; // Normalize to -1 to 1 range (±80°)
            
            // Find the phase when wiper was at this angle
            float wiperPhase = asin(clamp(angleProgress, -1.0, 1.0));
            float currentPhase = mod(wiperTime, 6.283185307); // Current phase in sine wave
            
            // Calculate time difference (considering sine wave period)
            float phaseDiff = min(abs(currentPhase - wiperPhase), abs(currentPhase - wiperPhase + 6.283185307));
            timeSinceWiped = phaseDiff / 5000.0; // Convert back to game time units (match speed)
        }
        
        // Skip raindrop effect if in wiped area OR recently wiped (dry time)
        float dryDuration = 3.0; // Stay dry for 3 game time units after wiping
        bool isCurrentlyWiped = inWipedArea;
        bool isRecentlyWiped = timeSinceWiped < dryDuration;
        
        if (isCurrentlyWiped || isRecentlyWiped) {
            return; // Just show the clean base color
        }
    }
    // If WiperActive <= 0.5: Full screen rain (no wiper logic)
    
    // Loop through the different inverse sizes of drops
    for (float r = 4.0; r > 0.0; r--) {
        // Number of potential drops (in a grid)
        vec2 x = InSize * r * 0.015;
        
        vec2 p = 6.28 * u * x + (n - 0.5) * 2.0;
        vec2 s = sin(p);
        
        // Current drop properties. Coordinates are rounded to ensure a
        // consistent value among the fragments of a given drop.
        vec4 d = texture(NoiseSampler, round(u * x - 0.25) / x);
        
        // Drop shape and fading - EXACT like original (GameTime * 1200 for good speed)
        float t = (s.x + s.y) * max(0.0, 1.0 - fract(GameTime * 1200.0 * (d.b + 0.1) + d.g) * 2.0);
        
        // d.r -> only x% of drops are kept on, with x depending on the size of drops
        if (d.r < (5.0 - r) * 0.08 && t > 0.5) {
            // Drop normal calculation
            vec3 v = normalize(-vec3(cos(p), mix(0.2, 2.0, t - 0.5)));
            
            // Poor man's refraction (no visual need to do more)
            fragColor = texture(InSampler, u - v.xy * 0.3);
        }
    }
} 