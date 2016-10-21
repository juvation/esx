
import java.io.File;
import java.io.FileInputStream;

// POWER TOOL for reading and generating ESX files haha


public class ESX
{
	public static void
	main (String[] inArgs)
	throws Exception
	{
		ESX	esx = new ESX (new File (inArgs [0]));
		
		System.err.println ("number of mono samples used = " + esx.getNumMonoSamples ());
		System.err.println ("number of stereo samples used = " + esx.getNumStereoSamples ());
	}
	
	public
	ESX (File inFile)
	throws Exception
	{
		System.err.println ("making buffer of size " + inFile.length ());
		
		this.data = new byte [(int) inFile.length ()];
		
		FileInputStream	fis = new FileInputStream (inFile);
		
		try
		{
			fis.read (this.data);
		}
		finally
		{
			fis.close ();
		}
	}
	
	public int
	getNumMonoSamples ()
	{
		return this.data [kNumMonoSamplesOffset] << 8 | this.data [kNumMonoSamplesOffset];
	}

	public int
	getNumStereoSamples ()
	{
		return this.data [kNumStereoSamplesOffset] << 8 | this.data [kNumStereoSamplesOffset];
	}
	
	// PRIVATE DATA
	
	private byte[]
	data = null;
	
	// CONSTANTS
	
	// in ascending offset order
	
	private static int
	kPatternOffset = 0x200;
	
	private static int
	kSongOffset = 0x00130000;
	
	private static int
	kSampleOffset = 0x001B0000;

	private static int
	kNumMonoSamplesOffset = 0x001B0020;

	private static int
	kNumStereoSamplesOffset = 0x001B0024;

	private static int
	kCurrentSampleOffset = 0x001B0028;

	// 256 mono sample headers at 40 bytes each
	private static int
	kMonoSamplesOffset = 0x001B0100;
	
	// 128 stereo sample headers at 44 bytes each
	private static int
	kStereoSamplesOffset = 0x001B0100;
	
	// 256 slices at 2048 bytes each
	private static int
	kSliceDataOffset = 0x001B4200;
	
	private static int
	kSampleDataOffset = 0x00250000;
		
}
