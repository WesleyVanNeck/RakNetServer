package raknetserver.pipeline.encapsulated;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.CompositeByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.MessageToMessageDecoder;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import raknetserver.packet.EncapsulatedPacket;
import raknetserver.packet.internal.InternalPacketData;
import raknetserver.utils.Constants;

public class EncapsulatedPacketUnsplitter extends MessageToMessageDecoder<EncapsulatedPacket> {

	//TODO: limits checks

	//TODO: this is also the reliability layer for real...

	protected final Int2ObjectOpenHashMap<Defragmenter> pendingPackets = new Int2ObjectOpenHashMap<>();

	@Override
	public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
		super.handlerRemoved(ctx);
		pendingPackets.values().forEach(unsplit -> unsplit.release());
		pendingPackets.clear();
	}

	@Override
	protected void decode(ChannelHandlerContext ctx, EncapsulatedPacket packet, List<Object> list) {
		if (!packet.hasSplit()) {
			list.add(packet.retain());
		} else {
			final int splitID = packet.getSplitId();
			final Defragmenter partial = pendingPackets.get(splitID);
			if (partial == null) {
				if (packet.getSplitCount() > Constants.MAX_PACKET_LOSS) {
					throw new DecoderException("Too big packet loss (resend queue)");
				}
				pendingPackets.put(splitID, Defragmenter.create(ctx.alloc(), packet));
			} else {
				partial.add(packet);
				if (partial.isDone()) {
					pendingPackets.remove(splitID);
					list.add(partial.finish());
				}
			}
		}
	}

	protected static class Defragmenter {

		protected final Int2ObjectOpenHashMap<ByteBuf> queue = new Int2ObjectOpenHashMap<>(8);
		protected EncapsulatedPacket samplePacket;
		protected CompositeByteBuf data;
		protected int splitIdx;
		protected int orderId;
		protected InternalPacketData.Reliability reliability;

		protected static Defragmenter create(ByteBufAllocator alloc, EncapsulatedPacket packet) {
			final Defragmenter out = new Defragmenter();
			out.init(alloc, packet);
			return out;
		}

		void init(ByteBufAllocator alloc, EncapsulatedPacket packet) {
			assert data == null;
			splitIdx = 0;
			data = alloc.compositeDirectBuffer(packet.getSplitCount());
			orderId = packet.getOrderChannel();
			reliability = packet.getReliability();
			samplePacket = packet.retain();
			add(packet);
		}

		void add(EncapsulatedPacket packet) {
			assert packet.getReliability().equals(samplePacket.getReliability());
			assert packet.getOrderChannel() == samplePacket.getOrderChannel();
			assert packet.getOrderIndex() == samplePacket.getOrderIndex();
			if (!queue.containsKey(packet.getSplitIndex()) && packet.getSplitIndex() >= splitIdx) {
				queue.put(packet.getSplitIndex(), packet.retainedFragmentData());
				update();
			}
		}

		void update() {
			ByteBuf fragment;
			while((fragment = queue.remove(splitIdx)) != null) {
				data.addComponent(true, fragment);
				splitIdx++;
			}
		}

		EncapsulatedPacket finish() {
			assert isDone();
			assert queue.isEmpty();
			try {
				return samplePacket.completeFragment(data.consolidate());
			} finally {
				release();
			}
		}

		boolean isDone() {
			assert samplePacket.getSplitCount() >= splitIdx;
			return samplePacket.getSplitCount() == splitIdx;
		}

		void release() {
			if (data != null) {
				data.release();
				data = null;
			}
			if (samplePacket != null) {
				samplePacket.release();
				samplePacket = null;
			}
			queue.values().forEach(buf -> buf.release());
			queue.clear();
		}

	}

}
