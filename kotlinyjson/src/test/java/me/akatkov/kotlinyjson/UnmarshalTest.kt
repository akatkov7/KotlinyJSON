import me.akatkov.kotlinyjson.*

class UnmarshalTest : junit.framework.TestCase() {

    // override keyword required to override the setUp method
    override fun setUp() {
        // set up the test case

    }

    // override keyword required to override the tearDown method
    override fun tearDown() {
        // tear down the test case
    }

    data class MutableUser(var id: Long = 0, var name: String = "")

    fun testSimpleMutableSuccessUnmarshal() {
        val json = JSON("{\"name\":\"Johnny\",\"id\":1234567}")
        val user = json.unmarshal(MutableUser())

        assertNotNull(user)
        user!!
        assertEquals(1234567, user.id)
        assertEquals("Johnny", user.name)
    }

    fun testSimpleMutableFailureUnmarshal() {
        val json = JSON("{\"name\":\"Johnny\",\"idd\":1234567}")
        val user = json.unmarshal(MutableUser())

        assertNull(user)
    }

    data class User(val id: Long = 0, val name: String = "")

    fun testSimpleFailuresUnmarshal() {
        val json = JSON("{\"name\":\"Johnny\",\"id\":1234567}")
        try {
            json.unmarshal(User())
            fail()
        } catch (e: Exception) {
            assertTrue(e is JSONUnmarshalException)
        }
    }

    data class IgnoreUser(@Ignore val id: Long = 0, @Ignore val name: String = "")

    fun testSimpleIgnoreUnmarshal() {
        val json = JSON("{\"name\":\"Johnny\",\"id\":1234567}")
        val user = json.unmarshal(IgnoreUser())

        assertNotNull(user)
        user!!
        assertEquals(0, user.id)
        assertEquals("", user.name)
    }

    data class ModifiedKeyUser(@JSONKey("id") var userID: Long = 0, @JSONKey("name") var userName: String = "")

    fun testSimpleModifiedKeyUnmarshal() {
        val json = JSON("{\"name\":\"Johnny\",\"id\":1234567}")
        val user = json.unmarshal(ModifiedKeyUser())

        assertNotNull(user)
        user!!
        assertEquals(1234567, user.userID)
        assertEquals("Johnny", user.userName)
    }

    data class Name(var first: String = "", var last: String = "")
    data class CompositeUser(var id: Long = 0, var name: Name = Name())

    fun testSimpleRecursiveUnmarshal() {
        val json = JSON("{\"name\":{\"first\":\"Johnny\",\"last\":\"Bravo\"},\"id\":1234567}")
        val user = json.unmarshal(CompositeUser())

        assertNotNull(user)
        user!!
        assertEquals(1234567, user.id)
        assertEquals("Johnny", user.name.first)
        assertEquals("Bravo", user.name.last)
    }

    data class ListCompositeUser(var id: Long = 0, @ListClass(Name::class) var names: MutableList<Name> = arrayListOf())

    fun testSimpleListRecursiveUnmarshal() {
        val json = JSON("{\"names\":[{\"first\":\"Johnny\",\"last\":\"Bravo\"},{\"first\":\"Little\",\"last\":\"Suzy\"}],\"id\":1234567}")
        val user = json.unmarshal(ListCompositeUser())

        assertNotNull(user)
        user!!
        assertEquals(1234567, user.id)
        assertEquals(2, user.names.size)
        assertEquals("Johnny", user.names[0].first)
        assertEquals("Bravo", user.names[0].last)
        assertEquals("Little", user.names[1].first)
        assertEquals("Suzy", user.names[1].last)
    }

    data class NullableUser(var id: Long = 0, var name: String? = null)

    fun testSimpleNullableMissingSuccessUnmarshal() {
        val json = JSON("{\"named\":\"Johnny\",\"id\":1234567}")
        val user = json.unmarshal(NullableUser())

        assertNotNull(user)
        user!!
        assertEquals(1234567, user.id)
        assertNull(user.name)
    }

    fun testSimpleNullableSuccessUnmarshal() {
        val json = JSON("{\"name\":\"Johnny\",\"id\":1234567}")
        val user = json.unmarshal(NullableUser())

        assertNotNull(user)
        user!!
        assertEquals(1234567, user.id)
        assertEquals("Johnny", user.name)
    }

    data class NullableRecursiveUser(var id: Long = 0, var name: Name? = null)

    fun testSimpleNullableRecursiveSuccessUnmarshal() {
        val json = JSON("{\"named\":{\"first\":\"Johnny\",\"last\":\"Bravo\"},\"id\":1234567}")
        val user = json.unmarshal(NullableRecursiveUser())

        assertNotNull(user)
        user!!
        assertEquals(1234567, user.id)
        assertNull(user.name)
    }

    data class NullableListUser(var id: Long = 0, @ListClass(String::class) var names: List<String>? = null)

    fun testSimpleNullableListSuccessUnmarshal() {
        val json = JSON("{\"namesd\":[\"Johnny\"],\"id\":1234567}")
        val user = json.unmarshal(NullableListUser())

        assertNotNull(user)
        user!!
        assertEquals(1234567, user.id)
        assertNull(user.names)
    }

    data class NullableListRecursiveUser(var id: Long = 0,
                                         @ListClass(String::class, optional = true) var names: List<String?> = arrayListOf())

    fun testSimpleNullableListRecursiveSuccessUnmarshal() {
        val json = JSON("{\"names\":[\"Johnny\", null, \"Bravo\"],\"id\":1234567}")
        val user = json.unmarshal(NullableListRecursiveUser())

        assertNotNull(user)
        user!!
        assertEquals(1234567, user.id)
        assertEquals("Johnny", user.names[0])
        assertNull(user.names[1])
        assertEquals("Bravo", user.names[2])
    }

    //    data class NoDefaultsUser(var id: Long, var name: String)
    //
    //    fun testSimpleNoDefaultsSuccessUnmarshal() {
    //        val json = JSON("{\"name\":\"Johnny\",\"id\":1234567}")
    //        val user = json.unmarshal(NoDefaultsUser::class)
    //
    //        assertNotNull(user)
    //        user!!
    //        assertEquals(1234567, user.id)
    //        assertEquals("Johnny", user.name)
    //    }
}