/**
 * Copyright (c) 2018 by Delphix. All rights reserved.
 */

package com.delphix.yamlparser.sdk.repos

import com.delphix.yamlparser.sdk.Http as Http
import com.delphix.yamlparser.sdk.objects.SelfServiceBookmark as SelfServiceBookmarkObj
import com.delphix.yamlparser.sdk.repos.SelfServiceContainer as SelfServiceContainer
import com.delphix.yamlparser.sdk.objects.SelfServiceContainer as SelfServiceContainerObj
import org.json.JSONObject
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class SelfServiceBookmark (
    var http: Http
) {
    val resource: String = "/resources/json/delphix/jetstream/bookmark"

    fun list(): List<SelfServiceBookmarkObj> {
        var bookmarks = mutableListOf<SelfServiceBookmarkObj>()
        val response = http.handleGet(resource).getJSONArray("result")
        for (i in 0 until response.length()) {
            val bookmark = response.getJSONObject(i);
            bookmarks.add(SelfServiceBookmarkObj.fromJson(bookmark))
        }
        return bookmarks
    }

    fun getRefByName(name: String): String {
        val bookmarks: List<SelfServiceBookmarkObj> = list()
        for (bookmark in bookmarks) {
            if (bookmark.name == name) return bookmark.reference
        }
        throw IllegalArgumentException("Self Service Bookmark '$name' does not exist.")
    }

    fun create(name: String, containerName: String): JSONObject {
        val container: SelfServiceContainerObj = SelfServiceContainer(http).getRefByName(containerName)
        val bookmark = mapOf("type" to "JSBookmark", "name" to name, "branch" to container.activeBranch)
        val timeline = mapOf("type" to "JSTimelinePointLatestTimeInput", "sourceDataLayout" to container.reference)
        val request = mapOf("type" to "JSBookmarkCreateParameters", "bookmark" to bookmark, "timelinePointParameters" to timeline)
        return http.handlePost("$resource", request)
    }

    fun share(name: String): JSONObject {
        val ref = getRefByName(name)
        return http.handlePost("$resource/$ref/share", emptyMap<String, Any>())
    }

    fun delete(name: String): JSONObject {
        val ref = getRefByName(name)
        return http.handlePost("$resource/$ref/delete", emptyMap<String, Any>())
    }
}
