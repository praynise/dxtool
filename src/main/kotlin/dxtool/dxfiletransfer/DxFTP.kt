package dxtool.dxfiletransfer

import org.apache.commons.net.ftp.FTPClient
import org.apache.commons.net.ftp.FTPReply
import java.io.File
import java.lang.Exception
import java.nio.file.Paths

class DxFTP (
    var username: String,
    var password: String,
    val host: String,
    val port: Int,
    val serverCharset : String,
    val timeout : Int = 60000
){
    var ftpClient : FTPClient
    init {
        ftpClient = FTPClient()
        ftpClient.controlEncoding = serverCharset
        if (username == ""){
            username = "Anonymous"
            password = ""
        }
    }

    fun connect(){
        ftpClient.connect(host,port)
        ftpClient.connectTimeout = timeout
        ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE)
        ftpClient.login(username,password)
        val reply = ftpClient.replyCode
        if (!FTPReply.isPositiveCompletion(reply)) {
            disconnect()
            throw Exception("login error:reply code:${reply}")
        }
    }

    fun disconnect(){
        ftpClient.abort()
        ftpClient.disconnect()
    }

    fun get(src : String,dst : String){
        val dstStream = File(dst).outputStream()
        try {
            ftpClient.retrieveFile(src, dstStream)
        }catch (e : Exception){
            throw e
        }finally {
            dstStream.flush()
            dstStream.close()
        }
    }

    fun put(src : String,dst : String){
        val srcStream = File(src).inputStream()
        try{
            ftpClient.storeFile(dst,srcStream)
        }catch (e : Exception){
            throw e
        }finally {
            srcStream.close()
        }
    }

    fun fileExists(dst : String) : Boolean{
//        val path = pathSplit(dst)
//        val lf = ftpClient.listFiles(path.path)
//        for (i in lf){
//            if (i.name == path.filename){
//                return true
//            }
//        }
//        return false
        /*
        这个实际上是有bug的，只能判断文件，不能判断路径，如果输入的是路径，那么mdtmFile的返回值也会是空
         */
        return ftpClient.mdtmFile(dst) != null
    }

    fun search(dst : String,path : String) : String{
        val pathlist = mutableListOf(path)
        val removepathlist = mutableListOf<String>()
        val addpathlist = mutableListOf<String>()
        println(pathlist)
        while (pathlist.size != 0){
            for (i in pathlist){
                removepathlist.add(i)
                println("searching ${i}")
                val filelist = ftpClient.listFiles(i)
                for (i2 in filelist){
                    val elem = i2
                    if (setOf(".","..").contains(i2.name)){
                        continue
                    }
                    if (elem.isDirectory){
                        addpathlist.add(Paths.get(i,elem.name).toString())
                    }else if (i2.name == dst){
                        return Paths.get(i,i2.name).toString()
                    }
                }
            }
            pathlist.removeAll(removepathlist)
            removepathlist.clear()
            pathlist.addAll(addpathlist)
            addpathlist.clear()
        }
        return ""
    }
}