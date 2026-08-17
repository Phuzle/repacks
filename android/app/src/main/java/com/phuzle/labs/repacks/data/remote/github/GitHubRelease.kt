package com.phuzle.labs.repacks.data.remote.github

data class GitHubRelease(
    val tagName: String,
    val name: String?,
    val body: String?,
    val htmlUrl: String,
    val apkAssetUrl: String?,
    val apkAssetName: String?,
)
