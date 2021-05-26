package dxtool.dxutil

import java.io.PrintWriter

import java.io.StringWriter




data class path(
    val path : String,
    val filename : String
)

fun pathSplit(pathname : String) : path {
    val pathname2 = pathname.replace("\\","/")
    if (pathname2.endsWith("/",true)){
            return path(path = pathname2,filename = "")
    }else{
        var path : String = ""
        var filename : String = ""
        val ps = pathname2.split("/")
        for ((i,v) in ps.withIndex()){
            if (i != ps.size -1){
                path += v + "/"
            }else{
                filename = v
            }
        }
        return path(path = path,filename = filename)
    }
}

fun toDBC(input: String): String {
    val c = input.toCharArray()
    for (i in c.indices) {
        if (c[i].code == 12288) {
            //全角空格为12288，半角空格为32
            c[i] = 32.toChar()
            continue
        }
        if (c[i].code > 65280 && c[i].code < 65375) //其他字符半角(33-126)与全角(65281-65374)的对应关系是：均相差65248
            c[i] = (c[i] - 65248)
    }
    return String(c)
}

fun toSBC(input: String): String {
    //半角转全角：
    val c = input.toCharArray()
    for (i in c.indices) {
        if (c[i].code == 32) {
            c[i] = 12288.toChar()
            continue
        }
        if (c[i].code < 127) c[i] = (c[i] + 65248)
    }
    return String(c)
}

fun exception2String(e : Exception): String{
    val sw = StringWriter()
    PrintWriter(sw).use { pw -> e.printStackTrace(pw) }
    return sw.toString()
}