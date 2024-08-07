package com.example.anton2319sdocsonwg;

import com.wireguard.android.backend.Tunnel;

import java.util.ArrayList;
import java.util.List;

public class WgTunnel implements Tunnel {

    private List<OnStateChangeListener> listeners = new ArrayList<>();
    private State currentState = State.DOWN;

    @Override
    public String getName() {
        return "WireGuard tunnel";
    }

    @Override
    public void onStateChange(State newState) {
        currentState = newState;
        notifyStateChange(newState);
    }

    public void addOnStateChangeListener(OnStateChangeListener listener) {
        listeners.add(listener);
    }

    public void removeOnStateChangeListener(OnStateChangeListener listener) {
        listeners.remove(listener);
    }

    private void notifyStateChange(State newState) {
        for (OnStateChangeListener listener : listeners) {
            listener.onStateChanged(newState);
        }
    }

    public interface OnStateChangeListener {
        void onStateChanged(State newState);
    }

    public State getCurrentState() {
        return currentState;
    }
}
