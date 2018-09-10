package archiveasia.jp.co.hakenman.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import archiveasia.jp.co.hakenman.Extension.hourMinuteToDouble
import archiveasia.jp.co.hakenman.Model.DetailWork
import archiveasia.jp.co.hakenman.R
import kotlinx.android.synthetic.main.month_worksheet_item.view.*
import java.text.SimpleDateFormat

class WorksheetAdapter(private val context: Context,
                       private val detailWorkList: MutableList<DetailWork>): BaseAdapter() {

    private val inflater: LayoutInflater
            = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

        val detailWork = getItem(position) as DetailWork

        val view: View
        val holder: ViewHolder

        if (convertView == null) {
            view = inflater.inflate(R.layout.month_worksheet_item, parent, false)

            holder = ViewHolder()
            holder.dayTextView = view.day_textView
            holder.weekTextView = view.week_textView
            holder.workFlagTextView = view.workFlag_textView
            holder.beginWorkTextView = view.beginWork_textView
            holder.endWorkTextView = view.endWork_textView
            holder.breakTimeTextView = view.breakTime_textView
            holder.workTimeTextView = view.workTime_textView
            holder.noteTextView = view.note_textView

            view.tag = holder
        } else {
            view = convertView
            holder = convertView.tag as ViewHolder
        }

        holder.dayTextView.text = detailWork.workDay.toString()
        holder.weekTextView.text = detailWork.workWeek
        holder.workFlagTextView.text = if (detailWork.workFlag == true) "O" else "X"
        if (detailWork.beginTime != null) {
            holder.beginWorkTextView.text = SimpleDateFormat("HH:mm").format(detailWork.beginTime)
        }
        if (detailWork.endTime != null) {
            holder.endWorkTextView.text = SimpleDateFormat("HH:mm").format(detailWork.endTime)
        }
        if (detailWork.breakTime != null) {
            holder.breakTimeTextView.text = detailWork.breakTime!!.hourMinuteToDouble().toString()
        }
        if (detailWork.duration != null) {
            holder.workTimeTextView.text = detailWork.duration.toString()
        }
        holder.noteTextView.text = detailWork.note

        return view
    }

    override fun getItem(position: Int): Any {
        return detailWorkList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return detailWorkList.size
    }

    private class ViewHolder {
        lateinit var dayTextView: TextView
        lateinit var weekTextView: TextView
        lateinit var workFlagTextView: TextView
        lateinit var beginWorkTextView: TextView
        lateinit var endWorkTextView: TextView
        lateinit var breakTimeTextView: TextView
        lateinit var workTimeTextView: TextView
        lateinit var noteTextView: TextView
    }
}