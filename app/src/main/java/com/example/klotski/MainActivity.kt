package com.example.klotski

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.GridLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.abs

class MainActivity : AppCompatActivity() {

    private lateinit var tvSteps: TextView
    private lateinit var tvTime: TextView
    private lateinit var gameGrid: GridLayout
    private lateinit var btnStart: Button

    // 注意：这里改成了 TextView 数组，如果旧代码是 Button 会导致类型错误引发闪退
    private val buttons = arrayOfNulls<TextView>(16)
    private var numbers = IntArray(16) { it }
    private var emptyIndex = 15
    private var stepCount = 0
    
    private var seconds = 0
    private var isPlaying = false
    private val handler = Handler(Looper.getMainLooper())
    private val timerRunnable = object : Runnable {
        override fun run() {
            seconds++
            tvTime.text = "${seconds}s"
            handler.postDelayed(this, 1000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 绑定视图，如果 XML 里没有对应的 ID，这里就会崩溃
        tvSteps = findViewById(R.id.tvSteps)
        tvTime = findViewById(R.id.tvTime)
        gameGrid = findViewById(R.id.gameGrid)
        btnStart = findViewById(R.id.btnStart)

        // 计算屏幕宽度来设置格子大小
        val displayMetrics = resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val padding = (40 * displayMetrics.density).toInt()
        val spacing = (8 * 4 * displayMetrics.density).toInt() // 估算间距
        val tileSize = (screenWidth - padding - spacing) / 4

        setupGrid(tileSize)
        resetBoardOrder()

        btnStart.setOnClickListener { startGame() }
    }

    private fun setupGrid(size: Int) {
        gameGrid.removeAllViews()
        for (i in 0 until 16) {
            val tile = TextView(this).apply {
                textSize = 24f
                setTextColor(Color.WHITE)
                gravity = Gravity.CENTER
                paint.isFakeBoldText = true
                layoutParams = GridLayout.LayoutParams().apply {
                    width = size
                    height = size
                    setMargins(8, 8, 8, 8)
                }
                setOnClickListener { onTileClick(i) }
            }
            buttons[i] = tile
            gameGrid.addView(tile)
        }
    }

    private fun getRoundedBackground(colorHex: String): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 24f
            setColor(Color.parseColor(colorHex))
        }
    }

    private fun resetBoardOrder() {
        numbers = IntArray(16) { (it + 1) % 16 }
        emptyIndex = 15
        updateUI()
    }

    private fun startGame() {
        stopTimer()
        seconds = 0
        stepCount = 0
        isPlaying = true
        tvTime.text = "0s"
        tvSteps.text = "0"
        btnStart.text = "RESTART"
        btnStart.setBackgroundColor(Color.parseColor("#d63031"))

        resetBoardOrder()
        var lastMove = -1
        repeat(500) {
            val neighbors = getNeighbors(emptyIndex).filter { it != lastMove }
            if (neighbors.isNotEmpty()) {
                val target = neighbors.random()
                swap(emptyIndex, target)
                lastMove = emptyIndex
                emptyIndex = target
            }
        }
        updateUI()
        handler.post(timerRunnable)
    }

    private fun stopTimer() {
        handler.removeCallbacks(timerRunnable)
    }

    private fun onTileClick(index: Int) {
        if (!isPlaying) return
        if (isAdjacent(index, emptyIndex)) {
            swap(index, emptyIndex)
            emptyIndex = index
            stepCount++
            updateUI()
            checkWin()
        }
    }

    private fun isAdjacent(i1: Int, i2: Int): Boolean {
        val r1 = i1 / 4; val c1 = i1 % 4
        val r2 = i2 / 4; val c2 = i2 % 4
        return (r1 == r2 && abs(c1 - c2) == 1) || (c1 == c2 && abs(r1 - r2) == 1)
    }

    private fun swap(i: Int, j: Int) {
        val temp = numbers[i]
        numbers[i] = numbers[j]
        numbers[j] = temp
    }

    private fun getNeighbors(idx: Int): List<Int> {
        val list = mutableListOf<Int>()
        val r = idx / 4; val c = idx % 4
        if (r > 0) list.add(idx - 4)
        if (r < 3) list.add(idx + 4)
        if (c > 0) list.add(idx - 1)
        if (c < 3) list.add(idx + 1)
        return list
    }

    private fun updateUI() {
        tvSteps.text = "$stepCount"
        for (i in 0 until 16) {
            val num = numbers[i]
            val tile = buttons[i]!!
            
            if (num == 0) {
                tile.visibility = View.INVISIBLE
            } else {
                tile.visibility = View.VISIBLE
                tile.text = num.toString()
                if (!isPlaying) {
                    tile.background = getRoundedBackground("#B2BEC3")
                } else if (num == i + 1) {
                    tile.background = getRoundedBackground("#00b894")
                } else {
                    tile.background = getRoundedBackground("#0984E3")
                }
            }
        }
    }

    private fun checkWin() {
        for (i in 0 until 15) {
            if (numbers[i] != i + 1) return
        }
        isPlaying = false
        stopTimer()
        btnStart.text = "START GAME"
        btnStart.setBackgroundColor(Color.parseColor("#0984E3"))
        AlertDialog.Builder(this)
            .setTitle("You Win!")
            .setMessage("Steps: $stepCount\nTime: ${seconds}s")
            .setPositiveButton("OK", null)
            .show()
    }
}
