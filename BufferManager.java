

public class BufferManager
{
	public
	BufferManager (byte[] inBuffer, int inOffset)
	{
		this.buffer = inBuffer;
		this.offset = inOffset;
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
	getShort (int inOffset)
	{
		short	result = (short) (this.buffer [this.offset + inOffset] & 0xff);
		result <<= 8;
		result |= (this.buffer [this.offset + inOffset + 1] & 0xff);
		return result;
	}
	
	public int
	getInteger (int inOffset)
	{
		int	result = (this.buffer [this.offset + inOffset] & 0xff);
		result <<= 8;
		result |= (this.buffer [this.offset + inOffset + 1] & 0xff);
		result <<= 8;
		result |= (this.buffer [this.offset + inOffset + 2] & 0xff);
		result <<= 8;
		result |= (this.buffer [this.offset + inOffset + 3] & 0xff);
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
	
	// PRIVATE DATA
	
	protected byte[]
	buffer = null;
	
	protected int
	offset = 0;

}

	