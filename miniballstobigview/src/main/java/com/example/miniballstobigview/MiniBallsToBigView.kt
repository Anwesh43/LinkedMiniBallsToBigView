package com.example.miniballstobigview

import android.view.View 
import android.view.MotionEvent 
import android.graphics.Paint 
import android.graphics.Color 
import android.graphics.Canvas 
import android.graphics.RectF 
import android.app.Activity 
import android.content.Context 

val colors : Array<Int> = arrayOf(
    "#F44336",
    "#2196F3",
    "#8BC34A",
    "#FFC107",
    "#009688"
).map {
    Color.parseColor(it)
}.toTypedArray()
val backColor : Int = Color.parseColor("#BDBDBD")
val delay : Long = 20
val parts : Int = 4
val scGap : Float = 0.02f / parts
val strokeFactor : Float = 90f
val rFactor : Float = 5.9f
val srFactor : Float = 32.9f
val balls : Int = 6

fun Int.inverse() : Float = 1f / this
fun Float.maxScale(i : Int, n : Int) : Float = Math.max(0f, this - i * n.inverse())
fun Float.divideScale(i : Int, n : Int) : Float = Math.min(n.inverse(), maxScale(i, n)) * n
fun Float.sinify() : Float = Math.sin(this * Math.PI).toFloat()

fun Canvas.drawMiniBallsToBig(scale : Float, w : Float, h : Float, paint : Paint) {
    val r : Float = Math.min(w, h) / rFactor
    val sr : Float = Math.min(w, h) / srFactor
    val sf : Float = scale.sinify()
    val sf1 : Float = sf.divideScale(0, parts)
    val sf2 : Float = sf.divideScale(1, parts)
    val sf3 : Float = sf.divideScale(2, parts)
    val sf4 : Float = sf.divideScale(3, parts)
    save()
    translate(w / 2, h / 2)
    paint.style = Paint.Style.STROKE
    drawArc(RectF(-r, -r, r, r), 0f, 360f * sf1, false, paint)
    paint.style = Paint.Style.FILL
    for (j in 0..(balls - 1)) {
        save()
        rotate((360f / balls) * j)
        drawCircle((r - sr) * (sf2 - sf3), 0f, sr * sf1 + (r - sr) * sf4, paint)
        restore()
    }
    restore()
}

fun Canvas.drawMBTBNode(i : Int, scale : Float, paint : Paint) {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    paint.color = colors[i]
    paint.strokeCap = Paint.Cap.ROUND
    paint.strokeWidth = Math.min(w, h) / strokeFactor
    drawMiniBallsToBig(scale, w, h, paint)
}

class MiniBallsToBigView(ctx : Context) : View(ctx) {

    private val renderer : Renderer = Renderer(this)

    override fun onDraw(canvas : Canvas) {
        renderer.render(canvas)
    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.handleTap()
            }
        }
        return true
    }

    data class State(var scale : Float = 0f, var dir : Float = 0f, var prevScale : Float = 0f) {

        fun update(cb : (Float) -> Unit) {
            scale += scGap * dir
            if (Math.abs(scale - prevScale) > 1) {
                scale = prevScale + dir
                dir = 0f
                prevScale = scale
                cb(prevScale)
            }
        }

        fun startUdpating(cb : () -> Unit) {
            if (dir == 0f) {
                dir = 1f - 2 * prevScale
                cb()
            }
        }
    }

    data class Animator(var view : View, var animated : Boolean = false) {

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(delay)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }

    data class MTBNode(var i : Int, private val state : State = State()) {

        private var next : MTBNode? = null
        private var prev : MTBNode? = null

        init {
            addNeighbor()
        }

        fun addNeighbor() {
            if (i < colors.size - 1) {
                next = MTBNode(i + 1)
                next?.prev = this
            }
        }

        fun draw(canvas : Canvas, paint : Paint) {
            canvas.drawMBTBNode(i, state.scale, paint)
        }

        fun update(cb : (Float) -> Unit) {
            state.update(cb)
        }

        fun startUpdating(cb : () -> Unit) {
            state.startUdpating(cb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : MTBNode {
            var curr : MTBNode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }
    }

    data class MiniBallsToBig(var i : Int) {

        private var dir : Int = 1
        private var curr : MTBNode = MTBNode(0)

        fun draw(canvas : Canvas, paint : Paint) {
            curr.draw(canvas, paint)
        }

        fun update(cb : (Float) -> Unit) {
            curr.update {
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                cb(it)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            curr.startUpdating(cb)
        }
    }

    data class Renderer(var view : MiniBallsToBigView) {

        private val animator : Animator = Animator(view)
        private val mtb : MiniBallsToBig = MiniBallsToBig(0)
        private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

        fun render(canvas : Canvas) {
            canvas.drawColor(backColor)
            mtb.draw(canvas, paint)
            animator.animate {
                mtb.update {
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            mtb.startUpdating {
                animator.start()
            }
        }
    }

    companion object {

        fun create(activity : Activity) : MiniBallsToBigView {
            val view : MiniBallsToBigView = MiniBallsToBigView(activity)
            activity.setContentView(view)
            return view
        }
    }
}