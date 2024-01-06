package org.teamvoided.civilization.util

enum class ResultType {
    SUCCESS, FAIL;
    fun didFail() = this == FAIL
}
