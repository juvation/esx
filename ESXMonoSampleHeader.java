

public class ESXMonoSampleHeader
extends BufferManager
{
	public
	ESXMonoSampleHeader (byte[] inBuffer, int inOffset)	
	{
		super (inBuffer, inOffset);
		
		// System.err.println ("ESXMonoSampleHeader (" + inOffset + ") (0x" + Integer.toHexString (inOffset) + ")");
		
	}

	// PUBLIC METHODS
	
	public void
	dump ()
	{
		System.err.println ("name = '" + getString (NAME_OFFSET, 8) + "'");
		
		// note this is *absolute* from the start of the buffer
		int	dataStartOffset = getInteger (DATA_START_OFFSET);
		
		for (int i = 0; i < 8; i++)
		{
			byte	b = this.buffer [dataStartOffset + i];
			
			System.err.print ("0x" + Integer.toHexString (b & 0xff) + " ");
		}
		
		System.err.println ();
	}
	
	// PUBLIC CONSTANTS
	
	public static final int
	BUFFER_SIZE = 40;
	
	// PRIVATE CONSTANTS
	
	private static final int
	NAME_OFFSET = 0x0;
	
	private static final int
	DATA_START_OFFSET = 0x8;
	
	private static final int
	DATA_END_OFFSET = 0xc;
	
	private static final int
	SAMPLE_START_OFFSET = 0x10;
	
	private static final int
	SAMPLE_END_OFFSET = 0x14;
			
	private static final int
	LOOP_OFFSET = 0x18;
			
	private static final int
	SAMPLE_RATE_OFFSET = 0x1c;
			
	private static final int
	SAMPLE_TUNE_OFFSET = 0x20;
			
	private static final int
	LEVEL_OFFSET = 0x22;
			
}

