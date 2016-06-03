
// important to include in order to use rendered Android View to gl texture
#extension GL_OES_EGL_image_external : require

//make sure to use samplerExternalOES instead of sampler2D
uniform samplerExternalOES Texture0;    // The input texture.
uniform int LensLimits;


//uniform sampler2D Texture0;

varying mediump vec4 oColor;
varying mediump vec2 oTexCoord0;
varying mediump vec2 oTexCoord1;
varying mediump vec2 oTexCoord2;

void main()
{
    mediump float ResultA = 0.0;;

    mediump float ResultR;

    if (oTexCoord0.x > 1.0 || oTexCoord0.y > 1.0 || oTexCoord0.x < 0.0 || oTexCoord0.y < 0.0)
    {

         ResultR = (LensLimits == 1 ? 0.1 : 0.0);
    }
    else
    {
        ResultR = texture2D(Texture0, oTexCoord0).r;
        ResultA = texture2D(Texture0, oTexCoord1).a;
    }

    mediump float ResultG;
    if (oTexCoord1.x > 1.0 || oTexCoord1.y > 1.0 || oTexCoord1.x < 0.0 || oTexCoord1.y < 0.0)
    {
         ResultG = (LensLimits == 1 ? 0.2 : 0.0);
    }
    else
    {
        ResultG = texture2D(Texture0, oTexCoord1).g;
    }

    mediump float ResultB;
    if (oTexCoord2.x > 1.0 || oTexCoord2.y > 1.0 || oTexCoord2.x < 0.0 || oTexCoord2.y < 0.0)
    {
         ResultB = (LensLimits == 1 ? 0.3 : 0.0);
    }
    else
    {
        ResultB = texture2D(Texture0, oTexCoord2).b;
    }

    gl_FragColor = vec4(ResultR *  oColor.r, ResultG * oColor.g , ResultB * oColor.b, ResultA);
    //gl_FragColor = vec4((ResultR + 0.1) *  oColor.r, (ResultG  + 0.2) * oColor.g, (ResultB + 0.3) * oColor.b , 1.0);
}
