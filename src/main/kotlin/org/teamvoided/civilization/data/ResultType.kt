package org.teamvoided.civilization.data

enum class ResultType {
    SUCCESS, FAIL, LOGIC;
    fun didFail() = this == FAIL
}
