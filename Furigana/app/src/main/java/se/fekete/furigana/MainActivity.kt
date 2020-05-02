package se.fekete.furigana

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import se.fekete.furiganatextview.furiganaview.FuriganaTextView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val furiganaTextView = findViewById<FuriganaTextView>(R.id.text_view_furigana)
        furiganaTextView?.setFuriganaText("<ruby>宮崎駿<rt>みやざきはやお</rt></ruby>さんは<ruby>有名<rt>ゆうめい</rt></ruby>です。")
    }
}
