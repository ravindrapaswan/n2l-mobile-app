package practice.english.n2l.uicomponent

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min


class MovableFloatingActionButton : FloatingActionButton, OnTouchListener {
    private var downRawX = 0f
    private var downRawY = 0f
    private var dX = 0f
    private var dY = 0f
    var coordinatorLayout: ConstraintLayout.LayoutParams? = null

    constructor(context: Context?) : super(context!!) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(
        context!!, attrs
    ) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context!!, attrs, defStyleAttr
    ) {
        init()
    }

    private fun init() {
        setOnTouchListener(this)
    }

    override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
        val viewParent: View
        return when (motionEvent.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                Log.d("MovableFAB", "ACTION_DOWN")
                downRawX = motionEvent.rawX
                downRawY = motionEvent.rawY
                dX = view.x - downRawX
                dY = view.y - downRawY
                true // Consumed
            }

            MotionEvent.ACTION_MOVE -> {
                val viewWidth = view.width
                val viewHeight = view.height
                viewParent = view.parent as View
                val parentWidth = viewParent.width
                val parentHeight = viewParent.height
                var newX = motionEvent.rawX + dX
                newX =
                    max(
                        0.0,
                        newX.toDouble()
                    ).toFloat() // Don't allow the FAB past the left hand side of the parent
                newX =
                    min(
                        (parentWidth - viewWidth).toDouble(),
                        newX.toDouble()
                    ).toFloat() // Don't allow the FAB past the right hand side of the parent
                var newY = motionEvent.rawY + dY
                newY =
                    max(
                        0.0,
                        newY.toDouble()
                    ).toFloat() // Don't allow the FAB past the top of the parent
                newY =
                    min(
                        (parentHeight - viewHeight).toDouble(),
                        newY.toDouble()
                    ).toFloat() // Don't allow the FAB past the bottom of the parent
                view.animate()
                    .x(newX)
                    .y(newY)
                    .setDuration(0)
                    .start()
                true // Consumed
            }

            MotionEvent.ACTION_UP -> {
                val upRawX = motionEvent.rawX
                val upRawY = motionEvent.rawY
                val upDX = upRawX - downRawX
                val upDY = upRawY - downRawY
                if (abs(upDX.toDouble()) < CLICK_DRAG_TOLERANCE && abs(upDY.toDouble()) < CLICK_DRAG_TOLERANCE && performClick()) return true
                val viewParent2 = view.parent as View
                val borderY: Float
                val borderX: Float
                val oldX = view.x
                val oldY = view.y
                var finalX: Float
                var finalY: Float
                borderY = min(
                    (view.y - viewParent2.top).toDouble(),
                    (viewParent2.bottom - view.y).toDouble()
                )
                    .toFloat()
                borderX = min(
                    (view.x - viewParent2.left).toDouble(),
                    (viewParent2.right - view.x).toDouble()
                )
                    .toFloat()

                //You can set your dp margin from dimension resources (Suggested)
                //float fab_margin= getResources().getDimension(R.dimen.fab_margin);
                val fab_margin = 15f

                //check if is nearest Y o X
                if (borderX > borderY) {
                    if (view.y > viewParent2.height / 2) { //view near Bottom
                        finalY = (viewParent2.bottom - view.height).toFloat()
                        finalY = (min(
                            (viewParent2.height - view.height).toDouble(),
                            finalY.toDouble()
                        ) - fab_margin).toFloat() // Don't allow the FAB past the bottom of the parent
                    } else {  //view vicina a Top
                        finalY = viewParent2.top.toFloat()
                        finalY =
                            (max(
                                0.0,
                                finalY.toDouble()
                            ) + fab_margin).toFloat() // Don't allow the FAB past the top of the parent
                    }
                    //check if X it's over fab_margin
                    finalX = oldX
                    if (view.x + viewParent2.left < fab_margin) finalX =
                        viewParent2.left + fab_margin
                    if (viewParent2.right - view.x - view.width < fab_margin) finalX =
                        viewParent2.right - view.width - fab_margin
                } else {  //view near Right
                    if (view.x > viewParent2.width / 2) {
                        finalX = (viewParent2.right - view.width).toFloat()
                        finalX =
                            (max(
                                0.0,
                                finalX.toDouble()
                            ) - fab_margin).toFloat() // Don't allow the FAB past the left hand side of the parent
                    } else {  //view near Left
                        finalX = viewParent2.left.toFloat()
                        finalX = (min(
                            (viewParent2.width - view.width).toDouble(),
                            finalX.toDouble()
                        ) + fab_margin).toFloat() // Don't allow the FAB past the right hand side of the parent
                    }
                    //check if Y it's over fab_margin
                    finalY = oldY
                    if (view.y + viewParent2.top < fab_margin) finalY = viewParent2.top + fab_margin
                    if (viewParent2.bottom - view.y - view.height < fab_margin) finalY =
                        viewParent2.bottom - view.height - fab_margin
                }
                view.animate()
                    .x(finalX)
                    .y(finalY)
                    .setDuration(400)
                    .start()
                Log.d("MovableFAB", "ACTION_UP")
                false
            }

            else -> super.onTouchEvent(motionEvent)
        }
    }

    companion object {
        private const val CLICK_DRAG_TOLERANCE =
            10f // Often, there will be a slight, unintentional, drag when the user taps the FAB, so we need to account for this.
    }
}