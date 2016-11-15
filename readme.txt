Electribe SX Command Line Tools README
--------------------------------------

Intro

The Electribe SX is a wicked box but it was made before USB was generally available on such instruments. Hence, loading samples requires copying them to a card, and even then, the Electribe only allows loading them singly. So if you have, say, a load of nice vintage drum machine samples, it’s a laborious process to get them all where you want them.

Hence this collection of tools, which make bulk sample loading and a few other things a lot easier. The general idea is that you get yourself a card for your Electribe and a reader for your computer, then use these tools to generate “all” files, which are then copied to the card and loaded into your Electribe.

Command Line Tools?!?

Yes, sorry, no GUI here. For this kind of job, I prefer having driver files in which I can copy and paste, etc, instead of having to make 300 trips through a file dialog to locate samples. It’s easy, trust me, and much faster than the other way.

Facilities

These tools essentially allow you to import Wave files into your Electribe via its “all” file format. This file contains the entire memory of the Electribe, including patterns, songs, samples, slices, everything. So if we have a tool which understands the format of that file, we can generate files with that format outside the Electribe, then load them in. The net effect is the same as using the Electribe to do it, but in the case of loading samples, in the blink of an eye instead of all day.

Usually you might start with the “empty.esx” file included in the distro, which has nothing in it. You would then write a file which told the tool which samples to put where, and optionally to assign various samples to parts in patterns. Then the tool loads the “empty.esx”, reads the driver file for instructions, then generates a new “out.esx” file which can then be loaded into the Electribe.

Tutorial

