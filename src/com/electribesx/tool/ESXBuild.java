package com.electribesx.tool;

import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import com.electribesx.model.ESXDrumPart;
import com.electribesx.model.ESXFile;
import com.electribesx.model.ESXKeyboardPart;
import com.electribesx.model.ESXPattern;

// POWER TOOL for reading and generating ESX files haha

public class ESXBuild
{
	public static void
	main (String[] inArgs)
	throws Exception
	{
		if (inArgs.length < 3)
		{
			System.err.println ("usage: ESX file.esx file.properties outputfile.esx");
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
				file.getMonoSample (i).initFromFile2 (new File (value));

				key = "monosample." + i + ".name";
				value = properties.getProperty (key);
				
				if (value != null && value.length () > 0)
				{
					System.out.println ("setting mono sample " + i + " name to " + value);
					file.getMonoSample (i).setName (value);
				}

				// this is handy because it's lost on wav export/import
				key = "monosample." + i + ".tune";
				value = properties.getProperty (key);
				
				if (value != null && value.length () > 0)
				{
					float	tune = Float.parseFloat (value);
					System.out.println ("setting mono sample " + i + " tune to " + tune);
					file.getMonoSample (i).setSampleTune (tune);
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
				file.getStereoSample (i).initFromFile2 (new File (value));

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
		
		// check first that the user hasn't specified pattern 0
		if (properties.getProperty ("pattern.0.name") != null)
		{
			throw new Exception ("pattern numbering starts from 1");
		}
		
		for (int i = 1; i < 257; i++)
		{
			// note patterns are numbered 0-255 internally
			// but numbered 1-256 on the electribe, and therefore also in the driver file
			ESXPattern	pattern = file.getPattern (i - 1);
			
			String	key = "pattern." + i + ".name";
			String	value = properties.getProperty (key);
			
			if (value == null || value.length () == 0)
			{
				continue;
			}
			
			System.out.println ("setting pattern " + i + " name to " + value);
			pattern.setName (value);

			// tempo
			
			key = "pattern." + i + ".tempo";
			value = properties.getProperty (key);
			
			if (value != null && value.length () > 0)
			{
				System.out.println ("setting pattern " + i + " tempo to " + value);
				
				pattern.setTempo (Float.parseFloat (value));
			}

			// length in bars (kinda)
			
			key = "pattern." + i + ".length";
			value = properties.getProperty (key);
			
			if (value != null && value.length () > 0)
			{
				System.out.println ("setting pattern " + i + " length to " + value);

				int	length = Integer.parseInt (value);
				
				if (length >= 1 && length <= 8)
				{
					pattern.setLength (length - 1);
				}
				else
				{
					throw new Exception ("length must be in range 1-8");
				}
			}
			
			// beat type 
			
			key = "pattern." + i + ".beat";
			value = properties.getProperty (key);
			
			if (value != null && value.length () > 0)
			{
				System.out.println ("setting pattern " + i + " beat to " + value);

				// this is stupid
				List<String>	beatNameList = Arrays.asList (PATTERN_BEAT_NAMES);
				
				int	index = beatNameList.indexOf (value);
				
				if (index >= 0)
				{
					pattern.setBeat (index);
				}
				else
				{
					throw new Exception ("beat must be one of 16/32/8T/16T");
				}
			}

			// last step in each bar (kinda)
			
			key = "pattern." + i + ".laststep";
			value = properties.getProperty (key);
			
			if (value != null && value.length () > 0)
			{
				System.out.println ("setting pattern " + i + " last step to " + value);
				
				int	lastStep = Integer.parseInt (value);
				
				if (lastStep >= 1 && lastStep <= 16)
				{
					pattern.setLastStep (lastStep - 1);
				}
				else
				{
					throw new Exception ("last step must be in range 1-16");
				}
			}

			// DRUM PARTS
			
			// check first that the user hasn't specified part 0
			if (properties.getProperty ("pattern." + i + ".drumpart.0.sample") != null)
			{
				throw new Exception ("drum part numbering runs 1-9");
			}

			for (int j = 0; j < 9; j++)
			{
				// note drum parts are numbered 0-8 internally
				// but in the driver file, numbered as they are displayed on the Electribe itself
				ESXDrumPart	part = pattern.getDrumPart (j);

				String	partName = DRUM_PART_NAMES [j];
				
				key = "pattern." + i + ".drumpart." + partName + ".sample";
				value = properties.getProperty (key);
			
				if (value != null && value.length () > 0)
				{
					int	sampleNumber = Integer.parseInt (value);
					
					System.out.println ("setting pattern " + i + " drum part " + partName + " to sample " + 
						file.getSample (sampleNumber).getName ());

					part.setSampleNumber (sampleNumber);
					
					// see if there's a pattern... :-)
				}
				
				key = "pattern." + i + ".drumpart." + j + ".level";
				value = properties.getProperty (key);
			
				if (value != null && value.length () > 0)
				{
					int	level = Integer.parseInt (value);
					
					if (level < 0 || level > 127)
					{
						throw new Exception ("level must be 0-127");
					}
					
					System.out.println ("setting pattern " + i + " part " + j + " to level " + level);

					part.setLevel ((byte) level);
				}
				
				key = "pattern." + i + ".drumpart." + partName + ".pattern";
				value = properties.getProperty (key);
		
				if (value != null && value.length () > 0)
				{
					System.out.println ("setting pattern " + i + " drum part " + partName + " to pattern " + value);

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
			
			// KEYBOARD PARTS
			
			// check first that the user hasn't specified part 0
			if (properties.getProperty ("pattern." + i + ".keyboard.0.sample") != null)
			{
				throw new Exception ("keyboard part numbering runs 1-2");
			}

			for (int j = 1; j < 3; j++)
			{
				// note keyboard parts are numbered 0-1 internally
				// but numbered 1-2 on the electribe, and therefore also in the driver file
				ESXKeyboardPart	part = pattern.getKeyboardPart (j - 1);

				key = "pattern." + i + ".keyboardpart." + j + ".sample";
				value = properties.getProperty (key);
			
				if (value != null && value.length () > 0)
				{
					int	sampleNumber = Integer.parseInt (value);
					
					System.out.println ("setting pattern " + i + " part " + j + " to sample " + 
						file.getSample (sampleNumber).getName ());

					part.setSampleNumber (sampleNumber);
				}

				key = "pattern." + i + ".keyboardpart." + j + ".level";
				value = properties.getProperty (key);
			
				if (value != null && value.length () > 0)
				{
					int	level = Integer.parseInt (value);
					
					if (level < 0 || level > 127)
					{
						throw new Exception ("level must be 0-127");
					}
					
					System.out.println ("setting pattern " + i + " part " + j + " to level " + level);

					part.setLevel ((byte) level);
				}
				
				// see if there's a pattern... :-)
				
				key = "pattern." + i + ".keyboardpart." + j + ".sequence";
				value = properties.getProperty (key);
		
				if (value != null && value.length () > 0)
				{
					System.out.println ("setting pattern " + i + " keyboard part " + j + " to notes " + value);

					String[]	noteStrings = value.split (" ");
					
					for (int k = 0; k < noteStrings.length; k++)
					{
						String	noteString = noteStrings [k];
						
						if (noteString.equals ("."))
						{
							// ASSUME gate zero = rest
							part.setSequenceGate (k, (byte) 0);
						}
						else
						{
							// ASSUME gate 2 = 0.75 somehow
							part.setSequenceGate (k, (byte) 2);
							part.setSequenceNote (k, noteStrings [k]);
						}
					}
				}
			}
		}
		
		File	newFile = new File (inArgs [2]);
		file.write (newFile);
		
		// verify only works on a freshly loaded file
		file = ESXFile.fromFile (newFile);
		file.verify ();
	}
	
	// PRIVATE STATIC CONSTANTS
	
	private static final String[]
	PATTERN_BEAT_NAMES =
	{
		"16",
		"32",
		"8T",
		"16T"
	};

	private static final String[]
	DRUM_PART_NAMES =
	{
		"1",
		"2",
		"3",
		"4",
		"5",
		"6A",
		"6B",
		"7A",
		"7B"
	};
	
	// PRIVATE DATA

	// CONSTANTS
	
		
}
