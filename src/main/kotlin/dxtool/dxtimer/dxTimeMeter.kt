package dxtool.dxtimer

import java.lang.Exception
import java.text.DecimalFormat
import java.util.*

class DxTimeMeter()
{
    private var startTime : Long = -1
    private var endTime : Long = -1
    private var timing : Boolean = false
    private var cost : Double = -1.0
    fun startTiming(){
        this.startTime = Date().time
        timing = true
    }

    fun endTiming(){
        if (!timing){
            throw Exception("timing does not started!")
        }else{
            endTime = Date().time
            this.cost = (this.endTime - this.startTime).toDouble()
            this.timing = false
        }
    }

    fun printCost(type : String = "second"){
        if (this.cost < 0){
            throw Exception("do not have a cost record!")
        }
        when(type){
            "second" -> println(DecimalFormat("#.0000").format(this.cost/1000))
            "miniute" -> println(DecimalFormat("#.0000").format(this.cost/(1000*60)))
            "hour" -> println(DecimalFormat("#.0000").format(this.cost/(1000*60*60)))
            "day" -> println(DecimalFormat("#.0000").format(this.cost/(1000*60*60*24)))
        }
    }

    fun getCost(type : String = "second") : String{
        if (this.cost < 0){
            throw Exception("do not have a cost record!")
        }
        when(type){
            "second" -> return DecimalFormat("#.0000").format(this.cost/1000)
            "miniute" -> return DecimalFormat("#.0000").format(this.cost/(1000*60))
            "hour" -> return DecimalFormat("#.0000").format(this.cost/(1000*60*60))
            "day" -> return DecimalFormat("#.0000").format(this.cost/(1000*60*60*24))
            else -> {
                return DecimalFormat("#.0000").format(this.cost/1000)
            }
        }
    }
}