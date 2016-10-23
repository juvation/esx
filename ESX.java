
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
	}
	
	public
	ESX (File inFile)
	throws Exception
	{
		System.err.println ("making buffer of size " + inFile.length ()
			+ " (0x" + Long.toHexString (inFile.length ()) + ")");
		
		byte[]	data = new byte [(int) inFile.length ()];
		
		FileInputStream	fis = new FileInputStream (inFile);
		
		try
		{
			fis.read (data);

			ESXFile	file = new ESXFile (data);
			file.dump ();
		}
		finally
		{
			fis.close ();
		}
	}
	
	// PRIVATE DATA

	// CONSTANTS
	
		
}
