package dxtool.dxtime

import java.text.SimpleDateFormat
import java.util.*

fun Date.toFormatedString(pattern: String = "yyyy-MM-dd HH:mm:ss"):String{
    return SimpleDateFormat(pattern).format(this)
}

fun Date.addDay(dayCnt : Int) : Date{
    val calendar =GregorianCalendar()
    calendar.time = this
    //calendar.add(Calendar.YEAR, 1) //把日期往后增加一年.整数往后推,负数往前移动
    //calendar.add(Calendar.DAY_OF_MONTH, 1) //把日期往后增加一个月.整数往后推,负数往前移动
    //calendar.add(Calendar.WEEK_OF_MONTH, 1) //把日期往后增加一个月.整数往后推,负数往前移动
    calendar.add(Calendar.DATE, 1*dayCnt) //把日期往后增加一天.整数往后推,负数往前移动
    return calendar.time
}

fun Date.addMonth(monthCnt : Int) : Date{
    val calendar =GregorianCalendar()
    calendar.time = this
    calendar.add(Calendar.MONTH, 1*monthCnt) //把日期往后增加一天.整数往后推,负数往前移动
    return calendar.time
}

fun Date.addYear(yearCnt : Int) : Date{
    val calendar =GregorianCalendar()
    calendar.time = this
    calendar.add(Calendar.YEAR, 1*yearCnt) //把日期往后增加一天.整数往后推,负数往前移动
    return calendar.time
}

fun Date.lastDayOfMonth() : Date{
    val calendar =GregorianCalendar()
    calendar.time = this
    calendar.set(Calendar.DAY_OF_MONTH,1)
    calendar.add(Calendar.MONTH,1)
    calendar.add(Calendar.DATE,-1)
    return calendar.time
}

