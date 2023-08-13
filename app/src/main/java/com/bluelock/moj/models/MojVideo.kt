package com.bluelock.moj.models

class MojVideo {
    var error = false
    var data: ArrayList<Data>? = null

    class Data {
        var url: String? = null
    }
}