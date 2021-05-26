package dxtool.dxcompress

import com.linkedin.migz.MiGzInputStream
import com.linkedin.migz.MiGzOutputStream
import org.apache.commons.compress.archivers.ArchiveEntry
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry
import org.apache.commons.compress.archivers.sevenz.SevenZFile
import org.apache.commons.compress.archivers.sevenz.SevenZOutputFile
import org.apache.commons.compress.archivers.tar.TarArchiveEntry
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream
import org.apache.commons.io.FileUtils
import java.io.*
import java.nio.file.Files
import java.nio.file.Paths
import java.util.zip.Deflater


fun tarFile(tarFileName : String,inputFiles : Array<String>,skipPath : Boolean = false){
    val taos = TarArchiveOutputStream(FileOutputStream(File(tarFileName)))
    for (i in inputFiles){
        val tarElemF = File(i)
        val tarElem = TarArchiveEntry(tarElemF)
        tarElem.size = tarElemF.length()
        if (skipPath){
            tarElem.name = String(tarElemF.name.toByteArray())
        }
        taos.putArchiveEntry(tarElem)
        val bis = BufferedInputStream(FileInputStream(i))
        var count: Int
        val data = ByteArray(1024)
        while (bis.read(data, 0, 1024).also { count = it } != -1) {
            taos.write(data, 0, count)
        }
        bis.close()
        taos.closeArchiveEntry()
    }
    taos.close()
}

fun deTarFile(tarFileName : String,detarPath : String = "") : Array<String>{
    val fileList = mutableListOf<String>()
    val tais = TarArchiveInputStream(FileInputStream(File(tarFileName)))
    var tae : TarArchiveEntry?
    while (tais.nextTarEntry.also { tae = it } != null) {
        var dir: String
        if (detarPath == ""){
            dir = tae!!.name //tar档中文件
        }else{
            dir = detarPath + File.separator + tae!!.name
        }
        fileList.add(tae!!.name)
        val dirFile = File(dir)
        val bos = BufferedOutputStream(FileOutputStream(dirFile))
        var count: Int
        val data = ByteArray(1024)
        while (tais.read(data, 0, 1024).also { count = it } != -1) {
            bos.write(data, 0, count)
        }
        bos.close()
    }
    tais.close()
    return fileList.toTypedArray()
}

fun gzipFile(outFile : String,inFile : String){
    val fin = Files.newInputStream(Paths.get(inFile))
    val fout = Files.newOutputStream(Paths.get(outFile))
    val out = BufferedOutputStream(fout)
    val gzOut = GzipCompressorOutputStream(out)
    val buffer = ByteArray(1024)
    var n : Int
    while (-1 != fin.read(buffer).also { n = it }) {
        gzOut.write(buffer, 0, n)
    }
    gzOut.close()
    fin.close()
}

fun gunzipFile(outFile : String,inFile : String){
    val fin = Files.newInputStream(Paths.get(inFile))
    val `in` = BufferedInputStream(fin)
    val out = Files.newOutputStream(Paths.get(outFile))
    val gzIn = GzipCompressorInputStream(`in`,true)
    val buffer = ByteArray(1024)
    var n : Int
    while (-1 != gzIn.read(buffer).also { n = it }) {
        out.write(buffer, 0, n)
    }
    out.close()
    gzIn.close()
}

fun zip7zFile(outFile : String,inFile : String,feedback : Boolean = false){
    val zip7z = {input : File,out : SevenZOutputFile -> val fos = FileInputStream(input)
        val bis = BufferedInputStream(fos)
        val entry : ArchiveEntry
        entry = out.createArchiveEntry(input, input.absolutePath)
        out.putArchiveEntry(entry)
        if (feedback)println("compressing ${input.absolutePath}")
        var len : Int
        val buf = ByteArray(1024)
        while (bis.read(buf).also { len = it } != -1) {
            out.write(buf, 0, len)
        }
        bis.close()
        fos.close()
        out.closeArchiveEntry()
    }
    if (File(inFile).isDirectory) {
        val fl = FileUtils.listFiles(File(inFile), null, true)
        val out = SevenZOutputFile(File(outFile))
        for (i in fl) {
            zip7z(i, out)
        }
        out.close()
    }else{
        val out = SevenZOutputFile(File(outFile))
        zip7z(File(inFile), out)
        out.close()
    }
}

fun Unzip7zFile(outputPath : String,inputFile : String,feedback : Boolean = false){
    val srcFile = File(inputFile) //获取当前压缩文件
// 判断源文件是否存在
    if (!srcFile.exists()) {
        throw Exception(srcFile.path + "所指文件不存在")
    }
//开始解压
    val zIn = SevenZFile(srcFile)
    var entry: SevenZArchiveEntry?
    var file: File?
    while (zIn.nextEntry.also { entry = it } != null) {
        if (!entry!!.isDirectory) {
            file = File(outputPath,entry!!.name)
            if (!file.parentFile.exists()){
                file.parentFile.mkdirs()
            }
            if (feedback){
                println("uncompressing "+entry!!.name + " to "+ file.absolutePath)
            }
            val out: OutputStream = FileOutputStream(file)
            val bos = BufferedOutputStream(out)
            var len: Int
            val buf = ByteArray(1024)
            while (zIn.read(buf).also { len = it } != -1) {
                bos.write(buf, 0, len)
            }
            // 关流顺序，先打开的后关闭
            bos.close()
            out.close()
        }
    }
}

fun MiGzip(
    inputFile: String,
    outputFile: String = inputFile + ".mgz",
    thread: Int = MiGzOutputStream.DEFAULT_THREAD_COUNT,
    blockSize : Int = MiGzOutputStream.DEFAULT_BLOCK_SIZE
) {
    val fis = FileInputStream(inputFile)
    val fos = FileOutputStream(outputFile)
    val gzipOS = MiGzOutputStream(fos,thread,blockSize).setCompressionLevel(Deflater.DEFAULT_COMPRESSION)
    val buffer = ByteArray(blockSize)
    var len: Int
    while (fis.read(buffer).also { len = it } != -1) {
        gzipOS.write(buffer, 0, len)
    }
    //close resources
    gzipOS.close()
    fos.close()
    fis.close()
}

fun MiGunzip(inputFile: String,outputFile: String = inputFile.substringBeforeLast(".mgz"),thread : Int = MiGzOutputStream.DEFAULT_THREAD_COUNT,bufferSize : Int = MiGzOutputStream.DEFAULT_BLOCK_SIZE){
    if (!inputFile.endsWith(".mgz")){
        throw Exception("the filename must be end with .mgz!")
    }
    val fis = FileInputStream(inputFile)
    val gis = MiGzInputStream(fis,thread)
    val fos = FileOutputStream(outputFile)
    val buffer = ByteArray(bufferSize)
    var len: Int
    while (gis.read(buffer).also { len = it } != -1) {
        fos.write(buffer, 0, len)
    }
    //close resources
    fos.close()
    gis.close()
}