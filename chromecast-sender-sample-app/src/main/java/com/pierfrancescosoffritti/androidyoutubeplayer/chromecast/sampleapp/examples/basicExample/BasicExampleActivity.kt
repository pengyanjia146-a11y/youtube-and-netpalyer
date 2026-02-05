package com.pierfrancescosoffritti.androidyoutubeplayer.chromecast.sampleapp.examples.basicExample

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import com.pierfrancescosoffritti.cyplayersample.R
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class BasicExampleActivity : AppCompatActivity() {

    // --- UI 组件 (lateinit var 允许稍后初始化) ---
    private lateinit var youtubeView: YouTubePlayerView
    private lateinit var neteaseCard: CardView
    private lateinit var bgImage: ImageView
    private lateinit var albumCover: ImageView
    private lateinit var titleText: TextView
    private lateinit var artistText: TextView
    private lateinit var searchInput: EditText
    private lateinit var searchBtn: Button
    private lateinit var playBtn: ImageView
    private lateinit var seekBar: SeekBar

    // --- 核心逻辑组件 ---
    // 必须是 var，因为我们会赋值为 null 或具体对象
    private var activeYouTubePlayer: YouTubePlayer? = null 
    private var mediaPlayer: MediaPlayer? = null
    private val httpClient = OkHttpClient() // client 是 val，因为实例不变
    
    // --- 状态标记 (必须是 var，因为会随播放状态改变) ---
    private var isPlaying = false
    private var currentMode = MODE_NONE 

    companion object {
        const val MODE_NONE = "NONE"
        const val MODE_YT = "YT"
        const val MODE_NE = "NE"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_basic_example)

        initViews()
        initYouTube()
        initMediaPlayer()
        setupListeners()
    }

    private fun initViews() {
        youtubeView = findViewById(R.id.youtube_player_view)
        neteaseCard = findViewById(R.id.netease_card)
        bgImage = findViewById(R.id.bg_image)
        albumCover = findViewById(R.id.album_cover)
        titleText = findViewById(R.id.song_title)
        artistText = findViewById(R.id.song_artist)
        searchInput = findViewById(R.id.search_input)
        searchBtn = findViewById(R.id.btn_search)
        playBtn = findViewById(R.id.btn_play_toggle)
        seekBar = findViewById(R.id.player_seekbar)

        // 初始状态：显示网易云封面，隐藏 YouTube
        youtubeView.visibility = View.GONE
        neteaseCard.visibility = View.VISIBLE
        
        lifecycle.addObserver(youtubeView)
    }

    private fun initYouTube() {
        youtubeView.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
            override fun onReady(youTubePlayer: YouTubePlayer) {
                activeYouTubePlayer = youTubePlayer
            }
            override fun onStateChange(youTubePlayer: YouTubePlayer, state: com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants.PlayerState) {
                if (state == com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants.PlayerState.PLAYING) {
                    isPlaying = true // 这里修改 var 变量
                    updatePlayIcon(true)
                } else if (state == com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants.PlayerState.PAUSED) {
                    isPlaying = false // 这里修改 var 变量
                    updatePlayIcon(false)
                }
            }
        })
    }

    private fun initMediaPlayer() {
        mediaPlayer = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
            setOnCompletionListener { 
                isPlaying = false
                updatePlayIcon(false)
            }
        }
    }

    private fun setupListeners() {
        searchBtn.setOnClickListener { performSearch() }
        
        searchInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch()
                true
            } else false
        }

        playBtn.setOnClickListener { togglePlayState() }
    }

    private fun performSearch() {
        val query = searchInput.text.toString().trim()
        if (query.isEmpty()) return

        // 收起键盘
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(searchInput.windowToken, 0)

        // 逻辑分流
        if (query.startsWith("yt:") || query.length == 11) {
            val videoId = if (query.startsWith("yt:")) query.substring(3) else query
            switchToYouTubeMode(videoId)
        } else {
            switchToNeteaseMode(query)
        }
    }

    // --- 模式切换 ---
    private fun switchToYouTubeMode(videoId: String) {
        currentMode = MODE_YT // 修改 var
        mediaPlayer?.pause()
        
        youtubeView.visibility = View.VISIBLE
        neteaseCard.visibility = View.GONE
        
        updateMetadata("YouTube Video", "Loading...")
        
        activeYouTubePlayer?.loadVideo(videoId, 0f)
        isPlaying = true
        updatePlayIcon(true)
    }

    private fun switchToNeteaseMode(keyword: String) {
        currentMode = MODE_NE // 修改 var
        activeYouTubePlayer?.pause()
        
        youtubeView.visibility = View.GONE
        neteaseCard.visibility = View.VISIBLE
        
        updateMetadata("Searching...", keyword)

        // 模拟 API 请求
        val searchUrl = "https://music.163.com/api/search/get/web?s=$keyword&type=1&offset=0&total=true&limit=1"
        val request = Request.Builder()
            .url(searchUrl)
            .header("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)")
            .header("Referer", "https://music.163.com/")
            .header("Cookie", "appver=1.5.0.75771")
            .build()

        httpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread { 
                    Toast.makeText(this@BasicExampleActivity, "Network Error", Toast.LENGTH_SHORT).show() 
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string() ?: return
                try {
                    val json = JSONObject(body)
                    val result = json.optJSONObject("result")
                    val songs = result?.optJSONArray("songs")
                    
                    if (songs != null && songs.length() > 0) {
                        val song = songs.getJSONObject(0)
                        val songId = song.getLong("id")
                        val songName = song.getString("name")
                        val artists = song.getJSONArray("artists")
                        val artistName = if (artists.length() > 0) artists.getJSONObject(0).getString("name") else "Unknown"
                        val album = song.getJSONObject("album")
                        val coverUrl = album.getString("picUrl")
                        val playUrl = "http://music.163.com/song/media/outer/url?id=$songId.mp3"

                        runOnUiThread {
                            updateMetadata(songName, artistName)
                            playNeteaseStream(playUrl, coverUrl)
                        }
                    } else {
                        runOnUiThread { 
                            Toast.makeText(this@BasicExampleActivity, "No songs found", Toast.LENGTH_SHORT).show() 
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        })
    }

    private fun playNeteaseStream(url: String, coverUrl: String) {
        try {
            mediaPlayer?.reset()
            mediaPlayer?.setDataSource(url)
            mediaPlayer?.prepareAsync()
            mediaPlayer?.setOnPreparedListener { 
                it.start()
                isPlaying = true // 修改 var
                updatePlayIcon(true)
            }

            // 加载封面
            Glide.with(this).load(coverUrl).into(albumCover)
            
            // 背景高斯模糊 (如果库不可用则忽略)
            try {
                Glide.with(this)
                    .load(coverUrl)
                    .apply(RequestOptions.bitmapTransform(jp.wasabeef.glide.transformations.BlurTransformation(25, 3)))
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(bgImage)
            } catch (e: Exception) {
               // Ignore
            }

        } catch (e: Exception) {
            Toast.makeText(this, "Play Error", Toast.LENGTH_SHORT).show()
        }
    }

    private fun togglePlayState() {
        if (currentMode == MODE_YT) {
            if (isPlaying) activeYouTubePlayer?.pause() else activeYouTubePlayer?.play()
        } else if (currentMode == MODE_NE) {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.pause()
                isPlaying = false // 修改 var
            } else {
                mediaPlayer?.start()
                isPlaying = true // 修改 var
            }
            updatePlayIcon(isPlaying)
        }
    }

    private fun updateMetadata(title: String, artist: String) {
        titleText.text = title
        artistText.text = artist
    }

    private fun updatePlayIcon(playing: Boolean) {
        val iconRes = if (playing) R.drawable.ayp_ic_pause_36dp else R.drawable.ayp_ic_play_36dp
        playBtn.setImageResource(iconRes)
    }

    override fun onDestroy() {
        super.onDestroy()
        youtubeView.release()
        mediaPlayer?.release()
    }
}
