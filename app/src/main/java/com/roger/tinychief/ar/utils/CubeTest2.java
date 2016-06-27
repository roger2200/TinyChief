package com.roger.tinychief.ar.utils;

import java.nio.Buffer;

public class CubeTest2 extends MeshObject{
	private Buffer mVertBuff;
    private Buffer mTexCoordBuff;
    private Buffer mNormBuff;
    private Buffer mIndBuff;
    
    private int indicesNumber = 0;
    private int verticesNumber = 0;
	
	public CubeTest2()
    {
		setVerts();
        setTexCoords();
        setNorms();
        setIndices();
    }

	private void setIndices(){
		short[] indice = new short[]{0, 1, 2, 3,0};
		mIndBuff = fillBuffer(indice);
        indicesNumber = indice.length;
	}
	private void setVerts(){
		
		double[] verts = new double[] {-0.5, -0.5, 0.0, 0.9, -0.9, 0.0, 0.5, 0.5, 0.0, -0.5, 0.5, 0.0, };
		mVertBuff = fillBuffer(verts);
        verticesNumber = 6;
	}
	private void setNorms(){
		double[] norms = new double []{0.0, 0.0, 1.0, 0.0, 0.0, 1.0, 0.0, 0.0, 1.0, 0.0, 0.0, 1.0};
		mNormBuff = fillBuffer(norms);
	}
	private void setTexCoords(){
		double[] cords = new double[]{	0.0, 0.0, 1.0, 0.0, 1.0, 1.0, 0.0, 1.0};
		mTexCoordBuff = fillBuffer(cords);
	}
	 public int getNumObjectIndex()
	    {
	        return indicesNumber;
	    }
	    
	    
	    @Override
	    public int getNumObjectVertex()
	    {
	        return verticesNumber;
	    }
	    
	    
	    @Override
	    public Buffer getBuffer(BUFFER_TYPE bufferType)
	    {
	        Buffer result = null;
	        switch (bufferType)
	        {
	            case BUFFER_TYPE_VERTEX:
	                result = mVertBuff;
	                break;
	            case BUFFER_TYPE_TEXTURE_COORD:
	                result = mTexCoordBuff;
	                break;
	            case BUFFER_TYPE_NORMALS:
	                result = mNormBuff;
	                break;
	            case BUFFER_TYPE_INDICES:
	                result = mIndBuff;
	            default:
	                break;
	        }
	        
	        return result;
	    }
}
