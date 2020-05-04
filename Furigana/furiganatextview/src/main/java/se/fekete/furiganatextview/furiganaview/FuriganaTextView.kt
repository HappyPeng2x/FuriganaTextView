/*
 * FuriganaView widget
 * Copyright (C) 2013 sh0 <sh0@yutani.ee>
 * Licensed under Creative Commons BY-SA 3.0
 */

package se.fekete.furiganatextview.furiganaview

import android.content.Context
import android.graphics.Canvas
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import se.fekete.furiganatextview.R
import java.util.*

private fun enumerateChars(aExpression: String): List<String> {
    var pos = 0
    val ret = mutableListOf<String>()

    while (pos < aExpression.length) {
        val new_pos = aExpression.offsetByCodePoints(pos, 1)
        ret.add(aExpression.substring(pos, new_pos))
        pos = new_pos
    }

    return ret
}

class FuriganaTextView : AppCompatTextView {
    companion object Expressions {
        val spanRegex = "([^\\{\\}\\n]+)|\\{([^\\{\\}\\n;]*);([^\\{\\}\\n;]*)\\}|(\\n)".toRegex()
    }

    // Paints
    private var textPaintFurigana = TextPaint()
    private var textPaintNormal = TextPaint()

    // Sizes
    private var lineSize = 0.0f
    private var normalHeight = 0.0f
    private var furiganaHeight = 0.0f
    private var lineMax = 0.0f

    // Spans and lines
    private val spans = Vector<Span>()
    private val normalLines = Vector<LineNormal>()
    private val furiganaLines = Vector<LineFurigana>()

    //attributes
    private var hasRuby: Boolean = false
    private var furiganaTextColor: Int = 0

    // Constructors
    constructor(context: Context) : super(context) {
        initialize()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.FuriganaTextView, 0, 0)
        try {
            hasRuby = typedArray.getBoolean(R.styleable.FuriganaTextView_contains_ruby_tags, false)
            furiganaTextColor = typedArray.getColor(R.styleable.FuriganaTextView_furigana_text_color, 0)
        } finally {
            typedArray.recycle()
        }

        initialize()
    }

    private fun initialize() {
        val viewText = text
        if (viewText.isNotEmpty()) {
            setFuriganaText(viewText as String, hasRuby)
        }
    }

    /**
     * The method parseRuby converts kanji enclosed in ruby tags to the
     * format which is supported by the textview {Kanji:furigana}

     * @param textWithRuby
     * The text string with Kanji enclosed in ruby tags.
     */
    private fun replaceRuby(textWithRuby: String): String {
        var parsed = textWithRuby.replace("<ruby>", "{")
        parsed = parsed.replace("<rt>", ";")
        parsed = parsed.replace("</rt>", "")

        return parsed.replace("</ruby>", "}")
    }

    override fun setTextColor(color: Int) {
        super.setTextColor(color)
        invalidate()
    }

    fun setFuriganaText(text: String) {
        setFuriganaText(text, hasRuby = false)
    }

    fun setFuriganaText(text: String, hasRuby: Boolean) {
        super.setText(text)

        var textToDisplay = text
        if (this.hasRuby || hasRuby) {
            textToDisplay = replaceRuby(text)
        }

        setText(paint, textToDisplay)
    }

    private fun setText(tp: TextPaint, text: String) {
        // Text
        textPaintNormal = TextPaint(tp)
        textPaintFurigana = TextPaint(tp)
        textPaintFurigana.textSize = textPaintFurigana.textSize / 2.0f

        // Line size
        normalHeight = textPaintNormal.descent() - textPaintNormal.ascent()
        furiganaHeight = textPaintFurigana.descent() - textPaintFurigana.ascent()
        lineSize = normalHeight + furiganaHeight

        // Clear spans
        spans.clear()

        // Sizes
        lineSize = textPaintFurigana.fontSpacing + Math.max(textPaintNormal.fontSpacing, 0f)

        // Spannify text
        for (span in spanRegex.findAll(text)) {
            val (normalWithoutFurigana, normal, furigana, newLine) =
                    span.destructured

            if (furigana.isEmpty()) {
                if (!newLine.isEmpty()) {
                    spans.add(Span("", "\n", textPaintNormal, textPaintFurigana))
                } else {
                    for (subSpan in enumerateChars(normalWithoutFurigana)) {
                        spans.add(Span("", subSpan, textPaintNormal, textPaintFurigana))
                    }
                }
            } else {
                spans.add(Span(furigana, normal, textPaintNormal, textPaintFurigana))
            }
        }

        // Invalidate view
        this.invalidate()
        this.requestLayout()
    }

    // Size calculation
    override fun onMeasure(width_ms: Int, height_ms: Int) {
        // Modes
        val wmode = View.MeasureSpec.getMode(width_ms)
        val hmode = View.MeasureSpec.getMode(height_ms)

        // Dimensions
        val wold = View.MeasureSpec.getSize(width_ms)
        val hold = View.MeasureSpec.getSize(height_ms)

        if (text.isNotEmpty()) {
            // Draw mode
            if (wmode == View.MeasureSpec.EXACTLY || wmode == View.MeasureSpec.AT_MOST && wold > 0) {
                // Width limited
                calculateText(wold.toFloat())
            } else {
                // Width unlimited
                calculateText(-1.0f)
            }
        }

        // New height
        var hnew = Math.round(Math.ceil((lineSize * normalLines.size.toFloat()).toDouble())).toInt()
        var wnew = wold
        if (wmode != View.MeasureSpec.EXACTLY && normalLines.size <= 1)
            wnew = Math.round(Math.ceil(lineMax.toDouble())).toInt()
        if (hmode != View.MeasureSpec.UNSPECIFIED && hnew > hold)
            hnew = hnew or View.MEASURED_STATE_TOO_SMALL

        // Set result
        setMeasuredDimension(wnew, hnew)
    }

    private fun calculateText(lineMax: Float) {
        // Clear lines
        normalLines.clear()
        furiganaLines.clear()

        // Sizes
        this.lineMax = 0.0f

        // Check if no limits on width
        if (lineMax < 0.0) {

            // Create single normal and furigana line
            val lineN = LineNormal(textPaintNormal)
            val lineF = LineFurigana(this.lineMax, textPaintFurigana)

            // Loop spans
            for (span in spans) {
                // Text
                lineN.add(span.normal())
                lineF.add(span.furigana(this.lineMax))

                // Widths update
                this.lineMax += span.getWidth()
            }

            // Commit both lines
            normalLines.add(lineN)
            furiganaLines.add(lineF)

        } else {
            // Lines
            var lineX = 0.0f
            var lineN = LineNormal(textPaintNormal)
            var lineF = LineFurigana(this.lineMax, textPaintFurigana)

            // Initial span
            var spanI = 0
            var span: Span? = if (spans.isNotEmpty()) spans[spanI] else null

            // Iterate
            while (span != null) {
                // Start offset
                val lineS = lineX
                val spanWidth = span.getWidth()

                // No span longer than lineMax
                assert(spanWidth <= lineMax)

                if (lineX + spanWidth > lineMax || span.isEndline()) {
                    // Add
                    normalLines.add(lineN)
                    furiganaLines.add(lineF)

                    // Reset
                    lineN = LineNormal(textPaintNormal)
                    lineF = LineFurigana(this.lineMax, textPaintFurigana)
                    lineX = 0.0f

                    // Continue to add span on next line, or move to the next span
                    if (!span.isEndline()) {
                        continue
                    }
                } else {
                    // Span fits entirely
                    lineN.add(span.normal())
                    lineF.add(span.furigana(lineS))

                    lineX += spanWidth
                }

                // Next span
                span = null
                spanI++

                if (spanI < this.spans.size) {
                    span = this.spans[spanI]
                }
            }

            // Last span
            if (lineN.size() != 0) {
                // Add
                normalLines.add(lineN)
                furiganaLines.add(lineF)
            }
        }

        // Calculate furigana
        for (line in furiganaLines) {
            line.calculate()
        }
    }

    // Drawing
    public override fun onDraw(canvas: Canvas) {

        textPaintNormal.color = currentTextColor

        if (furiganaTextColor != 0) {
            textPaintFurigana.color = furiganaTextColor
        } else {
            textPaintFurigana.color = currentTextColor
        }

        // Coordinates
        var y = lineSize

        // Loop lines
        for (i in normalLines.indices) {
            normalLines[i].draw(canvas, y)
            furiganaLines[i].draw(canvas, y - normalHeight)
            y += lineSize
        }
    }
}
