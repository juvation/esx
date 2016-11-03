package com.electribesx.tool;

import java.io.*;

public class WavPrint
{
	public static void
	main (String[] inArgs)
		throws Exception
	{
		if (inArgs.length == 0)
		{
			System.err.println ("usage: java WavPrint wavfile");
			System.exit (1);
		}
		
		FileInputStream	fis = new FileInputStream (inArgs [0]);
		byte[]	buffer = new byte [1024];
		
		int	blockAlign = 0;
		int	bitsPerSample = 0;
		int	numChannels = 0;
		
		// read the RIFF header

		System.out.println ("RIFF literal = " + read4ByteLiteral (fis));
		System.out.println ("chunk size = " + read4ByteInteger (fis));
		System.out.println ("format = " + read4ByteLiteral (fis));

		for (int i = 1; fis.available () > 0; i++)
		{
			byte[]	vsFrame = new byte [3];
			
			String	subChunkType = read4ByteLiteral (fis);
			System.out.println ("subchunk" + i + " ID = " + subChunkType);
			
			int	subChunkSize = read4ByteInteger (fis);
			System.out.println ("subchunk" + i + " size = " + subChunkSize);

			if (subChunkType.equalsIgnoreCase ("fmt "))
			{
				System.out.println ("audio format = " + read2ByteInteger (fis));
				numChannels = read2ByteInteger (fis);
				System.out.println ("num channels = " + numChannels);
				System.out.println ("sample rate = " + read4ByteInteger (fis));
				System.out.println ("byte rate = " + read4ByteInteger (fis));
				blockAlign = read2ByteInteger (fis);
				System.out.println ("block align = " + blockAlign);
				bitsPerSample = read2ByteInteger (fis);
				System.out.println ("bits per sample = " + bitsPerSample);
				
				if (subChunkSize > 16)
				{
					fis.skip (subChunkSize - 16);
				}
			}
			else
			if (subChunkType.equalsIgnoreCase ("data"))
			{
				int	wavFrames = subChunkSize / blockAlign;
				System.out.println ("wavFrames = " + wavFrames);
				fis.skip (subChunkSize);
			}
			else
			if (subChunkType.equalsIgnoreCase ("smpl"))
			{
				// aha! some clue as to what's in the sample -- good-o!
				System.out.println ("mfr ID = " + read4ByteInteger (fis));
				System.out.println ("product ID = " + read4ByteInteger (fis));
				System.out.println ("sample period = " + read4ByteInteger (fis));
				System.out.println ("MIDI unity note = " + read4ByteInteger (fis));
				System.out.println ("MIDI pitch fraction note = " + read4ByteInteger (fis));
				System.out.println ("SMPTE format = " + read4ByteInteger (fis));
				System.out.println ("SMPTE offset = " + read4ByteInteger (fis));
				System.out.println ("num sample loops = " + read4ByteInteger (fis));
				System.out.println ("sampler data = " + read4ByteInteger (fis));

				// skip the rest of the chunk
				fis.skip (subChunkSize - (9 * 4));
			}
			else
			{
				// just skip if we don't recognise it
				fis.skip (subChunkSize);
			}
		}
	}

	public static int
	read2ByteInteger (InputStream inStream)
		throws IOException
	{
		int	value = inStream.read ();
		value |= inStream.read () << 8;
		
		return value;
	}

	public static int
	read3ByteInteger (InputStream inStream)
		throws IOException
	{
		int	value = inStream.read ();
		value |= inStream.read () << 8;
		value |= inStream.read () << 16;
		
		return value;
	}

	public static int
	read4ByteInteger (InputStream inStream)
		throws IOException
	{
		int	value = inStream.read ();
		value |= inStream.read () << 8;
		value |= inStream.read () << 16;
		value |= inStream.read () << 24;
		
		return value;
	}

	public static String
	read4ByteLiteral (InputStream inStream)
		throws IOException
	{
		byte[]	buffer = new byte [4];
		int	cc = inStream.read (buffer);
		if (cc < 1)
		{
			throw new EOFException ();
		}
		return new String (buffer, 0, 4);
	}
}
