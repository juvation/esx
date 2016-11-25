package com.electribesx.model;


public class ESXKeyboardPart
extends BufferManager
{
	public
	ESXKeyboardPart (ESXPattern inPattern, byte[] inBuffer, int inOffset)	
	{
		super (inBuffer, inOffset);
		
		this.pattern = inPattern;
		
		// System.out.println ("ESXKeyboardPart (" + inOffset + ") (0x" + Integer.toHexString (inOffset) + ")");
		
	}

	// PUBLIC METHODS
	
	public void
	dump (int inPartNumber)
	{
		System.out.println ("Keyboard Part " + inPartNumber);

		short	samplePointer = getBigEndian16 (SAMPLE_POINTER_OFFSET);

		boolean	off = (samplePointer & 0x8000) != 0;
		samplePointer &= 0x7fff;
				
		System.out.println ("sample = " + getBigEndian16 (SAMPLE_POINTER_OFFSET) + (off ? "off" : ""));
		System.out.println ("filter cutoff = " + getByte (FILTER_CUTOFF_OFFSET));
		System.out.println ("filter resonance = " + getByte (FILTER_RESONANCE_OFFSET));

		System.out.print ("sequence = ");

		int	steps = this.pattern.getLastStep () + 1;
		
		byte[]	notes = getBytes (SEQUENCE_DATA_NOTE_OFFSET, steps);
		byte[]	gates = getBytes (SEQUENCE_DATA_GATE_OFFSET, steps);
		
		for (int i = 0; i < steps; i++)
		{
			if (i > 0)
			{
				System.out.print (" ");
			}
			
			System.out.print (noteToString (notes [i]));
			System.out.print (" (");
			System.out.print (gates [i]);
			System.out.print (")");
		}
		
		System.out.println ();
	}
	
	public String
	gateToString (byte inNote)
	{
		return Integer.toString (inNote);
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
	
	public String
	noteToString (byte inNote)
	{
		inNote &= 0x7f;
		
		int	octave = (inNote / 12) - 1;
		int	relativeNoteNumber = inNote % 12;
		
		return NOTE_NAMES [relativeNoteNumber] + octave;
	}
	
	public void
	setSampleNumber (int inSampleNumber)
	{
		setBigEndian16 (SAMPLE_POINTER_OFFSET, inSampleNumber);
	}
	
	public void
	setSequenceGate (int inStepNumber, byte inGateTime)
	{
		setByte (SEQUENCE_DATA_GATE_OFFSET + inStepNumber, inGateTime);
	}
	
	public void
	setSequenceNote (int inStepNumber, byte inNote)
	{
		setByte (SEQUENCE_DATA_NOTE_OFFSET + inStepNumber, inNote);
	}

	public void
	setSequenceNote (int inStepNumber, String inNoteString)
	throws Exception
	{
		if (inNoteString.length () == 0)
		{
			throw new Exception ("setSequenceStepNote with empty note string");
		}

		char	noteName = inNoteString.toLowerCase ().charAt (0);
		
		int	relativeNoteNumber = "ccddeffggaab".indexOf (noteName);
		
		if (relativeNoteNumber == -1)
		{
			throw new Exception ("setSequenceStepNote with bad note string: " + inNoteString);
		}
		
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
		
		// start at C0 which in Korg world is 12 (C4 = 60)
		int	noteNumber = 12;
		noteNumber += (octave * 12);
		noteNumber += relativeNoteNumber;
		noteNumber += modifier;

		if (noteNumber < 0 || noteNumber > 127)
		{
			throw new Exception ("setSequenceStepNote with bad note string: " + inNoteString);
		}
		
		byte	noteByte = (byte) noteNumber;
		
		setByte (SEQUENCE_DATA_NOTE_OFFSET + inStepNumber, noteByte);
	}
	
	// PUBLIC CONSTANTS
	
	public static final int
	BUFFER_SIZE = 274;
	
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
	
	private static String[]
	NOTE_NAMES =
	{
		"C",
		"C#",
		"D",
		"Eb",
		"E",
		"F",
		"F#",
		"G",
		"Ab",
		"A",
		"Bb",
		"B"
	};
	
	// PRIVATE DATA
	
	private ESXPattern
	pattern = null;
	
}

