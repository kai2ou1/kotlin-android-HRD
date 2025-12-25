package com.example.helloworld

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.GridLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.abs
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    // 延迟初始化 UI 组件
    private lateinit var tvSteps: TextView
    private lateinit var gameGrid: GridLayout
    private lateinit var btnReset: Button

    // 游戏数据
    private val buttons = arrayOfNulls<Button>(16)
    private var numbers = IntArray(16) { it } // 初始化 0-15
    private var emptyIndex = 15
    private var stepCount = 0
    private var isWon = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 绑定 UI
        tvSteps = findViewById(R.id.tvSteps)
        gameGrid = findViewById(R.id.gameGrid)
        btnReset = findViewById(R.id.btnReset)

        setupGrid() // 初始化格子
        startNewGame() // 开始游戏

        btnReset.setOnClickListener { startNewGame() }
    }

    private fun setupGrid() {
        // 动态生成 16 个按钮
        for (i in 0 until 16) {
            val btn = Button(this).apply {
                textSize = 24f
                setTextColor(Color.WHITE)
                gravity = Gravity.CENTER
                // 设置格子大小 (像素，简单粗暴)
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

    private fun startNewGame() {
        // 重置数据
        numbers = IntArray(16) { (it + 1) % 16 } // 1..15, 0
        emptyIndex = 15
        stepCount = 0
        isWon = false
        
        // 随机打乱 (模拟移动确保有解)
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
    }

    private fun onTileClick(index: Int) {
        if (isWon) return

        // 判断是否相邻
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
        tvSteps.text = "Steps: $stepCount"
        for (i in 0 until 16) {
            val num = numbers[i]
            val btn = buttons[i]!!
            
            if (num == 0) {
                btn.visibility = View.INVISIBLE // 空白格不可见
            } else {
                btn.visibility = View.VISIBLE
                btn.text = num.toString()
                // 根据是否归位显示不同颜色 (Kotlin 的 when 语法很优雅)
                val color = if (num == i + 1) Color.parseColor("#4CAF50") else Color.parseColor("#6200EE")
                btn.setBackgroundColor(color)
            }
        }
    }

    private fun checkWin() {
        for (i in 0 until 15) {
            if (numbers[i] != i + 1) return
        }
        isWon = true
        AlertDialog.Builder(this)
            .setTitle("You Win! 🎉")
            .setMessage("Total steps: $stepCount")
            .setPositiveButton("Play Again") { _, _ -> startNewGame() }
            .setCancelable(false)
            .show()
    }
}
