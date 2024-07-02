package com.digital.pianoassist.feature_songs.presentation.songs_screen.components

import android.content.Context
import android.view.View
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry

@Composable
fun PerformanceGraph(scores: List<Int>) {
    if (scores.isEmpty()) {
        println("PerformanceGraph - No data to display")
        return
    }

    Box(
        modifier = Modifier
            .background(
                Color(0xFFFEFBF3),
                shape = RoundedCornerShape(16.dp)
            )
            .clip(RoundedCornerShape(16.dp))
    ) {
        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            factory = { context ->
                createBarChart(context, scores)
            })
    }
}


fun createBarChart(context: Context, scores: List<Int>): View {
    println("initial scores size: ${scores.size}")
    val barChart = BarChart(context)
    val entries = scores.mapIndexed { index, score -> BarEntry(index.toFloat(), score.toFloat()) }
    val dataSet = BarDataSet(entries, "Scores")

    val colors = listOf(
        Color(0xFFFFEB3B),
        Color(0xFF4CAF50),
        Color(0xFFFFADAD),
        Color(0xFF03A9F4),
        Color(0xFFFFB319),
        Color(0xFFB1B2FF)
    ).map { it.toArgb() }
    dataSet.colors = colors

    val barData = BarData(dataSet)
    barChart.data = barData
    barData.barWidth = 0.9f

    // Customize the chart
    barChart.setFitBars(true)
    barChart.description.isEnabled = false

    val xAxis = barChart.xAxis
    xAxis.position = XAxis.XAxisPosition.BOTTOM


    val rightAxis = barChart.axisRight
    rightAxis.isEnabled = false


    barChart.setTouchEnabled(true)
    barChart.isDragEnabled = true

    // Invalidate the chart to refresh and apply changes
    barChart.invalidate()

    return barChart
}
