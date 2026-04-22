package net.basicgo.tutucast_demo;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.TrafficStats;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import android.os.Build;
import android.os.Environment;
import android.view.Display;
import android.view.WindowManager;
import android.widget.Toast;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

public class CommonUtil {
	/*
	1. Environment.getExternalStorageDirectory() 需要什么权限？
			Environment.getExternalStorageDirectory() 是获取外部存储（通常是 SD 卡或模拟 SD 卡）根目录的方法。

	权限要求：

	API 29 (Android 10) 及以下版本：

	读取文件需要 READ_EXTERNAL_STORAGE 权限。

	写入文件需要 WRITE_EXTERNAL_STORAGE 权限。

	注意： 这些是危险权限，需要在运行时向用户申请。

	API 30 (Android 11) 及以上版本：

	该方法已被弃用 (Deprecated)。

	由于 Scoped Storage（分区存储）的引入，应用即使声明了上述权限，也无法直接访问此公共根目录。

	应用只能访问自己的应用私有目录 (getExternalFilesDir()) 或使用 MediaStore API 访问媒体集合，或者使用 Storage Access Framework (SAF) 访问公共文档。

	结论： 在新版本中，即使声明权限，该方法也基本失效，不应再使用。

			❌ 建议： 强烈建议不要再使用 Environment.getExternalStorageDirectory()，因为它与现代 Android 存储模型不兼容。

			2. getFilesDir() 和 getExternalFilesDir() 只能在 API 30 以上的版本上运行吗？
	答案：否。

	兼容性说明：

	getFilesDir()（内部存储）和 getExternalFilesDir()（外部存储应用私有目录）是 从 Android 早期版本就存在的基础 API。它们可以在 所有 Android 版本 上运行。

	关键点在于权限和存储模型的变化：

	存储 API	兼容性	权限要求 (所有版本)	存储模型影响
	getFilesDir()	所有版本	否（不需要任何权限）	始终有效且推荐用于私有数据。
	getExternalFilesDir()	所有版本	否（不需要任何权限）	始终有效且推荐用于应用卸载后可清理的大文件。
	*/

    private static final CommonLog log = LogFactory.createLog();

    public static boolean checkNetworkState(Context context){
        boolean netstate = false;
        ConnectivityManager connectivity = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivity != null)
        {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null) {
                for (int i = 0; i < info.length; i++)
                {
                    if (info[i].getState() == NetworkInfo.State.CONNECTED)
                    {
                        netstate = true;
                        break;
                    }
                }
            }
        }
        return netstate;
    }

    public static String getLocalMacAddress(Context mc) {
        String defmac = "02:00:00:00:00:00";

        try {


            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iF = interfaces.nextElement();

                byte[] addr = iF.getHardwareAddress();
                if (addr == null || addr.length == 0) {
                    continue;
                }

                StringBuilder buf = new StringBuilder();
                for (byte b : addr) {
                    buf.append(String.format("%02X:", b));
                }
                if (buf.length() > 0) {
                    buf.deleteCharAt(buf.length() - 1);
                }
                defmac = buf.toString();
                break;
            }
        }catch (SocketException e) {
            e.printStackTrace();
        }

        return defmac;
    }

    public static MulticastLock openWifiBrocast(Context context){
        WifiManager wifiManager=(WifiManager)context.getSystemService(Context.WIFI_SERVICE);
        MulticastLock  multicastLock=wifiManager.createMulticastLock("MediaRender");
        if (multicastLock != null){
            multicastLock.acquire();
        }
        return multicastLock;
    }


    public static void setCurrentVolume(int percent,Context mc){
        AudioManager am=(AudioManager)mc.getSystemService(Context.AUDIO_SERVICE);
        int maxvolume=am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        am.setStreamVolume(AudioManager.STREAM_MUSIC, (maxvolume*percent)/100,
                AudioManager.FLAG_PLAY_SOUND|AudioManager.FLAG_SHOW_UI);
        am.setMode(AudioManager.MODE_INVALID);
    }

    public static void setVolumeMute(Context mc){
        AudioManager am=(AudioManager)mc.getSystemService(Context.AUDIO_SERVICE);
        am.setStreamMute(AudioManager.STREAM_MUSIC, true);
    }
    public static void setVolumeUnmute(Context mc){
        AudioManager am=(AudioManager)mc.getSystemService(Context.AUDIO_SERVICE);
        am.setStreamMute(AudioManager.STREAM_MUSIC, false);
    }

    public static void showToask(Context context, String tip){
        Toast.makeText(context, tip, Toast.LENGTH_SHORT).show();
    }

    public static int getScreenWidth(Context context) {
        WindowManager manager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        return display.getWidth();
    }

    public static int getScreenHeight(Context context) {
        WindowManager manager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        return display.getHeight();
    }

    public static ViewSize getFitSize(Context context, MediaPlayer mediaPlayer)
    {
        int videoWidth = mediaPlayer.getVideoWidth();
        int videoHeight = mediaPlayer.getVideoHeight();
        double fit1 = videoWidth * 1.0 / videoHeight;

        int width2 = getScreenWidth(context);
        int height2 = getScreenHeight(context);
        double fit2 = width2 * 1.0 / height2;

        double fit = 1;
        if (fit1 > fit2)
        {
            fit = width2 * 1.0 / videoWidth;
        }else{
            fit = height2 * 1.0 / videoHeight;
        }

        ViewSize viewSize = new ViewSize();
        viewSize.width = (int) (fit * videoWidth);
        viewSize.height = (int) (fit * videoHeight);

        return viewSize;
    }

    public static class ViewSize
    {
        public int width = 0;
        public int height = 0;
    }

    public static boolean getWifiState(Context context){
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        State wifistate = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
        if (wifistate != State.CONNECTED){
            return false;
        }

        State mobileState = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();
        boolean ret = State.CONNECTED != mobileState;
        return ret;
    }


    public static boolean getMobileState(Context context){
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        State wifistate = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
        if (wifistate != State.CONNECTED){
            return false;
        }

        State mobileState = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();
        boolean ret = State.CONNECTED == mobileState;
        return ret;
    }

    public static String getWifiIp(Context context) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ip = wifiInfo.getIpAddress();
        return String.format("%d.%d.%d.%d",
                (ip & 0xff), (ip >> 8 & 0xff),
                (ip >> 16 & 0xff), (ip >> 24 & 0xff));
    }

    public static String getMobileIpAddress(Context context) {
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = cm.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                    NetworkInterface intf = en.nextElement();
                    for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                        InetAddress inetAddress = enumIpAddr.nextElement();
                        if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                            return inetAddress.getHostAddress();
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getLocalIpAddress() {
        try {
            List<InetAddress> candidateAddresses = new ArrayList<>();
            for (NetworkInterface intf : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                // 过滤无效接口
                if (!intf.isUp() || intf.isLoopback() || intf.isVirtual()) continue;

                // 识别接口类型
                boolean isWifi = intf.getName().equalsIgnoreCase("wlan0");
                boolean isEth0 = intf.getName().equalsIgnoreCase("eth0");

                for (InetAddress inetAddress : Collections.list(intf.getInetAddresses())) {
                    if (inetAddress instanceof Inet4Address) { // 仅处理 IPv4 地址
                        if (isEth0) {
                            // 优先返回 Wi-Fi 地址
                            return inetAddress.getHostAddress();
                        } else {
                            // 有线网络插入列表头部，其他接口追加到尾部
                            if (isWifi) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
                                    candidateAddresses.addFirst(inetAddress);
                                }else{
                                    candidateAddresses.add(0, inetAddress);
                                }
                            } else {
                                candidateAddresses.add(inetAddress);
                            }
                        }
                    }
                }
            }

            // 按优先级返回候选地址
            if (!candidateAddresses.isEmpty()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
                    return candidateAddresses.getFirst().getHostAddress();
                }else{
                    return candidateAddresses.get(0).getHostAddress();
                }
            }
        } catch (SocketException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private static long m_lSysNetworkSpeedLastTs = 0;
    private static long m_lSystNetworkLastBytes = 0;
    private static float m_fSysNetowrkLastSpeed = 0.0f;
    public static float getSysNetworkDownloadSpeed() {
        long nowMS = System.currentTimeMillis();
        long nowBytes = TrafficStats.getTotalRxBytes();

        long timeinterval = nowMS - m_lSysNetworkSpeedLastTs;
        long bytes = nowBytes - m_lSystNetworkLastBytes;

        if(timeinterval > 0) m_fSysNetowrkLastSpeed = (float)bytes * 1.0f / (float)timeinterval;

        m_lSysNetworkSpeedLastTs = nowMS;
        m_lSystNetworkLastBytes = nowBytes;

        return m_fSysNetowrkLastSpeed;
    }
}
