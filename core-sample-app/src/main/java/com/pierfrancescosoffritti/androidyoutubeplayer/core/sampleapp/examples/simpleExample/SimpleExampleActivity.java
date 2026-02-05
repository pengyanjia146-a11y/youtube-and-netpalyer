package com.pierfrancescosoffritti.androidyoutubeplayer.core.sampleapp.examples.simpleExample;

import android.content.res.Configuration;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.sampleapp.R;

public class SimpleExampleActivity extends AppCompatActivity {

  private YouTubePlayerView youTubePlayerView;
  private WebView neteaseWebView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_simple_example);

    initYouTubePlayer();
    initNeteaseWebView();
  }

  private void initYouTubePlayer() {
    youTubePlayerView = findViewById(R.id.youtube_player_view);
    getLifecycle().addObserver(youTubePlayerView);

    youTubePlayerView.addYouTubePlayerListener(new AbstractYouTubePlayerListener() {
      @Override
      public void onReady(@NonNull YouTubePlayer youTubePlayer) {
        // 默认加载 Lofi Girl
        youTubePlayer.loadVideo("jfKfPfyJRdk", 0);
      }
    });
  }

  private void initNeteaseWebView() {
    neteaseWebView = findViewById(R.id.netease_webview);
    WebSettings settings = neteaseWebView.getSettings();

    settings.setJavaScriptEnabled(true);
    settings.setDomStorageEnabled(true); // 登录关键
    settings.setDatabaseEnabled(true);
    settings.setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);

    neteaseWebView.setWebViewClient(new WebViewClient() {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    });

    neteaseWebView.loadUrl("https://music.163.com/m/");
  }

  @Override
  public void onConfigurationChanged(@NonNull Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
      youTubePlayerView.matchParent();
    } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
      youTubePlayerView.wrapContent();
    }
  }

  @Override
  public void onBackPressed() {
      if (neteaseWebView != null && neteaseWebView.canGoBack()) {
          neteaseWebView.goBack();
      } else {
          super.onBackPressed();
      }
  }
}
