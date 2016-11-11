// WavFile.java

package com.electribesx.model;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;

public class WavFile
{
	// the boolean allows the reading of just the metadata
	public
	WavFile (File inFile, boolean inReadData)
	throws Exception
	{
		FileInputStream	fis = new FileInputStream (inFile);
		
		try
		{
			byte[]	temp = new byte [12];
			int	cc = fis.read (temp, 0, 12);
			
			if (cc < 12)
			{
				throw new Exception ("cannot read initial RIFF block");
			}
			
			BufferManager	bm = new BufferManager (temp, 0);
			
			String	riff = bm.getLiteral32 (0);
			
			if (! riff.equals ("RIFF"))
			{
				throw new Exception ("wav with no RIFF literal");
			}
			
			// file size
			bm.getLittleEndian32 (4);

			String	wave = bm.getLiteral32 (8);
			
			if (! wave.equals ("WAVE"))
			{
				throw new Exception ("wav is not WAVE format");
			}

			while (fis.available () > 0)
			{
				cc = fis.read (temp, 0, 8);
				
				if (cc < 8)
				{
					throw new Exception ("cannot read subchunk meta block");
				}

				bm = new BufferManager (temp, 0);
				
				String	subChunkType = bm.getLiteral32 (0);
				int	subChunkSize = bm.getLittleEndian32 (4);

				if (subChunkType.equalsIgnoreCase ("data"))
				{
					if (! inReadData)
					{
						fis.skip (subChunkSize);
						continue;
					}
				}
				
				byte[]	subChunkBuffer = new byte [subChunkSize];
				cc = fis.read (subChunkBuffer);
							
				if (cc < subChunkBuffer.length)
				{
					throw new Exception ("cannot read subchunk data");
				}

				this.chunks.put (subChunkType, subChunkBuffer);

				bm = new BufferManager (subChunkBuffer, 0);
				
				if (subChunkType.equalsIgnoreCase ("fmt "))
				{
					this.audioFormat = bm.getLittleEndian16 (0);
					this.numChannels = bm.getLittleEndian16 (2);
					this.sampleRate = bm.getLittleEndian32 (4);
					this.byteRate = bm.getLittleEndian32 (8);
					this.blockAlign = bm.getLittleEndian16 (12);
					this.bitsPerSample = bm.getLittleEndian16 (14);
				}
				else
				if (subChunkType.equalsIgnoreCase ("data"))
				{
					this.numFrames = subChunkSize / this.blockAlign;
				}
				else
				if (subChunkType.equalsIgnoreCase ("smpl"))
				{
					this.midiUnityNote = bm.getLittleEndian32 (12);
					this.numLoops = bm.getLittleEndian32 (28);
					this.sampleDataSize = bm.getLittleEndian32 (32);
					
					// only grab the first loop
					if (this.numLoops > 0)
					{
						this.loopStart = bm.getLittleEndian32 (36 + 8);
						this.loopEnd = bm.getLittleEndian32 (36 + 12);
					}
				}
				else
				{
					// System.err.println ("unrecognised subchunk type " + subChunkType);
				}
			}			
		}
		finally
		{
			fis.close ();
		}
	}

	// PUBLIC METHODS
	
	public int
	getBitsPerSample ()
	{
		return this.bitsPerSample;
	}

	public int
	getBlockAlign ()
	{
		return this.blockAlign;
	}

	public byte[]
	getChunk (String inName)
	{
		return this.chunks.get (inName);
	}
	
	public int
	getLoopStart ()
	{
		return this.loopStart;
	}

	public int
	getNumFrames ()
	{
		return this.numFrames;
	}
	
	public int
	getNumLoops ()
	{
		return this.numLoops;
	}
	
	public int
	getSampleRate ()
	{
		return this.sampleRate;
	}
	
	// PRIVATE METHODS
	
	// PRIVATE DATA
	
	private int
	audioFormat = 0;
	
	private int
	bitsPerSample = 0;
	
	private int
	blockAlign = 0;
	
	private int
	byteRate = 0;
	
	private int
	loopStart = 0;

	private int
	loopEnd = 0;

	private int
	midiUnityNote = 0;

	private int
	numChannels = 0;

	private int
	numLoops = 0;

	private int
	numFrames = 0;
	
	private int
	sampleDataSize = 0;

	private int
	sampleRate = 0;
	
	private Map<String, byte[]>
	chunks = new HashMap<String, byte[]> ();
	
}
