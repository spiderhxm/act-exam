package org.activiti.examples.cnfig;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.net.util.IPAddressUtil;

public class NetWorkUtil {
    private static final Logger logger = LoggerFactory.getLogger(NetWorkUtil.class);

    public NetWorkUtil() {
    }

    public static String getLANIP(String defaultIP) {
        InetAddress inetAddress = getLANIP();
        return inetAddress == null ? defaultIP : inetAddress.getHostAddress();
    }

    public static InetAddress getLANIP() {
        try {
            Enumeration netInterfaces = NetworkInterface.getNetworkInterfaces();

            while(true) {
                NetworkInterface ni;
                Enumeration ips;
                do {
                    do {
                        do {
                            do {
                                if (!netInterfaces.hasMoreElements()) {
                                    return null;
                                }

                                ni = (NetworkInterface)netInterfaces.nextElement();
                                ips = ni.getInetAddresses();
                            } while(ni.isLoopback());
                        } while(!ni.isUp());
                    } while(ni.isVirtual());
                } while(ni.isPointToPoint());

                while(ips.hasMoreElements()) {
                    InetAddress inetAddress = (InetAddress)ips.nextElement();
                    String hostAddress = inetAddress.getHostAddress();
                    if (!IPAddressUtil.isIPv6LiteralAddress(hostAddress)) {
                        return inetAddress;
                    }
                }
            }
        } catch (SocketException var5) {
            logger.error(var5.getMessage());
            return null;
        }
    }

    public static long getLocalMac(InetAddress inetAddress) {
        try {
            byte[] mac = NetworkInterface.getByInetAddress(inetAddress).getHardwareAddress();
            long binMac = 0L;

            for(int i = 0; i < mac.length; ++i) {
                binMac = binMac << 8 | (long)(mac[i] & 255);
            }

            return binMac;
        } catch (SocketException var5) {
            throw new RuntimeException("Can not get MAC address", var5);
        }
    }
}