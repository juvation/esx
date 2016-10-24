

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
		short	samplePointer = getShort (SAMPLE_POINTER_OFFSET);

		boolean	off = (samplePointer & 0x8000) != 0;
		samplePointer &= 0x7fff;
				
		System.err.println ("sample pointer = " + getShort (SAMPLE_POINTER_OFFSET) + (off ? "off" : ""));
		System.err.println ("filter cutoff = " + getByte (FILTER_CUTOFF_OFFSET));
		System.err.println ("filter resonance = " + getByte (FILTER_RESONANCE_OFFSET));
		
		System.err.print ("sequence = ");
		
		byte[]	sequence = getBytes (SEQUENCE_DATA_OFFSET, 16);
		
		// hardwire to 16 steps
		for (int i = 0; i < 2; i++)
		{
			for (int j = 0; j < 8; j++)
			{
				int	mask = 1 << j;
				
				if ((sequence [i] & mask) == 0)
				{
					System.err.print (".");
				}
				else
				{
					System.err.print ("X");
				}
			}
		}
		
		System.err.println ();
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
			
}

