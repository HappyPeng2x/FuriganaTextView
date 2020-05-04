package se.fekete.furiganatextview.furiganaview

import android.graphics.Paint
import java.util.*

internal class Span (aFurigana: TextFurigana?, aNormal: TextNormal) {
    // Text
    private val furigana = aFurigana
    private val normal = aNormal

    // Widths
    private val widthChars = Vector<Float>()
    private var widthTotal = 0.0f

    init {
        calculateWidths()
    }

    constructor(aFurigana: String? = null, aNormal: String,
                aPaint: Paint, aPaintF: Paint) :
            this(if (aFurigana == null || aFurigana.isEmpty())
                        { null } else {TextFurigana(aFurigana, aPaintF) },
                    TextNormal(aNormal, aPaint))

    constructor(aNormal: TextNormal) : this(null, aNormal)

    // Text
    fun furigana(x: Float): TextFurigana? {
        if (furigana == null) {
            return null
        }

        furigana.setOffset(x + widthTotal / 2.0f)

        return furigana
    }

    fun normal(): TextNormal {
        return normal
    }

    // Width
    fun getWidth(): Float {
        return widthChars.reduce { acc, size -> acc + size }
    }

    private fun calculateWidths() {
        // Chars
        if (furigana == null) {
            for (v in normal.charsWidth()) {
                widthChars.add(v)
            }
        } else {
            var sum = 0.0f

            for (v in normal.charsWidth()) {
                sum += v
            }

            widthChars.add(sum)
        }

        // Total
        widthTotal = 0.0f

        for (v in widthChars) {
            widthTotal += v
        }
    }

    // Info
    fun isEndline(): Boolean {
        return normal.isEndline()
    }
}
