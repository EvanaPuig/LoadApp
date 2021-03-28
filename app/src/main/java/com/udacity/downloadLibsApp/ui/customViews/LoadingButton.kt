package com.udacity.downloadLibsApp.ui.customViews

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import com.udacity.downloadLibsApp.R
import timber.log.Timber
import kotlin.math.min
import kotlin.properties.Delegates

class LoadingButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var widthSize = 0
    private var heightSize = 0

    private val rect = RectF()

    private val progressCircleRect = RectF()
    private var progressCircleSize = START_CIRCLE_ANGLE
    private var currentProgressCircleAnimationValue = START_CIRCLE_ANGLE

    private var currentButtonText = ""
    private var currentProgressBackgroundAnimationValue = START_BUTTON_BACKGROUND_POSITION

    // Button rectangle paint
    private val paintRect = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        strokeWidth = 2f
        color = resources.getColor(R.color.colorPrimary, context.theme)
    }

    // Button text paint
    private val paintText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = Typeface.create("", Typeface.BOLD)
        textAlign = Paint.Align.CENTER
        textSize = 55.0f
        color = Color.WHITE
    }

    // Button State
    private var buttonState: ButtonState by Delegates.observable<ButtonState>(ButtonState.Completed) { _, _, newState ->
        Timber.d("Button state changed to $newState")
        currentButtonText = resources.getString(newState.labelResource)
        when(newState) {
            ButtonState.Clicked -> {
                Timber.d("Button is in clicked state")
                isClickable = false
                invalidate()
            }
            ButtonState.Completed -> {
                Timber.d("Button is in completed state")
                animatorSet.cancel()
                isClickable = true
                invalidate()
            }
            ButtonState.Loading -> {
                Timber.d("Button is in loading state")
                measureProgressCircle()
                animatorSet.start()
                isClickable = false
                invalidate()
            }
        }
    }

    // Circle Animator
    private val circleAnimator = ValueAnimator.ofFloat(START_CIRCLE_ANGLE, END_CIRCLE_ANGLE).apply {
        repeatMode = ValueAnimator.RESTART
        repeatCount = ValueAnimator.INFINITE
        interpolator = LinearInterpolator()
        addUpdateListener {
            currentProgressCircleAnimationValue = it.animatedValue as Float
            invalidate()
        }
    }

    // Background Animator
    private val buttonBackgroundAnimator = ValueAnimator.ofFloat(0f, widthSize.toFloat()).apply {
            repeatMode = ValueAnimator.RESTART
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()
            addUpdateListener {
                currentProgressBackgroundAnimationValue = it.animatedValue as Float
                invalidate()
            }
        }

    // Both animations
    private val animatorSet: AnimatorSet = AnimatorSet().apply {
        duration = ANIMATION_TIME
        playTogether(circleAnimator, buttonBackgroundAnimator)
    }

    init {
        isClickable = true
        buttonState = ButtonState.Completed
        currentButtonText = resources.getString(ButtonState.Completed.labelResource)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.let {
            it.drawBackgroundColor()
            it.drawButtonText()
            it.drawProgressCircle()
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        progressCircleSize = (min(w, h) / 2) * PROGRESS_CIRCLE_SIZE_INCREASE
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

    fun changeButtonState(state: ButtonState) {
        if (state != buttonState) {
            buttonState = state
            invalidate()
        }
    }

    private fun Canvas.drawProgressCircle() {
        if (buttonState != ButtonState.Loading) return

        paintRect.color = resources.getColor(R.color.colorAccent, context.theme)
        this.drawArc(
            progressCircleRect,
            0f,
            currentProgressCircleAnimationValue,
            true,
            paintRect
        )
    }

    private fun measureProgressCircle() {
        val textLimits = Rect()
        paintText.getTextBounds(currentButtonText, 0, currentButtonText.length, textLimits)
        val horizontalCenter =
            (textLimits.right + textLimits.width() + PROGRESS_CIRCLE_PADDING)
        val verticalCenter = (heightSize / 2)

        progressCircleRect.set(
            horizontalCenter - progressCircleSize,
            verticalCenter - progressCircleSize,
            horizontalCenter + progressCircleSize,
            verticalCenter + progressCircleSize
        )
    }

    private fun Canvas.drawLoadingBackgroundColor() = paintRect.apply {
        color = resources.getColor(R.color.colorPrimaryDark, context.theme)
    }.run {
        drawRect(
            0f,
            0f,
            currentProgressBackgroundAnimationValue,
            heightSize.toFloat(),
            paintRect
        )
    }

    private fun Canvas.drawDefaultBackgroundColor() = paintRect.apply {
        color = resources.getColor(R.color.colorPrimary, context.theme)
    }.run {
        drawRect(
            currentProgressBackgroundAnimationValue,
            0f,
            widthSize.toFloat(),
            heightSize.toFloat(),
            paintRect
        )
    }

    private fun Canvas.drawBackgroundColor() {
        when (buttonState) {
            ButtonState.Loading -> {
                drawLoadingBackgroundColor()
                drawDefaultBackgroundColor()
            }
            else -> drawColor(resources.getColor(R.color.colorPrimary, context.theme))
        }
    }

    private fun Canvas.drawButtonText() {
        paintText.color = Color.WHITE
        drawText(currentButtonText, widthSize / TEXT_X_COORDINATE, heightSize / TEXT_Y_COORDINATE, paintText)
    }

    companion object {
        private const val START_CIRCLE_ANGLE = 0f
        private const val END_CIRCLE_ANGLE = 360f
        private const val ANIMATION_TIME = 1000L
        private const val START_BUTTON_BACKGROUND_POSITION = 0f
        private const val PROGRESS_CIRCLE_PADDING = 16f
        private const val PROGRESS_CIRCLE_SIZE_INCREASE = 0.4f
        private const val TEXT_Y_COORDINATE = 1.6f
        private const val TEXT_X_COORDINATE = 2f
    }
}
