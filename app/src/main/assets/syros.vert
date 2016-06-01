uniform highp vec2 EyeToSourceUVScale;
uniform highp vec2 EyeToSourceUVOffset;
uniform highp vec2 EyeToSourceScale;
uniform highp vec2 EyeToSourceOffset;

attribute highp vec2 Position;
attribute highp vec4 Color;

attribute highp vec2 TexCoord0;
attribute highp vec2 TexCoord1;
attribute highp vec2 TexCoord2;


varying mediump vec2 oTexCoord0;
varying mediump vec2 oTexCoord1;
varying mediump vec2 oTexCoord2;

varying mediump vec4 oColor;

void main()
{
    gl_Position.x = Position.x * EyeToSourceScale.x + EyeToSourceOffset.x;
    gl_Position.y = Position.y * EyeToSourceScale.y + EyeToSourceOffset.y;
    gl_Position.z = 0.5;
    gl_Position.w = 1.0;

    float red_color_distorsion = 0.0;
    float green_color_distorsion = 0.0;
    float blue_color_distorsion = 0.0;

    if(1 == 1)
    {
        red_color_distorsion = -0.007;
        green_color_distorsion = 0.0;
        blue_color_distorsion = 0.009;
    }


    
    // Vertex inputs are in TanEyeAngle space for the R,G,B channels (i.e. after chromatic aberration and distortion).
    // Scale them into the correct [0-1],[0-1] UV lookup space (depending on eye)
    oTexCoord0 = ((TexCoord0 - vec2(0.5,0.5)) * EyeToSourceUVScale * (1.0 + red_color_distorsion)) + vec2(0.5,0.5) + EyeToSourceUVOffset;
    //oTexCoord0.y = 1.0-oTexCoord0.y;
    oTexCoord1 = ((TexCoord1 - vec2(0.5,0.5)) * EyeToSourceUVScale * (1.0 + green_color_distorsion)) + vec2(0.5,0.5) + EyeToSourceUVOffset;
    //oTexCoord1.y = 1.0-oTexCoord1.y;
    oTexCoord2 = ((TexCoord2 - vec2(0.5,0.5)) * EyeToSourceUVScale* (1.0 + blue_color_distorsion)) + vec2(0.5,0.5) + EyeToSourceUVOffset;
    //oTexCoord2.y = 1.0-oTexCoord2.y;

    //mediump float colorCorrectionRatio = 0.75;
    //oTexCoord1 = (1.0 - colorCorrectionRatio) * oTexCoord0 + colorCorrectionRatio * oTexCoord1;
    //oTexCoord2 = (1.0 - colorCorrectionRatio) * oTexCoord0 + colorCorrectionRatio * oTexCoord2;

    /*oTexCoord0 = TexCoord0;
    oTexCoord1 = TexCoord1;
    oTexCoord2 = TexCoord2;*/

    oColor = Color; // Used for vignette fade.
}
