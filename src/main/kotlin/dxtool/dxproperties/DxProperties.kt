package dxtool.dxproperties

import dxtool.dxResources.getResourceAsReader
import java.io.File

fun propToMapWithResource(propertiesInResource : String) : Map<String,Any>{
    val prop = java.util.Properties()
    val propMap : MutableMap<String,Any> = mutableMapOf()
    prop.load(getResourceAsReader(propertiesInResource))
    for (i in prop.keys){
        propMap.put(i.toString(),prop.get(i.toString()).toString())
    }
    return propMap
}

fun propToMapWithFile(propertiesFile : String) : Map<String,Any> {
    val prop = java.util.Properties()
    val propMap: MutableMap<String, Any> = mutableMapOf()
    prop.load(File(propertiesFile).inputStream())
    for (i in prop.keys) {
        propMap.put(i.toString(), prop.get(i.toString()).toString())
    }
    return propMap
}