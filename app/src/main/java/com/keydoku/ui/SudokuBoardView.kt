package com.keydoku.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.keydoku.domain.CellIndex
import com.keydoku.domain.SudokuBoard
import com.keydoku.game.GameState

class SudokuBoardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {
    var onCellSelected: ((CellIndex) -> Unit)? = null

    private var state: GameState? = null
    private val boardRect = RectF()

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Colors.INK
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.SQUARE
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Colors.INK
        textAlign = Paint.Align.CENTER
        typeface = android.graphics.Typeface.create(android.graphics.Typeface.SANS_SERIF, android.graphics.Typeface.BOLD)
    }
    private val notePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Colors.MUTED
        textAlign = Paint.Align.CENTER
        typeface = android.graphics.Typeface.create(android.graphics.Typeface.MONOSPACE, android.graphics.Typeface.NORMAL)
    }

    init {
        isFocusable = true
        isFocusableInTouchMode = true
        contentDescription = "Sudoku board"
    }

    fun render(state: GameState) {
        this.state = state
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val maxWidth = MeasureSpec.getSize(widthMeasureSpec)
        val maxHeight = MeasureSpec.getSize(heightMeasureSpec)
        val size = minOf(maxWidth, maxHeight).coerceAtLeast(suggestedMinimumWidth)
        setMeasuredDimension(size, size)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val snapshot = state ?: return
        val size = minOf(width, height).toFloat()
        boardRect.set(0f, 0f, size, size)
        val cell = size / 9f

        drawHighlights(canvas, snapshot, cell)
        drawNumbers(canvas, snapshot, cell)
        drawGrid(canvas, cell)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action != MotionEvent.ACTION_UP) return true
        val cellSize = boardRect.width() / 9f
        val column = ((event.x - boardRect.left) / cellSize).toInt()
        val row = ((event.y - boardRect.top) / cellSize).toInt()
        if (row in 0..8 && column in 0..8) {
            requestFocus()
            onCellSelected?.invoke(CellIndex(row, column))
        }
        return true
    }

    private fun drawHighlights(canvas: Canvas, snapshot: GameState, cell: Float) {
        val selected = snapshot.selected
        val selectedValue = snapshot.current[selected]
        for (row in 0..8) {
            for (column in 0..8) {
                val index = row * 9 + column
                val isSelected = selected.row == row && selected.column == column
                val sameUnit = row == selected.row ||
                    column == selected.column ||
                    (row / 3 == selected.row / 3 && column / 3 == selected.column / 3)
                val sameDigit = selectedValue != SudokuBoard.EMPTY &&
                    snapshot.current.valueAt(row, column) == selectedValue
                val color = when {
                    snapshot.settings.mistakeChecking && index in snapshot.conflictIndexes -> Colors.CONFLICT
                    isSelected -> Colors.SELECTED
                    snapshot.settings.highlightMatchingDigits && sameDigit -> Colors.MATCH
                    snapshot.settings.highlightRelated && sameUnit -> Colors.RELATED
                    else -> null
                }
                if (color != null) {
                    fillPaint.color = color
                    canvas.drawRect(
                        column * cell,
                        row * cell,
                        (column + 1) * cell,
                        (row + 1) * cell,
                        fillPaint
                    )
                }
            }
        }
    }

    private fun drawNumbers(canvas: Canvas, snapshot: GameState, cell: Float) {
        textPaint.textSize = cell * 0.55f
        notePaint.textSize = cell * 0.22f
        val textOffset = (textPaint.descent() + textPaint.ascent()) / 2f
        val noteOffset = (notePaint.descent() + notePaint.ascent()) / 2f

        for (row in 0..8) {
            for (column in 0..8) {
                val index = row * 9 + column
                val value = snapshot.current.valueAt(row, column)
                val centerX = column * cell + cell / 2f
                val centerY = row * cell + cell / 2f
                if (value != SudokuBoard.EMPTY) {
                    textPaint.color = when {
                        index in snapshot.givenIndexes -> Colors.INK
                        snapshot.settings.mistakeChecking && value != snapshot.solution.valueAt(row, column) -> Colors.ERROR_TEXT
                        else -> Colors.ENTRY
                    }
                    canvas.drawText(value.toString(), centerX, centerY - textOffset, textPaint)
                } else {
                    drawNotes(canvas, snapshot.notes[index], column, row, cell, noteOffset)
                }
            }
        }
    }

    private fun drawNotes(
        canvas: Canvas,
        notes: Set<Int>,
        column: Int,
        row: Int,
        cell: Float,
        noteOffset: Float,
    ) {
        for (note in notes) {
            val noteIndex = note - 1
            val noteColumn = noteIndex % 3
            val noteRow = noteIndex / 3
            val x = column * cell + cell * (noteColumn + 0.5f) / 3f
            val y = row * cell + cell * (noteRow + 0.5f) / 3f
            canvas.drawText(note.toString(), x, y - noteOffset, notePaint)
        }
    }

    private fun drawGrid(canvas: Canvas, cell: Float) {
        for (line in 0..9) {
            linePaint.strokeWidth = if (line % 3 == 0) 3.5f else 1.2f
            val position = line * cell
            canvas.drawLine(position, 0f, position, cell * 9, linePaint)
            canvas.drawLine(0f, position, cell * 9, position, linePaint)
        }
    }

    private object Colors {
        val INK = Color.rgb(29, 29, 27)
        val MUTED = Color.rgb(97, 102, 106)
        val ENTRY = Color.rgb(28, 95, 128)
        val ERROR_TEXT = Color.rgb(150, 38, 38)
        val SELECTED = Color.rgb(168, 213, 226)
        val RELATED = Color.rgb(230, 237, 220)
        val MATCH = Color.rgb(242, 219, 143)
        val CONFLICT = Color.rgb(245, 197, 190)
    }
}
