import java.net.*;
import java.io.*;

/**
 * 网络连接诊断工具
 * 用于快速测试服务器连接问题
 */
public class NetworkTest {
    
    public static void main(String[] args) {
        String host = "112.124.44.144";
        int port = 8888;
        
        System.out.println("========================================");
        System.out.println("网络连接诊断工具");
        System.out.println("========================================");
        System.out.println("目标服务器: " + host);
        System.out.println("目标端口: " + port);
        System.out.println("========================================\n");
        
        // 测试1: DNS解析
        testDNS(host);
        
        // 测试2: Ping测试
        testPing(host);
        
        // 测试3: TCP连接测试
        testTCP(host, port);
        
        // 测试4: 查看本地网络信息
        showLocalNetworkInfo();
        
        System.out.println("\n========================================");
        System.out.println("诊断完成");
        System.out.println("========================================");
    }
    
    private static void testDNS(String host) {
        System.out.println("【测试1】DNS解析测试");
        try {
            InetAddress addr = InetAddress.getByName(host);
            System.out.println("  ✓ DNS解析成功");
            System.out.println("  - IP地址: " + addr.getHostAddress());
            System.out.println("  - 主机名: " + addr.getHostName());
        } catch (UnknownHostException e) {
            System.out.println("  ✗ DNS解析失败: " + e.getMessage());
        }
        System.out.println();
    }
    
    private static void testPing(String host) {
        System.out.println("【测试2】Ping测试 (ICMP, 5秒超时)");
        try {
            InetAddress addr = InetAddress.getByName(host);
            long startTime = System.currentTimeMillis();
            boolean reachable = addr.isReachable(5000);
            long endTime = System.currentTimeMillis();
            
            if (reachable) {
                System.out.println("  ✓ 主机可达 (耗时: " + (endTime - startTime) + "ms)");
            } else {
                System.out.println("  ✗ 主机不可达 (可能防火墙阻止ICMP)");
                System.out.println("  注意: Ping不通不代表TCP连接会失败");
            }
        } catch (Exception e) {
            System.out.println("  ✗ Ping测试失败: " + e.getMessage());
        }
        System.out.println();
    }
    
    private static void testTCP(String host, int port) {
        System.out.println("【测试3】TCP连接测试");
        Socket socket = null;
        try {
            System.out.println("  正在连接 " + host + ":" + port + " ...");
            socket = new Socket();
            
            long startTime = System.currentTimeMillis();
            socket.connect(new InetSocketAddress(host, port), 10000);
            long endTime = System.currentTimeMillis();
            
            System.out.println("  ✓ TCP连接成功! (耗时: " + (endTime - startTime) + "ms)");
            System.out.println("  - 本地地址: " + socket.getLocalAddress() + ":" + socket.getLocalPort());
            System.out.println("  - 远程地址: " + socket.getRemoteSocketAddress());
            
        } catch (ConnectException e) {
            System.out.println("  ✗ 连接被拒绝");
            System.out.println("  原因: " + e.getMessage());
            System.out.println("  可能的问题:");
            System.out.println("    1. 服务器未启动");
            System.out.println("    2. 服务器未在 " + port + " 端口监听");
            System.out.println("    3. 防火墙阻止了连接");
            System.out.println("    4. 服务器只监听localhost，未绑定0.0.0.0");
        } catch (SocketTimeoutException e) {
            System.out.println("  ✗ 连接超时 (10秒)");
            System.out.println("  可能的问题:");
            System.out.println("    1. 网络不可达");
            System.out.println("    2. 防火墙丢弃了数据包");
            System.out.println("    3. 路由问题");
        } catch (Exception e) {
            System.out.println("  ✗ 连接失败: " + e.getClass().getSimpleName());
            System.out.println("  错误: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
        System.out.println();
    }
    
    private static void showLocalNetworkInfo() {
        System.out.println("【测试4】本地网络信息");
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            System.out.println("  本地主机名: " + localHost.getHostName());
            System.out.println("  本地IP地址: " + localHost.getHostAddress());
            
            System.out.println("  所有网络接口:");
            java.util.Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();
                if (ni.isUp() && !ni.isLoopback()) {
                    System.out.println("    - " + ni.getDisplayName());
                    java.util.Enumeration<InetAddress> addresses = ni.getInetAddresses();
                    while (addresses.hasMoreElements()) {
                        InetAddress addr = addresses.nextElement();
                        if (addr instanceof Inet4Address) {
                            System.out.println("      IPv4: " + addr.getHostAddress());
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("  ✗ 获取本地网络信息失败: " + e.getMessage());
        }
        System.out.println();
    }
}
