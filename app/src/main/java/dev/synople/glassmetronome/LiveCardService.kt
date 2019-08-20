package dev.synople.glassmetronome

import com.google.android.glass.timeline.LiveCard
import com.google.android.glass.timeline.LiveCard.PublishMode

import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.ConnectivityManager
import android.os.IBinder
import android.util.Log
import android.widget.RemoteViews
import com.google.android.glass.media.Sounds

class LiveCardService : Service() {

    private var liveCard: LiveCard? = null
    private lateinit var remoteViews: RemoteViews
    private lateinit var broadcastReceiver: BroadcastReceiver
    private var metronomeThread: Thread? = null
    private lateinit var mediaPlayer: MediaPlayer

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (liveCard == null) {
            liveCard = LiveCard(this, LIVE_CARD_TAG)

            remoteViews = RemoteViews(packageName, R.layout.live_card)
            liveCard!!.setViews(remoteViews)

            remoteViews.setTextViewText(R.id.tvTempo, "$bpm bpm")
            mediaPlayer = MediaPlayer.create(this, R.raw.metronome)
            startStopMetronome()

            val menuIntent = Intent(this, LiveCardMenuActivity::class.java)
            liveCard!!.setAction(PendingIntent.getActivity(this, 0, menuIntent, 0))
            liveCard!!.publish(PublishMode.REVEAL)

            broadcastReceiver = MyBroadcastReceiver()
            val filter = IntentFilter(BPM_CHANGE_ACTION)
            registerReceiver(broadcastReceiver, filter)
        } else {
            liveCard!!.navigate()
        }
        return START_STICKY
    }

    private fun startStopMetronome() {
        metronomeThread = Thread(Runnable {
            while (true) {
                mediaPlayer.start()
                Thread.sleep(60_000L / bpm)
            }
        })
        metronomeThread?.start()
    }

    override fun onDestroy() {
        if (liveCard != null && liveCard!!.isPublished) {
            liveCard!!.unpublish()
            liveCard = null
        }
        unregisterReceiver(broadcastReceiver)
        metronomeThread?.interrupt()
        mediaPlayer.release()
        super.onDestroy()
    }

    companion object {
        const val BPM_CHANGE_ACTION = "dev.synople.glassmetronome.BPM_CHANGE"
        const val BPM = "bpm"
        private const val LIVE_CARD_TAG = "LiveCardService"
        var bpm = 120
    }

    class MyBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            (context as LiveCardService).remoteViews.setTextViewText(R.id.tvTempo, "$bpm bpm")
            context.liveCard?.setViews(context.remoteViews)
            context.startStopMetronome()
        }

    }
}
