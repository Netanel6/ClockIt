package com.netanel.clockit.utils

import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Environment
import com.netanel.clockit.R
import com.netanel.clockit.model.MonthlySummary
import com.netanel.clockit.model.Shift
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.NumberFormat
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PdfExporter(context: Context) {

    private val appContext = context.applicationContext
    private val locale = Locale("he", "IL")
    private val currencyFormatter: NumberFormat = NumberFormat.getCurrencyInstance(locale)
    private val numberFormatter: NumberFormat = NumberFormat.getNumberInstance(locale).apply {
        maximumFractionDigits = 2
        minimumFractionDigits = 0
    }
    private val integerFormatter: NumberFormat = NumberFormat.getIntegerInstance(locale)
    private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMM yyyy", locale)
    private val englishMonthFormatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("MMM", Locale.ENGLISH)

    suspend fun exportMonthlyReport(
        month: YearMonth,
        shifts: List<Shift>,
        summary: MonthlySummary
    ): File = withContext(Dispatchers.IO) {
        val cycleStart = month.minusMonths(1).atDay(23)
        val cycleEnd = month.atDay(22)
        val cycleLabel = appContext.getString(
            R.string.pdf_title,
            buildCycleLabel(cycleStart, cycleEnd)
        )

        val fileName = buildFileName(cycleStart, cycleEnd)
        val document = PdfDocument()

        val pageWidth = 595
        val pageHeight = 842
        val marginHorizontal = 40f
        val marginTop = 48f
        val marginBottom = 48f

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val contentPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 11f
        }
        val sectionTitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 14f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val summaryLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val summaryValuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 11f
        }

        fun lineHeight(paint: Paint): Float {
            val metrics = paint.fontMetrics
            return metrics.descent - metrics.ascent
        }

        val titleLineHeight = lineHeight(titlePaint)
        val headerLineHeight = lineHeight(headerPaint)
        val contentLineHeight = lineHeight(contentPaint)
        val sectionTitleLineHeight = lineHeight(sectionTitlePaint)
        val summaryLineHeight = lineHeight(summaryLabelPaint)

        data class Column(val header: String, val width: Float, val align: Paint.Align)

        val columns = listOf(
            Column(appContext.getString(R.string.pdf_col_date), 130f, Paint.Align.LEFT),
            Column(appContext.getString(R.string.pdf_col_duration), 60f, Paint.Align.CENTER),
            Column(appContext.getString(R.string.pdf_col_km), 55f, Paint.Align.RIGHT),
            Column(appContext.getString(R.string.pdf_col_engine_cc), 65f, Paint.Align.RIGHT),
            Column(appContext.getString(R.string.pdf_col_hourly_rate), 90f, Paint.Align.RIGHT),
            Column(appContext.getString(R.string.pdf_col_callouts), 60f, Paint.Align.CENTER),
            Column(appContext.getString(R.string.pdf_col_caught), 55f, Paint.Align.CENTER)
        )

        var pageNumber = 1
        var page = document.startPage(
            PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        )
        var canvas = page.canvas
        var currentY = marginTop
        var headerDrawnOnPage = false

        fun finishPageAndStartNew() {
            document.finishPage(page)
            pageNumber += 1
            page = document.startPage(
                PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            )
            canvas = page.canvas
            currentY = marginTop
            headerDrawnOnPage = false
        }

        fun ensureSpace(height: Float, spacing: Float = 0f) {
            val bottomLimit = pageHeight - marginBottom
            if (currentY + height + spacing > bottomLimit) {
                finishPageAndStartNew()
            }
        }

        fun drawHeaderIfNeeded() {
            if (headerDrawnOnPage) return
            ensureSpace(headerLineHeight, spacing = 6f)
            val baseline = currentY - headerPaint.fontMetrics.ascent
            var x = marginHorizontal
            for (col in columns) {
                headerPaint.textAlign = col.align
                val textX = when (col.align) {
                    Paint.Align.LEFT -> x
                    Paint.Align.CENTER -> x + col.width / 2f
                    Paint.Align.RIGHT -> x + col.width
                }
                canvas.drawText(col.header, textX, baseline, headerPaint)
                x += col.width
            }
            currentY += headerLineHeight + 6f
            headerDrawnOnPage = true
        }

        // Title
        ensureSpace(titleLineHeight, spacing = 12f)
        canvas.drawText(
            cycleLabel,
            marginHorizontal,
            currentY - titlePaint.fontMetrics.ascent,
            titlePaint
        )
        currentY += titleLineHeight + 16f

        if (shifts.isEmpty()) {
            ensureSpace(contentLineHeight, spacing = 12f)
            canvas.drawText(
                appContext.getString(R.string.pdf_no_shifts),
                marginHorizontal,
                currentY - contentPaint.fontMetrics.ascent,
                contentPaint
            )
            currentY += contentLineHeight + 16f
        } else {
            for (shift in shifts) {
                drawHeaderIfNeeded()
                ensureSpace(contentLineHeight, spacing = 6f)
                val baseline = currentY - contentPaint.fontMetrics.ascent
                val values = listOf(
                    shift.date.format(dateFormatter),
                    TimeUtils.minutesToHHmm(shift.workedMinutes),
                    numberFormatter.format(shift.km),
                    integerFormatter.format(shift.engineCc),
                    currencyFormatter.format(shift.hourlyRate),
                    integerFormatter.format(shift.callouts),
                    integerFormatter.format(shift.caughtFound)
                )
                var x = marginHorizontal
                values.forEachIndexed { index, value ->
                    val col = columns[index]
                    contentPaint.textAlign = col.align
                    val textX = when (col.align) {
                        Paint.Align.LEFT -> x
                        Paint.Align.CENTER -> x + col.width / 2f
                        Paint.Align.RIGHT -> x + col.width
                    }
                    canvas.drawText(value, textX, baseline, contentPaint)
                    x += col.width
                }
                currentY += contentLineHeight + 6f
            }
            currentY += 10f
        }

        // Summary section
        ensureSpace(sectionTitleLineHeight, spacing = 8f)
        canvas.drawText(
            appContext.getString(R.string.pdf_summary_header),
            marginHorizontal,
            currentY - sectionTitlePaint.fontMetrics.ascent,
            sectionTitlePaint
        )
        currentY += sectionTitleLineHeight + 8f

        val summaryItems = listOf(
            R.string.pdf_total_base to summary.totalBase,
            R.string.pdf_total_ot1 to summary.totalOt1,
            R.string.pdf_total_ot2 to summary.totalOt2,
            R.string.pdf_total_travel to summary.totalTravel,
            R.string.pdf_total_callouts to summary.totalCallouts,
            R.string.pdf_total_caught to summary.totalCaught,
            R.string.pdf_total_grand to summary.grandTotal
        )

        for ((labelRes, value) in summaryItems) {
            ensureSpace(summaryLineHeight, spacing = 4f)
            val baseline = currentY - summaryLabelPaint.fontMetrics.ascent
            summaryLabelPaint.textAlign = Paint.Align.LEFT
            summaryValuePaint.textAlign = Paint.Align.RIGHT
            canvas.drawText(
                appContext.getString(labelRes),
                marginHorizontal,
                baseline,
                summaryLabelPaint
            )
            canvas.drawText(
                currencyFormatter.format(value),
                pageWidth - marginHorizontal,
                baseline,
                summaryValuePaint
            )
            currentY += summaryLineHeight + 4f
        }

        document.finishPage(page)

        val directory = appContext.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            ?: throw IOException("Unable to access external files directory")
        if (!directory.exists() && !directory.mkdirs()) {
            throw IOException("Unable to create directory for exports")
        }

        val outputFile = File(directory, fileName)
        try {
            FileOutputStream(outputFile).use { output ->
                document.writeTo(output)
            }
        } finally {
            document.close()
        }

        outputFile
    }

    private fun buildCycleLabel(start: LocalDate, end: LocalDate): String {
        val startLabel = start.format(dateFormatter)
        val endLabel = end.format(dateFormatter)
        return "$startLabel – $endLabel"
    }

    private fun buildFileName(start: LocalDate, end: LocalDate): String {
        val startMonth = englishMonthFormatter.format(start)
        val endMonth = englishMonthFormatter.format(end)
        val startLabel = "$startMonth${start.dayOfMonth}"
        val endLabel = "$endMonth${end.dayOfMonth}"
        return "ClockIt_${startLabel}–${endLabel}_${end.year}.pdf"
    }
}
