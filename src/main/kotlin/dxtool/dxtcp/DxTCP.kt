package dxtool.dxtcp

import java.lang.Exception
import java.net.InetAddress
import java.net.Socket

fun netDetect(ip : String,port : Int) : Boolean{
    return try {
        val soc = Socket(InetAddress.getByName(ip), port)
        soc.close()
        true
    }catch (e : Exception){
        false
    }
}