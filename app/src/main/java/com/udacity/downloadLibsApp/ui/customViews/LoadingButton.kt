package com.udacity.downloadLibsApp.ui.customViews

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.os.SystemClock.sleep
import android.util.AttributeSet
import android.view.View
import com.udacity.downloadLibsApp.R
import timber.log.Timber
import kotlin.properties.Delegates

class LoadingButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var widthSize = 0
    private var heightSize = 0

    private val rect = RectF()

    private val valueAnimator = ValueAnimator()

    private var buttonState: ButtonState by Delegates.observable<ButtonState>(ButtonState.Completed) { p, old, new ->
        Timber.d("Button State changed: ${resources.getString(buttonState.labelResource)}")
    }

    init {
        isClickable = true
    }

    private val paintRect = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        strokeWidth = 2f
        color = resources.getColor(R.color.colorPrimary, context.theme)
    }

    private val paintText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = Typeface.create("", Typeface.BOLD)
        textAlign = Paint.Align.CENTER
        textSize = 55.0f
        color = Color.WHITE
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.drawRect(rect, paintRect)
        canvas?.drawText(resources.getString(buttonState.labelResource), widthSize / 2f, heightSize/1.6f, paintText)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minw: Int = paddingLeft + paddingRight + suggestedMinimumWidth
        val w: Int = resolveSizeAndState(minw, widthMeasureSpec, 1)
        val h: Int = resolveSizeAndState(
            MeasureSpec.getSize(w),
            heightMeasureSpec,
            0
        )
        widthSize = w
        heightSize = h
        setMeasuredDimension(w, h)

        val offset = paintRect.strokeWidth / 2f // offset used to keep the edges inside the visible rect

        rect.top = paddingTop + offset
        rect.left = paddingStart + offset
        rect.right = measuredWidth - paddingEnd - offset
        rect.bottom = measuredHeight - paddingBottom - offset
    }

    override fun performClick(): Boolean {
        super.performClick()

        Timber.d("Button was clicked")

        buttonState = ButtonState.Clicked

        invalidate()

        sleep(4000)

        buttonState = ButtonState.Loading

        sleep(4000)

        buttonState = ButtonState.Completed

        return true
    }
}
