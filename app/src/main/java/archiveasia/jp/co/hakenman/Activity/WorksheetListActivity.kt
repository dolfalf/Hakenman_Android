package archiveasia.jp.co.hakenman.Activity

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.widget.TextView
import archiveasia.jp.co.hakenman.R
import archiveasia.jp.co.hakenman.Adapter.WorksheetListAdapter
import archiveasia.jp.co.hakenman.CustomLog
import archiveasia.jp.co.hakenman.Manager.WorksheetManager
import kotlinx.android.synthetic.main.activity_main.*
import android.content.res.Resources
import android.view.*
import android.widget.NumberPicker
import kotlinx.android.synthetic.main.datepicker_dialog.view.*
import java.util.*

class WorksheetListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        adaptListView()

        // FloatingActionButton リスナー設定
        fab.setOnClickListener { view ->
            showCreateWorksheetDialog()
        }
        title = getString(R.string.main_activity_title)

        CustomLog.d("勤務表一覧画面")
    }

    override fun onResume() {
        super.onResume()
        reloadListView()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.setting_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            R.id.action_setting -> {
                val intent = Intent(this, SettingActivity::class.java)
                startActivity(intent)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showAlertDialog(title: String, btn: String, completion: () -> Unit) {
        val alertDialog = AlertDialog.Builder(this)
        with (alertDialog) {

            val titleView = TextView(context)
            titleView.text = title
            titleView.gravity = Gravity.CENTER_HORIZONTAL
            titleView.textSize = 20F
            titleView.setTextColor(resources.getColor(R.color.colorBlack))
            setView(titleView)

            setPositiveButton(btn) {
                dialog, whichButton ->
                completion()
            }

            setNegativeButton(getString(R.string.negative_button)) {
                dialog, whichButton ->
                dialog.dismiss()
            }
        }
        val dialog = alertDialog.create()
        dialog.show()
    }

    private fun showCreateWorksheetDialog() {

        // TODO: Picker Dialogで修正
        val dialogView = LayoutInflater.from(this).inflate(R.layout.datepicker_dialog, null)
        val dayIdentify = Resources.getSystem().getIdentifier("day", "id", "android")
        dialogView.date_picker.findViewById<NumberPicker>(dayIdentify).visibility = View.GONE
        dialogView.date_picker.maxDate = Date().time

        val alertDialog = AlertDialog.Builder(this)

        with (alertDialog) {
            setView(dialogView)
            setTitle("作成する年月を洗濯してください。")

            setPositiveButton(getString(archiveasia.jp.co.hakenman.R.string.positive_button)) {
                dialog, _ ->
                val yearMonth = getPickerDateToString(dialogView)

                var worksheet = WorksheetManager.createWorksheet(yearMonth)
                    if (WorksheetManager.isAlreadyExistWorksheet(yearMonth)) {
                        showAlertDialog(getString(R.string.update_worksheet_title), getString(R.string.positive_button)) {
                            WorksheetManager.updateWorksheet(worksheet)
                            CustomLog.d("勤務表生成 : " + yearMonth)
                            reloadListView()
                        }
                    } else {
                        WorksheetManager.addWorksheetToJsonFile(worksheet)
                        CustomLog.d("勤務表生成 : " + yearMonth)
                        reloadListView()
                    }


                dialog.dismiss()
            }

            setNegativeButton(getString(archiveasia.jp.co.hakenman.R.string.negative_button)) {
                dialog, _ ->
                dialog.dismiss()
            }

            create()
            show()
        }
    }

    private fun getPickerDateToString(view: View): String {
        val year = view.date_picker.year.toString()
        val month = view.date_picker.month + 1
        val finalMonth = if (month < 10) {
            "0" + month.toString()
        } else {
            month.toString()
        }

        return year+finalMonth
    }

    private fun reloadListView() {
        adaptListView()
        work_listView.invalidateViews()
    }

    private fun adaptListView() {
        WorksheetManager.loadLocalWorksheet()

        var worksheetList = WorksheetManager.getWorksheetList()

        val adapter = WorksheetListAdapter(this, worksheetList)

        work_listView.adapter = adapter
        work_listView.setOnItemClickListener { parent, view, position, id ->
            val intent = MonthWorkActivity.newIntent(this, position, worksheetList[position])
            startActivity(intent)
        }

        work_listView.setOnItemLongClickListener { parent, view, position, id ->
            showAlertDialog(getString(R.string.delete_worksheet_title), getString(R.string.delete_button)) {
                adapter.remove(position)
                reloadListView()
            }
            true
        }
    }
}
