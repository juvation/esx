package com.electribesx.model;


public class ESXDrumPart
extends BufferManager
{
	public
	ESXDrumPart (ESXPattern inPattern, byte[] inBuffer, int inOffset)	
	{
		super (inBuffer, inOffset);
		
		this.pattern = inPattern;
		
		// System.out.println ("ESXDrumPart (" + inOffset + ") (0x" + Integer.toHexString (inOffset) + ")");
		
	}

	// PUBLIC METHODS
	
	public void
	dump (int inPartNumber)
	{
		System.out.println ("Drum Part " + inPartNumber);
		
		short	samplePointer = getBigEndian16 (SAMPLE_POINTER_OFFSET);

		boolean	off = (samplePointer & 0x8000) != 0;
		samplePointer &= 0x7fff;
				
		System.out.println ("sample = " + getBigEndian16 (SAMPLE_POINTER_OFFSET) + (off ? "off" : ""));
		System.out.println ("filter cutoff = " + getByte (FILTER_CUTOFF_OFFSET));
		System.out.println ("filter resonance = " + getByte (FILTER_RESONANCE_OFFSET));
		
		System.out.print ("sequence = ");
		
		byte[]	sequence = getBytes (SEQUENCE_DATA_OFFSET, 16);
		
		int	bytes = (this.pattern.getLastStep () + 1) / 8;
		
		for (int i = 0; i < bytes; i++)
		{
			for (int j = 0; j < 8; j++)
			{
				int	mask = 1 << j;
				
				if ((sequence [i] & mask) == 0)
				{
					System.out.print (".");
				}
				else
				{
					System.out.print ("X");
				}
			}
		}
		
		System.out.println ();
	}
	
	public byte[]
	getSequence ()
	{
		return getBytes (SEQUENCE_DATA_OFFSET, 16);
	}
	
	public void
	setSampleNumber (int inSampleNumber)
	{
		setBigEndian16 (SAMPLE_POINTER_OFFSET, inSampleNumber);
	}
	
	public void
	setSequenceStep (int inStepNumber, boolean inValue)
	{
		int	byteNumber = inStepNumber / 8;
		int	bitNumber = inStepNumber % 8;
		int	mask = 1 << bitNumber;
				
		byte	sequenceByte = getByte (SEQUENCE_DATA_OFFSET + byteNumber);
		
		if (inValue)
		{
			sequenceByte |= mask;
		}
		else
		{
			sequenceByte &= ~mask;
		}
		
		setByte (SEQUENCE_DATA_OFFSET + byteNumber, sequenceByte);
	}
	
	// PUBLIC CONSTANTS
	
	public static final int
	BUFFER_SIZE = 34;
	
	// PRIVATE CONSTANTS
	
	private static final int
	SAMPLE_POINTER_OFFSET = 0x0;
	
	private static final int
	FILTER_CUTOFF_OFFSET = 0x5;
	
	private static final int
	FILTER_RESONANCE_OFFSET = 0x6;
	
	private static final int
	SEQUENCE_DATA_OFFSET = 0x12;
			
	// PRIVATE DATA
	
	private ESXPattern
	pattern = null;
	
}

