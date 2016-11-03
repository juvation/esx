package com.electribesx.tool;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import com.electribesx.model.ESXDrumPart;
import com.electribesx.model.ESXFile;
import com.electribesx.model.ESXPattern;

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
		
		// MONO SAMPLES
		
		for (int i = 0; i < 256; i++)
		{
			String	key = "monosample." + i + ".file";
			String	value = properties.getProperty (key);
			
			if (value != null && value.length () > 0)
			{
				System.out.println ("setting mono sample " + i + " to file " + value);
				file.getMonoSample (i).initFromFile (new File (value));

				key = "monosample." + i + ".name";
				value = properties.getProperty (key);
				
				if (value != null && value.length () > 0)
				{
					System.out.println ("setting mono sample " + i + " name to " + value);
					file.getMonoSample (i).setName (value);
				}
			}
		}

		// STEREO SAMPLES
		
		for (int i = 0; i < 128; i++)
		{
			String	key = "stereosample." + i + ".file";
			String	value = properties.getProperty (key);
			
			if (value != null && value.length () > 0)
			{
				System.out.println ("setting stereo sample " + i + " to file " + value);
				file.getStereoSample (i).initFromFile (new File (value));

				key = "stereosample." + i + ".name";
				value = properties.getProperty (key);
				
				if (value != null && value.length () > 0)
				{
					System.out.println ("setting stereo sample " + i + " name to " + value);
					file.getStereoSample (i).setName (value);
				}
			}
		}
		
		// PATTERNS
		
		for (int i = 0; i < 256; i++)
		{
			ESXPattern	pattern = file.getPattern (i);

			boolean	found = false;
			
			String	key = "pattern." + i + ".name";
			String	value = properties.getProperty (key);
			
			if (value != null && value.length () > 0)
			{
				System.out.println ("setting pattern " + i + " name to " + value);
				pattern.setName (value);
				found = true;
			}
			
			for (int j = 0; j < 9; j++)
			{
				key = "pattern." + i + ".drumpart." + j + ".samplenumber";
				value = properties.getProperty (key);
			
				if (value != null && value.length () > 0)
				{
					found = true;
					
					ESXDrumPart	part = pattern.getDrumPart (j);
					int	sampleNumber = Integer.parseInt (value);
					
					System.out.println ("setting pattern " + i + " part " + j + " to sample " + 
						file.getSample (sampleNumber).getName ());

					part.setSampleNumber (sampleNumber);
					
					// see if there's a pattern... :-)
					
					key = "pattern." + i + ".drumpart." + j + ".pattern";
					value = properties.getProperty (key);
			
					if (value != null && value.length () > 0)
					{
						for (int k = 0; k < value.length () && k < 64; k++)
						{
							char	ch = value.charAt (k);
							
							if (ch == '.')
							{
								part.setSequenceStep (k, false);
							}
							else
							{
								part.setSequenceStep (k, true);
							}
						}
					}
				}
			}
			
			if (! found)
			{
				break;
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
