package dxtool.dxfileext

import java.io.File
import java.lang.Exception
import org.apache.commons.codec.digest.DigestUtils

fun File.MD5() : String{
    if (!this.isFile){
        throw Exception("target is not a file!")
    }
    val ips = this.inputStream()
    val md5 = DigestUtils.md5Hex(this.inputStream())
    ips.close()
    return md5
}

fun File.lineSize() : Long{
    if (!this.isFile){
        throw Exception("target is not a file!")
    }
    var cnt : Long = 0
    this.forEachLine {
        cnt++
    }
    return cnt
}

fun File.fileSize() : Long{
    return this.length()
}