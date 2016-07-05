uniform highp vec2 EyeToSourceUVScale;
uniform highp vec2 EyeToSourceUVOffset;
uniform highp vec2 EyeToSourceScale;
uniform highp vec2 EyeToSourceOffset;
uniform int ChromaticAberrationCorrection;

attribute highp vec2 Position;
attribute highp vec4 Color;

attribute highp vec2 TexCoord0;
attribute highp vec2 TexCoord1;
attribute highp vec2 TexCoord2;


varying highp vec2 oTexCoord0;
varying highp vec2 oTexCoord1;
varying highp vec2 oTexCoord2;

varying highp vec4 oColor;

void main()
{
    gl_Position.x = Position.x * EyeToSourceScale.x + EyeToSourceOffset.x;
    gl_Position.y = Position.y * EyeToSourceScale.y + EyeToSourceOffset.y;
    gl_Position.z = 0.5;
    gl_Position.w = 1.0;

    float red_color_distorsion = 0.0;
    float green_color_distorsion = 0.0;
    float blue_color_distorsion = 0.0;

    if(ChromaticAberrationCorrection == 1)
    {
        red_color_distorsion = -0.006;
        blue_color_distorsion = 0.009;
    }

    // Vertex inputs are in TanEyeAngle space for the R,G,B channels (i.e. after chromatic aberration and distortion).
    // Scale them into the correct [0-1],[0-1] UV lookup space (depending on eye)
    oTexCoord0 = ((TexCoord0 - vec2(0.5,0.5)) * EyeToSourceUVScale * (1.0 + red_color_distorsion)) + vec2(0.5,0.5) + EyeToSourceUVOffset;
    oTexCoord1 = ((TexCoord1 - vec2(0.5,0.5)) * EyeToSourceUVScale * (1.0 + green_color_distorsion)) + vec2(0.5,0.5) + EyeToSourceUVOffset;
    oTexCoord2 = ((TexCoord2 - vec2(0.5,0.5)) * EyeToSourceUVScale * (1.0 + blue_color_distorsion)) + vec2(0.5,0.5) + EyeToSourceUVOffset;

    oColor = Color; // Used for vignette fade.
}
