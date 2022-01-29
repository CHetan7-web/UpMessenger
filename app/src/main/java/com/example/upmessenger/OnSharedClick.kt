package com.example.upmessenger

interface OnSharedClick {
    fun updateSendButtonUI(selectedUsers:MutableMap<String,String>,hide: Boolean)
    fun sendMessage(users:List<String>)
}