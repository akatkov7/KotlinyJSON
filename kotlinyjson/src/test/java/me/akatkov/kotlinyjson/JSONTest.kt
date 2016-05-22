package me.akatkov.kotlinyjson

class JSONTest : junit.framework.TestCase() {

    // override keyword required to override the setUp method
    override fun setUp() {
        // set up the test case

    }

    // override keyword required to override the tearDown method
    override fun tearDown() {
        // tear down the test case
    }

    fun testStacking() {
        val jsonString = "{\"foo\":[{\"id\": 0}, {\"id\": 1}], \"bar\":{\"baz\": 2}}"
        val json = JSON(jsonString)

        assertEquals(0, json["foo"][0]["id"].int)
        assertEquals(1, json["foo"][1]["id"].int)
        assertEquals(2, json["bar"]["baz"].int)
    }

    fun testSettingList() {
        val jsonString = "{\"foo\": 1}"
        val json = JSON(jsonString)
        json["bar"] = JSON("[1, 2, 3]")

        assertEquals(3, json["bar"].list?.size)
    }
}