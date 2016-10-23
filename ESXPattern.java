

public class ESXPattern
extends BufferManager
{
	public
	ESXPattern (byte[] inBuffer, int inOffset)	
	{
		super (inBuffer, inOffset);
		
		// System.err.println ("ESXPattern (" + inOffset + ") (0x" + Integer.toHexString (inOffset) + ")");
		
		this.drumParts = new ESXDrumPart [9];
		
		for (int i = 0; i < 9; i++)
		{
			this.drumParts [i] = new ESXDrumPart (this.buffer, DRUM_PARTS_OFFSET + (ESXDrumPart.BUFFER_SIZE));
		}
	}

	// PUBLIC METHODS
	
	public void
	dump ()
	{
		int	tempo = getShort (TEMPO_OFFSET);
		int	integerTempo = (tempo >> 7) & 0x1ffff;
		int	fractionTempo = tempo & 0xf;
		
		System.err.println ("name = '" + getString (NAME_OFFSET, 8) + "'");
		System.err.println ("tempo = " + integerTempo + "." + fractionTempo);
		System.err.println ("last step = " + getByte (LAST_STEP_OFFSET));
		
		for (int i = 0; i < 9; i++)
		{
			System.err.println ("drum part " + i);
			this.drumParts [i].dump ();
		}
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
	LAST_STEP_OFFSET = 0xd;
	
	private static final int
	DRUM_PARTS_OFFSET = 0x18;
	
	private static final int
	KEYBOARD_PARTS_OFFSET = 0x14a;
	
	// private data

	private ESXDrumPart[]
	drumParts	= null;
	
}

