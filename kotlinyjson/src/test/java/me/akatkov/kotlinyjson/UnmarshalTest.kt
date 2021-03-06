package me.akatkov.kotlinyjson

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
        val user = json.unmarshal(MutableUser::class)

        assertNotNull(user)
        user!!
        assertEquals(1234567, user.id)
        assertEquals("Johnny", user.name)
    }

    fun testSimpleMutableFailureUnmarshal() {
        val json = JSON("{\"name\":\"Johnny\",\"idd\":1234567}")
        val user = json.unmarshal(MutableUser::class)

        assertNull(user)
    }

    data class IgnoreUser(@Ignore val id: Long = 0, @Ignore val name: String = "")

    fun testSimpleIgnoreUnmarshal() {
        val json = JSON("{\"name\":\"Johnny\",\"id\":1234567}")
        val user = json.unmarshal(IgnoreUser::class)

        assertNotNull(user)
        user!!
        assertEquals(0, user.id)
        assertEquals("", user.name)
    }

    data class ModifiedKeyUser(@JSONKey("id") var userID: Long = 0, @JSONKey("name") var userName: String = "")

    fun testSimpleModifiedKeyUnmarshal() {
        val json = JSON("{\"name\":\"Johnny\",\"id\":1234567}")
        val user = json.unmarshal(ModifiedKeyUser::class)

        assertNotNull(user)
        user!!
        assertEquals(1234567, user.userID)
        assertEquals("Johnny", user.userName)
    }

    data class Name(var first: String = "", var last: String = "")
    data class CompositeUser(var id: Long = 0, var name: Name = Name())

    fun testSimpleRecursiveUnmarshal() {
        val json = JSON("{\"name\":{\"first\":\"Johnny\",\"last\":\"Bravo\"},\"id\":1234567}")
        val user = json.unmarshal(CompositeUser::class)

        assertNotNull(user)
        user!!
        assertEquals(1234567, user.id)
        assertEquals("Johnny", user.name.first)
        assertEquals("Bravo", user.name.last)
    }

    data class ListCompositeUser(var id: Long = 0, @ListClass(Name::class) var names: MutableList<Name> = mutableListOf())

    fun testSimpleListRecursiveUnmarshal() {
        val json = JSON("{\"names\":[{\"first\":\"Johnny\",\"last\":\"Bravo\"},{\"first\":\"Little\",\"last\":\"Suzy\"}],\"id\":1234567}")
        val user = json.unmarshal(ListCompositeUser::class)

        assertNotNull(user)
        user!!
        assertEquals(1234567, user.id)
        assertEquals(2, user.names.size)
        assertEquals("Johnny", user.names[0].first)
        assertEquals("Bravo", user.names[0].last)
        assertEquals("Little", user.names[1].first)
        assertEquals("Suzy", user.names[1].last)
    }

    fun testInvalidListFailureUnmarshal() {
        val json = JSON("{\"names\":[{\"first\":\"Johnny\",\"last\":\"Bravo\"},null,{\"first\":\"Little\",\"last\":\"Suzy\"}],\"id\":1234567}")
        val user = json.unmarshal(ListCompositeUser::class)

        assertNull(user)
    }

    data class NullableUser(var id: Long = 0, var name: String? = null)

    fun testSimpleNullableMissingSuccessUnmarshal() {
        val json = JSON("{\"named\":\"Johnny\",\"id\":1234567}")
        val user = json.unmarshal(NullableUser::class)

        assertNotNull(user)
        user!!
        assertEquals(1234567, user.id)
        assertNull(user.name)
    }

    fun testSimpleNullableSuccessUnmarshal() {
        val json = JSON("{\"name\":\"Johnny\",\"id\":1234567}")
        val user = json.unmarshal(NullableUser::class)

        assertNotNull(user)
        user!!
        assertEquals(1234567, user.id)
        assertEquals("Johnny", user.name)
    }

    data class NullableRecursiveUser(var id: Long = 0, var name: Name? = null)

    fun testSimpleNullableRecursiveSuccessUnmarshal() {
        val json = JSON("{\"named\":{\"first\":\"Johnny\",\"last\":\"Bravo\"},\"id\":1234567}")
        val user = json.unmarshal(NullableRecursiveUser::class)

        assertNotNull(user)
        user!!
        assertEquals(1234567, user.id)
        assertNull(user.name)
    }

    data class NullableListUser(var id: Long = 0, @ListClass(String::class) var names: List<String>? = null)

    fun testSimpleNullableListSuccessUnmarshal() {
        val json = JSON("{\"namesd\":[\"Johnny\"],\"id\":1234567}")
        val user = json.unmarshal(NullableListUser::class)

        assertNotNull(user)
        user!!
        assertEquals(1234567, user.id)
        assertNull(user.names)
    }

    data class NullableListRecursiveUser(var id: Long = 0,
                                         @ListClass(String::class, optional = true) var names: List<String?> = listOf())

    fun testSimpleNullableListRecursiveSuccessUnmarshal() {
        val json = JSON("{\"names\":[\"Johnny\", null, \"Bravo\"],\"id\":1234567}")
        val user = json.unmarshal(NullableListRecursiveUser::class)

        assertNotNull(user)
        user!!
        assertEquals(1234567, user.id)
        assertEquals("Johnny", user.names[0])
        assertNull(user.names[1])
        assertEquals("Bravo", user.names[2])
    }

    @SnakeCase
    data class SnakeUser(var userId: Long = 0, var userName: String = "")

    fun testSnakeMutableSuccessUnmarshal() {
        val json = JSON("{\"user_name\":\"Johnny\",\"user_id\":1234567}")
        val user = json.unmarshal(SnakeUser::class)

        assertNotNull(user)
        user!!
        assertEquals(1234567, user.userId)
        assertEquals("Johnny", user.userName)
    }

    @PascalCase
    data class PascalUser(var userId: Long = 0, var userName: String = "")

    fun testCamelMutableSuccessUnmarshal() {
        val json = JSON("{\"UserName\":\"Johnny\",\"UserId\":1234567}")
        val user = json.unmarshal(PascalUser::class)

        assertNotNull(user)
        user!!
        assertEquals(1234567, user.userId)
        assertEquals("Johnny", user.userName)
    }

    data class NoDefaultUser(var id: Long, var name: String)

    fun testSimpleNoDefaultsSuccessUnmarshal() {
        val json = JSON("{\"name\":\"Johnny\",\"id\":1234567}")
        val user = json.unmarshal(NoDefaultUser::class)

        assertNotNull(user)
        user!!
        assertEquals(1234567, user.id)
        assertEquals("Johnny", user.name)
    }

    data class ImmutableUser(val id: Long, val name: String)

    fun testSimpleImmutableSuccessUnmarshal() {
        val json = JSON("{\"name\":\"Johnny\",\"id\":1234567}")
        val user = json.unmarshal(ImmutableUser::class)

        assertNotNull(user)
        user!!
        assertEquals(1234567, user.id)
        assertEquals("Johnny", user.name)
    }

    data class ImmutableIgnoreUser(val id: Long, @Ignore val name: String) {
        constructor(id: Long): this(id, "Johnny")
    }

    fun testSimpleImmutableIgnoreSuccessUnmarshal() {
        val json = JSON("{\"name\":\"Johnny\",\"id\":1234567}")
        val user = json.unmarshal(ImmutableIgnoreUser::class)

        assertNotNull(user)
        user!!
        assertEquals(1234567, user.id)
        assertEquals("Johnny", user.name)
    }

    data class LotsOfNamesUser(val id: Long, @ListClass(Name::class) val names: List<List<Name>>)

    fun testNestedListsSuccessUnmarshal() {
        val json = JSON("{\"names\":[[{\"first\":\"Johnny\",\"last\":\"Bravo\"}]],\"id\":1234567}")
        val user = json.unmarshal(LotsOfNamesUser::class)

        assertNotNull(user)
        user!!
        assertEquals(1234567, user.id)
        assertEquals("Johnny", user.names[0][0].first)
        assertEquals("Bravo", user.names[0][0].last)
    }

    data class LotsOfNamesMaybeUser(val id: Long, @ListClass(Name::class) val names: List<List<Name>?>)

    fun testNullableNestedListsSuccessUnmarshal() {
        val json = JSON("{\"names\":[null, [{\"first\":\"Johnny\",\"last\":\"Bravo\"}], null],\"id\":1234567}")
        val user = json.unmarshal(LotsOfNamesMaybeUser::class)

        assertNotNull(user)
        user!!
        assertEquals(1234567, user.id)
        assertNull(user.names[0])
        assertEquals("Johnny", user.names[1]?.get(0)?.first)
        assertEquals("Bravo", user.names[1]?.get(0)?.last)
        assertNull(user.names[2])
    }

    @SnakeCase
    data class OverwatchPlayStats(val winPercentage: Double, val wins: Int, @JSONKey("lost") val losses: Int, @JSONKey("played") val gamesPlayed: Int)
    data class OverwatchProfile(val username: String, val level: Int, @JSONKey("games") val playStats: OverwatchPlayStats, val playtime: String, val avatar: String)

    fun testOverwatchAPI() {
        val jsonString = """{
  "data": {
    "username": "Username",
    "level": 22,
    "games": {
      "win_percentage": "51.0",
      "wins": "45",
      "lost": 44,
      "played": "89"
    },
    "playtime": "10 hours",
    "avatar": "https://d1u1mce87gyfbn.cloudfront.net/game/unlocks/0x025000000000030D.png"
  }
}"""
        val json = JSON(jsonString)
        val profile = json["data"].unmarshal(OverwatchProfile::class)

        assertNotNull(profile)
        profile!!
        assertEquals("Username", profile.username)
        assertEquals(22, profile.level)
        assertEquals(45, profile.playStats.wins)
        assertEquals(89, profile.playStats.gamesPlayed)
        assertEquals("10 hours", profile.playtime)
    }

}