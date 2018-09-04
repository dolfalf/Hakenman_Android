package archiveasia.jp.co.hakenman.Activity

import android.content.res.Resources
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.NumberPicker
import android.widget.TextView
import android.widget.TimePicker
import archiveasia.jp.co.hakenman.CustomLog
import archiveasia.jp.co.hakenman.Extension.hourMinute
import archiveasia.jp.co.hakenman.Extension.hourMinuteToDate
import archiveasia.jp.co.hakenman.Manager.PrefsManager
import archiveasia.jp.co.hakenman.R
import kotlinx.android.synthetic.main.activity_setting.*
import kotlinx.android.synthetic.main.timepicker_dialog.view.*
import java.util.*

class SettingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        title = getString(R.string.setting)

        defaultInit()

        val packageInfo = packageManager.getPackageInfo(packageName, 0)
        version_textView.text = packageInfo.versionName

        if (PrefsManager(this).interval == 15) {
            interval_radio_group.check(R.id.button15)
        } else {
            interval_radio_group.check(R.id.button30)
        }

        interval_radio_group.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.button15 ->
                    PrefsManager(this).interval = 15
                R.id.button30 ->
                    PrefsManager(this).interval = 30
            }

        }

        defalut_beginTime_view.setOnClickListener {
            showAddDialog(getString(R.string.set_beginTime_title), default_beginTime_textView)
        }

        defalut_endTime_view.setOnClickListener {
            showAddDialog(getString(R.string.set_endTime_title), default_endTime_textView)
        }

        CustomLog.d("設定画面")
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return if (item?.itemId == android.R.id.home) {
            PrefsManager(this).emailTo = email_to_editText.text.toString()
            PrefsManager(this).defaultBeginTime = default_beginTime_textView.text.toString()
            PrefsManager(this).defaultEndTime = default_endTime_textView.text.toString()
            finish()
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }

    private fun defaultInit() {
        email_to_editText.setText(PrefsManager(this).emailTo)
        default_beginTime_textView.text = PrefsManager(this).defaultBeginTime
        default_endTime_textView.text = PrefsManager(this).defaultEndTime
    }

    private fun showAddDialog(title: String, textView: TextView) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.timepicker_dialog, null)
        dialogView.time_picker.setIs24HourView(true)


        var calendar = Calendar.getInstance()
        calendar.time = textView.text.toString().hourMinuteToDate()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
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

            setPositiveButton(getString(R.string.confirm)) {
                dialog, _ ->
                textView.text = getPickerTime(dialogView).hourMinute()

                dialog.dismiss()
            }

            setNegativeButton(getString(R.string.cancel)) {
                dialog, _ ->
                dialog.dismiss()
            }

            create()
            show()
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

}
