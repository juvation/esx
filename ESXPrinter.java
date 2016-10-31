
import java.io.File;

// calls dump() on the file
// expecting helpful output

public class ESXPrinter
{
	public static void
	main (String[] inArgs)
	throws Exception
	{
		ESXFile	file = ESXFile.fromFile (new File (inArgs [0]));
		file.dump ();
	}
	
	// PRIVATE DATA

	// CONSTANTS
	
		
}
