Electribe SX Command Line Tools README
--------------------------------------

LEGAL

This software is copyright 2016 Jason Proctor. I’m not responsible for any loss of content in your Electribe which results from using these tools. Please save all content in your Electribe before going any further.

ACKNOWLEDGEMENTS

Thanks to Jeff @skratchdot for his Open Electribe Editor (previously ESX Wave Organizer) without the source to which, this tool would not exist - or at least it would have taken much longer to develop, for sure.

INTRO

The Electribe SX is a wicked box but it was made before USB was generally available on such instruments. Hence, loading samples requires copying them to a card, and even then, the Electribe only allows loading them singly. So if you have, say, a load of nice vintage drum machine samples, it’s a laborious process to get them all where you want them.

Hence this collection of tools, which make bulk sample loading and a few other things a lot easier. The general idea is that you get yourself a card for your Electribe and a reader for your computer, then use these tools to generate ESX format files, which are then copied to the card and loaded into your Electribe.

COMMAND LINE TOOLS?!?

Yes, sorry, no GUI here. For this kind of job, I prefer having driver files in which I can copy and paste, etc, instead of having to make 300 trips through a file dialog to locate samples. It’s easy, trust me, and much faster than the other way.

The tools are written in Java, so you’ll need at a Java Virtual Machine of version 1.6 or later installed. If you can’t run “java” from the command line, then you’ll need to fix that before the tools will work.

FACILITIES

These tools essentially allow you to import Wave files into your Electribe via its .esx file format. This file contains the entire memory of the Electribe, including patterns, songs, samples, slices, everything. So if we have a tool which understands the format of that file, we can generate files with that format outside the Electribe, then load them in. The net effect is the same as using the Electribe to do it, but in the case of loading samples, in the blink of an eye instead of all day.

Usually you might start with the “empty.esx” ESX file included in the distro, which has nothing in it. You would then write a driver file which tells the tool which samples to put where, and optionally to assign various samples to parts in patterns, etc. Then the tool loads the “empty.esx”, reads the driver file for instructions, then generates a new ESX file which can then be loaded into the Electribe.

WARNING!

Please note that loading the files made by these tools into your Electribe will erase everything that is currently there. So, before messing with this stuff, please save the current contents of your Electribe as an ESX file (save all in the save options screen) to a card before continuing.

LOADING SAMPLES INTO AN ESX FILE

Let’s do an easy one to start with. The distribution contains some TR909 samples and a driver file to load them into an ESX file. Fire up a Terminal (Mac) or regular shell (Linux) or write yourself an equivalent script (Windows).

Change into the directory containing this file - and, one assumes, the other files from the distribution, like build.sh, electribesx.jar, etc. Type the following command -

build.sh empty.esx tr909.properties tr909.esx

You should see some output which starts like this —

setting mono sample 0 to file samples/TR909/TR909_BD.wav
setting mono sample 1 to file samples/TR909/TR909_SD.wav
setting mono sample 2 to file samples/TR909/TR909_CH.wav
setting mono sample 3 to file samples/TR909/TR909_OH.wav

— and ends like this —

checking end block magic 1
checking end block sample offset
checking end block magic 2
checking end block magic 3
verify OK

The file “tr909.esx” should now be a valid “all” file which can be copied to a card and loaded into your Electribe. Give it a try - first ensuring to save the current contents of your Electribe, of course.

In general, usage of the build command is as follows —

build.sh inputesxfile driverfile outputesxfile

The first parameter is usually the empty ESX file included with the distribution, but this isn’t always the case. If you want to modify the current contents of your Electribe, for example, you’d save an ESX file from there first and then have the tool work on it.

The second parameter is the driver file, which tells the tool which samples to load into which locations. Also, the driver file can contain pattern sample assignments, and even drum part patterns. Of course, the Electribe shines at pattern creation, so you’d usually do this there, but the option is there if you need it.

The third parameter is the output ESX file, which is essentially the input ESX file with all the changes instructed by the driver file. Please note that this file is overwritten by the tool without warning.

MAKING A DRIVER FILE

Creating a driver file for the tool is the most complicated part of the process. Driver files are Java properties files, which means they are essentially lists of key-value pairs, in which the key and value are separated by an equals sign.

As an example, here’s the first line of the TR909 driver file —

monosample.0.file=samples/TR909/TR909_BD.wav

When the tool sees this line, it will import the wave file whose path is specified, and assign it to the first mono sample slot. For comparison, the following line does the same thing for the 100th sample slot —

monosample.100.file=samples/TR909/TR909_BD.wav

By default, the tool will set the sample name to the name of the wave file. But you can also set it to something specific, using a similar line —

monosample.0.name=MY909BD

Similar key/value pairs can be used for stereo samples -

stereosample.0.file=samples/stereopiano.wav
stereosample.0.name=pianothing

The tool will convert mono to stereo and vice versa, and will convert 8 and 24 bit samples to the 16 bit format that the Electribe supports. Most samples can be imported, but if anything goes wrong during the import process, the error messages might less than friendly. Sorry.

MAKING PATTERNS

The tool supports various pattern-oriented operations, such as naming patterns and assigning samples to pattern parts, which are tiresome processes on the Electribe.

Note that unlike samples, the Electribe numbers patterns and parts from one. If you attempt to set anything off pattern zero or part zero in the driver file, the builder tool will show an error. Note also that the tool currently refers to drum parts 1-9 even though the Electribe calls the last four 6A, 6B, 7A and 7B. This might be changed in a future rev of the tool.

The following line names the first pattern “TR909” —

pattern.1.name=TR909

The following line assigns sample 100 to the first drum part of the first pattern —

pattern.1.drumpart.1.sample=100

For comparison, the following line assigns sample 50 to the 4th drum part of pattern 100 —

pattern.100.drumpart.4.sample=50

In addition to naming patterns and assigning samples, it’s also possible to write patterns in the driver file. The Electribe shines at this, so usually you’d just poke buttons on the machine itself, but during the development of the tools I found myself constantly writing patterns to test stuff, and added this facility to reduce iteration time.

The following line writes a four on the floor series of hits and rests for the first drum part of the first pattern —

pattern.0.drumpart.0.pattern=x...x...x...x...

A dot indicates a rest, and anything else indicates a hit. You can include up to 64 steps, the tool will write as many as it finds.


