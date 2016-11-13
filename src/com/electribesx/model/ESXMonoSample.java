// ESXMonoSample.java

package com.electribesx.model;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

public class ESXMonoSample
extends ESXSample
{
	// offset is into the buffer to the sample's metadata
	public
	ESXMonoSample (byte[] inBuffer, int inOffset)	
	{
		super (inBuffer, inOffset);
		
		// System.out.println ("ESXMonoSample (" + inOffset + ") (0x" + Integer.toHexString (inOffset) + ")");
		
		// note these are offsets from the start of the sample area @0x250000
		int	dataStartOffset = getBigEndian32 (DATA_START_OFFSET);
		int	dataEndOffset = getBigEndian32 (DATA_END_OFFSET);
		
		this.data = inBuffer;
		this.offset = 0x00250000 + dataStartOffset;
		this.size = dataEndOffset - dataStartOffset;
	}

	// PUBLIC METHODS
	
	// i don't like the way we cache the file
	public void
	initFromFile (File inFile)
	throws Exception
	{
		// System.out.println ("ESXMonoSample.initFromFile(" + inFile.getName () + ")");
		
		AudioInputStream	sourceStream = AudioSystem.getAudioInputStream (inFile);
		AudioInputStream	esxStream = null;
		
		try
		{
			AudioFormat	sourceFormat = sourceStream.getFormat ();
			
			/*
			System.out.println ("sample rate = " + sourceFormat.getSampleRate ());
			System.out.println ("sample size = " + sourceFormat.getSampleSizeInBits ());
			System.out.println ("sample channels = " + sourceFormat.getChannels ());
			System.out.println ("big endian = " + sourceFormat.isBigEndian ());
			*/
			
			// ESX requires 16-bit PCM signed big-endian
			// note we convert to mono here too
			AudioFormat	esxFormat = new AudioFormat
			(
				AudioFormat.Encoding.PCM_SIGNED,
				sourceFormat.getSampleRate (),
				16,
				1,
				2,
				sourceFormat.getSampleRate (),
				true
			);
			
			esxStream = AudioSystem.getAudioInputStream (esxFormat, sourceStream);
			ByteArrayOutputStream	bos = new ByteArrayOutputStream ();
			
			int	cc = 0;
			byte[]	buffer = new byte [1024];
			
			do
			{
				cc = esxStream.read (buffer);
				
				if (cc > 0)
				{
					bos.write (buffer, 0, cc);
				}
			}
			while (cc > 0);

			this.data = bos.toByteArray ();
			this.offset = 0;
			this.size = this.data.length;
			
			int	numFrames = (this.size / 2);
			
			/*
			System.out.println ("copied buffer of length " + this.size);
			System.out.println ("setting sample rate to " + sourceFormat.getSampleRate ());
			System.out.println ("setting sample end to " + (numFrames - 1));
			*/
			
			// copy some config information across
			// note we use numFrames - 2 to avoid pops at the end of some samples (?)
			setBigEndian32 (SAMPLE_RATE_OFFSET, (int) sourceFormat.getSampleRate ());
			setBigEndian32 (SAMPLE_START_OFFSET, 0);
			setBigEndian32 (SAMPLE_END_OFFSET, numFrames - 2);

			// right now read the fucking file again to determine metadata
			// quite why AudioFormat etc won't give you this...
			WavFile	wavFile = new WavFile (inFile, false);
			
			if (wavFile.getNumLoops () > 0)
			{
				setBigEndian32 (LOOP_OFFSET, wavFile.getLoopStart ());
			}
			else
			{
				setBigEndian32 (LOOP_OFFSET, numFrames - 2);
			}
			
			// and default the name
			String	name = inFile.getName ();
			int	dotIndex = name.indexOf ('.');
			setName (name.substring (0, dotIndex));
		}
		finally
		{
			if (esxStream != null)
			{
				try
				{
					// not sure if this closes the source stream too?
					esxStream.close ();
				}
				catch (Throwable inThrowable)
				{
				}
			}
			
			try
			{
				sourceStream.close ();
			}
			catch (Throwable inThrowable)
			{
				System.out.println ("error closing source stream");
			}
		}
	}
	
	public void
	initFromFile2 (File inFile)
	throws Exception
	{
		WavFile	wavFile = new WavFile (inFile, true);
		
		System.out.println ("sample rate is " + wavFile.getSampleRate ());
		System.out.println ("bits per sample is " + wavFile.getBitsPerSample ());
		System.out.println ("frame count is " + wavFile.getNumFrames ());
		System.out.println ("block align is " + wavFile.getBlockAlign ());
		
		byte[]	sampleData = wavFile.getChunk ("data");
		ByteArrayOutputStream	bos = new ByteArrayOutputStream ();
		
		if (wavFile.getBitsPerSample () == 8)
		{
			for (int i = 0; i < wavFile.getNumFrames (); i++)
			{
				int	offset = i * wavFile.getBlockAlign ();
				int	sample = (int) sampleData [offset];
				
				// 8-bit audio is 0-255, convert to -128-127
				sample -= 128;
				
				// ESX samples are big-endian 16-bit
				bos.write (sample);
				bos.write (0);
			}
		}
		else
		if (wavFile.getBitsPerSample () == 16)
		{
			for (int i = 0; i < wavFile.getNumFrames (); i++)
			{
				int	offset = i * wavFile.getBlockAlign ();
				
				// ESX samples are big-endian 16-bit
				bos.write (sampleData [offset + 1]);
				bos.write (sampleData [offset]);
			}
		}
		else
		if (wavFile.getBitsPerSample () == 24)
		{
			for (int i = 0; i < wavFile.getNumFrames (); i++)
			{
				int	offset = i * wavFile.getBlockAlign ();

				// ESX samples are big-endian 16-bit
				bos.write (sampleData [offset + 2]);
				bos.write (sampleData [offset + 1]);
			}
		}
		else
		{
			throw new Exception (wavFile.getBitsPerSample () + " not supported");
		}
		
		this.data = bos.toByteArray ();
		this.offset = 0;
		this.size = this.data.length;

		System.out.println ("data size is " + this.size);
		
		setBigEndian32 (SAMPLE_RATE_OFFSET, wavFile.getSampleRate ());
		setBigEndian32 (SAMPLE_START_OFFSET, 0);
		
		if (wavFile.getNumLoops () > 0)
		{
			System.out.println ("loops found in wav, setting loop start to " + wavFile.getLoopStart ());
			setBigEndian32 (LOOP_OFFSET, wavFile.getLoopStart ());
			setBigEndian32 (SAMPLE_END_OFFSET, wavFile.getLoopEnd ());
		}
		else
		{
			System.out.println ("no loops found in wav");
			setBigEndian32 (LOOP_OFFSET, wavFile.getNumFrames () - 1);
			setBigEndian32 (SAMPLE_END_OFFSET, wavFile.getNumFrames () - 1);
		}
		
		// and default the name
		String	name = inFile.getName ();
		int	dotIndex = name.indexOf ('.');
		setName (name.substring (0, dotIndex));
	}
	
	// DEBUG PUBLIC METHODS
	
	public void
	dump (int inSampleNumber)
	{
		int	dataStartOffset = getBigEndian32 (DATA_START_OFFSET);
		
		if (dataStartOffset >= 0)
		{
			System.out.println (inSampleNumber + ": '" + getString (NAME_OFFSET, 8) + "'");
			
			System.out.println (" data start offset " + getBigEndian32 (DATA_START_OFFSET));
			System.out.println (" data end offset " + getBigEndian32 (DATA_END_OFFSET));
			System.out.println (" sample start " + getBigEndian32 (SAMPLE_START_OFFSET));
			System.out.println (" sample end " + getBigEndian32 (SAMPLE_END_OFFSET));
			System.out.println (" loop end " + getBigEndian32 (LOOP_OFFSET));
			System.out.println (" sample rate " + getBigEndian32 (SAMPLE_RATE_OFFSET));
			System.out.println (" sample tune " + getBigEndian16 (SAMPLE_TUNE_OFFSET));
			System.out.println (" sample level " + getByte (LEVEL_OFFSET));
		}
	}
	
	public int
	getDataStartOffset ()
	{
		return getBigEndian32 (DATA_START_OFFSET);
	}

	public int
	getLoopEnd ()
	{
		return getBigEndian32 (SAMPLE_END_OFFSET);
	}

	public int
	getLoopStart ()
	{
		return getBigEndian32 (LOOP_OFFSET);
	}
	
	public String
	getName ()
	{
		return getString (NAME_OFFSET, 8);
	}
	
	public int
	getSample (int inSampleNumber)
	{
		byte	msb = this.data [this.offset + (inSampleNumber * 2)];
		byte	lsb = this.data [this.offset + (inSampleNumber * 2) + 1];
		
		return (msb << 8) | (lsb & 0xff);
	}
	
	public int
	getSampleRate ()
	{
		return getBigEndian32 (SAMPLE_RATE_OFFSET);
	}
	
	public int
	getSampleSize ()
	{
		return this.size;
	}
	
	public void
	setDataStartOffset (int inOffset)
	{
		setBigEndian32 (DATA_START_OFFSET, inOffset);
	}
	
	public void
	setDataEndOffset (int inOffset)
	{
		setBigEndian32 (DATA_END_OFFSET, inOffset);
	}
	
	public void
	setName (String inName)
	{
		setString (NAME_OFFSET, inName, 8, ' ');
	}
	
	public void
	setSampleRate (int inSampleRate)
	{
		setBigEndian32 (SAMPLE_RATE_OFFSET, inSampleRate);
	}
	
	public void
	setSampleTune (float inSampleTune)
	throws Exception
	{
		setBigEndian16 (SAMPLE_TUNE_OFFSET, serialiseSampleTune (inSampleTune));
	}
	
	// well oops there's more than just the actual sample data
	// even though it SOUNDS fine
	public void
	writeSampleData (OutputStream outStream, int inSampleNumber)
	throws Exception
	{
		DataOutputStream	dis = new DataOutputStream (outStream);
		
		// magic number header
		dis.writeInt (0x80007fff);
		
		// wtf
		dis.writeInt (getBigEndian32 (DATA_START_OFFSET));
		dis.writeInt (getBigEndian32 (DATA_END_OFFSET));

		// sample number as byte		
		dis.writeByte ((byte) (inSampleNumber & 0xff));

		// sample type for some reason
		// 0=mono
		dis.writeByte ((byte) 0);
				
		// maybe not this one
		dis.writeShort (0xffff);
		
		// the actual byteage
		dis.write (this.data, this.offset, this.size);
		
		// so here we add the loop start sample and blockAlign
		// i've no idea why this would be the case
		
		int	numSampleFrames = getSampleSize () / 2;
		
		if ((numSampleFrames % 2) == 0)
		{
			dis.writeInt (0);
		}
		else
		{
			dis.writeShort (0);
		}

		dis.flush ();
	}

	// PUBLIC CONSTANTS
	
	public static final int
	BUFFER_SIZE = 40;
	
	// PRIVATE CONSTANTS
	
	private static final int
	NAME_OFFSET = 0x0;
	
	private static final int
	DATA_START_OFFSET = 0x8;
	
	private static final int
	DATA_END_OFFSET = 0xc;
	
	private static final int
	SAMPLE_START_OFFSET = 0x10;
	
	private static final int
	SAMPLE_END_OFFSET = 0x14;
			
	private static final int
	LOOP_OFFSET = 0x18;
			
	private static final int
	SAMPLE_RATE_OFFSET = 0x1c;
			
	private static final int
	SAMPLE_TUNE_OFFSET = 0x20;
			
	private static final int
	LEVEL_OFFSET = 0x22;
	
	private static final int
	STRETCH_STEP_OFFSET = 0x24;
	
	// PRIVATE DATA
	
	private byte[]
	data = null;
	
	private int
	offset = 0;
	
	private int
	size = 0;
			
}

