// ESXStereoSample.java

package com.electribesx.model;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

public class ESXStereoSample
extends ESXSample
{
	// offset is into the buffer to the sample's metadata
	public
	ESXStereoSample (byte[] inBuffer, int inOffset)	
	{
		super (inBuffer, inOffset);
		
		// System.err.println ("ESXStereoSample (" + inOffset + ") (0x" + Integer.toHexString (inOffset) + ")");
		
		// note these are offsets from the start of the sample area @0x250000

		int	data1StartOffset = getBigEndian32 (DATA_1_START_OFFSET);
		int	data1EndOffset = getBigEndian32 (DATA_1_END_OFFSET);
		
		this.data1 = inBuffer;
		this.offset1 = 0x00250000 + data1StartOffset;

		int	data2StartOffset = getBigEndian32 (DATA_2_START_OFFSET);
		int	data2EndOffset = getBigEndian32 (DATA_2_END_OFFSET);
		
		this.data2 = inBuffer;
		this.offset2 = 0x00250000 + data2StartOffset;

		// we assume the two samples are of the same length...
		this.size = data1EndOffset - data1StartOffset;
	}

	// PUBLIC METHODS
	
	// i don't like the way we cache the file
	public void
	initFromFile (File inFile)
	throws Exception
	{
		// System.err.println ("ESXStereoSample.initFromFile(" + inFile.getName () + ")");
		
		AudioInputStream	sourceStream = AudioSystem.getAudioInputStream (inFile);
		AudioInputStream	esxStream = null;
		
		try
		{
			AudioFormat	sourceFormat = sourceStream.getFormat ();
			
			/*
			System.err.println ("sample rate = " + sourceFormat.getSampleRate ());
			System.err.println ("sample size = " + sourceFormat.getSampleSizeInBits ());
			System.err.println ("sample channels = " + sourceFormat.getChannels ());
			System.err.println ("big endian = " + sourceFormat.isBigEndian ());
			*/
			
			// ESX requires 16-bit PCM signed big-endian
			// note we convert to stereo here too
			AudioFormat	esxFormat = new AudioFormat
			(
				AudioFormat.Encoding.PCM_SIGNED,
				sourceFormat.getSampleRate (),
				16,
				2,
				4,
				sourceFormat.getSampleRate (),
				true
			);
			
			esxStream = AudioSystem.getAudioInputStream (esxFormat, sourceStream);

			// demuxed left and right samples
			ByteArrayOutputStream	bos1 = new ByteArrayOutputStream ();
			ByteArrayOutputStream	bos2 = new ByteArrayOutputStream ();
			
			int	cc = 0;
			int	i = 0;
			byte[]	buffer = new byte [1024];
			
			do
			{
				cc = esxStream.read (buffer);
				
				if (cc > 0)
				{
					for (int j = 0; j < cc; j++, i++)
					{
						if ((i % 4) < 2)
						{
							bos1.write (buffer [j]);
						}
						else
						{
							bos2.write (buffer [j]);
						}
					}
				}
			}
			while (cc > 0);

			this.data1 = bos1.toByteArray ();
			this.offset1 = 0;

			this.data2 = bos2.toByteArray ();
			this.offset2 = 0;

			this.size = this.data1.length;

			int	numFrames = (this.size / 2);
			
			/*
			System.err.println ("left buffer is size " + this.data1.length);
			System.err.println ("right buffer is size " + this.data2.length);
						
			System.err.println ("setting sample rate to " + sourceFormat.getSampleRate ());
			System.err.println ("setting sample end to " + (numFrames - 1));
			*/
			
			// set some config information from the sample itself
			setBigEndian32 (SAMPLE_RATE_OFFSET, (int) sourceFormat.getSampleRate ());
			setBigEndian32 (SAMPLE_START_OFFSET, 0);
			setBigEndian32 (SAMPLE_END_OFFSET, numFrames - 2);
			
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
				System.err.println ("error closing source stream");
			}
		}
	}
	
	public void
	initFromFile2 (File inFile)
	throws Exception
	{
		WavFile	wavFile = new WavFile (inFile, true);
		
		byte[]	sampleData = wavFile.getChunk ("data");
		ByteArrayOutputStream	bos1 = new ByteArrayOutputStream ();
		ByteArrayOutputStream	bos2 = new ByteArrayOutputStream ();
		
		if (wavFile.getBitsPerSample () == 8)
		{
			for (int i = 0; i < wavFile.getNumFrames (); i++)
			{
				int	offset = i * wavFile.getBlockAlign ();
				int	sample = (int) sampleData [offset];
				
				// 8-bit audio is 0-255, convert to -128-127
				sample -= 128;
				
				// ESX samples are big-endian 16-bit
				bos1.write (sample);
				bos1.write (0);
				
				if (wavFile.getNumChannels () == 1)
				{
					bos2.write (sample);
					bos2.write (0);
				}
				else
				{
					sample = (int) sampleData [offset + 1];
					
					bos2.write (sample);
					bos2.write (0);
				}
			}
		}
		else
		if (wavFile.getBitsPerSample () == 16)
		{
			for (int i = 0; i < wavFile.getNumFrames (); i++)
			{
				int	offset = i * wavFile.getBlockAlign ();

				// ESX samples are big-endian 16-bit
				bos1.write (sampleData [offset + 1]);
				bos1.write (sampleData [offset]);

				if (wavFile.getNumChannels () == 1)
				{
					bos2.write (sampleData [offset + 1]);
					bos2.write (sampleData [offset]);
				}
				else
				{
					bos2.write (sampleData [offset + 3]);
					bos2.write (sampleData [offset + 2]);
				}
			}
		}
		else
		if (wavFile.getBitsPerSample () == 24)
		{
			for (int i = 0; i < wavFile.getNumFrames (); i++)
			{
				int	offset = i * wavFile.getBlockAlign ();

				// ESX samples are big-endian 16-bit
				bos1.write (sampleData [offset + 2]);
				bos1.write (sampleData [offset + 1]);

				if (wavFile.getNumChannels () == 1)
				{
					bos2.write (sampleData [offset + 2]);
					bos2.write (sampleData [offset + 1]);
				}
				else
				{
					bos2.write (sampleData [offset + 5]);
					bos2.write (sampleData [offset + 4]);
				}
			}
		}
		else
		{
			throw new Exception (wavFile.getBitsPerSample () + " not supported");
		}
		
		this.data1 = bos1.toByteArray ();
		this.offset1 = 0;

		this.data2 = bos2.toByteArray ();
		this.offset2 = 0;

		this.size = this.data1.length;

		setBigEndian32 (SAMPLE_RATE_OFFSET, wavFile.getSampleRate ());
		setBigEndian32 (SAMPLE_START_OFFSET, 0);
		setBigEndian32 (SAMPLE_END_OFFSET, wavFile.getNumFrames () - 1);
		
		// and default the name
		String	name = inFile.getName ();
		int	dotIndex = name.indexOf ('.');
		setName (name.substring (0, dotIndex));
	}
	
	// DEBUG PUBLIC METHODS
	
	public void
	dump (int inSampleNumber)
	{
		int	data1StartOffset = getBigEndian32 (DATA_1_START_OFFSET);
		
		if (data1StartOffset >= 0)
		{
			System.err.print (inSampleNumber + ": " + getString (NAME_OFFSET, 8) + "'");

			/*
			int	data1EndOffset = getBigEndian32 (DATA_1_END_OFFSET);
			System.err.println (" data 1 offset " + data1StartOffset + " to offset " + data1EndOffset);

			int	data2StartOffset = getBigEndian32 (DATA_2_START_OFFSET);
			int	data2EndOffset = getBigEndian32 (DATA_2_END_OFFSET);
			System.err.println (" data 2 offset " + data2StartOffset + " to offset " + data2EndOffset);

			int	sampleStartOffset = getBigEndian32 (SAMPLE_START_OFFSET);
			int	sampleEndOffset = getBigEndian32 (SAMPLE_END_OFFSET);
			System.err.println (" sample start " + sampleStartOffset + " end " + sampleEndOffset);
			*/
		}
	}
	
	public int
	getData1StartOffset ()
	{
		return getBigEndian32 (DATA_1_START_OFFSET);
	}

	public int
	getData2StartOffset ()
	{
		return getBigEndian32 (DATA_2_START_OFFSET);
	}
	
	public String
	getName ()
	{
		return getString (NAME_OFFSET, 8);
	}
	
	public int
	getSample1 (int inSampleNumber)
	{
		byte	msb = this.data1 [this.offset1 + (inSampleNumber * 2)];
		byte	lsb = this.data1 [this.offset1 + (inSampleNumber * 2) + 1];
		
		return (msb << 8) | (lsb & 0xff);
	}
	
	public int
	getSample2 (int inSampleNumber)
	{
		byte	msb = this.data2 [this.offset2 + (inSampleNumber * 2)];
		byte	lsb = this.data2 [this.offset2 + (inSampleNumber * 2) + 1];
		
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
	setData1StartOffset (int inOffset)
	{
		setBigEndian32 (DATA_1_START_OFFSET, inOffset);
	}
	
	public void
	setData1EndOffset (int inOffset)
	{
		setBigEndian32 (DATA_1_END_OFFSET, inOffset);
	}

	public void
	setData2StartOffset (int inOffset)
	{
		setBigEndian32 (DATA_2_START_OFFSET, inOffset);
	}
	
	public void
	setData2EndOffset (int inOffset)
	{
		setBigEndian32 (DATA_2_END_OFFSET, inOffset);
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
		
		// LEFT CHANNEL
		
		// magic number header
		dis.writeInt (0x80007fff);
		
		// wtf
		dis.writeInt (getBigEndian32 (DATA_1_START_OFFSET));
		dis.writeInt (getBigEndian32 (DATA_1_END_OFFSET));
		
		// sample number
		dis.writeByte ((byte) inSampleNumber);

		// sample type = 1 = stereo left channel
		dis.writeByte ((byte) 1);
		
		// maybe not this one
		dis.writeShort (0xffff);
		
		// the actual byteage
		dis.write (this.data1, this.offset1, this.size);
		
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

		// RIGHT CHANNEL
		
		// magic number header
		dis.writeInt (0x80007fff);
		
		// wtf
		dis.writeInt (getBigEndian32 (DATA_2_START_OFFSET));
		dis.writeInt (getBigEndian32 (DATA_2_END_OFFSET));
		
		// sample number
		dis.writeByte ((byte) inSampleNumber);
		
		// sample type = 2 = stereo right channel
		dis.writeByte ((byte) 2);
		
		// maybe not this one
		dis.writeShort (0xffff);
		
		// the actual byteage
		dis.write (this.data2, this.offset2, this.size);
	
		// so here we add the loop start sample and blockAlign
		// i've no idea why this would be the case
		numSampleFrames = getSampleSize () / 2;
		
		if ((numSampleFrames % 2) == 0)
		{
			dis.writeInt (0);
		}
		else
		{
			dis.writeShort (0);
		}

		// swoosh
		dis.flush ();		
	}
	
	// PUBLIC CONSTANTS
	
	public static final int
	BUFFER_SIZE = 44;
	
	// PRIVATE CONSTANTS
	
	private static final int
	NAME_OFFSET = 0x0;
	
	private static final int
	DATA_1_START_OFFSET = 0x8;
	
	private static final int
	DATA_1_END_OFFSET = 0xc;
	
	private static final int
	DATA_2_START_OFFSET = 0x10;
	
	private static final int
	DATA_2_END_OFFSET = 0x14;
	
	private static final int
	SAMPLE_START_OFFSET = 0x18;
	
	private static final int
	SAMPLE_END_OFFSET = 0x1c;
			
	private static final int
	SAMPLE_RATE_OFFSET = 0x20;
			
	private static final int
	SAMPLE_TUNE_OFFSET = 0x24;
			
	private static final int
	LEVEL_OFFSET = 0x26;
	
	// PRIVATE DATA
	
	private byte[]
	data1 = null;
	
	private byte[]
	data2 = null;
	
	private int
	offset1 = 0;
	
	private int
	offset2 = 0;
	
	private int
	size = 0;
			
}

