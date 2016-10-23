

public class ESXDrumPart
extends BufferManager
{
	public
	ESXDrumPart (byte[] inBuffer, int inOffset)	
	{
		super (inBuffer, inOffset);
		
		// System.err.println ("ESXDrumPart (" + inOffset + ") (0x" + Integer.toHexString (inOffset) + ")");
		
	}

	// PUBLIC METHODS
	
	public void
	dump ()
	{
		System.err.println ("sample pointer = " + getShort (SAMPLE_POINTER_OFFSET));
		System.err.println ("filter cutoff = " + getByte (FILTER_CUTOFF_OFFSET));
		System.err.println ("filter resonance = " + getByte (FILTER_RESONANCE_OFFSET));
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
	FILTER_RESONANCE_OFFSET = 0x5;
	
	private static final int
	SEQUENCE_DATA_OFFSET = 0x12;
			
}

