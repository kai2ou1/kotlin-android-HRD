package com.example.helloworld

import android.graphics.Color
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

    private val buttons = arrayOfNulls<Button>(16)
    private var numbers = IntArray(16) { it } // 0-15
    private var emptyIndex = 15
    private var stepCount = 0
    
    // 计时器相关变量
    private var seconds = 0
    private var isPlaying = false
    private val handler = Handler(Looper.getMainLooper())
    private val timerRunnable = object : Runnable {
        override fun run() {
            seconds++
            tvTime.text = "时间: ${seconds}s"
            handler.postDelayed(this, 1000) // 每隔1秒再次执行
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvSteps = findViewById(R.id.tvSteps)
        tvTime = findViewById(R.id.tvTime)
        gameGrid = findViewById(R.id.gameGrid)
        btnStart = findViewById(R.id.btnStart)

        setupGrid()
        resetBoardOrder() // 初始显示整齐的序列

        btnStart.setOnClickListener {
            startGame()
        }
    }

    private fun setupGrid() {
        for (i in 0 until 16) {
            val btn = Button(this).apply {
                textSize = 24f
                setTextColor(Color.WHITE)
                gravity = Gravity.CENTER
                layoutParams = GridLayout.LayoutParams().apply {
                    width = 180
                    height = 180
                    setMargins(8, 8, 8, 8)
                }
                setOnClickListener { onTileClick(i) }
            }
            buttons[i] = btn
            gameGrid.addView(btn)
        }
    }

    // 仅重置显示，不开始游戏
    private fun resetBoardOrder() {
        numbers = IntArray(16) { (it + 1) % 16 }
        emptyIndex = 15
        updateUI()
    }

    private fun startGame() {
        // 1. 重置数据
        stopTimer()
        seconds = 0
        stepCount = 0
        isPlaying = true
        isWon = false
        tvTime.text = "时间: 0s"
        btnStart.text = "重新开始"

        // 2. 洗牌 (确保有解)
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

        // 3. 启动计时器
        handler.post(timerRunnable)
    }

    private fun stopTimer() {
        handler.removeCallbacks(timerRunnable)
    }

    private fun onTileClick(index: Int) {
        // 如果游戏没开始，或者已经赢了，点击无效
        if (!isPlaying || isWon) return

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
        tvSteps.text = "步数: $stepCount"
        for (i in 0 until 16) {
            val num = numbers[i]
            val btn = buttons[i]!!
            
            if (num == 0) {
                btn.visibility = View.INVISIBLE
            } else {
                btn.visibility = View.VISIBLE
                btn.text = num.toString()
                // 没开始时是灰色，开始后是绿色/紫色
                val color = if (!isPlaying) Color.GRAY else if (num == i + 1) Color.parseColor("#4CAF50") else Color.parseColor("#6200EE")
                btn.setBackgroundColor(color)
            }
        }
    }

    private var isWon = false

    private fun checkWin() {
        for (i in 0 until 15) {
            if (numbers[i] != i + 1) return
        }
        // 胜利逻辑
        isWon = true
        isPlaying = false
        stopTimer() // 停止计时
        
        AlertDialog.Builder(this)
            .setTitle("🎉 挑战成功！")
            .setMessage("耗时: ${seconds}秒\n步数: $stepCount")
            .setPositiveButton("再来一局") { _, _ -> startGame() }
            .setCancelable(false)
            .show()
    }
}
