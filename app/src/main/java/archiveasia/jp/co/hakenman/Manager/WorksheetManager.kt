package archiveasia.jp.co.hakenman.Manager

import archiveasia.jp.co.hakenman.Extension.*
import archiveasia.jp.co.hakenman.Model.DetailWork
import archiveasia.jp.co.hakenman.Model.Worksheet
import archiveasia.jp.co.hakenman.MyApplication
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileReader
import java.io.PrintWriter
import java.text.SimpleDateFormat
import java.util.*

/**
 * 勤務表関連マネージャークラス
 *
 * 勤務表作成、修正などを担当するマネージャー
 *
 * @author Jeon SangJun
 */
object WorksheetManager {

    private const val JSON_FILE_NAME = "/worksheet.json"
    private const val DOC_SIGN_CREATE_BY = "<!-- generated from Hakenman. -->"
    private const val HEADER_COLUMN = "| 日 | 曜日 | 作業日 | 開始時間| 終了時間 | 休憩 | 勤務時間 | 備考 |"
    private const val HEADER_LINE = "|:--:|:---:|:-----:|:------:|:------:|:---:|:------:|:----:|"

    private var worksheetList = mutableListOf<Worksheet>()

    /**
     * JSONファイルをロードしてMutableList<Worksheet>に変更する
     *
     * @return MutableList<Worksheet>
     */
    fun loadLocalWorksheet() {
        var filepath = MyApplication.applicationContext().filesDir.path + JSON_FILE_NAME

        // 今月の勤務表
        val currentYearMonth = Calendar.getInstance().time.yearMonth()
        val newWorksheet = createWorksheet(currentYearMonth)

        if (File(filepath).exists()) {
            val gson = GsonBuilder().setDateFormat("MMM dd, yyyy hh:mm:ss a").create()
            var worksheetList: MutableList<Worksheet> = gson.fromJson(FileReader(File(filepath)), object : TypeToken<MutableList<Worksheet>>() {}.type)
            this.worksheetList = worksheetList

            // 今月の勤務表がJSONファイルにない場合、生成する
            if (!isAlreadyExistWorksheet(currentYearMonth)) {
                addFirstWorksheetToJsonFile(newWorksheet)
            }
        } else {
            println("No File")
            // TODO: 今月の勤務表を生成してファイル保存
            addFirstWorksheetToJsonFile(newWorksheet)
        }
    }

    fun addFirstWorksheetToJsonFile(worksheet: Worksheet) {
        worksheetList.add(0, worksheet)
        val gson = GsonBuilder().setPrettyPrinting().create()
        val jsonString = gson.toJson(worksheetList)

        var filepath = MyApplication.applicationContext().filesDir.path + JSON_FILE_NAME

        val writer = PrintWriter(filepath)
        writer.append(jsonString)
        writer.close()
    }

    /**
     * 勤務表（Worksheet）を勤務表リストに追加した後、JSONファイルとして保持
     *
     * @param worksheet 追加する勤務表
     * @return JSONファイルで保持
     */
    fun addWorksheetToJsonFile(worksheet: Worksheet) {
        worksheetList.add(worksheet)
        val gson = GsonBuilder().setPrettyPrinting().create()
        val jsonString = gson.toJson(worksheetList)

        var filepath = MyApplication.applicationContext().filesDir.path + JSON_FILE_NAME

        val writer = PrintWriter(filepath)
        writer.append(jsonString)
        writer.close()
    }

    /**
     * 既存の勤務表を上書きした後、JSONファイルとして保持
     *
     * @param worksheet 修正する勤務表
     * @return JSONファイルで保持
     */
    fun updateWorksheet(newValue: Worksheet) {
        var oldValue = this.worksheetList.find {
            it.workDate.yearMonth() == newValue.workDate.yearMonth()
        }

        this.worksheetList.remove(oldValue)
        this.worksheetList.add(newValue)

        val gson = GsonBuilder().setPrettyPrinting().create()
        val jsonString = gson.toJson(worksheetList)

        var filepath = MyApplication.applicationContext().filesDir.path + JSON_FILE_NAME

        val writer = PrintWriter(filepath)
        writer.append(jsonString)
        writer.close()
    }

    /**
     * 勤務表リストからIndexで新しい勤務表を上書きした後、JSONファイルとして保持
     *
     * @param index 勤務表位置
     * @param worksheet 修正する勤務表
     * @return JSONファイルで保持
     */
    fun updateWorksheetWithIndex(index: Int, worksheet: Worksheet) {
        worksheetList[index] = worksheet
        val gson = GsonBuilder().setPrettyPrinting().create()
        val jsonString = gson.toJson(worksheetList)

        var filepath = MyApplication.applicationContext().filesDir.path + JSON_FILE_NAME

        val writer = PrintWriter(filepath)
        writer.append(jsonString)
        writer.close()
    }

    /**
     * 勤務表リスト全体を修正し、JSONファイルとして保持
     *
     * @param worksheetList 修正する勤務表リスト
     * @return JSONファイルで保持
     */
    fun updateAllWorksheet(worksheetList: List<Worksheet>) {
        val gson = GsonBuilder().setPrettyPrinting().create()
        val jsonString = gson.toJson(worksheetList)

        var filepath = MyApplication.applicationContext().filesDir.path + JSON_FILE_NAME

        val writer = PrintWriter(filepath)
        writer.append(jsonString)
        writer.close()
    }

    /**
     * 勤務表リストをとる
     *
     * @return 勤務表リスト
     */
    fun getWorksheetList(): List<Worksheet> {
        return worksheetList.sortedByDescending { it.workDate.yearMonth() }
    }

    /**
     * 存在する勤務表かを確認
     *
     * @param yearMonth 追加する年月の値（yearmonth）
     * @return 存在すればtrue、でなければfalse
     */
    fun isAlreadyExistWorksheet(yearMonth: String): Boolean {
        return worksheetList.filter { it.workDate.yearMonth() == yearMonth}.isNotEmpty()
    }

    /**
     * 勤務時間を求める
     *
     * @param detailWork 1日勤務情報（DetailWork）
     * @return 開始、終了、休憩の３つの値が全部あれば、勤務時間を求める
     */
    fun calculateDuration(detailWork: DetailWork): Double? {
        if (detailWork.beginTime != null && detailWork.endTime != null && detailWork.breakTime != null) {
            var beginTime = detailWork.beginTime!!.time
            var endTime = detailWork.endTime!!.time
            var breakTime = detailWork.breakTime!!.hourMinuteToDouble()
            val workTime = (endTime - beginTime) / (60 * 60 * 1000)
            return workTime.toDouble() - breakTime
        } else {
            return null
        }
    }

    /**
     * 新しい勤務表を作成する
     *
     * @param yyyymm 追加する年月の値
     * @return 新しい勤務表をリターン
     */
    fun createWorksheet(yyyymm: String): Worksheet {
        val year = yyyymm.substring(0, 4).toInt()
        val month = yyyymm.substring(4, 6).toInt()

        val date = yyyymm.createDate()

        var calendar = Calendar.getInstance()
        calendar.time = date
        var lastDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

        var detailWorks = mutableListOf<DetailWork>()
        for (day in 1..lastDay) {
            val newDate = createDate(year, month, day)
            val week = newDate.week()
            val isHoliday = !newDate.isHoliday()

            var detailWork = DetailWork(year, month, day, week, isHoliday)
            detailWorks.add(detailWork)
        }
        var worksheet = Worksheet(date, 0.0, 0, detailWorks)
        worksheet.workDaySum = worksheet.detailWorkList
                .filter { it.workFlag == true }
                .count()
        return worksheet
    }

    fun generateWorksheetToMarkdown(worksheet: Worksheet): String {
        val detailWorkList = worksheet.detailWorkList

        var markdownString = DOC_SIGN_CREATE_BY + "\n" +
                HEADER_COLUMN + "\n" +
                HEADER_LINE + "\n"

        for (detailWork in detailWorkList) {
            markdownString += "| "

            markdownString += detailWork.workDay.toString() + "| "

            markdownString += detailWork.workWeek + "| "

            val workFlagString = if (detailWork.workFlag) "O" else "X"
            markdownString += workFlagString + "| "

            if (detailWork.beginTime != null) {
                markdownString += SimpleDateFormat("HH:mm").format(detailWork.beginTime)
            }
            markdownString += "| "

            if (detailWork.endTime != null) {
                markdownString += SimpleDateFormat("HH:mm").format(detailWork.endTime)
            }
            markdownString += "| "

            if (detailWork.breakTime != null) {
                markdownString += detailWork.breakTime!!.hourMinuteToDouble().toString()
            }
            markdownString += "| "

            if (detailWork.duration != null) {
                markdownString += detailWork.duration.toString()
            }
            markdownString += "| "

            if (detailWork.note != null) {
                markdownString += detailWork.note
            }
            markdownString += "| \n"


            detailWork
        }

        return markdownString
    }

    /**
     * 年、月、日のIntを持ってDateを作る
     *
     * @param year 年
     * @param month 月
     * @param day 日
     * @return Date値をリターンする
     */
    private fun createDate(year: Int, month: Int, day: Int): Date {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, month)
        cal.set(Calendar.DAY_OF_MONTH, day)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        return cal.time
    }
}