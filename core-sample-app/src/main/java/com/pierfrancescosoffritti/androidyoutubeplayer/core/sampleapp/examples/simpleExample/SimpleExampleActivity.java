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
        // 预加载一个 Lofi 视频
        String videoId = "jfKfPfyJRdk";
        youTubePlayer.loadVideo(videoId, 0);
      }
    });
  }

  private void initNeteaseWebView() {
    neteaseWebView = findViewById(R.id.netease_webview);
    WebSettings settings = neteaseWebView.getSettings();

    // 🔴 开启 JS
    settings.setJavaScriptEnabled(true);
    
    // 🔴 关键设置：开启 DOM Storage
    // 这是解决网易云登录后刷新掉线问题的关键
    settings.setDomStorageEnabled(true);
    settings.setDatabaseEnabled(true);
    
    // 允许混合内容加载 (http/https)
    settings.setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);

    // 强制在当前 WebView 打开链接，不跳浏览器
    neteaseWebView.setWebViewClient(new WebViewClient() {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    });

    // 加载网易云移动版
    neteaseWebView.loadUrl("https://music.163.com/m/");
  }

  @Override
  public void onConfigurationChanged(@NonNull Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    // 处理横竖屏切换
    if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
      youTubePlayerView.matchParent();
    } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
      youTubePlayerView.wrapContent();
    }
  }
  
  // 处理返回键：网页优先后退
  @Override
  public void onBackPressed() {
      if (neteaseWebView != null && neteaseWebView.canGoBack()) {
          neteaseWebView.goBack();
      } else {
          super.onBackPressed();
      }
  }
}s
