package dxtool.dxlog

import dxtool.dxtime.now2Str
import java.io.BufferedWriter
import java.io.File


/*
简单的文本格式日志模块
 */
object DXLog{
    const val LvlDebug = 0
    const val LvlInfo = 1
    const val LvlWarn = 2
    const val LvlError = 3
    const val LvlCirtial = 4
    private val Lvlmap = mapOf(0 to "DEBUG",1 to "INFO",2 to "WARN",3 to "ERROR",4 to "CIRTIAL")

    private var msg : String = ""
    private var logbuffer : BufferedWriter? = null
    var stdout = true
    var loglvl = 0
    fun setLogFile(logfile : String,charset:String = "UTF-8"){
        val extlog = File(logfile)
        if (extlog.exists()) extlog.renameTo(File("${logfile}.${now2Str("yyyyMMddhhmmss")}"))
        logbuffer = File(logfile).outputStream().bufferedWriter(charset(charset))
    }

    private fun doLog(loglvl : Int,logmsg : Any){
        if (loglvl >= DXLog.loglvl) {
            msg = "${now2Str()} [${Lvlmap[loglvl]}] ${logmsg}"
            if (logbuffer != null) {
                logbuffer!!.write(msg)
                logbuffer!!.newLine()
                logbuffer!!.flush()
            }
            if (stdout){
                println(msg)
            }
        }
    }

    fun debug(logmsg: Any){
        doLog(
            LvlDebug,
            logmsg
        )
    }

    fun info(logmsg: Any){
        doLog(
            LvlInfo,
            logmsg
        )
    }

    fun warn(logmsg: Any){
        doLog(
            LvlWarn,
            logmsg
        )
    }

    fun error(logmsg: Any){
        doLog(
            LvlError,
            logmsg
        )
    }

    fun cirtial(logmsg: Any){
        doLog(
            LvlCirtial,
            logmsg
        )
    }
}