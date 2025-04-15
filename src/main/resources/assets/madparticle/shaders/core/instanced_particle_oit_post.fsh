#version 400

uniform sampler2D accum;
uniform sampler2D reveal;

out vec4 fragColor;

const float EPSILON = 0.00001f;

bool isApproximatelyEqual(float a, float b){
    return abs(a - b) <= (abs(a) < abs(b) ? abs(b) : abs(a)) * EPSILON;
}

float max3(vec3 v){
    return max(max(v.x, v.y), v.z);
}

vec3 mapColor(vec3 color) {
    float luminance = dot(color, vec3(0.2126, 0.7152, 0.0722));
    if (luminance <= 1.5){
        return color;
    }
    float logLum = log2(max(luminance, 1e-4));
    float mappedLum = (logLum + 0.8) / (logLum + 1.0);
    vec3 tonemapped = color * (mappedLum / luminance);
    return tonemapped;
}

void main() {
    // fragment coordination
    ivec2 coords = ivec2(gl_FragCoord.xy);
    // fragment revealage
    float revealage = texelFetch(reveal, coords, 0).r;
    // save the blending and color texture fetch cost if there is not a transparent fragment
    if (revealage > 0.999f){
        discard;
    }
    // fragment color
    vec4 accumulation = texelFetch(accum, coords, 0);
    // suppress overflow
    if (isinf(max3(abs(accumulation.rgb)))){
        accumulation.rgb = vec3(accumulation.a);
    }
    // prevent floating point precision bug
    vec3 average_color = accumulation.rgb / max(accumulation.a, EPSILON);
    // blend pixels
    float alpha = 1.0f - revealage;
    fragColor = vec4(mapColor(average_color), alpha);
}