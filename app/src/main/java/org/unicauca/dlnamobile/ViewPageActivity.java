package org.unicauca.dlnamobile;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Activity;
import android.view.Window;
import android.webkit.WebView;

public class ViewPageActivity extends Activity {
    private WebView rep;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_view_page);
        rep = (WebView)findViewById(R.id.webRepro);
        rep.getSettings().setJavaScriptEnabled(true);
        rep.getSettings().setLoadsImagesAutomatically(true);
        rep.clearCache(true);
        prefs = getSharedPreferences("Preferencias",
                getApplicationContext().MODE_PRIVATE);
        prefs = getSharedPreferences("Preferencias", Context.MODE_PRIVATE);
        rep.loadUrl("http://"+prefs.getString("ip", "")+"/reproductor");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        rep.loadUrl("");
        finish();
    }
}
