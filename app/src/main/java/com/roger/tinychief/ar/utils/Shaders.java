/*===============================================================================
Copyright (c) 2016 PTC Inc. All Rights Reserved.

Copyright (c) 2012-2014 Qualcomm Connected Experiences, Inc. All Rights Reserved.

Vuforia is a trademark of PTC Inc., registered in the United States and other 
countries.
===============================================================================*/

package com.roger.tinychief.ar.utils;

//OpenGLES 2.0 才有的 繪圖用的Shader
public class Shaders
{
    public static final String CUBE_MESH_VERTEX_SHADER = "              \n"
        + "attribute vec4 vertexPosition;                                                      \n"
        + "attribute vec4 vertexNormal;                                                       \n"
        + "attribute vec2 vertexTexCoord;                                                   \n"
        + "varying vec2 texCoord;                                                              \n"
        + "varying vec4 normal;                                                                 \n"
        + "uniform mat4 modelViewProjectionMatrix;                                   \n"
        + "void main(){                                                                             \n"
        + "   gl_Position = modelViewProjectionMatrix * vertexPosition;         \n"
        + "   normal = vertexNormal;                                                           \n"
        + "   texCoord = vertexTexCoord;                                                    \n"
        + "} ";

    public static final String CUBE_MESH_FRAGMENT_SHADER = "          \n"
        + "precision mediump float;                                                             \n"
        + "varying vec2 texCoord;                                                               \n"
        + "varying vec4 normal;                                                                  \n"
        + "uniform sampler2D texSampler2D;                                                \n"
        + "void main(){                                                                               \n"
        + "     vec4 texColor = texture2D( texSampler2D, texCoord );               \n"
        + "     if(texColor.a < 0.1)                                                                 \n"
        + "         discard;                                                                              \n"
        + "     gl_FragColor = texColor;                                                         \n"
        + "} ";

}
