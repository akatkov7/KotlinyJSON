# KotlinyJSON

Inspired by [koher/KotlinyJSON](https://github.com/koher/KotlinyJSON) and [SwiftyJSON/SwiftyJSON](https://github.com/SwiftyJSON/SwiftyJSON)

KotlinyJSON is a thin wrapper of [org.json](https://github.com/stleary/JSON-java) in Kotlin, which has similar APIs to [SwiftyJSON](https://github.com/SwiftyJSON/SwiftyJSON).

A simple example:
```kotlin
val jsonString = "{\"foo\":[{\"id\": 0}, {\"id\": 1}], \"bar\":{\"baz\": 2}}"
val json = JSON(jsonString)

print(json["bar"]["baz"].int) // prints 2
print(json["foo"][0]["id"].int) // prints 0
print(json["moo"].string) // prints null
```

More examples to be added soon!

## Converting JSON to Object and vice versa

KotlinyJSON has the ability to convert your JSON to an Object and vice versa.

A simple example:
```kotlin
data class User(var id: Long, var name: String)
// ...
val json = JSON("{\"name\":\"Johnny\",\"id\":1234567}")
val user = json.unmarshal(User::class)
print(user.id) // prints 1234567
print(user.name) // prints "Johnny"
```

Then, you can convert a user back to JSON:
```kotlin
data class User(var id: Long, var name: String)
// ...
val json = JSON().marshal(user)
print(json["id"].long) // prints 1234567
print(json["name"].string) // prints "Johnny"
```

This process allows you to easily convert JSON data from APIs into objects for easy manipulation in your code!

More examples to be added soon!

## Installation Instrutions

This library can be used in your project through Gradle:

Just add the dependency:
```gradle
dependencies {
    compile 'me.akatkov.kotlinyjson:kotlinyjson:0.3.2'
}
```

## TODO
- more test cases, specifically for List unmarshaling
- specify requirements for classes to be unmarshal'ed such as class passed into unmarshal must have a constructor with parameters that alias all properties to be set in class i.e. excluding @Ignore properties with examples
- make sure all exceptions throw useful error messages for debugging purposes
