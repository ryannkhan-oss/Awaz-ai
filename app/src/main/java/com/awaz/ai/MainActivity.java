package com.awaz.ai;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.webkit.GeolocationPermissions;
import android.webkit.JavascriptInterface;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.webkit.WebViewAssetLoader;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.*;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    private WebView web;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        List<String> p = new ArrayList<>(Arrays.asList(
            Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.RECORD_AUDIO));
        if (Build.VERSION.SDK_INT >= 31) {
            p.add("android.permission.BLUETOOTH_ADVERTISE");
            p.add("android.permission.BLUETOOTH_CONNECT");
            p.add("android.permission.BLUETOOTH_SCAN");
        }
        if (Build.VERSION.SDK_INT >= 33) p.add("android.permission.NEARBY_WIFI_DEVICES");
        ActivityCompat.requestPermissions(this, p.toArray(new String[0]), 1);

        web = new WebView(this);
        setContentView(web);
        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setGeolocationEnabled(true);
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setAllowFileAccess(false);
        final File dir = new File(getFilesDir(), "tiles");
        dir.mkdirs();
        final WebViewAssetLoader l = new WebViewAssetLoader.Builder()
            .addPathHandler("/assets/", new WebViewAssetLoader.AssetsPathHandler(this)).build();
        web.addJavascriptInterface(new Mesh(this), "AwazNative");
        web.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onGeolocationPermissionsShowPrompt(String o, GeolocationPermissions.Callback c) {
                c.invoke(o, true, false);
            }
            @Override
            public void onPermissionRequest(final PermissionRequest r) {
                runOnUiThread(() -> r.grant(r.getResources()));
            }
        });
        web.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView v, WebResourceRequest r) {
                Uri u = r.getUrl();
                String sc = u.getScheme();
                if ("tel".equals(sc) || "sms".equals(sc) || "smsto".equals(sc)) {
                    startActivity(new Intent(Intent.ACTION_VIEW, u));
                    return true;
                }
                return false;
            }
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView v, WebResourceRequest r) {
                Uri u = r.getUrl();
                if ("server.arcgisonline.com".equals(u.getHost())) return tile(u, dir);
                return l.shouldInterceptRequest(u);
            }
        });
        web.loadUrl("https://appassets.androidplatform.net/assets/index.html");
    }

    /** Satellite tiles: serve from disk if cached, otherwise download once and keep. */
    private WebResourceResponse tile(Uri u, File dir) {
        List<String> sg = u.getPathSegments();
        int n = sg.size();
        File f = new File(dir, sg.get(n - 3) + "_" + sg.get(n - 2) + "_" + sg.get(n - 1) + ".jpg");
        try {
            if (!f.exists()) {
                HttpURLConnection c = (HttpURLConnection) new URL(u.toString()).openConnection();
                c.setConnectTimeout(6000);
                c.setReadTimeout(8000);
                if (c.getResponseCode() != 200) throw new IOException();
                File t = new File(f.getPath() + ".tmp");
                try (InputStream in = c.getInputStream(); OutputStream out = new FileOutputStream(t)) {
                    byte[] bf = new byte[8192];
                    int k;
                    while ((k = in.read(bf)) > 0) out.write(bf, 0, k);
                }
                t.renameTo(f);
            }
            return new WebResourceResponse("image/jpeg", null, new FileInputStream(f));
        } catch (Exception e) {
            return new WebResourceResponse("image/jpeg", null, 404, "Not Found",
                new HashMap<String, String>(), new ByteArrayInputStream(new byte[0]));
        }
    }

    /** Real phone-to-phone relay using Google Nearby Connections (Bluetooth / Wi-Fi Direct). */
    public class Mesh {
        private static final String SERVICE = "com.awaz.ai.mesh";
        private final ConnectionsClient nc;
        private final String me = "awaz" + Long.toHexString(new Random().nextLong() & 0xffffffL);
        private final Set<String> peers = Collections.synchronizedSet(new HashSet<String>());
        private boolean on = false;

        Mesh(Context c) {
            nc = Nearby.getConnectionsClient(c);
        }

        private void js(final String code) {
            web.post(() -> web.evaluateJavascript(code, null));
        }

        private void pushPeers() {
            js("window.AWAZ_peers&&AWAZ_peers(" + peers.size() + ")");
        }

        private final PayloadCallback pay = new PayloadCallback() {
            @Override
            public void onPayloadReceived(String id, Payload p) {
                if (p.getType() == Payload.Type.BYTES && p.asBytes() != null) {
                    String s = new String(p.asBytes(), StandardCharsets.UTF_8);
                    js("window.AWAZ_rx&&AWAZ_rx(" + JSONObject.quote(s) + ")");
                }
            }
            @Override
            public void onPayloadTransferUpdate(String id, PayloadTransferUpdate u) { }
        };

        private final ConnectionLifecycleCallback life = new ConnectionLifecycleCallback() {
            @Override
            public void onConnectionInitiated(String id, ConnectionInfo info) {
                nc.acceptConnection(id, pay);
            }
            @Override
            public void onConnectionResult(String id, ConnectionResolution r) {
                if (r.getStatus().isSuccess()) {
                    peers.add(id);
                    pushPeers();
                }
            }
            @Override
            public void onDisconnected(String id) {
                peers.remove(id);
                pushPeers();
            }
        };

        private final EndpointDiscoveryCallback disc = new EndpointDiscoveryCallback() {
            @Override
            public void onEndpointFound(String id, DiscoveredEndpointInfo info) {
                // only the phone with the smaller name initiates, to avoid duplicate connections
                if (me.compareTo(info.getEndpointName()) < 0) {
                    nc.requestConnection(me, id, life);
                }
            }
            @Override
            public void onEndpointLost(String id) { }
        };

        @JavascriptInterface
        public void start() {
            if (on) return;
            on = true;
            try {
                nc.startAdvertising(me, SERVICE, life,
                    new AdvertisingOptions.Builder().setStrategy(Strategy.P2P_CLUSTER).build());
                nc.startDiscovery(SERVICE, disc,
                    new DiscoveryOptions.Builder().setStrategy(Strategy.P2P_CLUSTER).build());
            } catch (Exception e) {
                on = false;
            }
        }

        @JavascriptInterface
        public void stop() {
            try {
                nc.stopAdvertising();
                nc.stopDiscovery();
                nc.stopAllEndpoints();
            } catch (Exception ignored) { }
            peers.clear();
            on = false;
            pushPeers();
        }

        @JavascriptInterface
        public int peers() {
            return peers.size();
        }

        @JavascriptInterface
        public int send(String json) {
            byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
            int n = 0;
            synchronized (peers) {
                for (String id : peers) {
                    nc.sendPayload(id, Payload.fromBytes(bytes));
                    n++;
                }
            }
            return n;
        }
    }
}
