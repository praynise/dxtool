package dxtool.dxjdbc

import com.alibaba.excel.EasyExcel
import com.github.sisyphsu.dateparser.DateParserUtils
import dxtool.dxtime.formatDatetime2Str
import java.io.File
import java.lang.Exception
import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.ResultSet

/*
            arrayOf("ORACLE","oracle.jdbc.OracleDriver","jdbc:oracle:thin:@{host}:{port}:{dbname}"),
            arrayOf("DB2","com.ibm.db2.jcc.DB2Driver","jdbc:db2://{host}:{port}/{dbname}"),
            arrayOf("PG","org.postgresql.Driver","jdbc:postgresql://{host}:{port}/{dbname}"),
            arrayOf("MYSQL","com.mysql.cj.jdbc.Driver","jdbc:mysql://{host}:{port}/{dbname}"),
            arrayOf("MSSQL","com.microsoft.sqlserver.jdbc.SQLServerDriver","jdbc:sqlserver://{host}:{port};DatabaseName={dbname}")
 */

const val mysqlDriver="com.mysql.jdbc.Driver"
const val mysqlUrl="jdbc:mysql://\${host}:\${port}/\${dbname}"
const val oracleDriver="oracle.jdbc.OracleDriver"
const val oracleUrl="jdbc:oracle:thin:@\${host}:\${port}:\${dbname}"
const val db2Driver="com.ibm.db2.jcc.DB2Driver"
const val db2Url="jdbc:db2://\${host}:\${port}/\${dbname}"
const val pgDriver="org.postgresql.Driver"
const val pgUrl="jdbc:postgresql://\${host}:\${port}/\${dbname}"
const val mssqlDriver="com.microsoft.sqlserver.jdbc.SQLServerDriver"
const val mssqlUrl="jdbc:sqlserver://\${host}:\${port};DatabaseName=\${dbname}"

//fun getJDBCUrl(dbType : String,ip : String,port : String,dbname : String) : String{
//    val template = DxStringTemplate(templates = mutableMapOf("mysql" to mysqlUrl,"oracle" to oracleUrl,"ora" to oracleUrl,
//    "db2" to db2Url,"postgres" to pgUrl,"pg" to pgUrl,"mssql" to mssqlUrl))
//    return template.execTempalte(dbType.toLowerCase(), mapOf("host" to ip,"port" to port,"dbname" to dbname))
//}

class DXJdbc(val driver : String,val url : String,val user : String,val passwd : String) {
    lateinit var conn: Connection
    lateinit var sql: String
    var prestmt: PreparedStatement? = null
    var cursor: ResultSet? = null
    var columns: ArrayList<String> = arrayListOf()
    var columnTypes: ArrayList<String> = arrayListOf()
    var columnTypeCodes: ArrayList<Int> = arrayListOf()
    var updcnt: Int = 0
    var resultAA = arrayListOf<ArrayList<Any>>()
    var resultAM = arrayListOf<LinkedHashMap<String,Any>>()
    var cursorIdx : Int = 0

    /*
    创建JDBC链接，由于每种数据库的url都不太一样，所以这里还是交给外部输入吧
     */
    fun createConn() {
        if (this.driver != ""){
            Class.forName(this.driver)
        }
        this.conn = DriverManager.getConnection(this.url, this.user, this.passwd)
    }

    fun closeConn() {
        this.cursor?.let {
            if (!it.isClosed) it.close()
        }
        this.prestmt?.let {
            if (!it.isClosed) it.close()
        }
        if (!this.conn.isClosed) {
            this.conn.close()
        }
    }

    fun closeQuery() {
        this.cursor?.let {
            if (!it.isClosed) it.close()
        }
        this.prestmt?.let {
            if (!it.isClosed) it.close()
        }
    }

    fun closeStmt() {
        this.prestmt?.let {
            if (!it.isClosed) it.close()
        }
    }

    fun closeAutoCommit(): DXJdbc {
        this.conn.autoCommit = false
        return this
    }

    fun commit(): DXJdbc {
        this.conn.commit()
        return this
    }

    fun openStmt(sql: String, fetchsize: Int = 10000): DXJdbc {
        this.prestmt = this.conn.prepareStatement(sql)
        this.sql = sql
        this.prestmt!!.fetchSize = fetchsize
        return this
    }

    fun setPara(para: ArrayList<Any>): DXJdbc {
        for ((i, v) in para.withIndex()) {
            this.prestmt!!.setObject(i + 1, v)
        }
        return this
    }


    fun executeUpdate(): DXJdbc {
        this.updcnt = this.prestmt?.executeUpdate() ?: throw Exception("no sql for execute")
        return this
    }

    fun executeBatch(batchPara: ArrayList<ArrayList<Any>>): DXJdbc {
        val auto = this.conn.autoCommit
        if (auto) {
            this.closeAutoCommit()
        }
        this.prestmt?.let {
            for (i1 in batchPara) {
                this.setPara(i1)
                it.addBatch()
            }
            it.executeBatch()
            this.conn.commit()
            it.clearBatch()
        }
        this.conn.autoCommit = auto
        return this
    }

    fun executeQuery(): DXJdbc {
        this.cursor = this.prestmt?.executeQuery()
        //初始化字段集
        this.columns.clear()
        for (i in 1..cursor!!.metaData.columnCount) {
            columns.add(cursor!!.metaData.getColumnName(i))
        }
        this.columnTypes.clear()
        for (i in 1..cursor!!.metaData.columnCount) {
            columnTypes.add(cursor!!.metaData.getColumnTypeName(i).uppercase())
        }
        this.columnTypeCodes.clear()
        for (i in 1..cursor!!.metaData.columnCount) {
            columnTypeCodes.add(cursor!!.metaData.getColumnType(i))
        }
        return this
    }

    fun getResultByAA(cnt: Int): DXJdbc {
        cleanCachedResult()
        var nowcnt = 1
        this.cursor?.let {
            while (it.next()) {
                val line = arrayListOf<Any>()
                for (i in this.columns) {
                    line.add(it.getObject(i))
                }
                this.resultAA.add(line)
                nowcnt++
                if (nowcnt > cnt && cnt != -1) {
                    break
                }
            }
        }
        return this
    }


    fun getResultByAM(cnt: Int): DXJdbc {
        cleanCachedResult()
        var nowcnt = 1
        this.cursor?.let {
            while (it.next()) {
                val line = LinkedHashMap<String,Any>()
                for (i in this.columns) {
                    line[i] = it.getObject(i)
                }
                this.resultAM.add(line)
                nowcnt++
                if (nowcnt > cnt && cnt != -1) {
                    break
                }
            }
        }
        return this
    }

    fun cleanCachedResult(): DXJdbc {
        this.resultAA.clear()
        this.resultAM.clear()
        return this
    }

    fun unloadToFile(
        filename: String, charset: String = "GBK", delimiter: String = "/",
        showhead: Boolean = false, jdateformat: String = "yyyy-MM-dd", jtimestampformat: String = "yyyy-MM-dd HH:mm:ss",
        flushsize: Int = 50000
    ): DXJdbc {
        this.cursor?.let {
            val f = File(filename)
            val buffer = f.outputStream().bufferedWriter(charset(charset)).buffered()

            if (showhead) {
                for (i in this.columns) {
                    buffer.write(i)
                    if (i != this.columns.last()) {
                        buffer.write(delimiter)
                    }
                }
                buffer.newLine()
            }

            while (it.next()) {
                for ((idx, i) in this.columns.withIndex()) {
                    when (this.columnTypes[idx]) {
                        "DATE" -> {
                            val x = it.getDate(i)
                            if (!it.wasNull()){
                                buffer.write(formatDatetime2Str(x, jdateformat))
                            }else{
                                buffer.write("")
                            }
                        }
                        "TIMESTAMP" -> {
                            val x = it.getTimestamp(i)
                            if (!it.wasNull()){
                                buffer.write(formatDatetime2Str(x, jtimestampformat))
                            }else{
                                buffer.write("")
                            }
                        }
                        else -> {
                            val x = it.getString(i)
                            if (!it.wasNull()){
                                buffer.write(x)
                            }else{
                                buffer.write("")
                            }
                        }
                    }
                    if (i != this.columns.last()) {
                        buffer.write(delimiter)
                    }
                }
                buffer.newLine()
                this.cursorIdx++
                if (this.cursorIdx % flushsize == 0) {
                    buffer.flush()
                }
            }
            buffer.flush()
            buffer.close()
        }
        return this
    }

    fun loadFile(
        filename: String,
        tablename: String,
        charset: String = "GBK",
        delimiter: String = "/",
        batchsize: Int = 50000,
        column: Array<String> = arrayOf("*"),
        skip: Long = 0,
        truncate: Boolean = false
    ) {
        var incnt = 0
        val f = File(filename)
        val reader = f.bufferedReader(charset(charset)).buffered()
        if (skip > 0) {
            for (i in 1..skip) {
                reader.readLine()
            }
        }
        //分析插入字段的字段类型
        if (truncate) {
            this.openStmt("truncate table ${tablename}").executeUpdate().closeStmt()
        }

        this.closeAutoCommit()
        this.openStmt("select ${column.joinToString(",")} from ${tablename}").executeQuery().closeStmt()
        this.openStmt("insert into ${tablename}(${this.columns.joinToString(",")}) values(${this.columns.map { "?" }
            .joinToString(",")})")

        //println("insert into ${tablename}(${this.columns.joinToString(",")}) values(${this.columns.map { "?" }.joinToString(",")})")
        //println("select ${column.joinToString(",") } from ${tablename}")
        //批量插入
        reader.forEachLine outloop@{
            for ((i, v) in it.split(delimiter).withIndex()) {
                if (v == ""){
                    this.prestmt!!.setNull(i+1,this.columnTypeCodes[i])
                }
                when (this.columnTypes[i]) {
                    "DATE" -> this.prestmt!!.setDate(i + 1, java.sql.Date(DateParserUtils.parseDate(v).time))
                    "TIMESTAMP" -> this.prestmt!!.setTimestamp(
                        i + 1,
                        java.sql.Timestamp(DateParserUtils.parseDate(v).time)
                    )
                    else -> this.prestmt!!.setObject(i + 1, v, this.columnTypeCodes[i])
                }
            }
            this.prestmt!!.addBatch()
            incnt++
            if (incnt % batchsize == 0) {
                this.prestmt!!.executeBatch()
                this.commit()
                this.prestmt!!.clearBatch()
            }

            return@outloop
        }
        this.prestmt!!.executeBatch()
        this.commit()
        this.prestmt!!.clearBatch()
        this.closeStmt()
        reader.close()
    }


//    fun unloadToXlsxWithEasyexcel(filename : String,jdateformat: String = "yyyy-MM-dd", jtimestampformat: String = "yyyy-MM-dd HH:mm:ss",batchSize : Int = 50000,feedback : Boolean = false,trace : Boolean) : DXJdbc{
//        this.cursor?.let {
//            if (trace){
//                println("create a excel instance")
//            }
//            val writer = EasyExcel.write(filename).build()
//            if (trace){
//                println("copy header info to excel instance")
//            }
//            val headerCol = mutableListOf<String>()
//            headerCol.clear()
//            for (column in this.columns){
//                headerCol.add(column)
//            }
//            if (trace){
//                println("create a sheet instance")
//            }
//            var sheet = EasyExcel.writerSheet().needHead(false).build()
//
//            var rownum = 1
//            val batchResult = mutableListOf<List<String>>(headerCol)
//            if (trace){
//                println("start copy selected data to sheet")
//            }
//            while (it.next()){
//                val rowContent = mutableListOf<String>()
//                for ((idx, i) in this.columns.withIndex()) {
//                    when (this.columnTypes[idx]) {
//                        "DATE" -> {
//                            val x = it.getDate(i)
//                            if (!it.wasNull()){
//                                rowContent.add(formatDatetime2Str(x, jdateformat))
//                            }else{
//                                rowContent.add("")
//                            }
//                        }
//                        "TIMESTAMP" -> {
//                            val x = it.getTimestamp(i)
//                            if (!it.wasNull()){
//                                rowContent.add(formatDatetime2Str(x, jtimestampformat))
//                            }else{
//                                rowContent.add("")
//                            }
//                        }
//                        else -> {
//                            val x = it.getString(i)
//                            if (!it.wasNull()){
//                                rowContent.add(x)
//                            }else{
//                                rowContent.add("")
//                            }
//                        }
//                    }
//                }
//                if (trace){
//                    println("adding ${rowContent}")
//                }
//                batchResult.add(rowContent)
//                rownum++
//                if (batchResult.size >= batchSize){
//                    if (trace){
//                        println("commit batch")
//                    }
//                    writer.write(batchResult,sheet)
//                    batchResult.clear()
//                    if (feedback) {
//                        print(".")
//                    }
//                }
//                if (rownum >= 1048570){
//                    writer.write(batchResult,sheet)
//                    batchResult.clear()
//                    sheet = EasyExcel.writerSheet().needHead(false).build()
//                    batchResult.add(headerCol)
//                    rownum = 2
//                }
//            }
//            if (trace){
//                println("final batch")
//            }
//            writer.write(batchResult,sheet)
//            batchResult.clear()
//            writer.finish()
//        }
//        return this
//    }

    fun unloadToXlsxWithEasyexcel(filename : String,jdateformat: String = "yyyy-MM-dd", jtimestampformat: String = "yyyy-MM-dd HH:mm:ss") : DXJdbc {
        this.cursor?.let {
            val headerCol = mutableListOf<String>()
            headerCol.clear()
            for (column in this.columns){
                headerCol.add(column)
            }
            var rownum = 1
            val batchResult = mutableListOf<List<String>>(headerCol)
            while (it.next()){
                val rowContent = mutableListOf<String>()
                for ((idx, i) in this.columns.withIndex()) {
                    when (this.columnTypes[idx]) {
                        "DATE" -> {
                            val x = it.getDate(i)
                            if (!it.wasNull()){
                                rowContent.add(formatDatetime2Str(x, jdateformat))
                            }else{
                                rowContent.add("")
                            }
                        }
                        "TIMESTAMP" -> {
                            val x = it.getTimestamp(i)
                            if (!it.wasNull()){
                                rowContent.add(formatDatetime2Str(x, jtimestampformat))
                            }else{
                                rowContent.add("")
                            }
                        }
                        else -> {
                            val x = it.getString(i)
                            if (!it.wasNull()){
                                rowContent.add(x)
                            }else{
                                rowContent.add("")
                            }
                        }
                    }
                }
                batchResult.add(rowContent)
                rownum++
                if (rownum >= 1048576 - 1){
                    println("rows beyonged 1048576!some data may not be exported!")
                    break
                }
            }
            EasyExcel.write(filename).sheet("sheet1").doWrite(batchResult)
        }
        return this
    }
}