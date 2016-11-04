package com.electribesx.tool;

import java.io.File;
import com.electribesx.model.ESXFile;
import com.electribesx.model.ESXMonoSample;
import com.electribesx.model.ESXStereoSample;

public class ESXVerify
{
	public static void
	main (String[] inArgs)
	throws Exception
	{
		ESXFile	file = ESXFile.fromFile (new File (inArgs [0]));
		file.verify ();
	}
	
	// PRIVATE DATA

	// CONSTANTS
	
		
}
