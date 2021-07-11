package com.dadachen

import java.io.*
import java.nio.file.Files
import java.nio.file.Path
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

fun downloadFeatureData(bids: Array<String>):File{
    val path = "building.zip"
    Files.deleteIfExists(File(path).toPath())
    val zipFile = "building"
    bids.forEach { bid->
        genFeatureData(bid)
    }
    zipAll(zipFile, path)
    return File(path)
}

fun genFeatureData(bid:String):String{
    if (checkUpdate(bid).not()){
        return "building/$bid"
    }
    val path = Files.createTempDirectory("building/$bid")
    path.toFile().deleteOnExit()
    val types = arrayOf("wifi","ble","pic","mag")
    types.forEach { type->
        when(type){
            "wifi" -> genWifiFeatureFile(bid, path)
            else -> getFeatureFilePath(bid, type, path)
        }

    }

    return "building/$bid"
}

fun checkUpdate(bid: String):Boolean{
    return false
}

//get ble, pic and mag, wifi excluded
fun getFeatureFilePath(bid: String, typeString:String, path: Path){
    val version = getLastVersion(bid, typeString)
    val sql = "select source_url from static_feature_lib where model_num = ${version[0]} and update_num = ${version[1]} and bid = '$bid'"
    val res = connection.sendQuery(sql).join()
    val fpath = res.rows[0][0].toString()
    Files.copy(File(fpath).toPath(), path )
}


fun genWifiFeatureFile(bid: String, path: Path){

}


fun zipAll(directory: String, zipFile: String) {
    val sourceFile = File(directory)

    ZipOutputStream(BufferedOutputStream(FileOutputStream(zipFile))).use {
        it.use {
            zipFiles(it, sourceFile, "")
        }
    }
}

private fun zipFiles(zipOut: ZipOutputStream, sourceFile: File, parentDirPath: String) {

    val data = ByteArray(2048)

    for (f in sourceFile.listFiles()) {

        if (f.isDirectory) {
            val entry = ZipEntry(f.name + File.separator)
            entry.time = f.lastModified()
            entry.isDirectory
            entry.size = f.length()

            zipOut.putNextEntry(entry)

            //Call recursively to add files within this directory
            zipFiles(zipOut, f, f.name)
        } else {

            if (!f.name.contains(".zip")) { //If folder contains a file with extension ".zip", skip it
                FileInputStream(f).use { fi ->
                    BufferedInputStream(fi).use { origin ->
                        val path = parentDirPath + File.separator + f.name
                        val entry = ZipEntry(path)
                        entry.time = f.lastModified()
                        entry.isDirectory
                        entry.size = f.length()
                        zipOut.putNextEntry(entry)
                        while (true) {
                            val readBytes = origin.read(data)
                            if (readBytes == -1) {
                                break
                            }
                            zipOut.write(data, 0, readBytes)
                        }
                    }
                }
            } else {
                zipOut.closeEntry()
                zipOut.close()
            }
        }
    }
}