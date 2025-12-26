package com.example.klotski  // <--- 1. 这一行必须和你的文件夹路径完全一致！如果不一致请修改！

import com.example.klotski.R  // <--- 强制引入自己的资源文件！
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.GridLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.abs
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private lateinit var tvSteps: TextView
    private lateinit var tvTime: TextView
    private lateinit var gameGrid: GridLayout
    private lateinit var btnStart: Button

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

        // 2. 绑定 ID，必须和 XML 对应
        try {
            tvSteps = findViewById(R.id.tvSteps)
            tvTime = findViewById(R.id.tvTime)
            gameGrid = findViewById(R.id.gameGrid)
            btnStart = findViewById(R.id.btnStart)
        } catch (e: Exception) {
            // 如果这里报错，说明 XML 没更新成功
            e.printStackTrace()
            return
        }

        // 3. 安全计算屏幕宽度
        val displayMetrics = resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val density = displayMetrics.density
        // 预留左右边距和格子间距
        val availableWidth = screenWidth - (40 * density).toInt() - (32 * density).toInt()
        val tileSize = availableWidth / 4

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
                    setMargins(8, 8, 8, 8) // 像素单位
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
