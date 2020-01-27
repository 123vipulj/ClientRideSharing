package com.errorguys.clientridesharing.CustomView

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.View
import com.errorguys.clientridesharing.R
import java.util.jar.Attributes
import kotlin.math.floor
import kotlin.math.roundToInt

class DashLine
@JvmOverloads constructor(context: Context?, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : View(context, attrs, defStyleAttr) {

    var dashHeight: Float
    var dashLength: Float
    var minimumDashGap: Float
    var orientation = Orientation.VERTICAL
    var circlePosBottom : Boolean = false

    val path: Path = Path()
    var paint: Paint = Paint()
    var circlePaint = Paint()

    init {
        val metrics = resources.displayMetrics
        val twoDpDefault = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2f, metrics)
        val defaultBlack = Color.argb(255, 0, 0, 0)

        if (attrs != null) {
            val typedArray = context?.theme?.obtainStyledAttributes(attrs, R.styleable.DashedLine, defStyleAttr, 0)
            dashHeight = typedArray?.getDimension(R.styleable.DashedLine_dashHeight, twoDpDefault) ?: twoDpDefault
            dashLength = typedArray?.getDimension(R.styleable.DashedLine_dashLength, twoDpDefault) ?: twoDpDefault
            minimumDashGap = typedArray?.getDimension(R.styleable.DashedLine_minimumDashGap, twoDpDefault) ?: twoDpDefault
            paint.color = typedArray?.getColor(R.styleable.DashedLine_dashColor, defaultBlack) ?: defaultBlack
            circlePosBottom = typedArray?.getBoolean(R.styleable.DashedLine_circlePosBottom, circlePosBottom) ?: circlePosBottom

            typedArray?.recycle()

        } else {
            dashHeight = twoDpDefault
            dashLength = twoDpDefault
            minimumDashGap = twoDpDefault
            paint.color = defaultBlack
            circlePosBottom = false
        }

        paint.strokeWidth = dashHeight
        paint.style = Paint.Style.STROKE


    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (orientation == Orientation.VERTICAL) {
            val widthNeeded = paddingLeft + paddingRight + suggestedMinimumWidth
            var width = resolveSize(widthNeeded, widthMeasureSpec)
            // if wrap content set the minimum height and width
            if (width == 0){
                width = 50
            }

            val heightNeeded = paddingTop + paddingBottom + dashHeight
            var height = resolveSize(heightNeeded.toInt(), heightMeasureSpec)
            if (height == dashHeight.toInt()) {
                height = 150
            }
            setMeasuredDimension(width, height)
        }
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        paint.isAntiAlias = true
        // setLayerType(View.LAYER_TYPE_HARDWARE, null)

        // if true then show circle at bottom of layout
        if (circlePosBottom) {
            paint.color = Color.BLACK

            path.moveTo(width.toFloat() / 2, paddingTop.toFloat())

            val h = (height - paddingTop - paddingBottom).toFloat()
            val d = dashLength
            val m = minimumDashGap

            val c: Int = floor(((h - d) / (d + m)).toDouble()).toInt()
            val g: Float = (h - (d * (c + 1))) / c

            paint.pathEffect = DashPathEffect(floatArrayOf(d, g), 0f)
            path.lineTo(width.toFloat() / 2, h / 2)
            canvas.drawPath(path, paint)

            // large circle
            circlePaint.color = Color.argb(200, 235, 31, 17)
            circlePaint.style = Paint.Style.FILL
            canvas.drawCircle(width.toFloat() / 2, (height /2).toFloat(), width.toFloat() / 4 , circlePaint)

            // small circle
            circlePaint.color = Color.argb(200, 240, 149, 143)
            canvas.drawCircle(width.toFloat() / 2, (height /2).toFloat(), width.toFloat() / 6 , circlePaint)


        }else {
            paint.color = Color.BLACK

            path.moveTo(width.toFloat() / 2, (height / 2).toFloat())

            val h = (height - paddingTop - paddingBottom).toFloat()
            val d = dashLength
            val m = minimumDashGap
            val c: Int = floor(((h - d) / (d + m)).toDouble()).toInt()
            val g: Float = (h - (d * (c + 1))) / c

            paint.pathEffect = DashPathEffect(floatArrayOf(d, g), 0f)
            path.lineTo(width.toFloat() / 2, h + 10)

            canvas.drawPath(path, paint)

            // large circle
            circlePaint.color = Color.argb(200, 117, 188, 35)
            circlePaint.style = Paint.Style.FILL

            canvas.drawCircle(width.toFloat() / 2,
                (height /2).toFloat(), width.toFloat() / 4 , circlePaint)

            // small circle
            circlePaint.color = Color.argb(200, 96, 154, 30)
            canvas.drawCircle(width.toFloat() / 2, (height /2).toFloat(), width.toFloat() / 6 , circlePaint)

        }

    }

    enum class Orientation {
        HORIZONTAL, VERTICAL
    }

}