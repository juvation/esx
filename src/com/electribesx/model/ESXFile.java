// ESXFile.java

package com.electribesx.model;

import java.io.DataOutputStream;
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
		
		this.patterns [0].dump (0);
		
		for (int i = 0; i < 256; i++)
		{
			this.monoSamples [i].dump (i);
		}

		for (int i = 0; i < 128; i++)
		{
			this.stereoSamples [i].dump (i);
		}
	}
	
	// largely only for the verifier
	public byte[]
	getBuffer ()
	{
		return this.buffer;
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
	
	public ESXSample
	getSample (int inIndex)
	{
		ESXSample	sample = null;
		
		if (inIndex < 256)
		{
			sample = getMonoSample (inIndex);
		}
		else
		{
			sample = getStereoSample (inIndex - 256);
		}
		
		return sample;
	}
	
	public ESXStereoSample
	getStereoSample (int inIndex)
	{
		return this.stereoSamples [inIndex];
	}
	
	public void
	copyPattern (ESXFile inFile, int inPatternIndex, int outPatternIndex)
	{
		int	inOffset = PATTERNS_OFFSET + (ESXPattern.BUFFER_SIZE * inPatternIndex);
		int	outOffset = PATTERNS_OFFSET + (ESXPattern.BUFFER_SIZE * outPatternIndex);
		
		copyIn (inFile.buffer, inOffset, outOffset, ESXPattern.BUFFER_SIZE);
	}
	
	// note this will ONLY work on a freshly loaded ESXFile
	// as it will have everything properly serialised in the ESXFile buffer
	public void
	verify ()
	throws Exception
	{
		System.out.println ("verifying");
		
		// verify that the mono samples start where they should
		for (int i = 0; i < 256; i++)
		{
			ESXMonoSample	sample = getMonoSample (i);
			
			int	sampleDataStartOffset = sample.getDataStartOffset ();
			
			if (sampleDataStartOffset >= 0)
			{
				System.out.println ("checking mono sample " + i + " " + sample.getName ());

				int	offset = 0x250000 + sampleDataStartOffset;
			
				int	magic = 0;
			
				for (int j = 0; j < 4; j++)
				{
					magic <<= 8;
					magic |= (this.buffer [offset + j] & 0xff);
				}
			
				if (magic != 0x80007fff)
				{
					// print some context
					for (int j = -4; j < 4; j++)
					{
						System.out.println ("byte at offset " + j + " = " + Integer.toHexString ((this.buffer [offset + j]) & 0xff));
					}

					throw new Exception ("magic number mismatch for mono sample " + i);
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
			ESXStereoSample	sample = getStereoSample (i);
			
			int	sampleData1StartOffset = sample.getData1StartOffset ();
			
			if (sampleData1StartOffset >= 0)
			{
				System.out.println ("checking stereo sample " + i + " " + sample.getName ());

				int	offset = 0x250000 + sampleData1StartOffset;
			
				int	magic = 0;
			
				for (int j = 0; j < 4; j++)
				{
					magic <<= 8;
					magic |= (buffer [offset + j] & 0xff);
				}
			
				if (magic != 0x80007fff)
				{
					// print some context
					for (int j = -4; j < 4; j++)
					{
						System.out.println ("byte at offset " + j + " = " + Integer.toHexString ((buffer [offset + j]) & 0xff));
					}

					throw new Exception ("magic number mismatch for left stereo sample " + i);
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
						// print some context
						for (int j = -4; j < 4; j++)
						{
							System.out.println ("byte at offset " + j + " = " + Integer.toHexString ((buffer [offset + j]) & 0xff));
						}

						throw new Exception ("magic number mismatch for right stereo sample " + i);
					}
				}
			}
			else
			{
				// System.out.println ("no sample in slot " + i);
			}
		}
		
		// and now verify that we have the correct magic shit at the end

		// note that the current sample offset stored in the file
		// is relative to the start of the sample area 0x250000
		int	sampleOffset = 0x250000 + getBigEndian32 (CURRENT_SAMPLE_OFFSET_OFFSET);

		int	magic = 0;
		
		System.out.println ("checking end block magic 1");

		magic = getBigEndian32 (sampleOffset);
		
		if (magic != 0x80007fff)
		{
			System.out.println ("magic number 1 mismatch in end block");
			System.out.println (Integer.toHexString (magic) + " != 0x80007fff");
			
			throw new Exception ("magic number 1 mismatch in end block");
		}
		
		System.out.println ("checking end block sample offset");

		int	verifySampleOffset = 0x250000 + getBigEndian32 (sampleOffset + 4);

		if (verifySampleOffset != sampleOffset)
		{
			System.out.println ("sample offset mismatch in end block");
			System.out.println (sampleOffset + " != " + verifySampleOffset);
			
			throw new Exception ("sample offset mismatch in end block");
		}

		System.out.println ("checking end block magic 2");

		magic = getBigEndian32 (sampleOffset + 8);
		
		if (magic != 0x017ffffe)
		{
			System.out.println ("magic number 2 mismatch in end block");
			System.out.println (Integer.toHexString (magic) + " != 0x017ffffe");
			
			throw new Exception ("magic number 2 mismatch in end block");
		}

		System.out.println ("checking end block magic 3");

		magic = getBigEndian32 (sampleOffset + 12);
		
		if (magic != 0x00ffffff)
		{
			System.out.println ("magic number 3 mismatch in end block");
			System.out.println (Integer.toHexString (magic) + " != 0x00ffffff");
			
			throw new Exception ("magic number 3 mismatch in end block");
		}
		
		System.out.println ("verify OK");
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
				// yes this is odd but this is how the machine sets it
				sample.setDataStartOffset (0xFFFFFFFF);
				sample.setDataEndOffset (0);
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
				// yes this is odd but this is how the machine sets it
				sample.setData1StartOffset (0xFFFFFFFF);
				sample.setData1EndOffset (0);
				sample.setData2StartOffset (0xFFFFFFFF);
				sample.setData2EndOffset (0);
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
					// System.out.println ("writing mono sample " + i + " of size " + sample.getSampleSize ());
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
					// System.out.println ("writing stereo sample " + i + " of size " + sample.getSampleSize ());
					sample.writeSampleData (fos, i + 256);
				}
			}
			
			// and now write the secret shit at the end of the file!
			// without which, the ESX will next boot in fucking recovery mode
			// and wipe out all your good work
			DataOutputStream	dis = new DataOutputStream (fos);
			
			dis.writeInt (0x80007fff);
			dis.writeInt (currentSampleOffset);
			dis.writeInt (0x017ffffe);
			dis.writeInt (0x00ffffff);

			dis.flush ();
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

