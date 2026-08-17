package com.phuzle.labs.repacks;

import android.os.Bundle;
import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        registerPlugin(RepacksPlugin.class);
        super.onCreate(savedInstanceState);
    }
}
