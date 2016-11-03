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
		
		byte[]	buffer = file.getBuffer ();
		
		// verify that the mono samples start where they should
		for (int i = 0; i < 256; i++)
		{
			ESXMonoSample	sample = file.getMonoSample (i);
			
			int	sampleDataStartOffset = sample.getDataStartOffset ();
			
			if (sampleDataStartOffset >= 0)
			{
				System.out.println ("checking mono sample " + i);

				int	offset = 0x250000 + sampleDataStartOffset;
			
				int	magic = 0;
			
				for (int j = 0; j < 4; j++)
				{
					magic <<= 8;
					magic |= (buffer [offset + j] & 0xff);
				}
			
				if (magic != 0x80007fff)
				{
					System.out.println ("magic number mismatch for mono sample " + i);
					
					// print some context
					for (int j = -4; j < 4; j++)
					{
						System.out.println ("byte at offset " + j + " = " + Integer.toHexString ((buffer [offset + j]) & 0xff));
					}
					break;
				}
			}
			else
			{
				// System.out.println ("no sample in slot " + i);
			}
		}

		// verify that the stereo samples start where they should
		for (int i = 0; i < 128; i++)
		{
			ESXStereoSample	sample = file.getStereoSample (i);
			
			int	sampleData1StartOffset = sample.getData1StartOffset ();
			
			if (sampleData1StartOffset >= 0)
			{
				System.out.println ("checking stereo sample " + i);

				int	offset = 0x250000 + sampleData1StartOffset;
			
				int	magic = 0;
			
				for (int j = 0; j < 4; j++)
				{
					magic <<= 8;
					magic |= (buffer [offset + j] & 0xff);
				}
			
				if (magic != 0x80007fff)
				{
					System.out.println ("magic number mismatch for left stereo sample " + i);
					
					// print some context
					for (int j = -4; j < 4; j++)
					{
						System.out.println ("byte at offset " + j + " = " + Integer.toHexString ((buffer [offset + j]) & 0xff));
					}
					break;
				}
	
				int	sampleData2StartOffset = sample.getData2StartOffset ();
			
				if (sampleData2StartOffset >= 0)
				{
					offset = 0x250000 + sampleData2StartOffset;
			
					magic = 0;
			
					for (int j = 0; j < 4; j++)
					{
						magic <<= 8;
						magic |= (buffer [offset + j] & 0xff);
					}
			
					if (magic != 0x80007fff)
					{
						System.out.println ("magic number mismatch for right stereo sample " + i);
					
						// print some context
						for (int j = -4; j < 4; j++)
						{
							System.out.println ("byte at offset " + j + " = " + Integer.toHexString ((buffer [offset + j]) & 0xff));
						}
						break;
					}
				}
		}
			else
			{
				// System.out.println ("no sample in slot " + i);
			}
		}
	}
	
	// PRIVATE DATA

	// CONSTANTS
	
		
}
