// ESXFile.java

package com.electribesx.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class ESXFile
extends BufferManager
{
	// PUBLIC STATIC METHODS
	
	public static ESXFile
	fromFile (File inFile)
	throws Exception
	{
		byte[]	data = new byte [(int) inFile.length ()];
		
		FileInputStream	fis = new FileInputStream (inFile);
		
		try
		{
			fis.read (data);

			return new ESXFile (data);
		}
		finally
		{
			fis.close ();
		}
	}
			
	// CONSTRUCTORS
	
	public
	ESXFile (byte[] inBuffer)
	throws Exception
	{
		super (inBuffer, 0);
		
		checkBuffer ();
		
		this.globalParameters = new ESXGlobalParameters (this.buffer, 0x20);
		
		this.patterns = new ESXPattern [256];
		
		for (int i = 0; i < 256; i++)
		{
			this.patterns [i] = new ESXPattern (this.buffer, PATTERNS_OFFSET + (i * ESXPattern.BUFFER_SIZE));
		}
		
		this.monoSamples = new ESXMonoSample [256];

		for (int i = 0; i < 256; i++)
		{
			this.monoSamples [i] = new ESXMonoSample
				(this.buffer, MONO_SAMPLE_HEADERS_OFFSET + (i * ESXMonoSample.BUFFER_SIZE));
		}

		this.stereoSamples = new ESXStereoSample [256];

		for (int i = 0; i < 128; i++)
		{
			this.stereoSamples [i] = new ESXStereoSample
				(this.buffer, STEREO_SAMPLE_HEADERS_OFFSET + (i * ESXStereoSample.BUFFER_SIZE));
		}
	}
	
	// PUBLIC METHODS
	
	public void
	dump ()
	{
		System.out.println ("number of mono samples = " + getBigEndian32 (NUM_MONO_SAMPLES_OFFSET));
		System.out.println ("number of stereo samples = " + getBigEndian32 (NUM_STEREO_SAMPLES_OFFSET));
		System.out.println ("current sample offset = " + getBigEndian32 (CURRENT_SAMPLE_OFFSET_OFFSET));
		
		// this.patterns [0].dump ();
		
		System.out.println ("mono samples...");
		
		for (int i = 0; i < 256; i++)
		{
			this.monoSamples [i].dump (i);
		}

		System.out.println ("stereo samples...");
		
		for (int i = 0; i < 128; i++)
		{
			this.stereoSamples [i].dump (i);
		}
	}
	
	public ESXMonoSample
	getMonoSample (int inIndex)
	{
		return this.monoSamples [inIndex];
	}

	public ESXPattern
	getPattern (int inIndex)
	{
		return this.patterns [inIndex];
	}
	
	public ESXStereoSample
	getStereoSample (int inIndex)
	{
		return this.stereoSamples [inIndex];
	}
	
	public void
	write (File outFile)
	throws Exception
	{
		// determine sample offsets first
		// as the current sample offset and counts need to go in OUR binary header
		
		int	currentSampleOffset = 0;
		int	numMonoSamples = 0;
		int	numStereoSamples = 0;
		
		// calculate mono sample offsets
		for (int i = 0; i < 256; i++)
		{
			ESXMonoSample	sample = this.monoSamples [i];
			
			// if there *is* a sample here
			// and for some reason empty sample slots are size 1!
			if (sample.getSampleSize () > 1)
			{
				numMonoSamples++;
				
				// note these MUST set the binary data in our buffer
				// as we will write the whole lot at once
				sample.setDataStartOffset (currentSampleOffset);

				// the 18 byte extra is for the magic number header and other stuffs
				// plus for mono samples, the loop start sample
				currentSampleOffset += sample.getSampleSize () + 16;

				sample.setDataEndOffset (currentSampleOffset);
			
				// apparently we update the offset by blockAlign/loopStartSample
				// this confuses me
				
				int	numSampleFrames = sample.getSampleSize () / 2;
				
				if ((numSampleFrames % 2) == 0)
				{
					currentSampleOffset += 4;
				}
				else
				{
					currentSampleOffset += 2;
				}
			}
			else
			{
				sample.setDataStartOffset (0xFFFFFFFF);
				sample.setDataEndOffset (0xFFFFFFFF);
			}
		}

		// note that the header is 18 bytes for mono due to the loop start
		// and 16 bytes for stereo due to no loop start
		
		// calculate stereo sample offsets
		for (int i = 0; i < 128; i++)
		{
			ESXStereoSample	sample = this.stereoSamples [i];
			
			// if there *is* a sample here
			// and for some reason empty sample slots are size 1!
			if (sample.getSampleSize () > 1)
			{
				numStereoSamples++;
				
				int	numSampleFrames = sample.getSampleSize () / 2;
				
				// LEFT CHANNEL
				
				// note these MUST set the binary data in our buffer
				// as we will write the whole lot at once
				sample.setData1StartOffset (currentSampleOffset);

				// the 16 byte extra is for the magic number header and other stuffs
				currentSampleOffset += sample.getSampleSize () + 16;
				sample.setData1EndOffset (currentSampleOffset);

				// apparently we update the offset by blockAlign/loopStartSample
				if ((numSampleFrames % 2) == 0)
				{
					currentSampleOffset += 4;
				}
				else
				{
					currentSampleOffset += 2;
				}

				// RIGHT CHANNEL
							
				// note these MUST set the binary data in our buffer
				// as we will write the whole lot at once
				sample.setData2StartOffset (currentSampleOffset);

				// the 16 byte extra is for the magic number header and other stuffs
				currentSampleOffset += sample.getSampleSize () + 16;
				sample.setData2EndOffset (currentSampleOffset);

				// apparently we update the offset by blockAlign/loopStartSample
				if ((numSampleFrames % 2) == 0)
				{
					currentSampleOffset += 4;
				}
				else
				{
					currentSampleOffset += 2;
				}
			}
			else
			{
				sample.setData1StartOffset (0xFFFFFFFF);
				sample.setData1EndOffset (0xFFFFFFFF);
				sample.setData2StartOffset (0xFFFFFFFF);
				sample.setData2EndOffset (0xFFFFFFFF);
			}
		}

		setBigEndian32 (CURRENT_SAMPLE_OFFSET_OFFSET, currentSampleOffset);
		setBigEndian32 (NUM_MONO_SAMPLES_OFFSET, numMonoSamples);
		setBigEndian32 (NUM_STEREO_SAMPLES_OFFSET, numStereoSamples);
		
		FileOutputStream	fos = new FileOutputStream (outFile);
		
		try
		{
			// write everything up to the samples
			fos.write (this.buffer, 0, 0x00250000);
			
			// now write the mono samples
			for (int i = 0; i < 256; i++)
			{
				ESXMonoSample	sample = this.monoSamples [i];
				
				// if there *is* a sample here
				// and for some reason empty sample slots are size 1!
				if (sample.getSampleSize () > 1)
				{
					System.out.println ("writing mono sample " + i + " of size " + sample.getSampleSize ());
					sample.writeSampleData (fos, i);
				}
			}
			
			// now write the stereo samples
			for (int i = 0; i < 128; i++)
			{
				ESXStereoSample	sample = this.stereoSamples [i];

				// if there *is* a sample here
				// and for some reason empty sample slots are size 1!
				if (sample.getSampleSize () > 1)
				{
					System.out.println ("writing stereo sample " + i + " of size " + sample.getSampleSize ());
					sample.writeSampleData (fos, i + 256);
				}
			}
		}
		finally
		{
			fos.close ();
		}
	}
	
	// PRIVATE METHODS
	
	private void
	checkBuffer ()
	throws Exception
	{
		// we should do more, but do we care?
		if (buffer [0] != (byte) 'K')
			throw new Exception ("bad header");
	}
	
	// PRIVATE CONSTANTS
	
	private static final int
	GLOBAL_PARAMETERS_OFFSET = 0x20;
	
	private static final int
	PATTERNS_OFFSET = 0x200;
	
	private static final int
	NUM_MONO_SAMPLES_OFFSET = 0x001b0020;
	
	private static final int
	NUM_STEREO_SAMPLES_OFFSET = 0x001b0024;
	
	private static final int
	CURRENT_SAMPLE_OFFSET_OFFSET = 0x001b0028;
	
	private static final int
	MONO_SAMPLE_HEADERS_OFFSET = 0x001b0100;
	
	private static final int
	STEREO_SAMPLE_HEADERS_OFFSET = 0x001b2900;
	
	private static final int
	SAMPLE_DATA_OFFSET = 0x00250000;
	
	// PRIVATE DATA

	private ESXGlobalParameters
	globalParameters = null;
	
	private ESXPattern
	patterns [] = null;
	
	private ESXMonoSample
	monoSamples [] = null;

	private ESXStereoSample
	stereoSamples [] = null;
	
}

