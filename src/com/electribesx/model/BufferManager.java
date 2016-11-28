package com.electribesx.model;

import java.io.EOFException;
import java.io.InputStream;

public class BufferManager
{
	// these don't belong here but tbh i didn't know where else to put them...
	
	// STATIC PUBLIC METHODS
	
	public static int
	read2ByteInteger (InputStream inStream)
	throws Exception
	{
		int	value = inStream.read ();
		value |= inStream.read () << 8;
		
		return value;
	}

	public static int
	read4ByteInteger (InputStream inStream)
	throws Exception
	{
		int	value = inStream.read ();
		value |= inStream.read () << 8;
		value |= inStream.read () << 16;
		value |= inStream.read () << 24;
		
		return value;
	}

	public static String
	read4ByteLiteral (InputStream inStream)
	throws Exception
	{
		byte[]	buffer = new byte [4];
		int	cc = inStream.read (buffer);
		if (cc < 1)
		{
			throw new EOFException ();
		}
		return new String (buffer, 0, 4);
	}
	
	// PUBLIC CONSTRUCTOR

	public
	BufferManager (byte[] inBuffer, int inOffset)
	{
		this.buffer = inBuffer;
		this.offset = inOffset;
	}

	// PUBLIC METHODS
	
	public void
	copyIn (byte[] inBuffer, int inOffset, int outOffset, int inLength)
	{
		System.arraycopy (inBuffer, inOffset, this.buffer, outOffset, inLength);
	}

	public void
	copyOut (int inOffset, byte[] outBuffer, int outOffset, int inLength)
	{
		System.arraycopy (this.buffer, inOffset, outBuffer, outOffset, inLength);
	}
	
	public byte
	getByte (int inOffset)
	{
		return this.buffer [this.offset + inOffset];
	}
	
	public byte[]
	getBytes (int inOffset, int inLength)
	{
		byte[]	result = new byte [inLength];
		
		for (int i = 0; i < inLength; i++)
		{
			result [i] = this.buffer [this.offset + inOffset + i];
		}
		
		return result;
	}
	
	public short
	getBigEndian16 (int inOffset)
	{
		return (short) getBigEndianInteger (inOffset, 2);
	}
	
	public int
	getBigEndian32 (int inOffset)
	{
		return getBigEndianInteger (inOffset, 4);
	}

	public int
	getBigEndianInteger (int inOffset, int inBytes)
	{
		int	result = 0;
		
		for (int i = 0; i < inBytes; i++)
		{
			result <<= 8;
			
			// don't chop the first one at 8 bits as we want it to sign-extend
			if (i == 0)
			{
				result = this.buffer [this.offset + inOffset + i];
			}
			else
			{
				result |= (this.buffer [this.offset + inOffset + i] & 0xff);
			}
		}
		
		return result;
	}

	public String
	getLiteral32 (int inOffset)
	{
		return new String (this.buffer, inOffset, 4);
	}
	
	public short
	getLittleEndian16 (int inOffset)
	{
		return (short) getLittleEndianInteger (inOffset, 2);
	}
	
	public int
	getLittleEndian32 (int inOffset)
	{
		return getLittleEndianInteger (inOffset, 4);
	}
	
	public int
	getLittleEndianInteger (int inOffset, int inBytes)
	{
		int	result = 0;
		
		for (int i = inBytes - 1; i >= 0; i--)
		{
			result <<= 8;
			
			// don't chop the first one at 8 bits as we want it to sign-extend
			if (i == (inBytes - 1))
			{
				result = this.buffer [this.offset + inOffset + i];
			}
			else
			{
				result |= (this.buffer [this.offset + inOffset + i] & 0xff);
			}
		}
		
		// sign extend properly
		if (inBytes < 4)
		{
			
		}
		
		return result;
	}

	public String
	getString (int inOffset, int inLength)
	{
		StringBuilder	sb = new StringBuilder ();
		
		for (int i = 0; i < inLength; i++)
		{
			byte	b = getByte (inOffset + i);
			
			if (b == 0)
			{
				break;
			}
			
			char	ch = (char) (b & 0xff);
			sb.append (ch);
		}
		
		return sb.toString ();
	}
	
	public void
	setBigEndian16 (int inOffset, int inValue)
	{
		this.buffer [this.offset + inOffset + 0] = (byte) ((inValue >> 8) & 0xff);
		this.buffer [this.offset + inOffset + 1] = (byte) ((inValue >> 0) & 0xff);
	}

	public void
	setBigEndian32 (int inOffset, int inValue)
	{
		this.buffer [this.offset + inOffset + 0] = (byte) ((inValue >> 24) & 0xff);
		this.buffer [this.offset + inOffset + 1] = (byte) ((inValue >> 16) & 0xff);
		this.buffer [this.offset + inOffset + 2] = (byte) ((inValue >> 8) & 0xff);
		this.buffer [this.offset + inOffset + 3] = (byte) ((inValue >> 0) & 0xff);
	}
	
	public void
	setByte (int inOffset, byte inValue)
	{
		this.buffer [this.offset + inOffset] = inValue;
	}
	
	public void
	setString (int inOffset, String inString, int inMaxLength, char inFill)
	{
		// this isn't guaranteed to do the right thing for 8-bit ESX characters btw
		byte[]	stringBytes = null;
		
		try
		{
			stringBytes = inString.getBytes ("UTF-8");
		}
		catch (Throwable inThrowable)
		{
			stringBytes = inString.getBytes ();
		}
		
		for (int i = 0; i < inMaxLength; i++)
		{
			if (i < stringBytes.length)
			{
				this.buffer [this.offset + i] = stringBytes [i];
			}
			else
			{
				this.buffer [this.offset + i] = (byte) inFill;
			}
		}
	}
	
	// PRIVATE DATA
	
	protected byte[]
	buffer = null;
	
	protected int
	offset = 0;

}

	