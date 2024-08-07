package com.example.anton2319sdocsonwg;

import android.content.Context;
import android.content.Intent;

import com.wireguard.android.backend.GoBackend;

public class PersistentConnectionProperties {
    private static PersistentConnectionProperties mInstance = null;

    private WgTunnel tunnel;
    private GoBackend backend;
    private Context context;

    private PersistentConnectionProperties(Context context) {
        this.context = context;
        tunnel = new WgTunnel();
    }

    public WgTunnel getTunnel() {
        return tunnel;
    }

    public GoBackend getBackend() {
        return backend;
    }

    public void setBackend(GoBackend backend) {
        this.backend = backend;
    }

    public static synchronized PersistentConnectionProperties getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new PersistentConnectionProperties(context);
        }
        return mInstance;
    }

    public void startVpnStatusService() {
        Intent intent = new Intent(context, VpnStatusService.class);
        context.startService(intent);
    }

    public void stopVpnStatusService() {
        Intent intent = new Intent(context, VpnStatusService.class);
        context.stopService(intent);
    }
}
