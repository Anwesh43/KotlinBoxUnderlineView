package ui.anwesome.com.boxunderlineview

/**
 * Created by anweshmishra on 10/02/18.
 */
import android.app.Activity
import android.view.*
import android.content.*
import android.graphics.*
import java.util.concurrent.ConcurrentLinkedQueue

class BoxUnderlineView(ctx:Context,var n:Int):View(ctx) {
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    val renderer = Renderer(this)
    var boxSelectionListener:BoxSelectionListener ?= null
    fun addBoxSelectionListener(onSelect:(Int)->Unit) {
        boxSelectionListener = BoxSelectionListener(onSelect)
    }
    override fun onDraw(canvas:Canvas) {
        renderer.render(canvas,paint)
    }
    override fun onTouchEvent(event:MotionEvent):Boolean {
        when(event.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.handleTap(event.x,event.y)
            }
        }
        return true
    }
    data class Animator(var view:View, var animated:Boolean = false) {
        var delay:Long = 50L
        fun setDelayForDiff(diff:Int) {
            delay = (12.5*diff).toLong()
        }
        fun animate(updatecb: () -> Unit) {
            if (animated) {
                updatecb()
                try {
                    Thread.sleep(delay)
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
    data class State(var scale:Float = 0f,var dir:Float = 0f) {
        fun update(stopcb:()->Unit) {
            scale += 0.1f*dir
            if(Math.abs(scale) > 1) {
                scale = 1f
                dir = 0f
                stopcb()
            }
        }
        fun startUpdating(startcb:()->Unit) {
            if(dir == 0f) {
                scale = 0f
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
    data class Underline(var i:Int,var x:Float,var y:Float, var size:Float, var position:UnderlinePosition = UnderlinePosition(x,y)) {
        fun draw(canvas:Canvas,paint:Paint) {
            position.executeCb {x,y ->
                paint.color = Color.parseColor("#009688")
                paint.strokeWidth = size/10
                paint.strokeCap = Paint.Cap.ROUND
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
    data class Box(var i:Int,var x:Float,var y:Float,var size:Float) {
        fun draw(canvas:Canvas,paint:Paint) {
            paint.color = Color.parseColor("#EF6C00")
            canvas.save()
            canvas.translate(x,y)
            canvas.drawRoundRect(RectF(-size/2,-size/2,size/2,size/2),size/4,size/4,paint)
            canvas.restore()
        }
        fun handleTap(x:Float,y:Float):Boolean = x >= this.x - size/2 && x <= this.x + size/2 &&
                y >= this.y - size/2 && y <= this.y + size/2
    }
    data class BoxContainer(var n:Int,var w:Float,var h:Float) {
        val boxes:ConcurrentLinkedQueue<Box> = ConcurrentLinkedQueue()
        var underline:Underline ?= null
        init {
            var gap = 2*w/(3*n-1)
            var x = gap/2
            var y = h/2
            underline = Underline(0,x,y+gap,2*gap/3)
            for(i in 1..n) {
                boxes.add(Box(i,x,y,gap))
                x += 3*gap/2
            }
        }
        fun draw(canvas:Canvas,paint:Paint) {
            boxes.forEach {
                it.draw(canvas,paint)
            }
            underline?.draw(canvas,paint)
        }
        fun update(stopcb:(Int)->Unit) {
            underline?.update {
                stopcb(underline?.i?:0)
            }
        }
        fun handleTap(x:Float,y:Float,startcb:(Int)->Unit) {
            boxes.forEach {
                val orig_i = underline?.i?:0
                if(it.handleTap(x,y) && underline?.i?:0 != it.i) {
                    underline?.startUpdating(it.x,it.i,{
                        val diff = Math.abs(orig_i - it.i)
                        startcb(diff)
                    })
                    return
                }
            }
        }
    }
    data class Renderer(var view:BoxUnderlineView, var time:Int = 0) {
        var boxContainer:BoxContainer?=null
        val animator = Animator(view)
        fun render(canvas:Canvas,paint:Paint) {
            if(time == 0) {
                val w = canvas.width.toFloat()
                val h = canvas.height.toFloat()
                boxContainer = BoxContainer(view.n,w,h)
            }
            canvas.drawColor(Color.parseColor("#212121"))
            boxContainer?.draw(canvas,paint)
            time++
            animator?.animate {
                boxContainer?.update {
                    view.boxSelectionListener?.onSelect?.invoke(it)
                    animator.stop()
                }
            }
        }
        fun handleTap(x:Float,y:Float) {
            boxContainer?.handleTap(x,y,{
                animator.setDelayForDiff(it)
                animator.start()
            })
        }
    }
    companion object {
        fun create(activity:Activity, n:Int):BoxUnderlineView {
            val view = BoxUnderlineView(activity,n)
            activity.setContentView(view)
            return view
        }
    }
    data class BoxSelectionListener(var onSelect:(Int)->Unit)
}