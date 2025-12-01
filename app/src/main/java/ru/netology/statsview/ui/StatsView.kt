package ru.netology.statsview.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.withStyledAttributes
import ru.netology.statsview.R
import ru.netology.statsview.utils.AndroidUtils
import kotlin.math.min
import kotlin.random.Random

class StatsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {

    private var textSize = AndroidUtils.dp(context, 20).toFloat()
    private var lineWidth = AndroidUtils.dp(context, 5).toFloat()
    private var colors = emptyList<Int>()

    init {
        context.withStyledAttributes(attrs, R.styleable.StatsView) {
            textSize = getDimension(R.styleable.StatsView_textSize, textSize)
            lineWidth = getDimension(R.styleable.StatsView_lineWidth, lineWidth)

            colors = listOf(
                getColor(R.styleable.StatsView_color_1, generateRandomColor()),
                getColor(R.styleable.StatsView_color_2, generateRandomColor()),
                getColor(R.styleable.StatsView_color_3, generateRandomColor()),
                getColor(R.styleable.StatsView_color_4, generateRandomColor())
            )
        }
    }

    var data: List<Float> = emptyList()
        set(value) {
            val sum = value.sum()
            field = if (sum == 0F) emptyList() else value.map { it / sum }
            invalidate()
        }

    var progress: Float = 0f
        set(value) {
            field = value
            invalidate()
        }

    private var radius = 0f
    private val center = PointF()
    private val oval = RectF()

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = this@StatsView.textSize
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        radius = min(w, h) / 2f - lineWidth
        center.set(w / 2f, h / 2f)
        oval.set(
            center.x - radius,
            center.y - radius,
            center.x + radius,
            center.y + radius
        )
        paint.strokeWidth = lineWidth
        textPaint.textSize = textSize
    }

    override fun onDraw(canvas: Canvas) {
        if (data.isEmpty()) return

        canvas.save()
        canvas.rotate(progress * 360f, center.x, center.y)

        drawArcs(canvas)

        canvas.restore()

        canvas.drawText(
            "%.2f%%".format(100F),
            center.x,
            center.y + textPaint.textSize / 4,
            textPaint
        )
    }

    private fun drawArcs(canvas: Canvas) {
        val firstDatum = data.firstOrNull() ?: return
        val firstAngle = firstDatum * 360f
        val firstColor = colors.getOrElse(0) { generateRandomColor() }

        var currentStartAngle = -90f + firstAngle

        for (i in 1 until data.size) {
            val datum = data[i]
            val angle = datum * 360f
            if (angle > 0) {
                paint.color = colors.getOrElse(i) { generateRandomColor() }
                canvas.drawArc(oval, currentStartAngle, angle, false, paint)
            }
            currentStartAngle += angle
        }

        if (firstAngle > 0) {
            paint.color = firstColor
            canvas.drawArc(oval, -90f, firstAngle, false, paint)
        }
    }
}

private fun generateRandomColor(): Int =
    Random.nextInt(0xFF000000.toInt(), 0xFFFFFFFF.toInt())