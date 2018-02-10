package ui.anwesome.com.boxunderlineview

/**
 * Created by anweshmishra on 10/02/18.
 */
import android.view.*
import android.content.*
import android.graphics.*

class BoxUnderlineView(ctx:Context):View(ctx) {
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    override fun onDraw(canvas:Canvas) {

    }
    override fun onTouchEvent(event:MotionEvent):Boolean {
        return true
    }
    data class Animator(var view:View, var animated:Boolean = false) {
        fun animate(updatecb: () -> Unit) {
            if (animated) {
                updatecb()
                try {
                    Thread.sleep(50)
                    view.invalidate()
                } catch(ex: Exception) {

                }
            }
        }
        fun start() {
            if(!animated) {
                animated = true
                view.postInvalidate()
            }
        }
        fun stop() {
            if(animated) {
                animated = false
            }
        }
    }
    data class State(var scale:Float = 0f,var dir:Float = 0f,var prevScale:Float = 0f) {
        fun update(stopcb:()->Unit) {
            scale += 0.1f*dir
            if(Math.abs(scale - prevScale) > 1) {
                scale = prevScale + dir
                dir = 0f
                prevScale = scale
                stopcb()
            }
        }
        fun startUpdating(startcb:()->Unit) {
            if(dir == 0f) {
                dir = 1f - 2*scale
                startcb()
            }
        }
    }
    data class UnderlinePosition(var x:Float,var y:Float,var ox:Float = x,var dx:Float = x) {
        val state = State()
        fun update(stopcb:()->Unit) {
            state.update {
                ox = x
                stopcb()
            }
            x = ox + (dx-ox)*state.scale
        }
        fun startUpdating(x:Float,startcb:()->Unit) {
            dx = x
            state.startUpdating(startcb)
        }
        fun executeCb(cb:(Float,Float)->Unit)  {
            cb(x,y)
        }
    }
    data class Underline(var i:Int,var x:Float,var y:Float,var position:UnderlinePosition = UnderlinePosition(x,y)) {
        fun draw(canvas:Canvas,paint:Paint,size:Float) {
            position.executeCb {x,y ->
                canvas.drawLine(x-size/2,y,x+size/2,y,paint)
            }
        }
        fun update(stopcb:()->Unit) {
            position.update(stopcb)
        }
        fun startUpdating(x:Float,i:Int,startcb:()->Unit) {
            position.startUpdating(x,startcb)
            this.i = i
        }
    }
}