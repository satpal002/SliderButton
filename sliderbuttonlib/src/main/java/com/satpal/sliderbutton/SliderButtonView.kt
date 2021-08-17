package com.satpal.sliderbutton

import android.animation.ObjectAnimator
import android.content.Context
import android.content.res.TypedArray
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.MotionEvent.*
import android.view.View
import android.view.animation.Interpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.animation.addListener
import androidx.core.content.res.ResourcesCompat
import com.airbnb.lottie.LottieAnimationView


class SliderButtonView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    var seekBar: SeekBar
    var lottieAnimationView: LottieAnimationView
    var handlerAnimationDelay: Handler
    var tvPrimaryText: TextView
    var tvSecondaryText: TextView
    var middleBackground: ImageView
    var topLayout: ConstraintLayout
    var animationRepeatDelay = 3000L
    var animationDuration = 800L
    var seekBarMinAllowedProgress = 6
    var seekBarMaxAllowedProgress = 94

    private var seekBarTouched = false
    var bounceAnimator: ObjectAnimator
    var runnableBounce: Runnable
    var callbacks: ISliderButtonCallbacks? = null

    init {
        val inflater =
            this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val childView = inflater.inflate(R.layout.slider_button_view, null)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            childView.setId(View.generateViewId())
        }
        addView(childView, 0)

        seekBar = findViewById(R.id.seekBar)
        lottieAnimationView = findViewById(R.id.lottieAnimationView)
        tvPrimaryText = findViewById(R.id.primaryText)
        tvSecondaryText = findViewById(R.id.secondaryText)
        middleBackground = findViewById(R.id.middleBackground)
        topLayout = findViewById(R.id.topLayout)

        handlerAnimationDelay = Handler(Looper.getMainLooper())
        bounceAnimator = ObjectAnimator.ofInt(seekBar, "progress", 10)
        runnableBounce = Runnable {
            Log.wtf("satpal", "startTimerToBounce: ${handlerAnimationDelay}")
            seekBar.progress = seekBarMinAllowedProgress
            lottieAnimationView.playAnimation()

            bounceAnimator.cancel()
            bounceAnimator.start()
        }


        bounceAnimator.setDuration(animationDuration)
        bounceAnimator.setInterpolator(CustomBounceInterpolator())
        bounceAnimator.addListener(onEnd = {
            seekBar.progress = seekBarMinAllowedProgress
            startTimerToBounce()
        }, onStart = {
        }, onCancel = {})

        seekBar.progress = seekBarMinAllowedProgress

        try {
            startTimerToBounce()
            setSeekBarEvents()

            val a: TypedArray = context.obtainStyledAttributes(attrs, R.styleable.sliderButtonStyle)
            val N: Int = a.getIndexCount()
            for (i in 0 until N) {
                try {
                    val attr: Int = a.getIndex(i)
                    when (attr) {
                        R.styleable.sliderButtonStyle_primaryText -> {
                            val primaryText = a.getString(attr)
                            tvPrimaryText.text = primaryText
                        }
                        R.styleable.sliderButtonStyle_secondaryText -> {
                            val secondaryText = a.getString(attr)
                            tvSecondaryText.text = secondaryText
                        }
                        R.styleable.sliderButtonStyle_textBackground -> {
                            val drawable = a.getResourceId(attr, -1)
                            middleBackground.setImageResource(drawable)
                        }
                        R.styleable.sliderButtonStyle_sliderBackground -> {
                            val drawable = a.getResourceId(attr, -1)
                            topLayout.setBackgroundResource(drawable)
                        }
                        R.styleable.sliderButtonStyle_sliderTypeface -> {
                            val typefaceType = a.getResourceId(attr, 0)
                            tvPrimaryText!!.typeface =
                                ResourcesCompat.getFont(context!!, typefaceType)
                        }
                        R.styleable.sliderButtonStyle_primaryTextColor -> {
                            val color = a.getColor(attr, 0)
                            tvPrimaryText!!.setTextColor(color)
                        }
                        R.styleable.sliderButtonStyle_secondaryTextColor -> {
                            val color = a.getColor(attr, 0)
                            tvSecondaryText!!.setTextColor(color)
                        }
                        R.styleable.sliderButtonStyle_primaryTextSize -> {
                            val dimen = a.getDimension(attr, spToPx(10f))
                            tvPrimaryText!!.setTextSize(pxToSp(dimen))
                        }
                        R.styleable.sliderButtonStyle_secondaryTextSize -> {
                            val dimen = a.getDimension(attr, spToPx(10f))
                            tvSecondaryText!!.setTextSize(pxToSp(dimen))
                        }
                        R.styleable.sliderButtonStyle_seekBarProgressDrawable -> {
                            val drawable = resources.getDrawable(a.getResourceId(attr, -1), null)
                            seekBar.progressDrawable = drawable
                        }
                        R.styleable.sliderButtonStyle_thumbDrawable -> {
                            val drawable = resources.getDrawable(a.getResourceId(attr, -1), null)
                            seekBar.thumb = drawable
                        }
                        R.styleable.sliderButtonStyle_lottieAnimationTime -> {
                            animationDuration = a.getInt(attr, 1000).toLong()
                        }
                        R.styleable.sliderButtonStyle_lottieAnimationDelay -> {
                            animationRepeatDelay = a.getInt(attr, 3000).toLong()
                        }
                        R.styleable.sliderButtonStyle_textStartMargin -> {
                            val margin = a.getDimension(attr, dpToPx(100.0f))
                            val constraintSet = ConstraintSet()
                            constraintSet.clone(topLayout)
                            constraintSet.setMargin(
                                R.id.primaryText,
                                ConstraintSet.START,
                                margin.toInt()
                            )
                            constraintSet.applyTo(topLayout)
                        }
                        R.styleable.sliderButtonStyle_backgroundLottie -> {
                            val drawableLottie = a.getResourceId(attr, -1)
                            lottieAnimationView.setAnimation(drawableLottie)
                        }
                        R.styleable.sliderButtonStyle_seekBarMinAllowedProgress -> {
                            seekBarMinAllowedProgress = a.getInt(attr, 5)
                        }
                        R.styleable.sliderButtonStyle_seekBarMaxAllowedProgress -> {
                            seekBarMaxAllowedProgress = a.getInt(attr, 95)
                        }
                    }
                } catch (e: Exception) {
                }
            }
            a.recycle()
        } catch (e: Exception) {
            Log.wtf("satpal", "satpal: e:${e.message}")
        }
    }

    private fun setCallback(callbacks: ISliderButtonCallbacks) {
        this.callbacks = callbacks
    }

    private fun startTimerToBounce() {
        if (!seekBarTouched) {
            bounceAnimator.cancel()

            handlerAnimationDelay.removeCallbacksAndMessages(null)
            handlerAnimationDelay.postDelayed(runnableBounce, animationRepeatDelay)
        }
    }

    private fun setSeekBarEvents() {

        seekBar.setOnTouchListener { view: View, motionEvent: MotionEvent ->
            Log.wtf("satpal", "seekbar: ${motionEvent}, progress: ${seekBar.progress}")
            when (motionEvent.action) {
                ACTION_DOWN -> {
                    seekBarTouched = true
                    handlerAnimationDelay.removeCallbacksAndMessages(null)
                }
                ACTION_UP -> {
                    if (seekBar.progress >= seekBarMaxAllowedProgress) {
                        callbacks?.sliderUnlocked()
                    }
                        seekBarTouched = false

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            seekBar.setProgress(seekBarMinAllowedProgress, true)
                        } else {
                            seekBar.setProgress(seekBarMinAllowedProgress)
                        }

                        startTimerToBounce()

                }
                ACTION_OUTSIDE -> {
                    seekBarTouched = false
                    seekBar.setProgress(seekBarMinAllowedProgress)
                }
                ACTION_CANCEL -> {
                    seekBarTouched = false
                    seekBar.setProgress(seekBarMinAllowedProgress)
                }
                ACTION_MOVE -> {
                    if (seekBar.progress >= seekBarMaxAllowedProgress) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            seekBar.setProgress(seekBarMaxAllowedProgress, true)
                        } else {
                            seekBar.setProgress(seekBarMaxAllowedProgress)
                        }
                    }
                    if (seekBar.progress <= seekBarMinAllowedProgress) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            seekBar.setProgress(seekBarMinAllowedProgress, true)
                        } else {
                            seekBar.setProgress(seekBarMinAllowedProgress)
                        }
                    }
                    true
                }
            }
            false
        }
    }

    private fun spToPx(sp: Float) =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, resources.displayMetrics)


    private fun pxToSp(px: Float) =
      px/resources.displayMetrics.scaledDensity

    private fun dpToPx(dp: Float) =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics)


    internal class CustomBounceInterpolator : Interpolator {
        var mAmplitude = 1.0
        var mFrequency = 10.0

        constructor() {}
        constructor(amp: Double, freq: Double) {
            mAmplitude = amp
            mFrequency = freq
        }

        override fun getInterpolation(time: Float): Float {
            return (-1 * Math.pow(
                Math.E,
                -time / mAmplitude
            ) * Math.cos(mFrequency * time) + 1).toFloat()
        }
    }

}



