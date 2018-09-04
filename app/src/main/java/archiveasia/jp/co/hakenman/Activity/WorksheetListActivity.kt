package archiveasia.jp.co.hakenman.Activity

import android.content.Intent
import android.content.res.Configuration
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import archiveasia.jp.co.hakenman.R
import archiveasia.jp.co.hakenman.Adapter.WorksheetListAdapter
import archiveasia.jp.co.hakenman.CustomLog
import archiveasia.jp.co.hakenman.Manager.WorksheetManager
import kotlinx.android.synthetic.main.activity_worksheet_list.*
import android.content.res.Resources
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.view.*
import android.widget.NumberPicker
import archiveasia.jp.co.hakenman.Dialog.BottomSheetDialog
import kotlinx.android.synthetic.main.datepicker_dialog.view.*
import kotlinx.android.synthetic.main.worksheet_listview_main.*
import java.util.*

class WorksheetListActivity : AppCompatActivity() {

    private lateinit var toggle: ActionBarDrawerToggle
    private var bottomSheetDialog = BottomSheetDialog()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_worksheet_list)
        adaptListView()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)

        toggle = ActionBarDrawerToggle(
                this, drawer_layout, R.string.navigation_drawer_open, R.string.navigation_drawer_close)

        drawer_layout.addDrawerListener(toggle)

        // FloatingActionButton リスナー設定
        fab.setOnClickListener { view ->
            bottomSheetDialog.show(supportFragmentManager, bottomSheetDialog.tag)
        }
        title = getString(R.string.main_activity_title)

        nav_view.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.nav_add_worksheet -> {
                    showCreateWorksheetDialog()
                }
                R.id.nav_setting -> {
                    val intent = Intent(this, SettingActivity::class.java)
                    startActivity(intent)
                }
            }

            drawer_layout.closeDrawer(GravityCompat.START)
            true
        }

        CustomLog.d("勤務表一覧画面")
    }

    override fun onResume() {
        super.onResume()
        reloadListView()
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        toggle.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        toggle.onConfigurationChanged(newConfig)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showAlertDialog(title: String, btn: String, completion: () -> Unit) {
        val alertDialog = AlertDialog.Builder(this)
        with (alertDialog) {
            setTitle(title)

            setPositiveButton(btn) {
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

    private fun showCreateWorksheetDialog() {

        // TODO: Picker Dialogで修正
        val dialogView = LayoutInflater.from(this).inflate(R.layout.datepicker_dialog, null)
        val dayIdentify = Resources.getSystem().getIdentifier("day", "id", "android")
        dialogView.date_picker.findViewById<NumberPicker>(dayIdentify).visibility = View.GONE
        dialogView.date_picker.maxDate = Date().time

        val alertDialog = AlertDialog.Builder(this)

        with (alertDialog) {
            setView(dialogView)
            setTitle(getString(R.string.select_yearmonth_message))

            setPositiveButton(getString(archiveasia.jp.co.hakenman.R.string.confirm)) {
                dialog, _ ->
                val yearMonth = getPickerDateToString(dialogView)

                var worksheet = WorksheetManager.createWorksheet(yearMonth)
                if (WorksheetManager.isAlreadyExistWorksheet(yearMonth)) {
                    showAlertDialog(getString(R.string.update_worksheet_title), getString(R.string.confirm)) {
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

            setNegativeButton(getString(archiveasia.jp.co.hakenman.R.string.cancel)) {
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
            showAlertDialog(getString(R.string.delete_worksheet_title), getString(R.string.delete)) {
                adapter.remove(position)
                reloadListView()
            }
            true
        }
    }
}
