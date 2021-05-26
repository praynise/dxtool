package dxtool.dxtime

import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.*
import java.util.TimeZone

/*
日期加工模块，用于日期转换、日期计算等
 */

    fun now2Date() : Date{
        return Date()
    }


    fun now2Str(pattern : String = "yyyy-MM-dd HH:mm:ss"):String{
        val timeZone = TimeZone.getTimeZone("GMT+8")
        TimeZone.setDefault(timeZone)
        return SimpleDateFormat(pattern).format(Date())
    }

    fun formatDatetime2Str(indate : Date,pattern: String = "yyyy-MM-dd HH:mm:ss"):String{
        return SimpleDateFormat(pattern).format(indate)
    }

    fun formatStr2Datetime(indate : String,pattern : String = "yyyy-MM-dd HH:mm:ss"):Date{
        return SimpleDateFormat(pattern).parse(indate)
    }

    fun addDay(indate : Date,daycnt : Int) : Date{
        val calendar =GregorianCalendar()
        calendar.time = indate
        //calendar.add(Calendar.YEAR, 1) //把日期往后增加一年.整数往后推,负数往前移动
        //calendar.add(Calendar.DAY_OF_MONTH, 1) //把日期往后增加一个月.整数往后推,负数往前移动
        //calendar.add(Calendar.WEEK_OF_MONTH, 1) //把日期往后增加一个月.整数往后推,负数往前移动
        calendar.add(Calendar.DATE, 1*daycnt) //把日期往后增加一天.整数往后推,负数往前移动
        return calendar.time
    }

    fun addMonth(indate : Date,monthcnt : Int) : Date{
        val calendar =GregorianCalendar()
        calendar.time = indate
        calendar.add(Calendar.MONTH, 1*monthcnt) //把日期往后增加一天.整数往后推,负数往前移动
        return calendar.time
    }

    fun addYear(indate : Date,yearcnt : Int): Date{
        val calendar =GregorianCalendar()
        calendar.time = indate
        calendar.add(Calendar.YEAR, 1*yearcnt) //把日期往后增加一天.整数往后推,负数往前移动
        return calendar.time
    }

    fun lastDayOfMonth(indate : Date) : Date{
        val calendar =GregorianCalendar()
        calendar.time = indate
        calendar.set(Calendar.DAY_OF_MONTH,1)
        calendar.add(Calendar.MONTH,1)
        calendar.add(Calendar.DATE,-1)
        return calendar.time
}