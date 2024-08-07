package com.example.anton2319sdocsonwg;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import com.wireguard.android.backend.Backend;
import com.wireguard.android.backend.GoBackend;
import com.wireguard.android.backend.Tunnel;
import com.wireguard.config.Config;
import com.wireguard.config.InetEndpoint;
import com.wireguard.config.InetNetwork;
import com.wireguard.config.Interface;
import com.wireguard.config.Peer;
import com.wireguard.crypto.Key;

public class MainActivity extends AppCompatActivity implements WgTunnel.OnStateChangeListener {

    private Backend backend;
    private WgTunnel tunnel;
    private Button connectButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        connectButton = findViewById(R.id.connectButton);
        tunnel = PersistentConnectionProperties.getInstance(this).getTunnel();
        tunnel.addOnStateChangeListener(this);

        backend = PersistentConnectionProperties.getInstance(this).getBackend();
        if (backend == null) {
            PersistentConnectionProperties.getInstance(this).setBackend(new GoBackend(this));
            backend = PersistentConnectionProperties.getInstance(this).getBackend();
        }

        updateButtonState();
    }

    public void connect(View v) {
        Intent intentPrepare = GoBackend.VpnService.prepare(this);
        if (intentPrepare != null) {
            startActivityForResult(intentPrepare, 0);
        }

        Interface.Builder interfaceBuilder = new Interface.Builder();
        Peer.Builder peerBuilder = new Peer.Builder();

        AsyncTask.execute(() -> {
            try {
                if (backend.getState(tunnel) == Tunnel.State.UP) {
                    backend.setState(tunnel, Tunnel.State.DOWN, null);
                } else {
                    // update your data here of vpn peer and interface
                    
                    backend.setState(tunnel, Tunnel.State.UP, new Config.Builder()
                            .setInterface(interfaceBuilder.addAddress(InetNetwork.parse("10.0.0.203/32")).parsePrivateKey("YL65C7i/aerowuerxxxxxxxxxxxxxxxxxxxxxxx=").build())
                            .addPeer(peerBuilder.addAllowedIp(InetNetwork.parse("0.0.0.0/0")).setEndpoint(InetEndpoint.parse("139.91.111.11:8000")).parsePublicKey("xmmxmxmmxmxmxmxmxmxmmxmxmxmmxx").setPersistentKeepalive(21).setPreSharedKey(Key.fromBase64("aslidalsiudlasul+vk=")).build())
                            .build());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void onStateChanged(Tunnel.State newState) {
        Log.d("MainActivity", "Tunnel state changed to: " + newState);
        runOnUiThread(this::updateButtonState);

        if (newState == Tunnel.State.UP) {
            PersistentConnectionProperties.getInstance(this).startVpnStatusService();
        } else {
            PersistentConnectionProperties.getInstance(this).stopVpnStatusService();
        }
    }

    private void updateButtonState() {
        if (tunnel.getCurrentState() == Tunnel.State.UP) {
            connectButton.setText("Disconnect");
        } else {
            connectButton.setText("Connect");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        tunnel.removeOnStateChangeListener(this);
        if (tunnel.getCurrentState() != Tunnel.State.UP) {
            PersistentConnectionProperties.getInstance(this).stopVpnStatusService();
        }
    }
}
