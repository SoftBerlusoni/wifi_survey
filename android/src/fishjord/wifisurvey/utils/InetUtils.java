package fishjord.wifisurvey.utils;

public class InetUtils {

	public static String inet4IntToString(int inetAddress) {
		StringBuilder ret = new StringBuilder();

		ret.append((inetAddress) & 0xff).append(".");
		ret.append((inetAddress >>> 8) & 0xff).append(".");
		ret.append((inetAddress >>> 16) & 0xff).append(".");
		ret.append((inetAddress >>> 24) & 0xff);

		return ret.toString();
	}
}
