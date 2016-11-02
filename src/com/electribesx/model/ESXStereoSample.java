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
extends BufferManager
implements ESXSample
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
		System.err.println ("ESXStereoSample.initFromFile(" + inFile.getName () + ")");
		
		AudioInputStream	sourceStream = AudioSystem.getAudioInputStream (inFile);
		AudioInputStream	esxStream = null;
		
		try
		{
			AudioFormat	sourceFormat = sourceStream.getFormat ();
			
			System.err.println ("sample rate = " + sourceFormat.getSampleRate ());
			System.err.println ("sample size = " + sourceFormat.getSampleSizeInBits ());
			System.err.println ("sample channels = " + sourceFormat.getChannels ());
			System.err.println ("big endian = " + sourceFormat.isBigEndian ());
			
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

			// weirdly we get the sample end 8 bytes too big
			// so we hack...
			int	numFrames = (this.size / 2);
			numFrames -= 8;
			
			System.err.println ("left buffer is size " + this.data1.length);
			System.err.println ("right buffer is size " + this.data2.length);
						
			System.err.println ("setting sample rate to " + sourceFormat.getSampleRate ());
			System.err.println ("setting sample end to " + (numFrames - 1));
			
			// set some config information from the sample itself
			setBigEndian32 (SAMPLE_RATE_OFFSET, (int) sourceFormat.getSampleRate ());
			setBigEndian32 (SAMPLE_START_OFFSET, 0);
			setBigEndian32 (SAMPLE_END_OFFSET, numFrames - 1);
			
			// HACK ok now just in case we're replacing an empty sample slot
			// overwrite the sample data start offset
			// so the file writer knows this slot as a sample in it...
			setData1StartOffset (0);
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
	
	// DEBUG PUBLIC METHODS
	
	public void
	dump (int inSampleNumber)
	{
		int	data1StartOffset = getBigEndian32 (DATA_1_START_OFFSET);
		
		if (data1StartOffset >= 0)
		{
			System.err.print (inSampleNumber + ": ");
			System.err.println ("'" + getString (NAME_OFFSET, 8) + "'");

			int	data1EndOffset = getBigEndian32 (DATA_1_END_OFFSET);
			System.err.println (" data 1 offset " + data1StartOffset + " to offset " + data1EndOffset);

			int	data2StartOffset = getBigEndian32 (DATA_2_START_OFFSET);
			int	data2EndOffset = getBigEndian32 (DATA_2_END_OFFSET);
			System.err.println (" data 2 offset " + data2StartOffset + " to offset " + data2EndOffset);

			int	sampleStartOffset = getBigEndian32 (SAMPLE_START_OFFSET);
			int	sampleEndOffset = getBigEndian32 (SAMPLE_END_OFFSET);
			System.err.println (" sample start " + sampleStartOffset + " end " + sampleEndOffset);
		}
	}
	
	public int
	getData1StartOffset ()
	{
		return getBigEndian32 (DATA_1_START_OFFSET);
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

