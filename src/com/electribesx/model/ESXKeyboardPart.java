package com.electribesx.model;


public class ESXKeyboardPart
extends BufferManager
{
	public
	ESXKeyboardPart (byte[] inBuffer, int inOffset)	
	{
		super (inBuffer, inOffset);
		
		// System.out.println ("ESXKeyboardPart (" + inOffset + ") (0x" + Integer.toHexString (inOffset) + ")");
		
	}

	// PUBLIC METHODS
	
	public void
	dump ()
	{
		short	samplePointer = getBigEndian16 (SAMPLE_POINTER_OFFSET);

		boolean	off = (samplePointer & 0x8000) != 0;
		samplePointer &= 0x7fff;
				
		System.out.println ("sample pointer = " + getBigEndian16 (SAMPLE_POINTER_OFFSET) + (off ? "off" : ""));
		System.out.println ("filter cutoff = " + getByte (FILTER_CUTOFF_OFFSET));
		System.out.println ("filter resonance = " + getByte (FILTER_RESONANCE_OFFSET));
		
		System.out.print ("sequence = ");
		
		/*
		byte[]	sequence = getBytes (SEQUENCE_DATA_OFFSET, 16);
		
		// hardwire to 16 steps
		for (int i = 0; i < 2; i++)
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
		*/
		
		System.out.println ();
	}
	
	public byte[]
	getSequenceGates ()
	{
		return getBytes (SEQUENCE_DATA_GATE_OFFSET, 128);
	}

	public byte[]
	getSequenceNotes ()
	{
		return getBytes (SEQUENCE_DATA_NOTE_OFFSET, 128);
	}
	
	public void
	setSampleNumber (int inSampleNumber)
	{
		setBigEndian16 (SAMPLE_POINTER_OFFSET, inSampleNumber);
	}
	
	public void
	setSequenceStepGate (int inStepNumber, byte inGateTime)
	{
		setByte (SEQUENCE_DATA_GATE_OFFSET + inStepNumber, inGateTime);
	}
	
	public void
	setSequenceStepNote (int inStepNumber, byte inNote)
	{
		setByte (SEQUENCE_DATA_NOTE_OFFSET + inStepNumber, inNote);
	}

	public void
	setSequenceStepNote (int inStepNumber, String inNoteString)
	throws Exception
	{
		if (inNoteString.length () == 0)
		{
			throw new Exception ("setSequenceStepNote with empty note string");
		}

		char	noteName = inNoteString.toLowerCase ().charAt (0);
		
		int	relativeNoteNumber = "c_d_ef_g_a_b".indexOf (noteName);
		
		if (relativeNoteNumber == -1)
		{
			throw new Exception ("setSequenceStepNote with bad note string: " + inNoteString);
		}
		
		System.out.println ("relative note is " + relativeNoteNumber);

		int	octave = 0;
		int	modifier = 0;
		
		if (inNoteString.length () > 1)
		{
			char	flatOrSharp = inNoteString.charAt (1);

			if (flatOrSharp == '-' || Character.isDigit (flatOrSharp))
			{
				octave = Integer.parseInt (inNoteString.substring (1));
			}
			else
			{
				if (flatOrSharp == 'b')
				{
					modifier = -1;
				}
				else
				if (flatOrSharp == '#')
				{
					modifier = 1;
				}
				else
				{
					throw new Exception ("setSequenceStepNote with bad flat or sharp: " + inNoteString);
				}
				
				if (inNoteString.length () > 2)
				{
					octave = Integer.parseInt (inNoteString.substring (2));
				}
			}
		}
		
		// start at C0 which in our world is 24
		int	noteNumber = 24;
		noteNumber += (octave * 12);
		noteNumber += relativeNoteNumber;
		noteNumber += modifier;
		
		System.out.println ("midi note is " + noteNumber);

		if (noteNumber < 0 || noteNumber > 127)
		{
			throw new Exception ("setSequenceStepNote with bad note string: " + inNoteString);
		}
		
		byte	noteByte = (byte) noteNumber;
		
		
		setByte (SEQUENCE_DATA_NOTE_OFFSET + inStepNumber, noteByte);
	}
	
	// PUBLIC CONSTANTS
	
	public static final int
	BUFFER_SIZE = 34;
	
	// PRIVATE CONSTANTS
	
	private static final int
	SAMPLE_POINTER_OFFSET = 0x0;
	
	private static final int
	FILTER_CUTOFF_OFFSET = 0x6;
	
	private static final int
	FILTER_RESONANCE_OFFSET = 0x7;
	
	private static final int
	SEQUENCE_DATA_NOTE_OFFSET = 0x12;

	private static final int
	SEQUENCE_DATA_GATE_OFFSET = 0x92;
			
}

