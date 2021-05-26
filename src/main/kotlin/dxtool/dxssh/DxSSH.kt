package dxtool.dxssh

import com.jcraft.jsch.Session
import com.jcraft.jsch.ChannelExec
import com.jcraft.jsch.ChannelSftp
import com.jcraft.jsch.JSch
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.Exception

data class ExecResult(val stdout : String,val stderr : String,val returncode :Int)

class DxSSH(val username: String,val password: String,val host: String,val port: Int,val timeout : Int = 60000){
    lateinit var session : Session

    fun getSession(){
        val jsch = JSch()
        session = jsch.getSession(username,host,port)
        session.setPassword(password)
        session.setConfig("StrictHostKeyChecking","no")
        session.timeout = timeout
        session.connect()
    }

    fun closeSession(){
        session.disconnect()
    }
    //执行shell命令然后返回
    fun newExec(command: String,charset: String = "UTF-8"): ExecResult {
        val re: ExecResult
        getSession()
        val exec = session.openChannel("exec") as ChannelExec
        try{
            exec.setCommand(command)
            exec.connect()
            val output = BufferedReader(InputStreamReader(exec.inputStream, charset(charset)))
            val errmsg = BufferedReader(InputStreamReader(exec.errStream, charset(charset)))
            re =  ExecResult(output.readLines().joinToString("\n"),errmsg.readLines().joinToString("\n"),exec.exitStatus)
            errmsg.close()
            output.close()
        }catch (e : Exception){
            throw e
        }finally {
            exec.disconnect()
            closeSession()
        }
        return re
    }

    fun newSftpClient(serverCharset: String = "UTF-8"): ChannelSftp {
        getSession()
        val sftp = session.openChannel("sftp") as ChannelSftp
        val c = sftp.javaClass
        val v = c.getDeclaredField("server_version")
        v.isAccessible = true
        sftp.connect()
        v.setInt(sftp,2)
        sftp.setFilenameEncoding(serverCharset)
        return sftp
    }
}