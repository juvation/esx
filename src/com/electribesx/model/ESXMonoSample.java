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
extends BufferManager
implements ESXSample
{
	// offset is into the buffer to the sample's metadata
	public
	ESXMonoSample (byte[] inBuffer, int inOffset)	
	{
		super (inBuffer, inOffset);
		
		// System.err.println ("ESXMonoSample (" + inOffset + ") (0x" + Integer.toHexString (inOffset) + ")");
		
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
		System.err.println ("ESXMonoSample.initFromFile(" + inFile.getName () + ")");
		
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
			
			System.err.println ("copied buffer of length " + this.size);
			System.err.println ("setting sample rate to " + sourceFormat.getSampleRate ());
			System.err.println ("setting sample end to " + (numFrames - 1));
			
			// copy some config information across
			setBigEndian32 (SAMPLE_RATE_OFFSET, (int) sourceFormat.getSampleRate ());
			setBigEndian32 (SAMPLE_START_OFFSET, 0);
			setBigEndian32 (SAMPLE_END_OFFSET, numFrames - 1);
			setBigEndian32 (LOOP_OFFSET, numFrames - 1);

			// HACK ok now just in case we're replacing an empty sample slot
			// overwrite the sample data start offset
			// so the file writer knows this slot as a sample in it...
			setDataStartOffset (0);
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
		int	dataStartOffset = getBigEndian32 (DATA_START_OFFSET);
		
		if (dataStartOffset >= 0)
		{
			System.err.println (inSampleNumber + ": ");
			System.err.println ("  '" + getString (NAME_OFFSET, 8) + "'");
			System.err.println (" data start offset " + getBigEndian32 (DATA_START_OFFSET));
			System.err.println (" data end offset " + getBigEndian32 (DATA_END_OFFSET));
			System.err.println (" sample start " + getBigEndian32 (SAMPLE_START_OFFSET));
			System.err.println (" sample end " + getBigEndian32 (SAMPLE_END_OFFSET));
			System.err.println (" loop end " + getBigEndian32 (LOOP_OFFSET));
			System.err.println (" sample rate " + getBigEndian32 (SAMPLE_RATE_OFFSET));
			System.err.println (" sample tune " + getBigEndian16 (SAMPLE_TUNE_OFFSET));
			System.err.println (" sample level " + getByte (LEVEL_OFFSET));
		}
	}
	
	public int
	getDataStartOffset ()
	{
		return getBigEndian32 (DATA_START_OFFSET);
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
		
		// only mono samples get a loop start marker
		// this is fucked up IMHO
		// we hack this for now
		dis.writeShort (0);

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

