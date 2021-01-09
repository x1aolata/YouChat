/**
 * @author x1aolata
 * @date 2020/11/25 20:52
 * @script ...
 */
public class Utils {

    public static String getIPFromSocketAddress(String socketAddress) {
        ///192.168.0.102:38518
        int beginIndex = socketAddress.indexOf('/');
        int endIndex = socketAddress.indexOf(':');
        return socketAddress.substring(beginIndex + 1, endIndex);
    }

    public static void main(String[] args) {
        System.out.println(getIPFromSocketAddress("/192.168.0.102:38518"));
    }


}
