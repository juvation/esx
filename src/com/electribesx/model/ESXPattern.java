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

		int	tempo = getBigEndian16 (TEMPO_OFFSET);
		int	integerTempo = (tempo >> 7) & 0x1ffff;
		int	fractionTempo = tempo & 0xf;
		
		System.out.println ("tempo = " + integerTempo + "." + fractionTempo);
		System.out.println ("pattern config = " + getByte (CONFIG_FLAGS_OFFSET));
		System.out.println ("last step = " + getByte (LAST_STEP_OFFSET));
		
		this.drumParts [0].dump (0);
		this.keyboardParts [0].dump (0);
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
		
	public void
	setLastStep (int inLastStep)
	{
		setByte (LAST_STEP_OFFSET, (byte) inLastStep);
	}
	
	public String
	getName ()
	{
		return getString (NAME_OFFSET, 8);
	}
	
	public void
	setName (String inName)
	{
		setString (NAME_OFFSET, inName, 8, ' ');
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
	
	// private data

	private ESXDrumPart[]
	drumParts	= null;
	
	private ESXKeyboardPart[]
	keyboardParts	= null;
	
}

