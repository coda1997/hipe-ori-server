package com.dadachen

import com.google.gson.Gson

data class BuildingInfo(
    var bid:String,
    var fids:Array<Int>
)

fun fetchInfos():Array<BuildingInfo>{
    return arrayOf(
        BuildingInfo(
            bid = "shilintong",
            fids = arrayOf(1,2,3,4,5)
        ),
        BuildingInfo(
            bid = "wukan",
            fids = arrayOf(-2,-1)
        )
    )
}