package it.aesys.flutter_video_cast

import androidx.mediarouter.app.MediaRouteButton
import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.Log
import androidx.core.graphics.drawable.DrawableCompat
import java.lang.reflect.Field


class ColorableMediaRouteButton : MediaRouteButton {
    protected var mRemoteIndicatorDrawable: Drawable? = null

    constructor(context: Context?) : super(context) {}
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {}
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
    }

    override fun setRemoteIndicatorDrawable(d: Drawable?) {
        mRemoteIndicatorDrawable = d
        super.setRemoteIndicatorDrawable(d)
    }

    fun applyTint(color: Int) {
        /*val field: Field = MediaRouteButton::class.java.getDeclaredField("mRemoteIndicator")
        field.isAccessible = true
        val remoteIndicator = field.get(this) as Drawable
        remoteIndicator.callback = null
        unscheduleDrawable(remoteIndicator)
        val wrapDrawable = DrawableCompat.wrap(remoteIndicator)
        DrawableCompat.setTint(wrapDrawable, color)*/
        val wrapDrawable = mRemoteIndicatorDrawable?.let { DrawableCompat.wrap(it) }
        if (wrapDrawable != null) {
            DrawableCompat.setTint(wrapDrawable, color)
        }
    }
}