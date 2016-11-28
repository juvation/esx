package com.electribesx.tool;

import java.io.File;
import com.electribesx.model.ESXFile;

// calls dump() on the file
// expecting helpful output

public class ESXCopy
{
	public static void
	main (String[] inArgs)
	throws Exception
	{
		if (inArgs.length < 5)
		{
			System.err.println ("usage: java ESXCopy fromfile tofile pattern fromnumber tonumber");
			System.exit (1);
		}
		
		ESXFile	fromFile = ESXFile.fromFile (new File (inArgs [0]));
		ESXFile	toFile = ESXFile.fromFile (new File (inArgs [1]));
		
		String	mode = inArgs [2];
		int	fromPattern = Integer.parseInt (inArgs [3]);
		int	toPattern = Integer.parseInt (inArgs [4]);
		
		System.out.println ("copying pattern " + fromPattern + " to pattern " + toPattern);

		toFile.copyPattern (fromFile, fromPattern, toPattern);
		
		toFile.write (new File ("copytemp.esx"));
	}
	
	// PRIVATE DATA

	// CONSTANTS
	
		
}
