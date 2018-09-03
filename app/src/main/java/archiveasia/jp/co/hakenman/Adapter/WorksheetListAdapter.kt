package archiveasia.jp.co.hakenman.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import archiveasia.jp.co.hakenman.Extension.day
import archiveasia.jp.co.hakenman.Extension.month
import archiveasia.jp.co.hakenman.Extension.year
import archiveasia.jp.co.hakenman.Manager.WorksheetManager
import archiveasia.jp.co.hakenman.Model.Worksheet
import archiveasia.jp.co.hakenman.R
import kotlinx.android.synthetic.main.worksheet_list_item.view.*
import kotlinx.android.synthetic.main.worksheet_list_top_item.view.*
import java.util.*

class WorksheetListAdapter(private val context: Context,
                           private val workList: List<Worksheet>): BaseAdapter() {

    private val inflater: LayoutInflater
            = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val work = getItem(position) as Worksheet

        if (position == 0 ) {
            val rowView = inflater.inflate(R.layout.worksheet_list_top_item, parent, false)

            // Year, Month, Dayに今日の情報を設定
            val currentDate = Calendar.getInstance().time

            rowView.thisMonth_year_textView.text = currentDate.year()
            rowView.thisMonth_month_textView.text = currentDate.month()
            rowView.thisMonth_day_textView.text = currentDate.day()
            rowView.thisMonth_total_hour_textView.text = work.workTimeSum.toString()
            rowView.thisMonth_total_day_textView.text = work.workDaySum.toString()

            return rowView
        } else {
            val rowView = inflater.inflate(R.layout.worksheet_list_item, parent, false)

            val yearTextView = rowView.year_textView
            yearTextView.text = work.workDate.year()
            val monthTextView = rowView.month_textView
            monthTextView.text = work.workDate.month()
            val workHourTextViewrowView = rowView.workHour_textView
            workHourTextViewrowView.text = work.workTimeSum.toString()
            val workDayTextViewrowView = rowView.workDay_textView
            workDayTextViewrowView.text = work.workDaySum.toString()


            return rowView
        }
    }

    override fun getItem(position: Int): Any {
        return workList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return workList.size
    }

    fun remove(index: Int) {
        var mutableList = workList.toMutableList()
        mutableList.removeAt(index)
        mutableList.toList()
        WorksheetManager.updateAllWorksheet(mutableList)

        notifyDataSetChanged()
    }
}