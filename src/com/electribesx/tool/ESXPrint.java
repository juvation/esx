package com.electribesx.tool;

import java.io.File;
import com.electribesx.model.ESXFile;

// calls dump() on the file
// expecting helpful output

public class ESXPrint
{
	public static void
	main (String[] inArgs)
	throws Exception
	{
		ESXFile	file = ESXFile.fromFile (new File (inArgs [0]));
		file.dump ();
	}
	
	// PRIVATE DATA

	// CONSTANTS
	
		
}
