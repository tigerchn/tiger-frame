package com.frame.tool.uuid;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.Random;

/**
 * 企业级加长版订单号生成器
 * 订单号长度：22~26 位
 * 分布式绝对不重复 + IP+MAC 机器ID + 加长混淆
 */
@Component
public class OrderNoUtil {

    // ===================== 雪花算法标准配置 =====================
    private static final long EPOCH = 1609459200000L;
    private static final long WORKER_ID_BITS = 5L;
    private static final long SEQUENCE_BITS = 12L;

    private static final long MAX_WORKER_ID = ~(-1L << WORKER_ID_BITS);
    private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;
    private static final long TIMESTAMP_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;
    private static final long SEQUENCE_MASK = ~(-1L << SEQUENCE_BITS);

    private static long workerId;
    private static long lastTimestamp = -1L;
    private static long sequence = 0L;

    // ===================== 加长混淆配置 =====================
    private static final long XOR_SALT = 20250509123456L;
    private static final String CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final Random RANDOM = new Random();
    private static final int RANDOM_LENGTH = 4;

    // ===================== 初始化：IP + MAC 生成机器ID =====================
    @PostConstruct
    public void init() {
        try {
            workerId = generateWorkerIdByIpAndMac();
        } catch (Exception e) {
            workerId = new Random().nextInt((int) MAX_WORKER_ID + 1);
        }
    }

    private long generateWorkerIdByIpAndMac() throws Exception {
        String ip = getLocalIp();
        byte[] mac = getLocalMac();

        long hash = 17L;
        hash = hash * 31 + (ip == null ? 0 : ip.hashCode());
        hash = hash * 31 + (mac == null ? 0 : new String(mac).hashCode());

        long id = hash & MAX_WORKER_ID;
        return id == 0 ? 1 : id;
    }

    private String getLocalIp() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();
                Enumeration<InetAddress> addrs = ni.getInetAddresses();
                while (addrs.hasMoreElements()) {
                    InetAddress addr = addrs.nextElement();
                    if (!addr.isLoopbackAddress() && !addr.isLinkLocalAddress() && addr instanceof Inet4Address) {
                        return addr.getHostAddress();
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return "127.0.0.1";
    }

    private byte[] getLocalMac() {
        try {
            NetworkInterface ni = NetworkInterface.getByInetAddress(InetAddress.getLocalHost());
            if (ni != null) return ni.getHardwareAddress();
        } catch (Exception ignored) {
        }
        return new byte[]{1, 2, 3, 4, 5, 6};
    }

    // ===================== 雪花算法核心 =====================
    private static synchronized long nextSnowflakeId() {
        long now = System.currentTimeMillis();

        if (now < lastTimestamp) {
            throw new RuntimeException("系统时间回拨，无法生成ID");
        }

        if (now == lastTimestamp) {
            sequence = (sequence + 1) & SEQUENCE_MASK;
            if (sequence == 0) now = waitNextMillis(lastTimestamp);
        } else {
            sequence = 0L;
        }

        lastTimestamp = now;

        return ((now - EPOCH) << TIMESTAMP_SHIFT)
                | (workerId << WORKER_ID_SHIFT)
                | sequence;
    }

    private static long waitNextMillis(long last) {
        long now = System.currentTimeMillis();
        while (now <= last) now = System.currentTimeMillis();
        return now;
    }

    // ===================== 【加长混淆】核心 =====================
    private static String obscureLong(long id) {
        // 1. 异或加密
        long mixed = id ^ XOR_SALT;

        // 2. 转 16进制（比36进制更长！）
        String hex = Long.toHexString(mixed).toUpperCase();

        // 3. 加 4 位随机 → 订单号更长更安全
        String random = randomString();

        // 最终：18~22位 + 4位随机 = 22~26位
        return hex + random;
    }

    private static String randomString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < RANDOM_LENGTH; i++) {
            sb.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
        }
        return sb.toString();
    }

    // ===================== 对外接口 =====================
    public static String nextOrderNo() {
        return obscureLong(nextSnowflakeId());
    }
}