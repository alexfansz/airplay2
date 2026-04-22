package net.basicgo.tutucast_demo;

import static net.basicgo.tutucast_demo.CommonUtil.getLocalIpAddress;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.nsd.NsdServiceInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.show.dlnadmr.center.DlnaMediaModel;
import com.show.dlnadmr.center.MediaControlBrocastFactory;
import com.show.dlnadmr.player.PlayerEngineListener;

import net.basicgo.tutucast.PlatinumReflection;

import java.util.Random;

import de.badaix.snapcast.SnapclientService;
import de.badaix.snapcast.SnapserverService;
import de.badaix.snapcast.control.RemoteControl;
import de.badaix.snapcast.control.json.Client;
import de.badaix.snapcast.control.json.Group;
import de.badaix.snapcast.control.json.ServerStatus;
import de.badaix.snapcast.control.json.Stream;
import de.badaix.snapcast.control.json.Volume;
import de.badaix.snapcast.utils.NsdHelper;
import de.badaix.snapcast.utils.Settings;

public class MainActivity extends AppCompatActivity
        implements RemoteControl.RemoteControlListener, SnapclientService.SnapclientListener, NsdHelper.NsdHelperListener
        ,PlatinumReflection.ActionReflectionListener
        , PlayerEngineListener {

    private static final String SERVICE_NAME = "tutucast";// #2";
    private boolean bound = false;
    private SnapclientService snapclientService;
    private SnapserverService snapserverService;
    private boolean batchActive = false;
    private ServerStatus serverStatus = null;
    private RemoteControl remoteControl = null;
    private String host = "";
    private int port = 1704;
    private int controlPort = 1705;

    private final ServiceConnection mConnectionServer = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            SnapserverService.LocalBinder binder = (SnapserverService.LocalBinder) service;
            snapserverService = binder.getService();
            snapserverService.setPlayerListener(MainActivity.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {}
    };

    private final ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            SnapclientService.LocalBinder binder = (SnapclientService.LocalBinder) service;
            snapclientService = binder.getService();
            snapclientService.setListener(MainActivity.this);
            bound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            bound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        /*
        启动Dlna音频全屋分发
         */
        /* server */
        Intent intent_s = new Intent(this, SnapserverService.class);
        bindService(intent_s, mConnectionServer, Context.BIND_AUTO_CREATE);

        boolean startServer = true;
        Intent i = new Intent(this, SnapserverService.class);
        i.setAction(startServer ? SnapserverService.ACTION_START : SnapserverService.ACTION_STOP);
        i.putExtra("name",SERVICE_NAME);
        startService(i);


        /* client */
        NsdHelper.getInstance(this).startListening(SERVICE_NAME, this);
        Intent intent = new Intent(this, SnapclientService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);


        /*
        启动Airplay2全屋音频
         */
        String ip4Addr = getLocalIpAddress();
        String dev_name;
        if(ip4Addr != null) {
            int lastDotIndex = ip4Addr.lastIndexOf('.');
           dev_name = ((lastDotIndex != -1) ? ip4Addr.substring(lastDotIndex) : "-");
        }else{
            final Random random = new Random();
            dev_name = "." + random.nextInt(99);
        }
        PlatinumReflection.setActionInvokeListener(this,this);
        PlatinumReflection.TutucastStart(/*Build.BRAND + " " +*/ Build.MODEL + dev_name);
    }

    @Override
    public void onStop()
    {
        super.onStop();

        Log.d("airplay2","onStop");
        /*
        停止Dlna音频全屋分发
         */
        stopRemoteControl();
        stopSnapclient();
        NsdHelper.getInstance(this).stopListening();
        if (bound) {
            unbindService(mConnection);
            bound = false;
        }
        unbindService(mConnectionServer);
        stopService(new Intent(this, SnapserverService.class));

        /*
        停止Airplay2全屋音频
         */
        PlatinumReflection.TutucastStop();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
    }

    @Override
    public void onPlayerStart(SnapclientService snapclientService) {

    }

    @Override
    public void onPlayerStop(SnapclientService snapclientService) {

    }

    @Override
    public void onLog(SnapclientService snapclientService, String s, String s1, String s2, String s3) {

    }

    @Override
    public void onError(SnapclientService snapclientService, String s, Exception e) {

    }

    @Override
    public void onConnected(RemoteControl remoteControl) {
        remoteControl.getServerStatus();
    }

    @Override
    public void onConnecting(RemoteControl remoteControl) {

    }

    @Override
    public void onDisconnected(RemoteControl remoteControl, Exception e) {
        serverStatus = new ServerStatus();
    }

    @Override
    public void onBatchStart() {
        batchActive = true;
    }

    @Override
    public void onBatchEnd() {
        batchActive = false;
    }

    @Override
    public void onConnect(Client client) {
        serverStatus.getClient(client.getId());
        if (client == null) {
            remoteControl.getServerStatus();
            return;
        }
        client.setConnected(true);
        serverStatus.updateClient(client);
    }

    @Override
    public void onDisconnect(String s) {
        Client client = serverStatus.getClient(s);
        if (client == null) {
            remoteControl.getServerStatus();
            return;
        }
        client.setConnected(false);
        serverStatus.updateClient(client);
    }

    @Override
    public void onUpdate(Client client) {
        serverStatus.updateClient(client);
    }

    @Override
    public void onVolumeChanged(RemoteControl.RPCEvent rpcEvent, String s, Volume volume) {
        if (rpcEvent == RemoteControl.RPCEvent.response)
            return;
        Client client = serverStatus.getClient(s);
        if (client == null) {
            remoteControl.getServerStatus();
            return;
        }
        client.setVolume(volume);
    }

    @Override
    public void onLatencyChanged(RemoteControl.RPCEvent rpcEvent, String s, long l) {
        Client client = serverStatus.getClient(s);
        if (client == null) {
            remoteControl.getServerStatus();
            return;
        }
        client.getConfig().setLatency((int) l);
    }

    @Override
    public void onNameChanged(RemoteControl.RPCEvent rpcEvent, String s, String s1) {
        Client client = serverStatus.getClient(s);
        if (client == null) {
            remoteControl.getServerStatus();
            return;
        }
        client.getConfig().setName(s1);
    }

    @Override
    public void onUpdate(Group group) {
        serverStatus.updateGroup(group);
    }

    @Override
    public void onMute(RemoteControl.RPCEvent rpcEvent, String s, boolean b) {
        Group g = serverStatus.getGroup(s);
        if (g == null) {
            remoteControl.getServerStatus();
            return;
        }
        g.setMuted(b);
        serverStatus.updateGroup(g);
    }

    @Override
    public void onStreamChanged(RemoteControl.RPCEvent rpcEvent, String s, String s1) {
        Group g = serverStatus.getGroup(s);
        if (g == null) {
            remoteControl.getServerStatus();
            return;
        }
        g.setStreamId(s1);
        serverStatus.updateGroup(g);
    }

    @Override
    public void onUpdate(ServerStatus serverStatus) {
        this.serverStatus = serverStatus;
    }

    @Override
    public void onUpdate(String s, Stream stream) {
        serverStatus.updateStream(stream);
    }

    @Override
    public void onResolved(NsdHelper nsdHelper, NsdServiceInfo nsdServiceInfo) {
        setHost(nsdServiceInfo.getHost().getCanonicalHostName(), nsdServiceInfo.getPort(), nsdServiceInfo.getPort() + 1);
        //startRemoteControl();

        boolean autoRecieve = true;

        stopRemoteControl();
        stopSnapclient();
        if(autoRecieve){
            startSnapclient();
        }
        startRemoteControl();


        /*测试播放url*/
        //MediaControlBrocastFactory.loadMedia(this,"http://music.163.com/song/media/outer/url?id=25906124.mp3");
    }

    @Override
    public void onServiceLost(NsdHelper nsdHelper, NsdServiceInfo nsdServiceInfo) {
        lostHost(this.host,0,0);
        stopSnapclient();
        stopRemoteControl();
    }

    private void lostHost(final String host, final int streamPort, final int controlPort) {
        if(this.host.equals(host)) {
            this.host = "";
            this.port = 0;
            this.controlPort = 0;
            Settings.getInstance(this).setHost(this.host, this.port, this.controlPort);
        }
    }

    private void setHost(final String host, final int streamPort, final int controlPort) {
        if (TextUtils.isEmpty(host))
            return;

        this.host = host;
        this.port = streamPort;
        this.controlPort = controlPort;
        Settings.getInstance(this).setHost(host, streamPort, controlPort);
    }

    private void startSnapclient() {
        if (TextUtils.isEmpty(host)) //important
            return;

        Intent i = new Intent(this, SnapclientService.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        i.putExtra(SnapclientService.EXTRA_HOST, host);
        i.putExtra(SnapclientService.EXTRA_PORT, port);
        i.setAction(SnapclientService.ACTION_START);

        startService(i);
    }

    private void stopSnapclient() {
        if (bound)
            snapclientService.stopPlayer();
//        stopService(new Intent(this, SnapclientService.class));
//              getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
    private void startRemoteControl() {
        if (remoteControl == null)
            remoteControl = new RemoteControl(this);
        if (!host.isEmpty())
            remoteControl.connect(host, controlPort);
    }

    private void stopRemoteControl() {
        if ((remoteControl != null) && (remoteControl.isConnected()))
            remoteControl.disconnect();
        remoteControl = null;
    }


    /* Airplay2相关回调接口 */
    @Override
    public         void airpay2_start_result(String result){
        Log.d("airplay2","airpay2_start_result:"+result);
    }

    @Override
    public void audio_init(int i, int i1, int i2) {
        Log.d("airplay2","audio_init:"+i+","+i1+","+i2);
    }

    @Override
    public void audio_data(byte[] bytes, double v, int i) {
        Log.d("airplay2","audio_data:"+bytes.length+","+","+v+","+i);
    }

    @Override
    public void audio_destroy() {
        Log.d("airplay2","audio_destroy");
    }

    @Override
    public void audio_volume(int i) {
        Log.d("airplay2","audio_volume:"+i);
    }

    @Override
    public void audio_album(String s, String s1) {
        Log.d("airplay2","audio_album:"+s+","+s1);
    }

    @Override
    public void audio_cover(String s, byte[] bytes) {
        Log.d("airplay2","audio_cover:"+s+","+bytes.length);
    }

    @Override
    public         void audio_process(String process){
        Log.d("airplay2","audio_process:"+process);
    }


    //播放器控制API
    /*
        MediaControlBrocastFactory.loadMedia(String url);
        MediaControlBrocastFactory.sendPlayBrocast(this);
        MediaControlBrocastFactory.sendPauseBrocast(this);
        MediaControlBrocastFactory.sendStopBorocast(this);
        MediaControlBrocastFactory.sendSeekBrocast(this,pos);
     */

    //播放器状态通知
    /*
    snapserverService.getDuration();
    snapserverService.getCurPos();
     */
    @Override
    public void onTrackPlay(DlnaMediaModel dlnaMediaModel) {

    }

    @Override
    public void onTrackStop(DlnaMediaModel dlnaMediaModel) {
        Log.d("onTrackStop" , dlnaMediaModel.getUrl());
    }

    @Override
    public void onTrackPause(DlnaMediaModel dlnaMediaModel) {

    }

    @Override
    public void onTrackPrepareSync(DlnaMediaModel dlnaMediaModel) {

    }

    @Override
    public void onTrackPrepareComplete(DlnaMediaModel dlnaMediaModel) {
        Log.d("onTrackPrepareComplete" , snapserverService.getDuration() + ","+ snapserverService.getCurPos());
    }

    @Override
    public void onTrackStreamError(DlnaMediaModel dlnaMediaModel) {

    }

    @Override
    public void onTrackPlayComplete(DlnaMediaModel dlnaMediaModel) {

    }
}