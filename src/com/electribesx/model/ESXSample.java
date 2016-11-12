// ESXSample.java

// interface shared by mono and stereo samples, fwiw
// the whole mono/stereo thing is broken and should be fixed

package com.electribesx.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;

public abstract class ESXSample
extends BufferManager
{
	// PUBLIC CONSTRUCTOR
	
	public
	ESXSample (byte[] inBuffer, int inOffset)
	{
		super (inBuffer, inOffset);
	}
	
	// INTERFACE
	
	public abstract void
	initFromFile (File inFile)
	throws Exception;
	
	public abstract void
	dump (int inSampleNumber);

	public abstract String
	getName ();
	
	public abstract int
	getSampleSize ();

	public abstract void
	setSampleRate (int inSampleRate);
	
	public abstract void
	setSampleTune (float inSampleTune)
	throws Exception;
	
	public abstract void
	writeSampleData (OutputStream outStream, int inSampleNumber)
	throws Exception;
	
	// PUBLIC METHODS
	
	// PROTECTED METHODS
	
	// ESX can only handle 44100
	// so if you're importing a sample of another sample rate
	// we have to adjust the tuning
	protected float
	getTuneFromSamplingRate (int inSampleRate)
	{
		float x = ((float) inSampleRate) / 44100;
		float y = (float) Math.log (x);
		float z = (float) (y / Math.log (2));

		BigDecimal bd = new BigDecimal (12 * z)
			.setScale (2, RoundingMode.HALF_EVEN);

		return bd.floatValue ();
	}
	
	protected float
	deserialiseSampleTune (int inSerialised)
	throws Exception
	{
		int	integer = (inSerialised >> 8) & 0x7f;

		if (integer > 99)
		{
			throw new Exception ("bad integer part in serialised tune: " + integer);
		}

		float	fraction = (float) (inSerialised & 0xff);
		fraction /= 100f;
		
		float	sampleTune = integer + fraction;
		
		if ((inSerialised & 0x8000) != 0)
		{
			sampleTune = -sampleTune;
		}
		
		return sampleTune;
	}
	
	protected int
	serialiseSampleTune (float inSampleTune)
	throws Exception
	{
		boolean	negative = inSampleTune < 0;
		float	absolute = Math.abs (inSampleTune);
		int	integer = (int) Math.floor (absolute);

		if (integer > 99)
		{
			throw new Exception ("bad integer part in tune: " + integer);
		}

		int	fraction = (int) Math.round ((absolute - integer) * 100f);
		
		int	serialised = 0;
		
		if (negative)
		{
			serialised |= 1 << 15;
		}
		
		integer &= 0x7f;
		serialised |= integer << 8;
		
		fraction &= 0xff;
		serialised |= fraction;
		
		serialised &= 0xffff;
		return serialised;
	}
	
}
