package raknetserver.utils;

import io.netty.handler.codec.DecoderException;

import java.net.InetSocketAddress;

public class Constants {

	public static final int BACK_PRESSURE_HIGH_WATERMARK = Integer.parseInt(System.getProperty("raknetserver.backPressureHighWatermark", "2048"));
	public static final int BACK_PRESSURE_LOW_WATERMARK = Integer.parseInt(System.getProperty("raknetserver.backPressureLowWatermark", "1024"));
	public static final int MAX_PENDING_FRAME_SETS = Integer.parseInt(System.getProperty("raknetserver.maxPendingFrameSets", "1024"));
	public static final int DEFAULT_PENDING_FRAME_SETS = Integer.parseInt(System.getProperty("raknetserver.defaultPendingFrameSets", "64"));
	public static final int MAX_PACKET_LOSS = Integer.parseInt(System.getProperty("raknetserver.maxPacketLoss", "8192"));
	public static final int UDP_IO_THREADS = Integer.parseInt(System.getProperty("raknetserver.udpIOThreads", "4"));
	public static final int RETRY_TICK_OFFSET = Integer.parseInt(System.getProperty("raknetserver.retryTickOffset", "2"));

    public static final byte[] MAGIC = new byte[] { (byte) 0x00, (byte) 0xff, (byte) 0xff, (byte) 0x00, (byte) 0xfe, (byte) 0xfe, (byte) 0xfe, (byte) 0xfe, (byte) 0xfd, (byte) 0xfd, (byte) 0xfd, (byte) 0xfd, (byte) 0x12, (byte) 0x34, (byte) 0x56, (byte) 0x78 };
    public static final long SERVER_ID = 0x0000000012345678L;
    public static final InetSocketAddress NULL_ADDR = new InetSocketAddress(0);

	public static void packetLossCheck(int n, String location) {
        if (n > Constants.MAX_PACKET_LOSS) {
            throw new DecoderException("Too big packet loss: " + location);
        }
    }

}
