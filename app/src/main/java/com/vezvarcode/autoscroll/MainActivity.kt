package com.vezvarcode.autoscroll

import android.annotation.SuppressLint
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Html
import com.vezvarcode.autoscroll.databinding.ActivityMainBinding
import android.media.MediaPlayer
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MotionEvent
import android.view.ViewConfiguration
import android.widget.SeekBar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Exception


class MainActivity : AppCompatActivity() {
    
    
    companion object {
        private const val TAG = "MainActivityTAG"
    }

    private lateinit var mediaPlayer : MediaPlayer
    private val handler = Handler(Looper.getMainLooper())
    private val extras = HashMap<Int , Int>()

    private lateinit var binding : ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        initLyrics()
        initExtras()
        initPlayer()
        listeners()


    }

    private fun initLyrics() {
        binding.txtLyrics.text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(getString(R.string.lyrics), Html.FROM_HTML_MODE_COMPACT)
        } else {
            Html.fromHtml(getString(R.string.lyrics))
        }
    }

    private fun initExtras() {
        extras[125000] = 156000
    }

    private fun initPlayer() {
        mediaPlayer = MediaPlayer()
        val mediaPath = Uri.parse("android.resource://" + packageName + "/" + R.raw.music)
        try {
            mediaPlayer.setDataSource(applicationContext, mediaPath)
            mediaPlayer.prepare()
            mediaPlayer.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        handler.post(timeRunnable)

        mediaPlayer.setOnPreparedListener {
            binding.seekBar.max = it.duration
        }
    }

    private val timeRunnable = object : Runnable {
        override fun run() {

            val lyricsMax = binding.txtLyrics.height
            val musicMax = mediaPlayer.duration
            val musicProgress = mediaPlayer.currentPosition
            if (binding.switchAuto.isChecked) {


                CoroutineScope(Dispatchers.Default).launch {
                    var isExtra = false
                    var extraSeconds = 0
                    extras.forEach {
                        if (musicProgress in it.key..it.value) {
                            isExtra = true
                        }

                        if (musicProgress > it.value) {
                            extraSeconds += it.value - it.key
                        }
                    }

                    if (!isExtra) {
                        val y =
                            ((lyricsMax.toFloat() * (musicProgress.toFloat() - extraSeconds) / musicMax.toFloat()).toInt())
                        withContext(Dispatchers.Main) {
                            binding.scrollView.scrollTo(0, y)
                        }
                    }
                }
            }



            binding.seekBar.progress = musicProgress

            handler.postDelayed(this , 1000)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun listeners(){
        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, progress: Int, p2: Boolean) {
                if (p2)
                    mediaPlayer.seekTo(progress)
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {

            }

            override fun onStopTrackingTouch(p0: SeekBar?) {

            }

        })



        var startClickTime = 0L
        binding.scrollView.setOnTouchListener { view, motionEvent ->
            if (motionEvent.action == MotionEvent.ACTION_DOWN) {

                startClickTime = System.currentTimeMillis()

            } else if (motionEvent.action == MotionEvent.ACTION_UP) {

                if (System.currentTimeMillis() - startClickTime >= ViewConfiguration.getTapTimeout()) {

                    binding.switchAuto.isChecked = false
                }

            }

            false
        }


        binding.imgPlay.setOnClickListener {
            mediaPlayer.pause()
            mediaPlayer.stop()
            mediaPlayer.release()

            initPlayer()
        }
    }
}