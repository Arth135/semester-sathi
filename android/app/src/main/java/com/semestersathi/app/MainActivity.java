package com.semestersathi.app;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends Activity {
    private static final int POST_NOTIFICATIONS_REQUEST = 801;
    private final ArrayList<ReminderRequest> waitingForPermission = new ArrayList<>();
    private boolean permissionRequestInFlight = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ReminderReceiver.ensureChannel(this);
        getWindow().setStatusBarColor(Color.rgb(36, 105, 79));

        WebView webView = new WebView(this);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(false);
        // The app loads only its bundled, local HTML. The Gemini API is the only remote request.
        settings.setAllowFileAccessFromFileURLs(true);
        settings.setAllowUniversalAccessFromFileURLs(true);
        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient());
        webView.addJavascriptInterface(new StudyBridge(), "Android");
        setContentView(webView);
        webView.loadUrl("file:///android_asset/index.html");
    }

    private static final class ReminderRequest {
        final String id;
        final String title;
        final String date;
        final String time;

        ReminderRequest(String id, String title, String date, String time) {
            this.id = id;
            this.title = title;
            this.date = date;
            this.time = time;
        }
    }

    private final class StudyBridge {
        @JavascriptInterface
        public void scheduleReminder(String id, String title, String date, String time) {
            runOnUiThread(() -> scheduleOrAsk(new ReminderRequest(id, title, date, time)));
        }

        @JavascriptInterface
        public void cancelReminder(String id) {
            runOnUiThread(() -> ReminderReceiver.cancel(MainActivity.this, id));
        }
    }

    private void scheduleOrAsk(ReminderRequest request) {
        if (Build.VERSION.SDK_INT >= 33
                && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            waitingForPermission.add(request);
            if (!permissionRequestInFlight) {
                permissionRequestInFlight = true;
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, POST_NOTIFICATIONS_REQUEST);
            }
            return;
        }
        schedule(request);
    }

    private void schedule(ReminderRequest request) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);
        format.setLenient(false);
        try {
            Date parsed = format.parse(request.date + " " + request.time);
            if (parsed == null || parsed.getTime() <= System.currentTimeMillis()) {
                Toast.makeText(this, "This task time has already passed, so no reminder was set.", Toast.LENGTH_LONG).show();
                return;
            }
            ReminderReceiver.schedule(this, request.id, request.title, parsed.getTime());
        } catch (ParseException exception) {
            Toast.makeText(this, "Could not read the reminder date and time.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != POST_NOTIFICATIONS_REQUEST) return;
        permissionRequestInFlight = false;
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            for (ReminderRequest request : new ArrayList<>(waitingForPermission)) schedule(request);
        } else {
            Toast.makeText(this, "Allow notifications in Android settings to receive study reminders.", Toast.LENGTH_LONG).show();
        }
        waitingForPermission.clear();
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onBackPressed() {
        super.onBackPressed();
    }
}
