package dxtool.dxResources

import java.io.File
import java.io.Reader
import java.lang.reflect.Method
import java.net.URL
import java.net.URLClassLoader

fun getResourceAsReader(path: String): Reader {
    return object{}::class.java.getResourceAsStream(path).bufferedReader()
}
/*
loadJar("/Users/praynise/IdeaProjects/ipayInterfaceParser/extlibs/commons-lang3-3.11.jar", arrayOf(String::class.java, String::class.java),
arrayOf("abcd", "abcdefg"),"org.apache.commons.lang3.StringUtils","difference")
 */
fun <T> loadJavaJar(jarPath: String,xx : Array<Class<T>>,pp : Array<T>,classname : String,funcname : String) : Any{
    val jarFile = File(jarPath)
    // 从URLClassLoader类中获取类所在文件夹的方法，jar也可以认为是一个文件夹
    var method: Method? = null
    try {
        method = URLClassLoader::class.java.getDeclaredMethod("addURL", URL::class.java)
    } catch (e1: NoSuchMethodException) {
        e1.printStackTrace()
    } catch (e1: SecurityException) {
        e1.printStackTrace()
    }
    // 获取方法的访问权限以便写回
    val accessible = method!!.isAccessible
    try {
        method.isAccessible = true
        // 获取系统类加载器
        val classLoader = ClassLoader.getSystemClassLoader() as URLClassLoader
        val url = jarFile.toURI().toURL()
        method.invoke(classLoader, url)
    } catch (e: Exception) {
        e.printStackTrace()
    } finally {
        method.isAccessible = accessible
    }

    val aClass = Class.forName(classname)
    val instance = aClass.newInstance()
    return aClass.getDeclaredMethod(funcname, *xx).invoke(instance, *pp)
}