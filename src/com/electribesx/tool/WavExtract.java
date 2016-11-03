// WavExtract.java

package com.electribesx.tool;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.electribesx.model.ESXFile;
import com.electribesx.model.ESXMonoSample;

// there's no stereo samples in the factory set
// so we don't bother extracting those!

public class WavExtract
{
	public static void
	main (String[] inArgs)
		throws Exception
	{
		if (inArgs.length < 2)
		{
			System.err.println ("usage: java WavExtract esxfile samplenumber (0-255)");
			System.exit (1);
		}
		
		ESXFile	file = ESXFile.fromFile (new File (inArgs [0]));
		int	sampleNumber = Integer.parseInt (inArgs [1]);
		
		if (sampleNumber < 0 || sampleNumber > 255)
		{
			throw new Exception ("sample number must be in range 0-383");
		}
		
		ESXMonoSample	sample = file.getMonoSample (sampleNumber);

		ByteArrayOutputStream	bos = new ByteArrayOutputStream ();
		write4ByteLiteral (bos, "RIFF");
		
		// we don't know the chunk size yet, but leave space for it
		write4ByteInteger (bos, 0);
		write4ByteLiteral (bos, "WAVE");

		// 'fmt ' CHUNK
		
		write4ByteLiteral (bos, "fmt ");

		// chunk size is always 16 for us
		write4ByteInteger (bos, 16);

		// audio format 1, PCM SIGNED
		write2ByteInteger (bos, 1);
		
		// num channels = 1
		write2ByteInteger (bos, 1);
		
		// sample rate = ?
		write4ByteInteger (bos, sample.getSampleRate ());
		
		// byte rate = sample rate * bytes per sample * channels
		write4ByteInteger (bos, sample.getSampleRate () * 2);
		
		// block align = 2 bytes
		write2ByteInteger (bos, 2);

		// bits per sample is always 16 for the ESX
		write2ByteInteger (bos, 16);
		
		// 'data' chunk
		
		write4ByteLiteral (bos, "data");
		
		// sample reports sample size in total bytes NOT frames
		write4ByteInteger (bos, sample.getSampleSize ());
		
		// write dat data
		int	numFrames = sample.getSampleSize () / 2;
		
		for (int i = 0; i < numFrames; i++)
		{
			int	value = sample.getSample (i);
			write2ByteInteger (bos, value);
		}
		
		bos.flush ();
		bos.close ();
		
		byte[]	wav = bos.toByteArray ();
		
		// now we can calculate the RIFF chunk size
		int	chunkSize = wav.length - 8;
		wav [4] = (byte) ((chunkSize >> 0) & 0xff);
		wav [5] = (byte) ((chunkSize >> 8) & 0xff);
		wav [6] = (byte) ((chunkSize >> 16) & 0xff);
		wav [7] = (byte) ((chunkSize >> 24) & 0xff);
		
		ByteArrayInputStream	bis = new ByteArrayInputStream (wav);
		FileOutputStream	fos = new FileOutputStream (sample.getName ().trim () + ".wav");
		
		int	cc = 0;
		byte[]	buffer = new byte [1024];
		
		do
		{
			cc = bis.read (buffer);
			
			if (cc > 0)
			{
				fos.write (buffer, 0, cc);
			}
		}
		while (cc > 0);
		
		fos.close ();
	}

	public static void
	write2ByteInteger (OutputStream outStream, int inValue)
		throws IOException
	{
		outStream.write (inValue & 0xff);
		outStream.write ((inValue >> 8) & 0xff);
	}

	public static void
	write4ByteInteger (OutputStream outStream, int inValue)
		throws IOException
	{
		outStream.write (inValue & 0xff);
		outStream.write ((inValue >> 8) & 0xff);
		outStream.write ((inValue >> 16) & 0xff);
		outStream.write ((inValue >> 24) & 0xff);
	}

	public static void
	write4ByteLiteral (OutputStream outStream, String inValue)
		throws IOException
	{
		byte[]	bytes = inValue.getBytes ();
		
		for (int i = 0; i < 4; i++)
		{
			outStream.write (bytes [i]);
		}
	}

}
