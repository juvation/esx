

public class ESXFile
extends BufferManager
{
	public
	ESXFile (byte[] inBuffer)
	throws Exception
	{
		super (inBuffer, 0);
		
		checkBuffer ();
		
		this.globalParameters = new ESXGlobalParameters (this.buffer, 0x20);
		
		this.patterns = new ESXPattern [256];
		
		for (int i = 0; i < 256; i++)
		{
			this.patterns [i] = new ESXPattern (this.buffer, PATTERNS_OFFSET + (i * ESXPattern.BUFFER_SIZE));
		}
		
		this.monoSampleHeaders = new ESXMonoSampleHeader [256];

		for (int i = 0; i < 256; i++)
		{
			this.monoSampleHeaders [i] = new ESXMonoSampleHeader
				(this.buffer, MONO_SAMPLE_HEADERS_OFFSET + (i * ESXMonoSampleHeader.BUFFER_SIZE));
		}
		
		
	}
	
	// PUBLIC METHODS
	
	public void
	dump ()
	{
		System.err.println ("number of mono samples = " + getInteger (NUM_MONO_SAMPLES_OFFSET));
		System.err.println ("number of stereo samples = " + getInteger (NUM_STEREO_SAMPLES_OFFSET));
		
		this.patterns [0].dump ();
		this.monoSampleHeaders [0].dump ();
	}
	
	// PRIVATE METHODS
	
	private void
	checkBuffer ()
	throws Exception
	{
		// we should do more, but do we care?
		if (buffer [0] != (byte) 'K')
			throw new Exception ("bad header");
	}
	
	// PRIVATE CONSTANTS
	
	private static final int
	GLOBAL_PARAMETERS_OFFSET = 0x20;
	
	private static final int
	PATTERNS_OFFSET = 0x200;
	
	private static final int
	NUM_MONO_SAMPLES_OFFSET = 0x001b0020;
	
	private static final int
	NUM_STEREO_SAMPLES_OFFSET = 0x001b0024;
	
	private static final int
	MONO_SAMPLE_HEADERS_OFFSET = 0x001b0100;
	
	private static final int
	STEREO_SAMPLE_HEADERS_OFFSET = 0x001b2900;
	
	private static final int
	SAMPLE_DATA_OFFSET = 0x00250000;
	
	// PRIVATE DATA

	private ESXGlobalParameters
	globalParameters = null;
	
	private ESXPattern
	patterns [] = null;
	
	private ESXMonoSampleHeader
	monoSampleHeaders [] = null;
	
}

