package com.roger.tinychief.ar.utils;

import java.nio.Buffer;

//這是食物的模型,只是一個2D的正方形
public class Food extends MeshObject {
    private Buffer mVertBuff;
    private Buffer mTexCoordBuff;
    private Buffer mIndBuff;

    private int indicesNumber = 0;
    private int verticesNumber = 0;
    public double[] verts = new double[]{
            -20, -20, 0.0,
            20, -20, 0.0,
            20, 20, 0.0,
            -20, 20, 0.0,
    };

    public Food() {
        setVerts();
        setTexCoords();
        setIndices();
    }

    private void setIndices() {
        short[] indice = new short[]{0, 1, 2, 0, 2, 3};
        mIndBuff = fillBuffer(indice);
        indicesNumber = indice.length;
    }

    public void setVerts() {
        mVertBuff = fillBuffer(verts);
        verticesNumber = verts.length / 3;
    }

    private void setTexCoords() {
        double[] cords = new double[]{
                0.0, 0.0,
                1.0, 0.0,
                1.0, 1.0,
                0.0, 1.0
        };
        mTexCoordBuff = fillBuffer(cords);
    }

    public int getNumObjectIndex() {
        return indicesNumber;
    }

    @Override
    public int getNumObjectVertex() {
        return verticesNumber;
    }

    @Override
    public Buffer getBuffer(BUFFER_TYPE bufferType) {
        Buffer result = null;
        switch (bufferType) {
            case BUFFER_TYPE_VERTEX:
                result = mVertBuff;
                break;
            case BUFFER_TYPE_TEXTURE_COORD:
                result = mTexCoordBuff;
                break;
            case BUFFER_TYPE_INDICES:
                result = mIndBuff;
            default:
                break;
        }
        return result;
    }
}
