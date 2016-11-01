package com.electribesx.tool;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import com.electribesx.model.ESXFile;

// POWER TOOL for reading and generating ESX files haha

public class ESXBuild
{
	public static void
	main (String[] inArgs)
	throws Exception
	{
		if (inArgs.length < 2)
		{
			System.err.println ("usage: ESX file.esx file.properties");
			System.exit (1);
		}

		ESXFile	file = ESXFile.fromFile (new File (inArgs [0]));

		Properties	properties = new Properties ();
		FileInputStream	fis = new FileInputStream (inArgs [1]);
		
		try
		{
			properties.load (fis);
		}
		finally
		{
			fis.close ();
		}
		
		for (int i = 0; i < 256; i++)
		{
			String	key = "monosample." + i + ".file";
			String	value = properties.getProperty (key);
			
			if (value != null && value.length () > 0)
			{
				file.getMonoSample (i).initFromFile (new File (value));

				key = "monosample." + i + ".name";
				value = properties.getProperty (key);
				
				if (value != null && value.length () > 0)
				{
					file.getMonoSample (i).setName (value);
				}
			}
		}

		for (int i = 0; i < 128; i++)
		{
			String	key = "stereosample." + i + ".file";
			String	value = properties.getProperty (key);
			
			if (value != null && value.length () > 0)
			{
				file.getStereoSample (i).initFromFile (new File (value));

				key = "stereosample." + i + ".name";
				value = properties.getProperty (key);
				
				if (value != null && value.length () > 0)
				{
					file.getStereoSample (i).setName (value);
				}
			}
		}
		
		

/*
		ESXPattern	pattern = file.getPattern (0);

		// kick pattern
		ESXDrumPart	part = pattern.getDrumPart (0);
		byte[]	sequence = part.getSequence ();
		
		sequence [0] = 0;
		sequence [1] = 0;
		sequence [2] = 0;
		sequence [3] = 0;
		
		sequence [0] = (1 << 0) | (1 << 4);
		sequence [1] = (1 << 0) | (1 << 4);

		// hh pattern
		part = pattern.getDrumPart (7);
		sequence = part.getSequence ();

		sequence [0] = 0xff;
		sequence [1] = 0xff;
*/
		
		File	newFile = new File ("out.esx");
		file.write (newFile);
	}
	
	// PRIVATE DATA

	// CONSTANTS
	
		
}
