package dxtool.dxtemplate

import org.beetl.core.Configuration
import org.beetl.core.GroupTemplate
import org.beetl.core.resource.StringTemplateResourceLoader


object DxTemplate{
    val gt : GroupTemplate
    init {
        val resourceLoader = StringTemplateResourceLoader()
        val cfg = Configuration.defaultConfiguration()

        gt = GroupTemplate(resourceLoader, cfg)
    }
    fun render(sourceString :String,bindMap : Map<String,Any>) :String{
        val t = gt.getTemplate(sourceString)
        t.binding(bindMap)
        return t.render()
    }

}

//class DxStringTemplate(
//        var charset : String = "UTF-8",
//        var templates : MutableMap<String,String>
//){
//    var config = Configuration(Configuration.VERSION_2_3_30)
//
//    init {
//        this.config.defaultEncoding = this.charset
//        flushTemplateLoader()
//    }
//
//    private fun flushTemplateLoader(){
//        val template = StringTemplateLoader()
//        this.templates.forEach { t, u ->  template.putTemplate(t,u)}
//        this.config.templateLoader = template
//    }
//
//    fun addTemplate(name : String,content : String){
//        this.templates[name] = content
//        flushTemplateLoader()
//    }
//
//    fun delTemplate(name: String){
//        this.templates.remove(name)
//        flushTemplateLoader()
//    }
//
//    fun execTempalte(name : String,value : Map<String,Any>) : String{
//        val buffer = StringWriter()
//        this.config.getTemplate(name).process(value,buffer)
//        return buffer.toString()
//    }
//}
//
//fun stringTemplate(charset : String = "UTF-8",stringTemplate : String,fillMap : Map<String,Any>): String{
//    val config = Configuration(Configuration.VERSION_2_3_30)
//    config.defaultEncoding = charset
//    val template = StringTemplateLoader()
//    template.putTemplate("t1",stringTemplate)
//    config.templateLoader = template
//
//    val wirter = StringWriter()
//    config.getTemplate("t1").process(fillMap,wirter)
//    return wirter.toString()
//}