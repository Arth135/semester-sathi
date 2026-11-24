package com.semestersathi.app;

import android.Manifest;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

public class MainActivity extends Activity {
    private static final int POST_NOTIFICATIONS_REQUEST = 801;
    private static final String LEGACY_FILE_URL = "file:///android_asset/index.html";
    private static final String APP_BASE_URL = "https://appassets.androidplatform.net/";
    private static final String MIGRATION_PREFS = "semester_sathi_webview_migration";
    private static final String MIGRATED_STATE_KEY = "legacy_state";
    private static final String MIGRATION_DONE_KEY = "https_origin_migration_done";
    private static final String API_KEY_PREFS = "semester_sathi_secure_settings";
    private static final String API_KEY_VALUE = "encrypted_gemini_api_key";
    private static final String API_KEY_ALIAS = "semester_sathi_gemini_api_key";

    private final ArrayList<ReminderRequest> waitingForPermission = new ArrayList<>();
    private boolean permissionRequestInFlight = false;
    private volatile String pendingMigrationApiKey = "";
    private SharedPreferences migrationPrefs;
    private WebView webView;

    @Override
    @SuppressWarnings("deprecation")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ReminderReceiver.ensureChannel(this);
        getWindow().setStatusBarColor(Color.rgb(36, 105, 79));
        migrationPrefs = getSharedPreferences(MIGRATION_PREFS, MODE_PRIVATE);

        webView = new WebView(this);
        webView.setVisibility(View.INVISIBLE);
        WebSettings settings = webView.getSettings();
        boolean migrationDone = migrationPrefs.getBoolean(MIGRATION_DONE_KEY, false);
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setAllowFileAccess(!migrationDone);
        settings.setAllowContentAccess(false);
        settings.setAllowFileAccessFromFileURLs(false);
        settings.setAllowUniversalAccessFromFileURLs(false);
        settings.setJavaScriptCanOpenWindowsAutomatically(false);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (!migrationPrefs.getBoolean(MIGRATION_DONE_KEY, false)
                        && LEGACY_FILE_URL.equals(url)) {
                    view.evaluateJavascript(
                            "(function(){var s=localStorage.getItem('semester-sathi-demo-v1')||'';" +
                                    "var k=sessionStorage.getItem('semester-sathi-gemini-key')||'';" +
                                    "Android.captureLegacyData(s,k);return 'captured';})()",
                            null);
                    return;
                }
                if (url != null && url.startsWith(APP_BASE_URL)) {
                    view.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return !isAppUrl(request.getUrl().toString());
            }

            @Override
            @SuppressWarnings("deprecation")
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return !isAppUrl(url);
            }
        });
        webView.setWebChromeClient(new WebChromeClient());
        webView.addJavascriptInterface(new StudyBridge(), "Android");
        setContentView(webView);

        if (migrationDone) {
            loadBundledPage();
        } else {
            // Load the previous origin once so existing localStorage and sessionStorage can be migrated.
            webView.loadUrl(LEGACY_FILE_URL);
        }
    }

    private boolean isAppUrl(String url) {
        if (url == null) return false;
        if (url.startsWith(APP_BASE_URL)) return true;
        return LEGACY_FILE_URL.equals(url) && !migrationPrefs.getBoolean(MIGRATION_DONE_KEY, false);
    }

    private void loadBundledPage() {
        try (InputStream input = getAssets().open("index.html");
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[8192];
            int count;
            while ((count = input.read(buffer)) != -1) output.write(buffer, 0, count);
            String html = output.toString("UTF-8");
            webView.loadDataWithBaseURL(APP_BASE_URL, html, "text/html", "UTF-8", APP_BASE_URL);
        } catch (IOException exception) {
            Toast.makeText(this, "Semester Sathi could not load its offline app files.", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void completeOriginMigration(String serializedState, String apiKey) {
        pendingMigrationApiKey = apiKey == null ? "" : apiKey;
        migrationPrefs.edit()
                .putString(MIGRATED_STATE_KEY, serializedState == null ? "" : serializedState)
                .putBoolean(MIGRATION_DONE_KEY, true)
                .apply();
        runOnUiThread(() -> {
            webView.getSettings().setAllowFileAccess(false);
            loadBundledPage();
        });
    }

    private String consumeMigrationPayload() {
        String legacyState = migrationPrefs.getString(MIGRATED_STATE_KEY, "");
        String apiKey = pendingMigrationApiKey;
        if ((legacyState == null || legacyState.isEmpty()) && apiKey.isEmpty()) return "";
        JSONObject payload = new JSONObject();
        try {
            payload.put("state", legacyState == null ? "" : legacyState);
            payload.put("apiKey", apiKey);
        } catch (JSONException ignored) {
            return "";
        }
        migrationPrefs.edit().remove(MIGRATED_STATE_KEY).apply();
        pendingMigrationApiKey = "";
        return payload.toString();
    }

    private SecretKey getOrCreateApiKeySecret() throws Exception {
        KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);
        if (keyStore.containsAlias(API_KEY_ALIAS)) {
            return (SecretKey) keyStore.getKey(API_KEY_ALIAS, null);
        }
        KeyGenerator generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
        generator.init(new KeyGenParameterSpec.Builder(API_KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setRandomizedEncryptionRequired(true)
                .setKeySize(256)
                .build());
        return generator.generateKey();
    }

    private String readSavedGeminiApiKey() {
        String encrypted = getSharedPreferences(API_KEY_PREFS, MODE_PRIVATE).getString(API_KEY_VALUE, "");
        if (encrypted.isEmpty()) return "";
        try {
            byte[] packed = Base64.decode(encrypted, Base64.NO_WRAP);
            if (packed.length <= 12) throw new IllegalArgumentException("Invalid encrypted key.");
            byte[] iv = Arrays.copyOfRange(packed, 0, 12);
            byte[] ciphertext = Arrays.copyOfRange(packed, 12, packed.length);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, getOrCreateApiKeySecret(), new GCMParameterSpec(128, iv));
            return new String(cipher.doFinal(ciphertext), StandardCharsets.UTF_8);
        } catch (Exception exception) {
            getSharedPreferences(API_KEY_PREFS, MODE_PRIVATE).edit().remove(API_KEY_VALUE).apply();
            return "";
        }
    }

    private String saveGeminiApiKey(String apiKey) {
        if (apiKey == null || apiKey.trim().isEmpty()) return "error";
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, getOrCreateApiKeySecret());
            byte[] iv = cipher.getIV();
            byte[] ciphertext = cipher.doFinal(apiKey.trim().getBytes(StandardCharsets.UTF_8));
            byte[] packed = new byte[iv.length + ciphertext.length];
            System.arraycopy(iv, 0, packed, 0, iv.length);
            System.arraycopy(ciphertext, 0, packed, iv.length, ciphertext.length);
            String encoded = Base64.encodeToString(packed, Base64.NO_WRAP);
            boolean saved = getSharedPreferences(API_KEY_PREFS, MODE_PRIVATE).edit()
                    .putString(API_KEY_VALUE, encoded).commit();
            return saved ? "saved" : "error";
        } catch (Exception exception) {
            return "error";
        }
    }

    private void clearSavedGeminiApiKey() {
        getSharedPreferences(API_KEY_PREFS, MODE_PRIVATE).edit().remove(API_KEY_VALUE).commit();
        try {
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            if (keyStore.containsAlias(API_KEY_ALIAS)) keyStore.deleteEntry(API_KEY_ALIAS);
        } catch (Exception ignored) { }
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
        public void captureLegacyData(String serializedState, String apiKey) {
            completeOriginMigration(serializedState, apiKey);
        }

        @JavascriptInterface
        public String consumeMigratedData() {
            return consumeMigrationPayload();
        }

        @JavascriptInterface
        public String getSavedGeminiApiKey() {
            return readSavedGeminiApiKey();
        }

        @JavascriptInterface
        public String saveGeminiApiKey(String apiKey) {
            return MainActivity.this.saveGeminiApiKey(apiKey);
        }

        @JavascriptInterface
        public void clearSavedGeminiApiKey() {
            MainActivity.this.clearSavedGeminiApiKey();
        }

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
