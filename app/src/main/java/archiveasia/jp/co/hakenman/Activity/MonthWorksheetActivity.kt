package archiveasia.jp.co.hakenman.Activity

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import archiveasia.jp.co.hakenman.Adapter.WorksheetAdapter
import archiveasia.jp.co.hakenman.CustomLog
import archiveasia.jp.co.hakenman.Extension.month
import archiveasia.jp.co.hakenman.Extension.year
import archiveasia.jp.co.hakenman.Manager.CSVManager
import archiveasia.jp.co.hakenman.Manager.PrefsManager
import archiveasia.jp.co.hakenman.Manager.WorksheetManager
import archiveasia.jp.co.hakenman.Model.Worksheet
import archiveasia.jp.co.hakenman.R
import kotlinx.android.synthetic.main.activity_month_work.*

const val INTENT_WORKSHEET_INDEX = "worksheet_index"
const val INTENT_WORKSHEET_VALUE = "worksheet_value"

class MonthWorkActivity : AppCompatActivity() {

    private var index: Int = -1
    private lateinit var worksheet: Worksheet

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_month_work)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        index = intent.getIntExtra(INTENT_WORKSHEET_INDEX, index)
        worksheet = intent.getParcelableExtra(INTENT_WORKSHEET_VALUE)
        adaptListView()
        title = getString(R.string.month_work_activity_title).format(worksheet.workDate.year(), worksheet.workDate.month())

        CustomLog.d("月勤務表画面")
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.send_email_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            R.id.send_csv -> {
                val csvManager = CSVManager(this, worksheet)
                csvManager.createCSVFile()
                sendMail(csvManager.getFileUri())

                return true
            }
            R.id.send_markdown -> {
                sendMail()
                return true
            }
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == RESULT_OK) {
            if (requestCode == 100) {
                worksheet = data!!.getParcelableExtra(INTENT_WORKSHEET_RETURN_VALUE)
                WorksheetManager.updateWorksheetWithIndex(index, worksheet)
                adaptListView()
                worksheet_listView.invalidateViews()
            }
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun sendMail(fileUri: Uri? = null) {
        val to = PrefsManager(this).emailTo

        if (to.isNullOrEmpty()) {
            // TODO: 登録メッセージdialog表示
            showAlertDialog {
                var intent= Intent(this, SettingActivity::class.java)
                startActivity(intent)
            }
        } else {
            val addresses = arrayOf(to)
            var subject = getString(R.string.month_work_activity_title).format(worksheet.workDate.year(), worksheet.workDate.month())

            var emailIntent = Intent(Intent.ACTION_SEND)

            if (fileUri == null) {
                val body = WorksheetManager.generateWorksheetToMarkdown(worksheet)
                emailIntent.putExtra(Intent.EXTRA_TEXT, body)
            }

            emailIntent.putExtra(Intent.EXTRA_EMAIL, addresses)
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject)
            emailIntent.putExtra(Intent.EXTRA_STREAM, fileUri)
            emailIntent.type = "message/rfc822"
            startActivity(Intent.createChooser(emailIntent, getString(R.string.send_email_type)))
        }
    }

    private fun showAlertDialog(completion: () -> Unit) {
        val alertDialog = AlertDialog.Builder(this)
        with (alertDialog) {

            val titleView = TextView(context)
            titleView.text = getString(R.string.request_set_address_message)
            titleView.gravity = Gravity.CENTER_HORIZONTAL
            titleView.textSize = 20F
            titleView.setTextColor(resources.getColor(R.color.colorBlack))
            setView(titleView)

            setPositiveButton(getString(R.string.setting)) {
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

    private fun adaptListView() {
        val adapter = WorksheetAdapter(this, worksheet.detailWorkList)
        worksheet_listView.adapter = adapter
        worksheet_listView.setOnItemClickListener { parent, view, position, id ->
            val intent = DayWorksheetActivity.newIntent(this, position, worksheet)
            startActivityForResult(intent, 100) // １００は臨時値
        }
    }

    companion object {

        fun newIntent(context: Context, index: Int, work: Worksheet): Intent {
            val intent = Intent(context, MonthWorkActivity::class.java)
            intent.putExtra(INTENT_WORKSHEET_INDEX, index)
            intent.putExtra(INTENT_WORKSHEET_VALUE, work)
            return intent
        }
    }
}
