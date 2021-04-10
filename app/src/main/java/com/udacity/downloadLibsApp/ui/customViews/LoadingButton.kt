package com.udacity.downloadLibsApp.ui.customViews

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.content.ContextCompat
import androidx.core.content.withStyledAttributes
import com.udacity.downloadLibsApp.R
import timber.log.Timber
import kotlin.math.min
import kotlin.properties.Delegates

class LoadingButton @JvmOverloads constructor(
    context: Context, private val attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var widthSize = 0
    private var heightSize = 0

    private val rect = RectF()

    private val progressCircleRect = RectF()
    private var progressCircleSize = START_CIRCLE_ANGLE
    private var currentProgressCircleAnimationValue = START_CIRCLE_ANGLE

    private var currentButtonText = ""
    private var currentProgressBackgroundAnimationValue = START_BUTTON_BACKGROUND_POSITION

    private var loadingDefaultBackgroundColor = 0
    private var loadingBackgroundColor = 0
    private var loadingDefaultText: CharSequence = ""
    private var loadingText: CharSequence = ""
    private var loadingTextColor = 0
    private var progressCircleBackgroundColor = 0

    // Button rectangle paint
    private val paintRect = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        strokeWidth = STROKE_SIZE
        color = resources.getColor(R.color.colorPrimary, context.theme)
    }

    // Button text paint
    private val paintText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = Typeface.create("", Typeface.BOLD)
        textAlign = Paint.Align.CENTER
        textSize = TEXT_SIZE
        color = Color.WHITE
    }

    // Button State
    private var buttonState: ButtonState by Delegates.observable(ButtonState.Completed) { _, _, newState ->
        when(newState) {
            ButtonState.Loading -> {
                Timber.d("Button is in loading state")
                currentButtonText = loadingText.toString()
                measureProgressCircle()
                animatorSet.start()
                isClickable = false
                invalidate()
            }
            else -> {
                Timber.d("Button is in $newState state")
                currentButtonText = loadingDefaultText.toString()
                animatorSet.cancel()
                isClickable = true
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
    private lateinit var buttonBackgroundAnimator: ValueAnimator

    // Both animations
    private val animatorSet: AnimatorSet = AnimatorSet().apply {
        duration = ANIMATION_TIME
    }

    init {
        isClickable = true
        buttonState = ButtonState.Completed

        context.withStyledAttributes(attrs, R.styleable.LoadingButton) {
            loadingDefaultBackgroundColor = getColor(R.styleable.LoadingButton_loadingDefaultBackgroundColor, 0)
            loadingBackgroundColor = getColor(R.styleable.LoadingButton_loadingBackgroundColor, 0)
            loadingDefaultText = getText(R.styleable.LoadingButton_loadingDefaultText)
            loadingTextColor = getColor(R.styleable.LoadingButton_loadingTextColor, 0)
            loadingText = getText(R.styleable.LoadingButton_loadingText)
        }.also {
            currentButtonText = loadingDefaultText.toString()
            progressCircleBackgroundColor = ContextCompat.getColor(context, R.color.colorAccent)
        }

    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.let {
            it.apply {
                drawBackgroundColor()
                drawButtonText()
                drawProgressCircle()
            }
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        progressCircleSize = (min(w, h) / HALF_DIVIDER) * PROGRESS_CIRCLE_SIZE_INCREASE
        setupButtonBackgroundAnimator()
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

        val offset = paintRect.strokeWidth / HALF_DIVIDER // offset used to keep the edges inside the visible rect

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

    private fun AnimatorSet.playProgressCircleAndButtonBackgroundTogether() =
        apply { playTogether(circleAnimator, buttonBackgroundAnimator) }

    fun setupButtonBackgroundAnimator() {
        ValueAnimator.ofFloat(START_BUTTON_BACKGROUND_POSITION, widthSize.toFloat()).apply {
            repeatMode = ValueAnimator.RESTART
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()
            addUpdateListener {
                currentProgressBackgroundAnimationValue = it.animatedValue as Float
                invalidate()
            }
        }.also {
            buttonBackgroundAnimator = it
            animatorSet.playProgressCircleAndButtonBackgroundTogether()
        }
    }

    private fun measureProgressCircle() {
        val textLimits = Rect()
        paintText.getTextBounds(currentButtonText, 0, currentButtonText.length, textLimits)
        val horizontalCenter =
            (textLimits.right + textLimits.width() + PROGRESS_CIRCLE_PADDING)
        val verticalCenter = (heightSize / HALF_DIVIDER)

        progressCircleRect.set(
            horizontalCenter - progressCircleSize,
            verticalCenter - progressCircleSize,
            horizontalCenter + progressCircleSize,
            verticalCenter + progressCircleSize
        )
    }

    private fun Canvas.drawLoadingBackgroundColor() = paintRect.apply {
        color = loadingBackgroundColor
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
        color = loadingDefaultBackgroundColor
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
        private const val TEXT_SIZE = 55.0f
        private const val STROKE_SIZE = 2f
        private const val HALF_DIVIDER = 2f
    }
}
