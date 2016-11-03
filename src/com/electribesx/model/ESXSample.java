// ESXSample.java

// interface shared by mono and stereo samples, fwiw

package com.electribesx.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;

public interface ESXSample
{
	public void
	initFromFile (File inFile)
	throws Exception;
	
	public void
	dump (int inSampleNumber);

	public String
	getName ();
	
	public int
	getSample (int inSampleNumber);
		
	public int
	getSampleSize ();

	public void
	setSampleRate (int inSampleRate);
	
	public void
	writeSampleData (OutputStream outStream, int inSampleNumber)
	throws Exception;
		
}
