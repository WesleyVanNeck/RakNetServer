package network.ycc.raknet;

import io.netty.channel.ChannelConfig;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.util.AttributeKey;

public class RakNet {

    public static final AttributeKey<Boolean> WRITABLE = AttributeKey.valueOf("RN_WRITABLE");
    public static final ChannelOption<Long> SERVER_ID = ChannelOption.valueOf("RN_SERVER_ID");
    public static final ChannelOption<MetricsLogger> METRICS = ChannelOption.valueOf("RN_METRICS");
    public static final ChannelOption<Integer> USER_DATA_ID = ChannelOption.valueOf("RN_USER_DATA_ID");
    public static final ChannelOption<Integer> MTU = ChannelOption.valueOf("RN_MTU");
    public static final ChannelOption<Long> RTT = ChannelOption.valueOf("RN_RTT");

    public static final Config config(ChannelHandlerContext ctx) {
        return (Config) ctx.channel().config();
    }

    public static final MetricsLogger metrics(ChannelHandlerContext ctx) {
        return config(ctx).getMetrics();
    }

    public interface MetricsLogger {
        MetricsLogger DEFAULT = new MetricsLogger() {};

        default void packetsIn(int delta) {}
        default void framesIn(int delta) {}
        default void bytesIn(int delta) {}
        default void packetsOut(int delta) {}
        default void framesOut(int delta) {}
        default void bytesOut(int delta) {}
        default void bytesRecalled(int delta) {}
        default void bytesACKd(int delta) {}
        default void bytesNACKd(int delta) {}
        default void acksSent(int delta) {}
        default void nacksSent(int delta) {}
        default void measureRTTns(long n) {}
        default void measureRTTnsStdDev(long n) {}
        default void measureBurstTokens(int n) {}
    }

    public interface Config extends ChannelConfig {
        int DEFAULT_MAX_PENDING_FRAME_SETS = 1024;
        int DEFAULT_DEFAULT_PENDING_FRAME_SETS = 64;

        MetricsLogger getMetrics();
        void setMetrics(MetricsLogger metrics);
        long getServerId();
        void setServerId(long serverId);
        int getUserDataId();
        void setUserDataId(int userDataId);
        int getMTU();
        void setMTU(int mtu);
        long getRetryDelay();
        void setRetryDelay(long retryDelay);
        long getRTT();
        void setRTT(long rtt);
        long getRTTStdDev();
        void updateRTT(long rttSample);
        int getMaxPendingFrameSets();
        void setMaxPendingFrameSets(int maxPendingFrameSets);
        int getDefaultPendingFrameSets();
        void setDefaultPendingFrameSets(int defaultPendingFrameSets);
    }
}
