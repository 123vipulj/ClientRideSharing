package com.errorguys.clientridesharing.CustomView

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import com.errorguys.clientridesharing.R
import kotlin.math.floor

class ConnectedDot
@JvmOverloads constructor(context: Context?, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : View(context, attrs, defStyleAttr) {

    var dashHeight: Float
    var dashLength: Float
    var minimumDashGap: Float

    var orientation = Orientation.VERTICAL

    var dashPaint = Paint()
    var circlePaint = Paint()
    var darkCircle = Paint()

    val path: Path = Path()

    init {
        val metrics = resources.displayMetrics
        val twoDpDefault = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,2f, metrics)
        val defaultBlack = Color.argb(255, 0, 0 , 0)
        if (attrs != null){
            val typedArray = context?.theme?.obtainStyledAttributes(attrs, R.styleable.DashedLine, defStyleAttr, 0)
            dashHeight = typedArray?.getDimension(R.styleable.DashedLine_dashHeight, twoDpDefault) ?: twoDpDefault
            dashLength = typedArray?.getDimension(R.styleable.DashedLine_dashLength, twoDpDefault) ?: twoDpDefault
            minimumDashGap = typedArray?.getDimension(R.styleable.DashedLine_minimumDashGap, twoDpDefault) ?: twoDpDefault
            dashPaint.color = typedArray?.getColor(R.styleable.DashedLine_dashColor, defaultBlack) ?: defaultBlack

            typedArray?.recycle()
        } else {
            dashHeight = twoDpDefault
            dashLength = twoDpDefault
            minimumDashGap = twoDpDefault
            dashPaint.color = defaultBlack
        }

        dashPaint.strokeWidth = dashHeight
        dashPaint.style = Paint.Style.STROKE

    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (orientation == Orientation.HORIZONTAL) {
            val widthNeeded = paddingLeft + paddingRight + suggestedMinimumWidth
            var width = resolveSize(widthNeeded, widthMeasureSpec)
            if (width == 0){
                width = 50
            }

            val heightNeeded = paddingTop + paddingBottom + dashHeight
            var height = resolveSize(heightNeeded.toInt(), heightMeasureSpec)
            if (height == dashHeight.toInt()) {
                height = 150
            }
            setMeasuredDimension(width, height)
        } else {
            val widthNeeded = paddingLeft + paddingRight + dashHeight
            val width = resolveSize(widthNeeded.toInt(), widthMeasureSpec)

            val heightNeeded = paddingTop + paddingBottom + suggestedMinimumHeight
            val height = resolveSize(heightNeeded, heightMeasureSpec)

            setMeasuredDimension(width, height)
        }
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        path.reset()

        // dashPaint.reset()
        dashPaint.color = Color.BLACK



        path.moveTo(width.toFloat() / 2, paddingTop.toFloat())

        val h = (height - paddingTop - paddingBottom).toFloat()
        val d = dashLength
        val m = minimumDashGap
        val c: Int = floor(((h - d) / (d + m)).toDouble()).toInt()
        val g: Float = (h - (d * (c + 1))) / c

        dashPaint.pathEffect = DashPathEffect(floatArrayOf(d, g), 0f)
        path.lineTo(width.toFloat() / 2, h)
        canvas.drawPath(path, dashPaint)

        circlePaint.isAntiAlias = true
        circlePaint.color = Color.parseColor("#F5F5F5")

        canvas.drawCircle(width.toFloat() / 2, paddingTop.toFloat(), 15f, circlePaint)

        darkCircle.isAntiAlias = true
        darkCircle.color = Color.parseColor("#70000000")
        canvas.drawCircle(width.toFloat() / 2, paddingTop.toFloat(), 7f, darkCircle)
    }

    enum class Orientation {
        HORIZONTAL, VERTICAL
    }


}