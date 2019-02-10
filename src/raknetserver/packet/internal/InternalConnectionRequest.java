package raknetserver.packet.internal;

import io.netty.buffer.ByteBuf;

public class InternalConnectionRequest extends AbstractInternalPacket {

	protected long timestamp;
	protected Reliability reliability = Reliability.RELIABLE;

	@Override
	public void decode(ByteBuf buf) {
		buf.skipBytes(8); //client id
		timestamp = buf.readLong();
		buf.skipBytes(1); //use security
	}

	@Override
	public void encode(ByteBuf buf) {
		throw new UnsupportedOperationException();
	}

	public long getTimeStamp() {
		return timestamp;
	}

}
