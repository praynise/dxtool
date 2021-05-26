package dxtool.dxstringext

import org.apache.commons.codec.digest.DigestUtils
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*

fun String.MD5() : String{
    return DigestUtils.md5Hex(this)
}

fun String.MD5_16() : String{
    return DigestUtils.md5Hex(this).substring(8, 24)
}

fun String.toInputStream() : InputStream{
    return ByteArrayInputStream(this.toByteArray())
}

fun String.toFormatedDate(pattern : String = "yyyy-MM-dd HH:mm:ss"): Date {
    return SimpleDateFormat(pattern).parse(this)
}