package com.dreiri.stolpersteine.activities.bio;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ProgressBar;

import com.dreiri.stolpersteine.R;
import com.dreiri.stolpersteine.helpers.PreferenceHelper;

public class BioActivity extends Activity {

    public enum ViewFormat {
        TEXT, WEB;

        public static ViewFormat toViewFormat(String viewFormatString) {
            try {
                return valueOf(viewFormatString);
            } catch (Exception e) {
                return TEXT;
            }
        }
    };

    private ViewFormat viewFormat;
    private PreferenceHelper preferenceHelper;
    private WebView browser;
    private WebSettings settings;
    private String bioUrl;
    private static final String CSS_QUERY = "div#biografie_seite";

    public static Intent createIntent(Context context, String url) {
        Intent intent = new Intent(context, BioActivity.class);
        intent.putExtra("url", url);

        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_bio);
        Intent intent = getIntent();
        bioUrl = intent.getStringExtra("url");

        browser = (WebView) findViewById(R.id.webview);
        browser.setWebViewClient(new SimpleWebViewClient((ProgressBar) findViewById(R.id.progressBar)));
        settings = browser.getSettings();
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);

        preferenceHelper = new PreferenceHelper(this);
        viewFormat = preferenceHelper.readViewFormat();
        if (viewFormat == ViewFormat.WEB) {
            loadUrlInBrowser(browser, bioUrl);
        } else {
            loadContentInBrowser(browser, bioUrl, CSS_QUERY);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.bio, menu);
        // TODO: needs to update icon
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_view_format) {
            if (viewFormat == ViewFormat.TEXT) {
                switchToAndLoadInWebView(item);
            } else {
                switchToAndLoadInTextView(item);
            }
        } else if (itemId == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    protected void loadContentInBrowser(WebView browser, String url, String cssQuery) {
        new HTMLContentLoader(browser).loadContent(this, url, cssQuery);
    }

    protected void loadUrlInBrowser(WebView browser, String url) {
        browser.loadUrl(url);
    }

    private void switchToAndLoadInTextView(MenuItem selectedItem) {
        viewFormat = ViewFormat.TEXT;
        preferenceHelper.saveViewFormat(viewFormat);
        selectedItem.setTitle(R.string.action_item_web);
        selectedItem.setIcon(R.drawable.ic_action_view_as_web);
        settings.setLoadWithOverviewMode(false);
        settings.setUseWideViewPort(false);
        loadContentInBrowser(browser, bioUrl, CSS_QUERY);
    }

    private void switchToAndLoadInWebView(MenuItem selectedItem) {
        viewFormat = ViewFormat.WEB;
        preferenceHelper.saveViewFormat(viewFormat);
        selectedItem.setTitle(R.string.action_item_text);
        selectedItem.setIcon(R.drawable.ic_action_view_as_text);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        loadUrlInBrowser(browser, bioUrl);
    }

}
