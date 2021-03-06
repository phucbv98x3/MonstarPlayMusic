package com.monstar_lab_lifetime.monstarplaymusic.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.MutableLiveData
import com.monstar_lab_lifetime.monstarplaymusic.R
import com.monstar_lab_lifetime.monstarplaymusic.model.Music
import com.monstar_lab_lifetime.monstarplaymusic.view.HomeActivity
import com.monstar_lab_lifetime.monstarplaymusic.view.MusicManager
import com.monstar_lab_lifetime.monstarplaymusic.viewmodel.MusicViewModel

class MusicService : Service() {

    var musicFromService = MutableLiveData<Music>()
    private var mNotificationManager :NotificationManager?=null
    companion object {
        const val ACTION_PLAY = "play"
        const val ACTION_PREVIOUS = "previous"
        const val ACTION_NEXT = "next"
        const val ACTION_CLOSE = "close"
    }
    private lateinit var mMusicViewModel: MusicViewModel
    private var mMusicManager: MusicManager? = null
    fun getMusicManager() = mMusicManager
   // fun getModel() = mMusicViewModel

    override fun onCreate() {
        super.onCreate()
        mMusicViewModel = MusicViewModel()
        mMusicManager = MusicManager()
        registerChanel()

    }


    override fun onBind(intent: Intent?): IBinder? {
        return MyBinder(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("st", "stcm")
        return START_REDELIVER_INTENT
    }


    fun playMusic(item: Music) {
        mMusicManager?.setData(this, item.uri)
        createNotificationMusic(item)
    }

    fun pauseMusic(item: Music) {
        mMusicManager?.pause()
        createNotificationMusic(item)
    }

    fun continuePlayMusic(item: Music) {
        mMusicManager?.continuePlay()
        createNotificationMusic(item)
    }

//    fun stopMusic(item: Music) {
//        mMusicManager?.stop()
//
//    }



    class MyBinder(musicService: MusicService) : Binder() {
        var getService: MusicService = musicService
    }

    private fun registerChanel() {
        mNotificationManager= getSystemService(Context.NOTIFICATION_SERVICE)  as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "MusicService",
                "MusicService",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            channel.description = "YOUR_NOTIFICATION_CHANNEL_DESCRIPTION"
            mNotificationManager?.createNotificationChannel(channel)
        }
    }

    private fun isPlaying():Boolean{
        if (getMusicManager()?.mMediaPlayer?.isPlaying!!){
            return true
        }
        return false
    }
    private fun createNotificationMusic(item: Music) {
        Log.d("no", item.toString())
        musicFromService.value = item
        //chỗ này log ra nó đã nhận bài hát đây rồi
        val notification = NotificationCompat.Builder(
            this,
            "MusicService"
        )
        val intent = Intent(this, HomeActivity::class.java)
//        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
//        intent.putExtra("re", "okbalo")
        val intentContentActivity = PendingIntent.getActivity(this, 1, intent, 0)
        val intentBroadPlay = Intent().setAction(ACTION_PLAY)
        val actionIntentPlay =
            PendingIntent.getBroadcast(this, 0, intentBroadPlay, PendingIntent.FLAG_UPDATE_CURRENT)
        val intentBroadPrevious = Intent().setAction(ACTION_PREVIOUS)
        val actionIntentPrevious = PendingIntent.getBroadcast(
            this,
            0,
            intentBroadPrevious,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        val intentBroadNext = Intent().setAction(ACTION_NEXT)
        val actionIntentNext =
            PendingIntent.getBroadcast(this, 0, intentBroadNext, PendingIntent.FLAG_UPDATE_CURRENT)
        val intentBroadClose = Intent().setAction(ACTION_CLOSE)
        val actionIntentClose =
            PendingIntent.getBroadcast(this, 0, intentBroadClose, PendingIntent.FLAG_UPDATE_CURRENT)
        notification.addAction(R.drawable.icon_previous_notifi, "previous", actionIntentPrevious)
        notification.addAction(if (isPlaying())R.drawable.icon_notifi else R.drawable.ic_baseline_play_arrow_24, "play", actionIntentPlay)
        notification.addAction(R.drawable.icon_next_notifi, "next", actionIntentNext)
        notification.addAction(R.drawable.ic_baseline_close_24, "Close", actionIntentClose)
        notification.setContentIntent(intentContentActivity)
        notification.setContentTitle("Music Offline From Phúc")
        notification.setContentText(item.nameMusic)
        notification.setSmallIcon(R.drawable.ic_baseline_library_music_24)
        notification.priority = NotificationCompat.PRIORITY_LOW
        notification.setStyle(androidx.media.app.NotificationCompat.MediaStyle())
        notification.setLargeIcon(
            BitmapFactory.decodeResource(
                resources,
                R.drawable.nhaccuatui
            )
        )


        startForeground(10, notification.build())

    }
    override fun onDestroy() {
        Log.d("si", "ondestroy")
        super.onDestroy()

        mMusicManager?.release()
    }
}