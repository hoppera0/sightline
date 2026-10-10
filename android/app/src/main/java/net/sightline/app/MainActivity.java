package net.sightline.app;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Sightline as an Android app: the same single built page as the website, opened from the
 * APK's own assets over file:// — the mode the page was built to run in. The app has no
 * INTERNET permission, so a picture can never leave the phone.
 *
 * The page needs three things a bare WebView does not give it: a file picker for "Open",
 * somewhere for "Save PNG" to go, and a way in for images shared from other apps.
 */
public class MainActivity extends Activity {
    private static final int PICK = 1;
    private WebView web;
    private ValueCallback<Uri[]> pending;
    private volatile String shared;   // "mime;base64" waiting for the page to collect it

    // Runs in the page once it has loaded. Save PNG builds a blob link and clicks it; a
    // WebView ignores that, so the click is caught and the bytes handed to Java instead.
    private static final String BRIDGE =
        "(function () {" +
        "  if (window.__sightlineAndroid) { window.__sightlineShared(); return; }" +
        "  window.__sightlineAndroid = true;" +
        "  var click = HTMLAnchorElement.prototype.click;" +
        "  HTMLAnchorElement.prototype.click = function () {" +
        "    if (this.download && /^(blob|data):/.test(this.href)) {" +
        "      var name = this.download;" +
        "      fetch(this.href).then(function (r) { return r.blob(); }).then(function (b) {" +
        "        return new Promise(function (res) { var f = new FileReader();" +
        "          f.onload = function () { res([b.type, f.result]); }; f.readAsDataURL(b); });" +
        "      }).then(function (x) {" +
        "        SightlineAndroid.save(name, x[0] || 'image/png', x[1].slice(x[1].indexOf(',') + 1));" +
        "      });" +
        "      return;" +
        "    }" +
        "    return click.call(this);" +
        "  };" +
        "  window.__sightlineShared = function () {" +
        "    var s = SightlineAndroid.takeShared(); if (!s) return;" +
        "    var i = s.indexOf(';'), bin = atob(s.slice(i + 1)), u = new Uint8Array(bin.length);" +
        "    for (var k = 0; k < bin.length; k++) u[k] = bin.charCodeAt(k);" +
        "    window.sightline.loadFile(new File([u], 'shared image', { type: s.slice(0, i) }));" +
        "  };" +
        "  window.__sightlineShared();" +
        "})();";

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        web = new WebView(this);
        setContentView(web);

        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setAllowFileAccess(true);
        s.setBuiltInZoomControls(false);

        web.addJavascriptInterface(new Bridge(), "SightlineAndroid");
        web.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                view.evaluateJavascript(BRIDGE, null);
            }
        });
        web.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> callback,
                                             FileChooserParams params) {
                if (pending != null) pending.onReceiveValue(null);
                pending = callback;
                Intent pick = new Intent(Intent.ACTION_GET_CONTENT);
                pick.addCategory(Intent.CATEGORY_OPENABLE);
                pick.setType("image/*");
                try {
                    startActivityForResult(Intent.createChooser(pick, "Open an image"), PICK);
                } catch (Exception e) {
                    pending = null;
                    return false;
                }
                return true;
            }
        });

        takeIntent(getIntent());
        if (state != null) web.restoreState(state);
        else web.loadUrl("file:///android_asset/index.html");
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (takeIntent(intent)) web.evaluateJavascript(BRIDGE, null);
    }

    // An image shared to Sightline from another app. Read it now, hand it over on load.
    private boolean takeIntent(Intent intent) {
        if (intent == null || !Intent.ACTION_SEND.equals(intent.getAction())) return false;
        Uri uri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
        if (uri == null) return false;
        try (InputStream in = getContentResolver().openInputStream(uri)) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buf = new byte[1 << 16];
            for (int n; (n = in.read(buf)) > 0; ) out.write(buf, 0, n);
            String type = intent.getType() != null ? intent.getType() : "image/jpeg";
            shared = type + ";" + Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP);
            return true;
        } catch (Exception e) {
            Toast.makeText(this, "Couldn't read that image.", Toast.LENGTH_LONG).show();
            return false;
        }
    }

    @Override
    protected void onActivityResult(int request, int result, Intent data) {
        if (request != PICK) { super.onActivityResult(request, result, data); return; }
        if (pending == null) return;
        Uri uri = (result == RESULT_OK && data != null) ? data.getData() : null;
        pending.onReceiveValue(uri != null ? new Uri[] { uri } : null);
        pending = null;
    }

    @Override
    protected void onSaveInstanceState(Bundle out) {
        super.onSaveInstanceState(out);
        web.saveState(out);
    }

    @Override
    protected void onDestroy() {
        web.destroy();
        super.onDestroy();
    }

    private class Bridge {
        // Into the phone's Downloads folder, where the page already says it went.
        @JavascriptInterface
        public void save(String name, String mime, String base64) {
            try {
                byte[] bytes = Base64.decode(base64, Base64.DEFAULT);
                ContentResolver cr = getContentResolver();
                ContentValues v = new ContentValues();
                v.put(MediaStore.Downloads.DISPLAY_NAME, name);
                v.put(MediaStore.Downloads.MIME_TYPE, mime);
                Uri uri = cr.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, v);
                if (uri == null) throw new IllegalStateException("no uri");
                try (OutputStream os = cr.openOutputStream(uri)) { os.write(bytes); }
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this,
                    "Couldn't save the PNG.", Toast.LENGTH_LONG).show());
            }
        }

        @JavascriptInterface
        public String takeShared() {
            String s = shared;
            shared = null;
            return s == null ? "" : s;
        }
    }
}
