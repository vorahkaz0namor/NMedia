package ru.netology.nmedia.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.withStyledAttributes
import ru.netology.nmedia.R
import ru.netology.nmedia.util.AndroidUtils
import kotlin.math.min
import kotlin.random.Random

class StatsView @JvmOverloads constructor(
    context: Context,
    // Набор атрибутов, которые можно передавать через xml
    attributeSet: AttributeSet? = null,
    // Стиль атрибоута по умолчанию
    defStyleAttr: Int = 0,
    // Стиль по умолчанию (ресурса-?)
    defStyleRes: Int = 0
) : View(
    context,
    attributeSet,
    defStyleAttr,
    defStyleRes
) {
    // Радиус индикатора прогресса
    private var radius = 0F
    // Центр окружности (индикатора)
    private var center = PointF()
    private var oval = RectF()
    // Ширина линии
    private var lineWidth = AndroidUtils.dp(context = context, dp = 12)
    private var textSize = AndroidUtils.dp(context = context, dp = 20)
    private val randomColor = { Random.nextInt(0xFF000000.toInt(), 0xFFFFFFFF.toInt()) }
    private var colors = emptyList<Int>()
    private var arcPaint: Paint
    private val textPaint: Paint
    var data: List<Float> = emptyList()
        set(value) {
            field = value
            invalidate()
        }

    init {
        context.withStyledAttributes(attributeSet, R.styleable.StatsView) {
            textSize = getDimension(
                /* index = */ R.styleable.StatsView_textSize,
                /* defValue = */ textSize
            )
            lineWidth = getDimension(R.styleable.StatsView_lineWidth, lineWidth)
            colors = listOf(
                getColor(R.styleable.StatsView_firstColor, randomColor()),
                getColor(R.styleable.StatsView_secondColor, randomColor()),
                getColor(R.styleable.StatsView_thirdColor, randomColor()),
                getColor(R.styleable.StatsView_fourthColor, randomColor())
            )
        }
        // Создание кисти
        arcPaint = Paint(Paint.ANTI_ALIAS_FLAG)
            .apply {
                // Толщина кисти
                strokeWidth = lineWidth
                // Стиль отрисовки (в данном случае это строки)
                style = Paint.Style.STROKE
                // Скругление краев при отрисовке (для концов линий, а также
                // при их пересечении)
                strokeJoin = Paint.Join.ROUND
                strokeCap = Paint.Cap.ROUND
            }
        textPaint = Paint(Paint.ANTI_ALIAS_FLAG /*Флаг сглаживания*/)
            .apply {
                textSize = this@StatsView.textSize
                style = Paint.Style.FILL
                textAlign = Paint.Align.CENTER
            }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        // Добавим отступ от краев окружности, чтобы она смогла уместиться
        // во время отрисовки
        radius = (min(w, h) - lineWidth) / 2F
        center = PointF(w / 2F, h / 2F)
        // Чтобы использовать область отрисовки, необходимо создать
        // прямоугольник типа RectF
        oval = RectF(
            /* left = */ center.x - radius,
            /* top = */ center.y - radius,
            /* right = */ center.x + radius,
            /* bottom = */ center.y + radius
        )
    }

    override fun onDraw(canvas: Canvas) {
        if (data.isNotEmpty()) {
            // Стартовый угол положения кисти
            var startAngle = -90F
            // Сектор, выделяемый для одного элемента
            val sector = 360F / data.size
            data.forEachIndexed { index, datum ->
                // Угол поворота (начертания дуги)
                val angle = sector * datum / data.max()
                // Для каждого элемента задается свой цвет.
                // При этом, если элемент в списке data отсутствует,
                // то цвет сгенерируется по указанной функции
                arcPaint.color = colors.getOrElse(index) { randomColor() }
                canvas.drawArc(
                    /* oval = */ oval,
                    /* startAngle = */ startAngle,
                    /* sweepAngle = */ angle,
                    /* useCenter = */ false,
                    /* paint = */ arcPaint)
                // Добавим отступ к стартовому углу
                startAngle += sector
            }
            val sum = data.map { (it / data.max()) * 100 / data.size }.sum()
            canvas.drawText(
                /* text = */ "%.2f%%".format(sum),
                /* x = */ center.x,
                // Для положения текста по оси y придется ввести поправочный коэффициент
                /* y = */ center.y + textPaint.textSize / 4,
                /* paint = */ textPaint
            )
        }
    }
}