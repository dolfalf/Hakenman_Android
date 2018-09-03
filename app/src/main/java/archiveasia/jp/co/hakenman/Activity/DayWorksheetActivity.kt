package archiveasia.jp.co.hakenman.Activity

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.NumberPicker
import android.widget.TextView
import android.widget.TimePicker
import archiveasia.jp.co.hakenman.CustomLog
import archiveasia.jp.co.hakenman.Extension.hourMinute
import archiveasia.jp.co.hakenman.Extension.hourMinuteToDate
import archiveasia.jp.co.hakenman.Extension.hourMinuteToDouble
import archiveasia.jp.co.hakenman.Manager.PrefsManager
import archiveasia.jp.co.hakenman.Manager.WorksheetManager
import archiveasia.jp.co.hakenman.Model.DetailWork
import archiveasia.jp.co.hakenman.Model.Worksheet
import archiveasia.jp.co.hakenman.R
import kotlinx.android.synthetic.main.activity_day_worksheet.*
import kotlinx.android.synthetic.main.timepicker_dialog.view.*
import java.util.*

const val INTENT_DETAILWORK_INDEX = "index"
const val INTENT_DETAILWORK_VALUE = "day"
const val INTENT_WORKSHEET_RETURN_VALUE = "worksheet_return_value"

class DayWorksheetActivity : AppCompatActivity() {

    enum class WorkTimeType {
        BEGIN_TIME, END_TIME, BREAK_TIME
    }

    private var index: Int = -1
    private lateinit var worksheet: Worksheet
    private lateinit var detailWork: DetailWork

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_day_worksheet)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        index = intent.getIntExtra(INTENT_DETAILWORK_INDEX, index)
        worksheet = intent.getParcelableExtra(INTENT_DETAILWORK_VALUE)
        detailWork = worksheet.detailWorkList[index]
        setDetailWork()

        worksheet_form_view.visibility = if (detailWork.workFlag) View.VISIBLE else View.INVISIBLE
        isWork_switch.isChecked = detailWork.workFlag

        isWork_switch.setOnCheckedChangeListener { _, isChecked ->
            isEditableWorksheet(isChecked)
        }

        title = getString(R.string.day_work_activity_title)
                .format(detailWork.workYear, detailWork.workMonth, detailWork.workDay)
        CustomLog.d("1日詳細勤務表画面")
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.add_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            R.id.action_add -> {
                saveWorksheet()
                finish()
                return true
            }
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun isEditableWorksheet(isWork: Boolean) {
        if (isWork) {
            worksheet_form_view.visibility = View.VISIBLE
            detailWork.workFlag = true
        } else {
            worksheet_form_view.visibility = View.INVISIBLE
            detailWork.workFlag = false
        }
    }

    private fun saveWorksheet() {
        val beginTime = with (day_start_time_textView.text) {
            if (isNotEmpty()) toString().hourMinuteToDate() else null
        }
        val endTime = with (day_end_time_textView.text) {
            if (isNotEmpty()) toString().hourMinuteToDate() else null
        }
        val breakTime = with (day_break_time_textView.text) {
            if (isNotEmpty()) toString().hourMinuteToDate() else null
        }
        val note = note_editText.text.toString()

        detailWork.beginTime = if (detailWork.workFlag) beginTime else null
        detailWork.endTime = if (detailWork.workFlag) endTime else null
        detailWork.breakTime = if (detailWork.workFlag) breakTime else null
        detailWork.duration = WorksheetManager.calculateDuration(detailWork)
        detailWork.workFlag = detailWork.workFlag
        detailWork.note = if (detailWork.workFlag) note else null

        worksheet.detailWorkList[index] = detailWork

        // 勤務日合計
        worksheet.workDaySum = worksheet.detailWorkList
                .filter { it.workFlag == true }
                .count()

        // 勤務時間合計
        worksheet.workTimeSum = worksheet.detailWorkList
                .filter { it.workFlag == true && it.duration != null }
                .sumByDouble { it.duration!! }

        var resultIntent = Intent()
        resultIntent.putExtra(INTENT_WORKSHEET_RETURN_VALUE, worksheet)
        setResult(RESULT_OK, resultIntent)
        finish()
    }

    private fun setDetailWork() {
        day_start_time_textView.text = if (detailWork.beginTime != null) detailWork.beginTime!!.hourMinute() else ""
        day_end_time_textView.text = if (detailWork.endTime != null) detailWork.endTime!!.hourMinute() else ""
        day_break_time_textView.text = if (detailWork.breakTime != null) detailWork.breakTime!!.hourMinute() else ""
        day_total_time_textView.text = if (detailWork.duration != null) detailWork.duration.toString() else ""
        note_editText.setText(if (detailWork.note != null) detailWork.note else "")

        beginTime_view.setOnClickListener {
            showAddDialog(getString(R.string.set_beginTime_title), day_start_time_textView, WorkTimeType.BEGIN_TIME)
        }

        endTime_view.setOnClickListener {
            showAddDialog(getString(R.string.set_endTime_title), day_end_time_textView, WorkTimeType.END_TIME)
        }

        breakTime_view.setOnClickListener {
            showAddDialog(getString(R.string.set_breakTime_title), day_break_time_textView, WorkTimeType.BREAK_TIME)
        }
    }

    private fun setTimePickerInterval(timePicker: TimePicker) {
        var minuteID = Resources.getSystem().getIdentifier("minute", "id", "android")
        var minutePicker = timePicker.findViewById<NumberPicker>(minuteID)

        val interval = PrefsManager(this).interval
        var numValue = 60 / interval
        var displayedValue = arrayListOf<String>()

        for (i in 0..numValue) {
            val value = i * interval
            displayedValue.add(i, value.toString())
        }

        minutePicker.minValue = 0
        minutePicker.maxValue = numValue - 1
        minutePicker.displayedValues = displayedValue.toTypedArray()
    }

    private fun showAddDialog(title: String, textView: TextView, workTimeType: WorkTimeType) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.timepicker_dialog, null)
        dialogView.time_picker.setIs24HourView(true)

        var calendar = Calendar.getInstance()
        var value: Date
        var hour: Int
        var minute: Int

        when (workTimeType) {
            WorkTimeType.BEGIN_TIME -> {
                value = PrefsManager(this).defaultBeginTime.hourMinuteToDate()
                calendar.time = value
                hour = calendar.get(Calendar.HOUR_OF_DAY)
                minute = calendar.get(Calendar.MINUTE)
            }
            WorkTimeType.END_TIME -> {
                value = PrefsManager(this).defaultEndTime.hourMinuteToDate()
                calendar.time = value
                hour = calendar.get(Calendar.HOUR_OF_DAY)
                minute = calendar.get(Calendar.MINUTE)
            }
            WorkTimeType.BREAK_TIME -> {
                hour = 1
                minute = 0
            }
        }

        // 値が存在している場合
        if (textView.text.isNotEmpty()) {
            value = textView.text.toString().hourMinuteToDate()
            calendar.time = value
            hour = calendar.get(Calendar.HOUR_OF_DAY)
            minute = calendar.get(Calendar.MINUTE)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            dialogView.time_picker.hour = hour
            dialogView.time_picker.minute = minute
        } else {
            dialogView.time_picker.currentHour = hour
            dialogView.time_picker.currentMinute = minute
        }


        setTimePickerInterval(dialogView.time_picker)
        val addDialog = AlertDialog.Builder(this)

        with (addDialog) {
            setView(dialogView)
            setTitle(title)

            setPositiveButton(getString(R.string.positive_button)) {
                dialog, _ ->
                textView.text = getPickerTime(dialogView).hourMinute()

                val beginTime = with (day_start_time_textView.text) {
                    if (isNotEmpty()) toString().hourMinuteToDate() else null
                }
                val endTime = with (day_end_time_textView.text) {
                    if (isNotEmpty()) toString().hourMinuteToDate() else null
                }
                val breakTime = with (day_break_time_textView.text) {
                    if (isNotEmpty()) toString().hourMinuteToDate() else null
                }

                if (beginTime != null && endTime != null && breakTime != null) {
                    var beginTimeLong = beginTime.time
                    var endTimeLong = endTime.time
                    var breakTimeDouble = breakTime.hourMinuteToDouble()
                    val workTime = (endTimeLong - beginTimeLong) / (60 * 60 * 1000)
                    val result = workTime.toDouble() - breakTimeDouble
                    day_total_time_textView.text = result.toString()
                }

                dialog.dismiss()
            }

            setNegativeButton(getString(R.string.negative_button)) {
                dialog, _ ->
                dialog.dismiss()
            }

            create()
            show()
        }
    }

    private fun getPickerTime(view: View): Date {
        val hour = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            view.time_picker.hour
        } else {
            view.time_picker.currentHour
        }
        val minute = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            view.time_picker.minute * PrefsManager(this).interval
        } else {
            view.time_picker.currentMinute * PrefsManager(this).interval
        }
        var cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, hour)
        cal.set(Calendar.MINUTE, minute)
        return cal.time
    }

    companion object {

        fun newIntent(context: Context, index: Int, worksheet: Worksheet): Intent {
            val intent = Intent(context, DayWorksheetActivity::class.java)
            intent.putExtra(INTENT_DETAILWORK_VALUE, worksheet)
            intent.putExtra(INTENT_DETAILWORK_INDEX, index)
            return intent
        }
    }
}
