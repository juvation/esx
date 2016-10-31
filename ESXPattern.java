

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
			this.drumParts [i] = new ESXDrumPart (this.buffer, this.offset + DRUM_PARTS_OFFSET + (i * ESXDrumPart.BUFFER_SIZE));
		}
	}

	// PUBLIC METHODS
	
	public void
	dump ()
	{
		System.err.println ("name = '" + getString (NAME_OFFSET, 8) + "'");

		int	tempo = getBigEndian16 (TEMPO_OFFSET);
		int	integerTempo = (tempo >> 7) & 0x1ffff;
		int	fractionTempo = tempo & 0xf;
		
		System.err.println ("tempo = " + integerTempo + "." + fractionTempo);
		System.err.println ("last step = " + getByte (LAST_STEP_OFFSET));
		
		System.err.println ("drum part 0");
		this.drumParts [0].dump ();

		System.err.println ("drum part 1");
		this.drumParts [1].dump ();

		System.err.println ("drum part 2");
		this.drumParts [2].dump ();
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

