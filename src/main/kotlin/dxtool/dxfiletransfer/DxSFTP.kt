package dxtool.dxfiletransfer

import com.jcraft.jsch.ChannelSftp
import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import com.jcraft.jsch.ChannelSftp.LsEntry
import java.lang.Exception
import java.nio.file.Paths

class DxSFTP (
    val username: String,
    val password: String,
    val host: String,
    val port: Int,
    val serverCharset : String,
    val timeout : Int = 60000
){
    var session : Session
    lateinit var sftpClient : ChannelSftp
    init {
        val jsch = JSch()
        session = jsch.getSession(username,host,port)
        session.setPassword(password)
        session.setConfig("StrictHostKeyChecking","no")
        session.timeout = timeout
    }

    fun connect(){
        session.connect()
        sftpClient = session.openChannel("sftp") as ChannelSftp
        val c = sftpClient.javaClass
        val v = c.getDeclaredField("server_version")
        v.isAccessible = true
        sftpClient.connect()
        v.setInt(sftpClient,2)
        sftpClient.setFilenameEncoding(serverCharset)
    }

    fun disconnect(){
        sftpClient.disconnect()
        session.disconnect()
    }

    fun put(src : String,dst : String){
        sftpClient.put(src,dst)
    }

    fun delete(src : String){
        if (this.fileExists(src)){
            sftpClient.rm(src)
        }
    }

    fun get(src : String,dst : String){
        sftpClient.get(src,dst)
    }

    fun fileExists(dst : String) : Boolean{
        try {
            sftpClient.lstat(dst)
            return true
        }catch (e : Exception){
            val returnCode = e.toString().split(":")[0].toInt()
            when (returnCode){
                ChannelSftp.SSH_FX_NO_SUCH_FILE -> return false
                else -> throw e
            }
        }
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
                val filelist = sftpClient.ls(i)
                for (i2 in filelist){
                    val elem = i2 as LsEntry
                    if (setOf(".","..").contains(i2.filename)){
                        continue
                    }
                    if (elem.attrs.isDir){
                        addpathlist.add(Paths.get(i,elem.filename).toString())
                    }else if (i2.filename == dst){
                        return Paths.get(i,i2.filename).toString()
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