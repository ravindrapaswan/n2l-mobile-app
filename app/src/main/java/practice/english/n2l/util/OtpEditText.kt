package practice.english.n2l.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.ActionMode
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import practice.english.n2l.R

class OtpEditText : AppCompatEditText {
    private var mSpace = 24f
// 24 dp by default, space between the lines
    private var mNumChars = 4f
    private var mLineSpacing = 8f // 8 dp by default, height of the text from the lines
    private var mMaxLength = 4
    private var mLineStroke = 2f
    private var mLinesPaint: Paint = Paint()
    private var mClickListener: OnClickListener? = null

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context, attrs, defStyleAttr) {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        val multi = context.resources.displayMetrics.density
        mLineStroke *= multi
        mLinesPaint = Paint(this.paint)
        mLinesPaint.strokeWidth = mLineStroke
        mLinesPaint.color = ContextCompat.getColor(context,R.color.colorPrimaryDark)
        background = null
        mSpace *= multi // convert to pixels for our density
        mLineSpacing *= multi // convert to pixels for our density
        mNumChars = mMaxLength.toFloat()
        super.setOnClickListener {
            // When tapped, move cursor to end of text.
            setSelection(text!!.length)
            mClickListener?.onClick(it)
        }
    }

    override fun setOnClickListener(l: OnClickListener?) {
        mClickListener = l
    }

    override fun setCustomSelectionActionModeCallback(actionModeCallback: ActionMode.Callback?) {
        throw RuntimeException("setCustomSelectionActionModeCallback() not supported.")
    }

    override fun onDraw(canvas: Canvas) {
        val availableWidth = width - paddingLeft - paddingRight
        val mCharSize: Float =
            if (mSpace < 0) (availableWidth / (mNumChars * 2 - 1)) else (availableWidth - (mSpace * (mNumChars - 1))) / mNumChars
        val startX = paddingLeft
        val bottom = height - paddingBottom

        // Text Width
        val text = text
        val textLength = text!!.length
        val textWidths = FloatArray(textLength)
        paint.getTextWidths(text, 0, textLength, textWidths)

        for (i in 0 until mNumChars.toInt()) {
            canvas.drawLine(startX.toFloat(),
                bottom.toFloat(), startX + mCharSize, bottom.toFloat(), mLinesPaint)
            if (text.length > i) {
                val middle = startX + mCharSize / 2


                canvas.drawText(
                    text,
                    i,
                    i + 1,
                    middle - textWidths[0] / 2,
                    bottom - mLineSpacing,
                    paint
                )
            }
            if (mSpace < 0) {
                (startX + mCharSize * 2).toInt()
            } else {
                (startX + (mCharSize + mSpace)).toInt()
            }
        }
    }
}