package se.fekete.furiganatextview.furiganaview

import android.graphics.Canvas
import android.graphics.Paint

class TextNormal(private val text: String, private val paint: Paint) {

    private var totalWidth: Float = 0.toFloat()
    private val charsWidth: FloatArray = FloatArray(text.length)

    init {
        paint.getTextWidths(text, charsWidth)

        // Total width
        totalWidth = 0.0f
        for (v in charsWidth)
            totalWidth += v
    }

    // Info
    fun length(): Int {
        return text.length
    }

    // Widths
    fun charsWidth(): FloatArray {
        return charsWidth
    }

    // Split
    fun split(offset: Int): Pair<TextNormal, TextNormal> {
        val textA = if (offset <= 0) { "" } else {
            if (offset >= text.length) { text } else {
                text.substring(0, offset)
            }
        }

        val textB = if (offset <= 0) { text } else {
            if (offset >= text.length) { "" } else {
                text.substring(offset)
            }
        }

        return Pair(TextNormal(textA, paint), TextNormal(textB, paint))
    }

    // Draw
    fun draw(canvas: Canvas, x: Float, y: Float): Float {
        canvas.drawText(text, 0, text.length, x, y, paint)
        return totalWidth
    }
}
