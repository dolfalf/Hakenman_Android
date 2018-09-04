package archiveasia.jp.co.hakenman.Dialog

import android.annotation.SuppressLint
import android.app.Dialog
import android.support.design.widget.BottomSheetDialogFragment
import android.support.v7.app.AlertDialog
import android.view.View
import archiveasia.jp.co.hakenman.Activity.MonthWorkActivity
import archiveasia.jp.co.hakenman.Extension.day
import archiveasia.jp.co.hakenman.Extension.hourMinuteToDate
import archiveasia.jp.co.hakenman.Manager.WorksheetManager
import archiveasia.jp.co.hakenman.R
import kotlinx.android.synthetic.main.bottom_sheet_layout.view.*
import java.util.*

class BottomSheetDialog: BottomSheetDialogFragment() {

    @SuppressLint("RestrictedApi")
    override fun setupDialog(dialog: Dialog?, style: Int) {
        super.setupDialog(dialog, style)

        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)

        var contentView = View.inflate(context, R.layout.bottom_sheet_layout, null)
        dialog?.setContentView(contentView)

        contentView.set_today_begin_time.setOnClickListener {
            showAlertDialog(getString(R.string.set_today_begin_time_message)) {
                val index = 0
                val firstWorksheet = WorksheetManager.getWorksheetList()[index]
                val currentDate = Calendar.getInstance().time
                val today = currentDate.day().toInt()
                val matchingWorksheet = firstWorksheet.detailWorkList.filter { it.workDay == today }[index]
                matchingWorksheet.beginTime = currentDate

                WorksheetManager.updateWorksheetWithIndex(index, firstWorksheet)
                dialog?.dismiss()
                val intent = MonthWorkActivity.newIntent(context!!, index, firstWorksheet)
                startActivity(intent)
            }
        }

        contentView.set_today_end_time.setOnClickListener {
            // TODO: 当日の退勤時間を登録する
            showAlertDialog(getString(R.string.set_today_end_time_message)) {
                val index = 0
                val firstWorksheet = WorksheetManager.getWorksheetList()[index]
                val currentDate = Calendar.getInstance().time
                val today = currentDate.day().toInt()
                val matchingWorksheet = firstWorksheet.detailWorkList.filter { it.workDay == today }[index]
                matchingWorksheet.endTime = currentDate

                // 出勤時間が登録されている場合、休憩時間を入れて勤務時間を計算する
                if (matchingWorksheet.beginTime != null) {
                    matchingWorksheet.breakTime = "01:00".hourMinuteToDate()
                    matchingWorksheet.duration = WorksheetManager.calculateDuration(matchingWorksheet)
                }

                WorksheetManager.updateWorksheetWithIndex(index, firstWorksheet)
                dialog?.dismiss()
                val intent = MonthWorkActivity.newIntent(context!!, index, firstWorksheet)
                startActivity(intent)
            }
        }

        contentView.bottom_sheet_close_button.setOnClickListener {
            dialog?.dismiss()
        }
    }

    private fun showAlertDialog(title: String, completion: () -> Unit) {
        val alertDialog = AlertDialog.Builder(context!!)
        with (alertDialog) {
            setTitle(title)

            setPositiveButton(getString(R.string.confirm)) {
                dialog, whichButton ->
                completion()
            }

            setNegativeButton(getString(R.string.cancel)) {
                dialog, whichButton ->
                dialog.dismiss()
            }
        }
        val dialog = alertDialog.create()
        dialog.show()
    }
}