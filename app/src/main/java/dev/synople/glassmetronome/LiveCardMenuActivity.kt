package dev.synople.glassmetronome

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import dev.synople.glassmetronome.LiveCardService.Companion.BPM
import dev.synople.glassmetronome.LiveCardService.Companion.BPM_CHANGE_ACTION
import dev.synople.glassmetronome.LiveCardService.Companion.bpm
import kotlinx.android.synthetic.main.live_card.*
import java.util.*

class LiveCardMenuActivity : Activity() {

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        openOptionsMenu()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.live_card, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_decrease -> {
                bpm -= 10
                Intent().also { intent ->
                    intent.action = BPM_CHANGE_ACTION
                    intent.putExtra(BPM, bpm)
                    sendBroadcast(intent)
                }
                return true
            }
            R.id.action_increase -> {
                bpm += 10
                Intent().also { intent ->
                    intent.action = BPM_CHANGE_ACTION
                    intent.putExtra(BPM, bpm)
                    sendBroadcast(intent)
                }
                return true
            }
            R.id.action_stop -> {
                stopService(Intent(this, LiveCardService::class.java))
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onOptionsMenuClosed(menu: Menu) {
        super.onOptionsMenuClosed(menu)
        finish()
    }
}
