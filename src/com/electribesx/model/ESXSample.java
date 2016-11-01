
package com.electribesx.model;

import java.io.File;
import java.io.FileInputStream;

public class ESXSample
{
/*
	public
	ESXSample (File inFile)
	throws Exception
	{
		byte[]	buffer = new byte [1024];
		FileInputStream	fis = new FileInputStream (inFile);
		
		// read the RIFF header
		String	riff = read4ByteLiteral (fis);
		
		if (! riff.equals ("RIFF"))
		{
			throw new Exception ("bad file - no RIFF literal");
		}
		
		int	chunkSize = read4ByteInteger (fis);
		
		String	format = read4ByteLiteral (fis);
				
		if (! format.equals ("WAVE"))
		{
			throw new Exception ("bad file - format not WAVE");
		}

		int	blockAlign = 0;
		
		while (fis.available () > 0)
		{
			String	subChunkType = read4ByteLiteral (fis);
			System.err.println ("subchunk ID = " + subChunkType);

			int	subChunkSize = read4ByteInteger (fis);
			System.out.println ("subchunk" + i + " size = " + subChunkSize);

			if (subChunkType.equalsIgnoreCase ("fmt "))
			{
				int	audioFormat = read2ByteInteger (fis);
				
				if (! audioFormat != 1)
				{
					throw new Exception ("bad file - audio format not PCM");
				}
					
				this.numChannels = read2ByteInteger (fis);
				System.out.println ("num channels = " + this.numChannels);
				
				this.sampleRate = read4ByteInteger (fis);
				System.out.println ("sample rate = " + this.sampleRate);

				this.byteRate = read4ByteInteger (fis);
				System.out.println ("byte rate = " + this.sampleRate);

				blockAlign = read2ByteInteger (fis);
				System.out.println ("block align = " + blockAlign);

				this.bitsPerSample = read2ByteInteger (fis);
				System.out.println ("bits per sample = " + this.bitsPerSample);
				
				if (subChunkSize > 16)
				{
					fis.skip (subChunkSize - 16);
				}
			}
			else
			if (subChunkType.equalsIgnoreCase ("data"))
			{
				int	numFrames = subChunkSize / blockAlign;
				
				// convert to 16-bit big-endian for the ESX
				
				this.data = new byte [subChunkSize];
				
				if (this.bitsPerSample == 8)
				{
					for (int j = 0; j < numFrames; j++)
					{
						this.data [j] = fis.read ();
					}
				}
				else
				if (this.bitsPerSample == 16)
				{
					for (int j = 0; j < numFrames; j++)
					{
						byte	lsb = fis.read ();
						byte	msb = fis.read ();
						
						this.data [j * 2] = msb;
						this.data [(j * 2) + 1] = lsb;
					}
				}
				else
				if (this.bitsPerSample == 24)
				{
					for (int j = 0; j < numFrames; j++)
					{
						// the ESX doesn't support 24 bits, so we truncate to 16 bit
						this.data [j] = read3ByteInteger (fis) >> 8;
					}
				}
				else
				{
					throw new Exception ("bad file - unrecognised bps: " + this.bitsPerSample);
				}
			}
			else
			{
				// just skip if we don't recognise it
				fis.skip (subChunkSize);
			}
		}
	}
				
	private int
	read2ByteInteger (InputStream inStream)
	throws Exception
	{
		int	value = inStream.read ();
		value |= inStream.read () << 8;
		
		return value;
	}

	private int
	read3ByteInteger (InputStream inStream)
	throws Exception
	{
		int	value = inStream.read ();
		value |= inStream.read () << 8;
		value |= inStream.read () << 16;
		
		return value;
	}

	private int
	read4ByteInteger (InputStream inStream)
	throws Exception
	{
		int	value = inStream.read ();
		value |= inStream.read () << 8;
		value |= inStream.read () << 16;
		value |= inStream.read () << 24;
		
		return value;
	}

	private String
	read4ByteLiteral (InputStream inStream)
	throws Exception
	{
		byte[]	buffer = new byte [4];
		int	cc = inStream.read (buffer);
		if (cc < 1)
		{
			throw new EOFException ();
		}
		return new String (buffer, 0, 4);
	}

	// PRIVATE DATA
	
	private int
	bitsPerSample = 0;
	
	private int
	sampleRate = 0;
	
	private int
	numChannels = 0;

	private byte[]
	data = null;
	
*/
}
