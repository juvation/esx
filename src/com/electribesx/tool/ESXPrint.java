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
		if (inArgs.length == 0)
		{
			System.err.println ("usage: java ESXPrint file [mode] [number]");
			System.exit (1);
		}
		
		ESXFile	file = ESXFile.fromFile (new File (inArgs [0]));
		
		String	mode = null;
		int	param = -1;
		
		if (inArgs.length > 1)
		{
			mode = inArgs [1];
			
			if (inArgs.length > 2)
			{
				param = Integer.parseInt (inArgs [2]);
			}
		}
		
		if (mode == null)
		{
			file.dump ();
		}
		else
		if (mode.equals ("pattern"))
		{
			if (param == -1)
			{
				file.getPattern (0).dump (0);
			}
			else
			{
				if (param == 0)
				{
					System.err.println ("pattern numbers start at 1");
					System.exit (1);
				}
				
				file.getPattern (param - 1).dump (param);
			}
		}
		else
		if (mode.equals ("patternheader"))
		{
			if (param == -1)
			{
				file.getPattern (0).dumpHeader (0);
			}
			else
			{
				if (param == 0)
				{
					System.err.println ("pattern numbers start at 1");
					System.exit (1);
				}
				
				file.getPattern (param - 1).dumpHeader (param);
			}
		}
		else
		if (mode.equals ("patterns"))
		{
			for (int i = 0; i < 256; i++)
			{
				String	name = file.getPattern (i).getName ();
				
				if (! name.equals ("        "))
				{
					System.out.println (i + " = '" + name + "'");
				}
			}
		}
		else
		if (mode.equals ("mono"))
		{
			if (param == -1)
			{
				for (int i = 0; i < 256; i++)
				{
					file.getMonoSample (i).dump (i);
				}
			}
			else
			{
				file.getMonoSample (param).dump (param);
			}
		}
		else
		if (mode.equals ("stereo"))
		{
			if (param == -1)
			{
				for (int i = 0; i < 128; i++)
				{
					file.getStereoSample (i).dump (i);
				}
			}
			else
			{
				file.getStereoSample (param).dump (param);
			}
		}
	}
	
	// PRIVATE DATA

	// CONSTANTS
	
		
}
