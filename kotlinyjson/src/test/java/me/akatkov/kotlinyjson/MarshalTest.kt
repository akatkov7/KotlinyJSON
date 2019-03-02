package me.akatkov.kotlinyjson

class MarshalTest : junit.framework.TestCase() {

    // override keyword required to override the setUp method
    override fun setUp() {
        // set up the test case

    }

    // override keyword required to override the tearDown method
    override fun tearDown() {
        // tear down the test case
    }

    data class MutableUser(var id: Long = 0, var name: String = "")

    fun testSimpleMutableSuccessMarshal() {
        val user = MutableUser(1234567, "Johnny")
        val json = JSON().marshal(user)

        assertEquals(1234567L, json["id"].long)
        assertEquals("Johnny", json["name"].string)
    }

    data class IgnoreUser(@Ignore val id: Long = 0, @Ignore val name: String = "")

    fun testSimpleIgnoreMarshal() {
        val user = IgnoreUser(1234567, "Johnny")
        val json = JSON().marshal(user)

        assertEquals(true, json.isEmpty())
    }

    data class ModifiedKeyUser(@JSONKey("id") var userID: Long = 0, @JSONKey("name") var userName: String = "")

    fun testSimpleModifiedKeyMarshal() {
        val user = ModifiedKeyUser(1234567, "Johnny")
        val json = JSON().marshal(user)

        assertEquals(1234567L, json["id"].long)
        assertEquals("Johnny", json["name"].string)
    }

    data class Name(var first: String = "", var last: String = "")
    data class CompositeUser(var id: Long = 0, var name: Name = Name())

    fun testSimpleRecursiveMarshal() {
        val user = CompositeUser(1234567, Name("Johnny", "Bravo"))
        val json = JSON().marshal(user)

        assertEquals(1234567L, json["id"].long)
        assertEquals("Johnny", json["name"]["first"].string)
        assertEquals("Bravo", json["name"]["last"].string)
    }

    data class ListCompositeUser(var id: Long = 0, @ListClass(Name::class) var names: MutableList<Name> = mutableListOf())

    fun testSimpleListRecursiveMarshal() {
        val user = ListCompositeUser(1234567, mutableListOf(Name("Johnny", "Bravo"), Name("Little", "Suzy")))
        val json = JSON().marshal(user)

        assertEquals(1234567L, json["id"].long)
        val list = json["names"].list
        assertNotNull(list)
        if (list != null) {
            assertEquals(2, list.size)
            if (list[0]["first"].string == "Johnny") {
                assertEquals("Johnny", list[0]["first"].string)
                assertEquals("Bravo", list[0]["last"].string)
                assertEquals("Little", list[1]["first"].string)
                assertEquals("Suzy", list[1]["last"].string)
            } else {
                assertEquals("Little", list[0]["first"].string)
                assertEquals("Suzy", list[0]["last"].string)
                assertEquals("Johnny", list[1]["first"].string)
                assertEquals("Bravo", list[1]["last"].string)
            }
        }
    }

    data class NullableUser(var id: Long = 0, var name: String? = null)

    fun testSimpleNullableMissingSuccessMarshal() {
        val user = NullableUser(1234567)
        val json = JSON().marshal(user)

        assertEquals(1234567, json["id"].int)
        assertNull(json["name"].string)
    }

    fun testSimpleNullableSuccessMarshal() {
        val user = NullableUser(1234567, "Johnny")
        val json = JSON().marshal(user)

        assertEquals(1234567, json["id"].int)
        assertEquals("Johnny", json["name"].string)
    }

    data class NullableRecursiveUser(var id: Long = 0, var name: Name? = null)

    fun testSimpleNullableRecursiveSuccessMarshal() {
        val user = NullableRecursiveUser(1234567)
        val json = JSON().marshal(user)

        assertEquals(1234567, json["id"].int)
        assertNull(json["name"].string)
    }

    data class NullableListUser(var id: Long = 0, @ListClass(kotlin.String::class) var names: List<String>? = null)

    fun testSimpleNullableListSuccessMarshal() {
        val user = NullableListUser(1234567)
        val json = JSON().marshal(user)

        assertEquals(1234567, json["id"].int)
        assertNull(json["names"].string)
    }

    data class NullableListRecursiveUser(var id: Long = 0,
                                         @ListClass(kotlin.String::class, optional = true) var names: List<String?> = listOf())

    fun testSimpleNullableListRecursiveSuccessMarshal() {
        val user = NullableListRecursiveUser(1234567, listOf("Johnny", null, "Bravo"))
        val json = JSON().marshal(user)

        assertEquals(1234567, json["id"].int)
        assertEquals("Johnny", json["names"][0].string)
        assertNull(json["user"][1].string)
        assertEquals("Bravo", json["names"][2].string)
    }

    @SnakeCase
    data class SnakeUser(var userId: Long = 0, var userName: String = "")

    fun testSnakeMutableSuccessMarshal() {
        val user = SnakeUser(1234567, "Johnny")
        val json = JSON().marshal(user)

        assertEquals(1234567, json["user_id"].int)
        assertEquals("Johnny", json["user_name"].string)
    }

    @PascalCase
    data class PascalUser(var userId: Long = 0, var userName: String = "")

    fun testCamelMutableSuccessMarshal() {
        val user = PascalUser(1234567, "Johnny")
        val json = JSON().marshal(user)

        assertEquals(1234567, json["UserId"].int)
        assertEquals("Johnny", json["UserName"].string)
    }

    data class LotsOfNamesUser(val id: Long, @ListClass(Name::class) val names: List<List<Name>>)

    fun testNestedListsSuccessMarshal() {
        val user = LotsOfNamesUser(1234567, listOf(listOf(Name("Johnny", "Bravo"))))
        val json = JSON().marshal(user)

        assertEquals(1234567, json["id"].int)
        assertEquals("Johnny", json["names"][0][0]["first"].string)
        assertEquals("Bravo", json["names"][0][0]["last"].string)
    }

    data class LotsOfNamesMaybeUser(val id: Long, @ListClass(Name::class) val names: List<List<Name>?>)

    fun testNullableNestedListsSuccessMarshal() {
        val user = LotsOfNamesMaybeUser(1234567, listOf(listOf(Name("Johnny", "Bravo")), null))
        val json = JSON().marshal(user)

        assertEquals(1234567, user.id)
        assertEquals("Johnny", json["names"][0][0]["first"].string)
        assertEquals("Bravo", json["names"][0][0]["last"].string)

        val uselessUser = LotsOfNamesMaybeUser(1234567, listOf(null, null))
        val uselessJson = JSON().marshal(uselessUser)
        assertEquals(1234567, user.id)
        assertNull(uselessJson["names"][0].list)
        assertNull(uselessJson["names"][1].list)
    }

    data class LotsOfNamesMaybeNamelessUser(val id: Long, @ListClass(me.akatkov.kotlinyjson.MarshalTest.Name::class) val names: List<List<Name?>?>)

    fun testNestedListsWithNullableSuccessMarshal() {
        val user = LotsOfNamesMaybeNamelessUser(1234567, listOf(listOf(Name("Johnny", "Bravo"), null), null))
        val json = JSON().marshal(user)

        assertEquals(1234567, user.id)
        assertEquals("Johnny", json["names"][0][0]["first"].string)
        assertEquals("Bravo", json["names"][0][0]["last"].string)
        assertNull(json["names"][0][1].map)
        assertNull(json["names"][1].list)
    }
}