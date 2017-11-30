package base.networkstate;

import android.net.TrafficStats;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.math.BigDecimal;

/**
 * 网络实时速度工具
 */
public class TrafficUtil {

	private int uid = android.os.Process.myUid();
	private long preRxBytes = 0;

	public TrafficUtil() {
	}

	/**
	 * 获取总流量
	 */
	public long getTrafficInfo() {
		return getTraffic(true) + getTraffic(false);
	}

	/**
	 * 获取上行/下行流量
	 * 某个应用的网络流量数据保存在系统的/proc/uid_stat/$UID/tcp_rcv | tcp_snd文件中
	 */
	public long getTraffic(boolean download) {
		long traffic = TrafficStats.getUidRxBytes(uid);
		RandomAccessFile file = null; // 用于访问数据记录文件
		String recordPath = "/proc/uid_stat/" + uid ;
		if(download){
			//下行流量
			recordPath +=  "/tcp_rcv";
		}else{
			//上行流量
			recordPath +=  "/tcp_snd";
		}
		try {
			file = new RandomAccessFile(recordPath, "r");
			traffic = Long.parseLong(file.readLine());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (file != null)
					file.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return traffic;
	}

	/**
	 * 获取当前下载流量总和
	 */
	public static long getNetworkRxBytes() {
		return TrafficStats.getTotalRxBytes();
	}

	/**
	 * 获取当前上传流量总和
	 */
	public static long getNetworkTxBytes() {
		return TrafficStats.getTotalTxBytes();
	}

	/**
	 * 获取当前网速
	 */
	public double getNetSpeed() {
		//获取总的流量情况
		long curRxBytes = getNetworkRxBytes();
		//获取当前应用的流量情况
//		long curRxBytes = getTrafficInfo();
		if (preRxBytes == 0)
			preRxBytes = curRxBytes;
		long bytes = curRxBytes - preRxBytes;
		preRxBytes = curRxBytes;
		double kb = (double)bytes / 1024;
		return new BigDecimal(kb).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
	}

}
