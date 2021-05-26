package dxtool.dxjsonlog

import dxtool.dxtime.now2Str
import java.io.BufferedWriter
import java.io.File


/*
简单的json格式日志模块
 */
object DXjsonLog{
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

    private fun doLog(loglvl : Int,logmsg : Any,vararg tags : () -> Pair<String,String>){
        if (loglvl >= DXjsonLog.loglvl) {
            msg = "{\"time\":\"${now2Str()}\",\"lvl\":\"${Lvlmap[loglvl]}\","
            for (i in tags){
                msg += "\"${i().first}\":\"${i().second}\","
            }
            msg += "\"msg\":${logmsg}\"}"
            if (logbuffer != null){
                logbuffer.let { it!!.write(msg);it.newLine();it.flush() }
            }
            if (stdout){
                println(msg)
            }
        }
    }

    fun debug(logmsg: Any,vararg tags : () -> Pair<String,String>){
        doLog(
            LvlDebug,
            logmsg,
            *tags
        )
    }

    fun info(logmsg: Any,vararg tags : () -> Pair<String,String>){
        doLog(
            LvlInfo,
            logmsg,
            *tags
        )
    }

    fun warn(logmsg: Any,vararg tags : () -> Pair<String,String>){
        doLog(
            LvlWarn,
            logmsg,
            *tags
        )
    }

    fun error(logmsg: Any,vararg tags : () -> Pair<String,String>){
        doLog(
            LvlError,
            logmsg,
            *tags
        )
    }

    fun cirtial(logmsg: Any,vararg tags : () -> Pair<String,String>){
        doLog(
            LvlCirtial,
            logmsg,
            *tags
        )
    }
}