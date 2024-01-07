package org.teamvoided.civilization.util

enum class ResultType {
    SUCCESS, FAIL, LOGIC;
    fun didFail() = this == FAIL
}
