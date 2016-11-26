package com.electribesx.model;


public class ESXPattern
extends BufferManager
{
	public
	ESXPattern (byte[] inBuffer, int inOffset)	
	{
		super (inBuffer, inOffset);
		
		// System.out.println ("ESXPattern (" + inOffset + ") (0x" + Integer.toHexString (inOffset) + ")");
		
		this.drumParts = new ESXDrumPart [9];
		
		for (int i = 0; i < 9; i++)
		{
			this.drumParts [i] = new ESXDrumPart	
				(this, this.buffer, this.offset + DRUM_PARTS_OFFSET + (i * ESXDrumPart.BUFFER_SIZE));
		}

		this.keyboardParts = new ESXKeyboardPart [2];
		
		for (int i = 0; i < 2; i++)
		{
			this.keyboardParts [i] = new ESXKeyboardPart
				(this, this.buffer, this.offset + KEYBOARD_PARTS_OFFSET + (i * ESXKeyboardPart.BUFFER_SIZE));
		}
	}

	// PUBLIC METHODS
	
	public void
	dump (int inPatternNumber)
	{
		System.out.println ("PATTERN " + inPatternNumber + " = '" + getString (NAME_OFFSET, 8) + "'");

		System.out.println ("tempo = " + getTempo ());

		int	length = getLength ();
		System.out.println ("length = " + length + " (" + (length + 1) + ")");

		System.out.println ("beat = " + getBeat () + " (" + getBeatString () + ")");
		
		int	lastStep = getLastStep ();
		System.out.println ("last step = " + lastStep + " (" + (lastStep + 1) + ")");
		
		this.drumParts [0].dump (0);
		this.keyboardParts [0].dump (0);
	}
	
	public int
	getBeat ()
	{
		byte	beat = getByte (CONFIG_FLAGS_OFFSET);
		
		return (beat >> 4) & 0x3;
	}

	public String
	getBeatString ()
	{
		return BEAT_NAMES [getBeat ()];
	}
	
	public ESXDrumPart
	getDrumPart (int inIndex)
	{
		return this.drumParts [inIndex];
	}

	public ESXKeyboardPart
	getKeyboardPart (int inIndex)
	{
		return this.keyboardParts [inIndex];
	}

	public int
	getLastStep ()
	{
		return getByte (LAST_STEP_OFFSET);
	}

	public int
	getLength ()
	{
		return getByte (CONFIG_FLAGS_OFFSET) & 0x7;
	}
		
	public String
	getName ()
	{
		return getString (NAME_OFFSET, 8);
	}
	
	public float
	getTempo ()
	{
		int	tempoBits = getBigEndian16 (TEMPO_OFFSET);
		int	integerTempo = (tempoBits >> 7) & 0x1ffff;
		int	fractionTempo = tempoBits & 0xf;

		float	tempo = (integerTempo * 10) + fractionTempo;
		tempo /= 10;
		
		return tempo;
	}
	
	public void
	setBeat (int inBeat)
	{
		byte	configFlags = getByte (CONFIG_FLAGS_OFFSET);
		configFlags &= (0x3 << 4);
		
		inBeat &= 0x3;
		inBeat <<= 4;
		configFlags |= inBeat;
		
		setByte (CONFIG_FLAGS_OFFSET, configFlags);
	}
	
	public void
	setLastStep (int inLastStep)
	{
		setByte (LAST_STEP_OFFSET, (byte) inLastStep);
	}
	
	public void
	setLength (int inLength)
	{
		byte	configFlags = getByte (CONFIG_FLAGS_OFFSET);
		configFlags &= 0x7;

		inLength &= 0x7;
		configFlags |= inLength;
		
		setByte (CONFIG_FLAGS_OFFSET, configFlags);
	}
	
	public void
	setName (String inName)
	{
		setString (NAME_OFFSET, inName, 8, ' ');
	}
	
	public void
	setTempo (float inTempo)
	{
		int	integerTempo = (int) Math.floor (inTempo);
		int	fractionTempo = (int) ((inTempo - integerTempo) * 10);

		int	tempo = fractionTempo & 0xf;
		tempo |= (integerTempo & 0x1ffff) << 7;
		
		setBigEndian16 (TEMPO_OFFSET, tempo);
	}
	
	// PUBLIC CONSTANTS
	
	public static final int
	BUFFER_SIZE = 4280;
	
	// PRIVATE CONSTANTS
	
	private static final int
	NAME_OFFSET = 0x0;
	
	private static final int
	TEMPO_OFFSET = 0x8;

	private static final int
	CONFIG_FLAGS_OFFSET = 0xb;

	private static final int
	LAST_STEP_OFFSET = 0xd;
	
	private static final int
	DRUM_PARTS_OFFSET = 0x18;
	
	private static final int
	KEYBOARD_PARTS_OFFSET = 0x14a;
	
	private static final String[]
	BEAT_NAMES =
	{
		"16",
		"32",
		"8T",
		"16T"
	};
	
	// private data

	private ESXDrumPart[]
	drumParts	= null;
	
	private ESXKeyboardPart[]
	keyboardParts	= null;
	
}

