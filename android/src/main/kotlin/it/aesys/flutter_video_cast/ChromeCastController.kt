package it.aesys.flutter_video_cast

import android.content.Context
import android.graphics.Color
import android.view.ContextThemeWrapper
import com.google.android.gms.cast.MediaInfo
import com.google.android.gms.cast.MediaLoadOptions
import com.google.android.gms.cast.framework.CastButtonFactory
import com.google.android.gms.cast.framework.CastContext
import com.google.android.gms.cast.framework.Session
import com.google.android.gms.cast.framework.SessionManagerListener
import com.google.android.gms.cast.framework.media.RemoteMediaClient
import com.google.android.gms.common.api.PendingResult
import com.google.android.gms.common.api.Status
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.platform.PlatformView
import java.util.HashMap

class ChromeCastController(
        messenger: BinaryMessenger,
        viewId: Int,
        context: Context?,
        args: Any?
) : PlatformView, MethodChannel.MethodCallHandler, SessionManagerListener<Session>, PendingResult.StatusListener {
    private val channel = MethodChannel(messenger, "flutter_video_cast/chromeCast_$viewId")
    private val chromeCastButton = ColorableMediaRouteButton(ContextThemeWrapper(context, R.style.Theme_AppCompat_NoActionBar))
    private val sessionManager = CastContext.getSharedInstance()?.sessionManager

    init {
        if (context != null) {
            CastButtonFactory.setUpMediaRouteButton(context, chromeCastButton)
        }
        applyTint(args)
        channel.setMethodCallHandler(this)
    }

    private fun applyTint(args: Any?){
        if (args is Map<*, *>) {
            val red = args["red"] as Int
            val green = args["green"] as Int
            val blue = args["blue"] as Int
            val alpha = args["alpha"] as Int
            chromeCastButton.post { chromeCastButton.applyTint(Color.argb(alpha, red, green, blue)) }
        }
    }

    private fun loadMedia(args: Any?) {
        if (args is Map<*, *>) {
            val url = args["url"] as? String
            val media = url?.let { MediaInfo.Builder(it).build() }
            val options = MediaLoadOptions.Builder().build()
            val request =
                media?.let { sessionManager?.currentCastSession?.remoteMediaClient?.load(it, options) }

            request?.addStatusListener(this)
            request?.addStatusListener { status -> if (status.isSuccess) {
                sessionManager?.currentCastSession?.remoteMediaClient?.removeProgressListener(mRemoteMediaClientListener)
                sessionManager?.currentCastSession?.remoteMediaClient?.addProgressListener(mRemoteMediaClientListener,2000)
            }}

        }
    }

    private val mRemoteMediaClientListener: RemoteMediaClient.ProgressListener =
        object : RemoteMediaClient.ProgressListener {
            override fun onProgressUpdated(p0: Long, p1: Long) {
                val event: MutableMap<String, Any> = HashMap()
                event["progress"] = p0
                channel.invokeMethod("chromeCast#progressChanged", event)
                //sendSeekToEvent(p0,true)
                //Log.d(TAG, "Cast Progress ${p0}")
            }

        }

    private fun play() {
        val request = sessionManager?.currentCastSession?.remoteMediaClient?.play()
        request?.addStatusListener(this)
    }

    private fun pause() {
        val request = sessionManager?.currentCastSession?.remoteMediaClient?.pause()
        request?.addStatusListener(this)
    }

    private fun seek(args: Any?) {
        if (args is Map<*, *>) {
            val relative = (args["relative"] as? Boolean) ?: false
            var interval = args["interval"] as? Double
            interval = interval?.times(1000)
            if (relative) {
                interval = interval?.plus(sessionManager?.currentCastSession?.remoteMediaClient?.mediaStatus?.streamPosition ?: 0)
            }
            val request = sessionManager?.currentCastSession?.remoteMediaClient?.seek(interval?.toLong() ?: 0)
            request?.addStatusListener(this)
        }
    }

    private fun stop() {
        val request = sessionManager?.currentCastSession?.remoteMediaClient?.stop()
        request?.addStatusListener(this)
    }

    private fun isPlaying() = sessionManager?.currentCastSession?.remoteMediaClient?.isPlaying ?: false

    private fun isConnected() = sessionManager?.currentCastSession?.isConnected ?: false

    private fun addSessionListener() {
        sessionManager?.addSessionManagerListener(this)
    }

    private fun removeSessionListener() {
        sessionManager?.removeSessionManagerListener(this)
    }

    override fun getView() = chromeCastButton

    override fun dispose() {
        sessionManager?.endCurrentSession(true)
    }

    // Flutter methods handling

    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        when(call.method) {
            "chromeCast#wait" -> result.success(null)
            "chromeCast#loadMedia" -> {
                loadMedia(call.arguments)
                result.success(null)
            }
            "chromeCast#play" -> {
                play()
                result.success(null)
            }
            "chromeCast#pause" -> {
                pause()
                result.success(null)
            }
            "chromeCast#seek" -> {
                seek(call.arguments)
                result.success(null)
            }
            "chromeCast#stop" -> {
                stop()
                result.success(null)
            }
            "chromeCast#isPlaying" -> result.success(isPlaying())
            "chromeCast#isConnected" -> result.success(isConnected())
            "chromeCast#addSessionListener" -> {
                addSessionListener()
                result.success(null)
            }
            "chromeCast#removeSessionListener" -> {
                removeSessionListener()
                result.success(null)
            }
            "chromeCast#dispose"-> {
                sessionManager?.endCurrentSession(true)
                result.success(null)
            }
        }
    }

    // SessionManagerListener

    override fun onSessionStarted(p0: Session, p1: String) {
        channel.invokeMethod("chromeCast#didStartSession", null)
    }

    override fun onSessionEnded(p0: Session, p1: Int) {
        channel.invokeMethod("chromeCast#didEndSession", null)
    }

    override fun onSessionResuming(p0: Session, p1: String) {

    }

    override fun onSessionResumed(p0: Session, p1: Boolean) {

    }

    override fun onSessionResumeFailed(p0: Session, p1: Int) {

    }

    override fun onSessionSuspended(p0: Session, p1: Int) {

    }

    override fun onSessionStarting(p0: Session) {

    }

    override fun onSessionEnding(p0: Session) {

    }

    override fun onSessionStartFailed(p0: Session, p1: Int) {

    }

    // PendingResult.StatusListener

    override fun onComplete(status: Status) {
        if (status.isSuccess) {
            channel.invokeMethod("chromeCast#requestDidComplete", null)
        }
    }
}
