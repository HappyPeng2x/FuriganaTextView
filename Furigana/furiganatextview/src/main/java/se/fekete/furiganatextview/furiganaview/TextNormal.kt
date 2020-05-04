package se.fekete.furiganatextview.furiganaview

import android.graphics.Canvas
import android.graphics.Paint

class TextNormal(private val text: String, private val paint: Paint) {

    private var totalWidth: Float = 0.toFloat()
    private val charsWidth: FloatArray = FloatArray(text.codePointCount(0, text.length))

    init {
        paint.getTextWidths(text, charsWidth)

        // Total width
        totalWidth = 0.0f
        for (v in charsWidth)
            totalWidth += v
    }

    fun isEndline(): Boolean {
        return text == "\n"
    }

    // Widths
    fun charsWidth(): FloatArray {
        return charsWidth
    }

    // Draw
    fun draw(canvas: Canvas, x: Float, y: Float): Float {
        canvas.drawText(text, x, y, paint)
        return totalWidth
    }
}
