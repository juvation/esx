
import java.io.File;

// POWER TOOL for reading and generating ESX files haha

public class ESX
{
	public static void
	main (String[] inArgs)
	throws Exception
	{
		ESXFile	file = ESXFile.fromFile (new File (inArgs [0]));

		file.getMonoSample (0).initFromFile (new File ("BD_1.WAV"));
		file.getMonoSample (0).setName ("BD_1    ");
		
/*
		file.getMonoSample (0).initFromFile (new File ("909/909_BD.wav"));
		file.getMonoSample (0).setName ("909_BD");

		file.getMonoSample (1).initFromFile (new File ("909/909_SD.wav"));
		file.getMonoSample (1).setName ("909_SD");

		file.getMonoSample (2).initFromFile (new File ("909/909_CH.wav"));
		file.getMonoSample (2).setName ("909_CH");

		file.getMonoSample (3).initFromFile (new File ("909/909_OH.wav"));
		file.getMonoSample (3).setName ("909_OH");

		file.getStereoSample (0).initFromFile (new File ("stereo.wav"));
		file.getStereoSample (0).setName ("STEREOPN");
*/

/*
		ESXPattern	pattern = file.getPattern (0);

		// kick pattern
		ESXDrumPart	part = pattern.getDrumPart (0);
		byte[]	sequence = part.getSequence ();
		
		sequence [0] = 0;
		sequence [1] = 0;
		sequence [2] = 0;
		sequence [3] = 0;
		
		sequence [0] = (1 << 0) | (1 << 4);
		sequence [1] = (1 << 0) | (1 << 4);

		// hh pattern
		part = pattern.getDrumPart (7);
		sequence = part.getSequence ();

		sequence [0] = 0xff;
		sequence [1] = 0xff;
*/
		
		File	newFile = new File ("out.esx");
		file.write (newFile);
	}
	
	// PRIVATE DATA

	// CONSTANTS
	
		
}
