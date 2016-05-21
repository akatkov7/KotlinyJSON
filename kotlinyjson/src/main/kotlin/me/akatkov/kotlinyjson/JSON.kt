package me.akatkov.kotlinyjson

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.*
import java.util.*
import kotlin.reflect.*
import kotlin.reflect.jvm.javaField

@Target(AnnotationTarget.CLASS)
/**
 * This annotation on a class will convert properties from camelCase to snake_case when
 * unmarshaling/marshaling from/to JSON e.g. propertyName -> property_name
 */
annotation class SnakeCase

@Target(AnnotationTarget.CLASS)
/**
 * This annotation on a class will convert properties from camelCase to CamelCase when
 * unmarshaling/marshaling from/to JSON e.g. propertyName -> PropertyName
 */
annotation class CamelCase

@Target(AnnotationTarget.PROPERTY)
/**
 * This annotation on a property will mean that the unmarshal/marshaling process should ignore
 * this property.
 */
annotation class Ignore

@Target(AnnotationTarget.PROPERTY)
/**
 * This annotation on a property will mean that the provided key will be used to fetch/store the
 * property in/from the JSON.
 *
 * @param key the JSON key
 */
annotation class JSONKey(val key: String)

@Target(AnnotationTarget.PROPERTY)
/**
 * This annotation on a property will mean that this property is a List which contains the class
 * provided. This is necessary to successfully marshal/unmarshal the list.
 *
 * @param clazz the class inside the list
 * @param optional whether the objects can be null
 */
annotation class ListClass(val clazz: KClass<*>, val optional: Boolean = false)

/**
 * This exception will be thrown when there is an issue with unmarshaling that is an user error.
 * Otherwise, unmarshaling will return null if it simply fails.
 */
class JSONUnmarshalException(val msg: String): Exception(msg)

enum class MarshalNullStrategy {
    NULL, NULL_STRING, OMIT
}

@Target(AnnotationTarget.CLASS, AnnotationTarget.PROPERTY)
annotation class MarshalNull(val strategy: MarshalNullStrategy = MarshalNullStrategy.NULL)

/**
 * This exception will be thrown when there is an issue with marshaling that is an user error.
 */
class JSONMarshalException(val msg: String): Exception(msg)

class JSON {
    private var jsonObject: JSONObject?
    private var jsonArray: JSONArray?

    private var parent: JSON?
    private var name: String?
    private var index: Int?

    init {
        jsonObject = null
        jsonArray = null

        parent = null
        name = null
        index = null
    }

    constructor() : this("{}")

    constructor(string: String) : this(string.toByteArray())

    constructor(bytes: ByteArray) {
        val string = String(bytes, Charsets.UTF_8)

        try {
            jsonObject = JSONObject(string)
        } catch (e: JSONException) {
            try {
                jsonArray = JSONArray(string)
            } catch (e2: JSONException) {
                try {
                    val parent = JSON("[$string]".toByteArray(Charsets.UTF_8))
                    if (parent.getJSONArray()?.length() == 1) {
                        this.parent = parent
                        index = 0
                    }
                } catch (e3: JSONException) {
                    throw JSONException("Failed to convert to JSON -- are you sure this is valid JSON?")
                }
            }
        }
    }

    constructor(inputStream: InputStream) : this(inputStreamToByteArray(inputStream) ?: ByteArray(0))

    constructor(file: File) : this(inputStreamToByteArray(FileInputStream(file), true) ?: ByteArray(0))

    constructor(value: List<JSON>) : this(("[" + value.map { it.rawString() ?: "" /* will create illegal JSON String */ }.joinToString(",") + "]").toByteArray(Charsets.UTF_8))

    constructor(value: Map<String, JSON>) : this(("{" + value.map { JSONObject.quote(it.key) + ":" + (it.value.rawString() ?: "") }.joinToString(",") + "}").toByteArray(Charsets.UTF_8))

    private constructor(parent: JSON, name: String) {
        this.parent = parent
        this.name = name
    }

    private constructor(parent: JSON, index: Int) {
        this.parent = parent
        this.index = index
    }

    private constructor(jsonArray: JSONArray) {
        this.jsonArray = jsonArray
    }

    operator fun get(name: String): JSON {
        return JSON(this, name)
    }

    operator fun get(index: Int): JSON {
        return JSON(this, index)
    }

    operator fun <V> set(key: String, value: V) {
        // setting a list can be a list of primitives or a list of JSON
        if (value is List<*>) {
            if (value.isEmpty()) {
                getJSONObject()?.put(key, JSONArray(value))
                return
            }
            val hasNulls = value.size == value.filterNotNull().size
            // the list has nullable objects
            if (hasNulls) {
                var sample: Any? = null
                value.forEach {
                    if (it != null) {
                        sample = it
                        return@forEach
                    }
                }
                // all nulls
                if (sample == null) {
                    getJSONObject()?.put(key, JSONArray(value))
                } else {
                    when (sample) {
                        is Int, is Long, is Double, is Boolean, is String -> getJSONObject()?.put(key, JSONArray(value))
                        is JSON -> getJSONObject()?.put(key, JSONArray(value.map { (it as JSON).getJSONObject() ?: it.getJSONArray() }))
                        else -> getJSONObject()?.put(key, JSONArray(value))
                    }
                }
            } else {
                val sample = value[0]
                when (sample) {
                    is Int, is Long, is Double, is Boolean, is String -> getJSONObject()?.put(key, JSONArray(value))
                    is JSON -> getJSONObject()?.put(key, JSONArray(value.map { (it as JSON).getJSONObject() ?: it.getJSONArray() }))
                    else -> getJSONObject()?.put(key, JSONArray(value))
                }
            }
        } else if (value is JSON) {
            if (value.getJSONObject() != null) {
                getJSONObject()?.put(key, value.getJSONObject())
            } else if (value.getJSONArray() != null) {
                getJSONObject()?.put(key, value.getJSONArray())
            }
        } else {
            getJSONObject()?.put(key, value)
        }
    }

    operator fun <V> set(index: Int, value: V) {
        // setting a list can be a list of primitives or a list of JSON
        if (value is List<*>) {
            if (value.isEmpty()) {
                getJSONArray()?.put(index, JSONArray(value))
                return
            }
            val hasNulls = value.size == value.filterNotNull().size
            // the list has nullable objects
            if (hasNulls) {
                var sample: Any? = null
                value.forEach {
                    if (it != null) {
                        sample = it
                        return@forEach
                    }
                }
                // all nulls
                if (sample == null) {
                    getJSONArray()?.put(index, JSONArray(value))
                } else {
                    when (sample) {
                        is Int, is Long, is Double, is Boolean, is String -> getJSONArray()?.put(index, JSONArray(value))
                        is JSON -> getJSONArray()?.put(index, JSONArray(value.map { (it as JSON).getJSONObject() ?: it.getJSONArray() }))
                        else -> getJSONArray()?.put(index, JSONArray(value))
                    }
                }
            } else {
                val sample = value[0]
                when (sample) {
                    is Int, is Long, is Double, is Boolean, is String -> getJSONArray()?.put(index, JSONArray(value))
                    is JSON -> getJSONArray()?.put(index, JSONArray(value.map { (it as JSON).getJSONObject() ?: it.getJSONArray() }))
                    else -> getJSONArray()?.put(index, JSONArray(value))
                }
            }
        } else if (value is JSON) {
            if (value.getJSONObject() != null) {
                getJSONArray()?.put(index, value.getJSONObject())
            } else if (value.getJSONArray() != null) {
                getJSONArray()?.put(index, value.getJSONArray())
            }
        } else {
            getJSONArray()?.put(index, value)
        }
    }

    private fun <T : Any> getValue(fromParentObject: (JSONObject, String) -> T?, fromParentArray: (JSONArray, Int) -> T?): T? {
        try {
            if (name is String) {
                val jsonObject = parent?.getJSONObject()
                if (jsonObject is JSONObject) {
                    return fromParentObject(jsonObject, name!!)
                }
            } else if (index is Int) {
                val jsonArray = parent?.getJSONArray()
                if (jsonArray is JSONArray) {
                    return fromParentArray(jsonArray, index!!)
                }
            }
        } catch(e: JSONException) {
        }

        return null
    }

    fun getJSONObject(): JSONObject? {
        if (jsonObject !is JSONObject) {
            jsonObject = getValue({ o, n -> o.getJSONObject(n) }, { a, i -> a.getJSONObject(i) })
        }

        return jsonObject
    }

    fun getJSONArray(): JSONArray? {
        if (jsonArray !is JSONArray) {
            jsonArray = getValue({ o, n -> o.getJSONArray(n) }, { a, i -> a.getJSONArray(i) })
        }

        return jsonArray
    }

    fun isEmpty(): Boolean {
        return (map?.isEmpty() ?: true) || (list?.isEmpty() ?: true)
    }

    fun isNullJSON(): Boolean {
        return map == null && list == null
    }

    val boolean: Boolean?
        get() {
            return getValue({ o, n -> o.getBoolean(n) }, { a, i -> a.getBoolean(i) })
        }

    fun booleanValue(default: Boolean): Boolean {
        return boolean ?: default
    }

    val int: Int?
        get() {
            return getValue({ o, n -> o.getInt(n) }, { a, i -> a.getInt(i) })
        }

    fun intValue(default: Int): Int {
        return int ?: default
    }

    val long: Long?
        get() {
            return getValue({ o, n -> o.getLong(n) }, { a, i -> a.getLong(i) })
        }

    fun longValue(default: Long): Long {
        return long ?: default
    }

    val double: Double?
        get() {
            return getValue({ o, n -> o.getDouble(n) }, { a, i -> a.getDouble(i) })
        }

    fun doubleValue(default: Double): Double {
        return double ?: default
    }

    val string: String?
        get() {
            return getValue({ o, n -> if (o.isNull(n)) null else o.getString(n) }, { a, i -> if (a.isNull(i)) null else a.getString(i) })
        }

    fun stringValue(default: String): String {
        return getValue({ o, n -> if (o.isNull(n)) null else o.getString(n) }, { a, i -> if (a.isNull(i)) null else a.getString(i) }) ?: default
    }

    val list: List<JSON>?
        get() {
            val length = getJSONArray()?.length()
            if (length is Int) {
                val result = ArrayList<JSON>()
                for (index in 0..(length - 1)) {
                    result.add(JSON(this, index))
                }
                return result
            } else {
                return null
            }
        }

    val map: Map<String, JSON>?
        get() {
            val names = getJSONObject()?.keys()
            if (names is Iterator<String>) {
                val result = HashMap<String, JSON>()
                while (names.hasNext()) {
                    val name = names.next()
                    result.put(name, get(name))
                }
                return result
            } else {
                return null
            }
        }

    fun rawBytes(): ByteArray? {
        return rawString()?.toByteArray(Charsets.UTF_8)
    }

    fun rawString(): String? {
        val jsonObject = getJSONObject()
        if (jsonObject != null) {
            return jsonObject.toString()
        }

        val jsonArray = getJSONArray()
        if (jsonArray != null) {
            return jsonArray.toString()
        }

        val booleanValue = boolean
        if (booleanValue != null) {
            return booleanValue.toString()
        }

        val doubleValue = double
        if (doubleValue != null) {
            if (doubleValue.toLong().toDouble() != doubleValue) { // has decimals
                return doubleValue.toString()
            }
        }

        val longValue = long // int is included
        if (longValue != null) {
            return longValue.toString()
        }

        val stringValue = string
        if (stringValue != null) {
            return JSONObject.quote(stringValue)
        }

        return null
    }

    private val _intNullable: Int? = null
    private val _longNullable: Long? = null
    private val _doubleNullable: Double? = null
    private val _stringNullable: String? = null
    private val _booleanNullable: Boolean? = null
    private val optionalMap: Map<String, KType> =
            hashMapOf(
                    "Int?" to JSON::class.memberProperties.filter { it.name.equals("_intNullable") }.first().returnType,
                    "Long?" to JSON::class.memberProperties.filter { it.name.equals("_longNullable") }.first().returnType,
                    "Double?" to JSON::class.memberProperties.filter { it.name.equals("_doubleNullable") }.first().returnType,
                    "String?" to JSON::class.memberProperties.filter { it.name.equals("_stringNullable") }.first().returnType,
                    "Boolean?" to JSON::class.memberProperties.filter { it.name.equals("_booleanNullable") }.first().returnType)

    fun <T : Any> unmarshal(clazz: KClass<T>): T? {
        val propertyMap = unmarshalHelper(clazz) ?: return null
        val propertyNames = propertyMap.keys.map { it.name }
        val parameterMap: MutableMap<KParameter, Any?> = mutableMapOf()
        // convert property map to parameter map
        clazz.constructors.forEach { constructor ->
            if (constructor.parameters.map { it.name }.containsAll(propertyNames)) {
                constructor.parameters.forEach { parameter ->
                    for ((key, value) in propertyMap) {
                        if (parameter.name == key.name) {
                            parameterMap.put(parameter, value)
                        }
                    }
                }
                return constructor.callBy(parameterMap)
            }
        }
        return null
    }

    private fun <T : Any> unmarshalHelper(clazz: KClass<T>): Map<KProperty1<T, *>, Any?>? {
        val properties = clazz.declaredMemberProperties
        var totalProperties = properties.size
        var setProperties = 0
        val constructorArgs: MutableMap<KProperty1<T, *>, Any?> = mutableMapOf()
        for (prop in properties) {
            var keyName = prop.name
            clazz.annotations.forEach {
                if (it is SnakeCase) {
                    keyName = prop.name.toSnakeCase()
                } else if (it is CamelCase) {
                    keyName = prop.name.toCamelCase()
                }
            }
            var ignored = false
            prop.annotations.forEach {
                if (it is Ignore) {
                    totalProperties--
                    ignored = true
                }
                if (it is JSONKey) {
                    keyName = it.key
                }
            }
            if (ignored) continue

            val isNullable = prop.returnType.isMarkedNullable
            if (isNullable) {
                totalProperties--
            }
            // since we successfully casted to a mutable property, the setter should exist
            var valueToSet: Any? = null
            when(prop.returnType) {
            // if the property isn't nullable, then we can return because this unmarshal won't succeed
                Int::class.defaultType -> valueToSet = this[keyName].int ?: return null
                optionalMap["Int?"]!! -> valueToSet = this[keyName].int
                Long::class.defaultType -> valueToSet = this[keyName].long ?: return null
                optionalMap["Long?"]!! -> valueToSet = this[keyName].long
                Double::class.defaultType -> valueToSet = this[keyName].double ?: return null
                optionalMap["Double?"]!! -> valueToSet = this[keyName].double
                String::class.defaultType -> valueToSet = this[keyName].string ?: return null
                optionalMap["String?"]!! -> valueToSet = this[keyName].string
                Boolean::class.defaultType -> valueToSet = this[keyName].boolean ?: return null
                optionalMap["Boolean?"]!! -> valueToSet = this[keyName].boolean
                else -> {
                    if (this[keyName].isNullJSON()) {
                        if (isNullable) {
                            valueToSet = null
                        } else {
                            return null
                        }
                    } else {
                        // check if it is a List first
                        val array = this[keyName].list
                        // if it's not a list
                        if (array == null) {
                            val propertyClazz = prop.javaField?.type?.kotlin ?: throw JSONUnmarshalException("Unknown property class")
                            val obj = this[keyName].unmarshal(propertyClazz)
                            if (obj == null) {
                                if (!isNullable) {
                                    return null
                                }
                            } else {
                                valueToSet = obj
                            }
                        } else {
                            var listClazz: KClass<Any>? = null
                            var optional = false
                            prop.annotations.forEach {
                                if (it is ListClass) {
                                    listClazz = it.clazz as? KClass<Any>
                                    optional = it.optional
                                }
                            }
                            if (listClazz == null) throw JSONUnmarshalException("List properties must specify their class generic in @ListClass.")

                            fun getListOfClazzInstances(listClazz: KClass<Any>, optional: Boolean): List<Any?>? {
                                if (optional) {
                                    val listOfClazzInstances = mutableListOf<Any?>()

                                    for (i in 0..(array.size - 1)) {
                                        when (listClazz) {
                                            Int::class -> listOfClazzInstances.add(array[i].int)
                                            Long::class -> listOfClazzInstances.add(array[i].long)
                                            Double::class -> listOfClazzInstances.add(array[i].double)
                                            String::class -> listOfClazzInstances.add(array[i].string)
                                            Boolean::class -> listOfClazzInstances.add(array[i].boolean)
                                            else -> listOfClazzInstances.add(array[i].unmarshal(listClazz))
                                        }
                                    }
                                    return listOfClazzInstances
                                } else {
                                    val listOfClazzInstances = mutableListOf<Any>()
                                    for (i in 0..(array.size - 1)) {
                                        when (listClazz) {
                                            Int::class -> listOfClazzInstances.add(array[i].int ?: return null)
                                            Long::class -> listOfClazzInstances.add(array[i].long ?: return null)
                                            Double::class -> listOfClazzInstances.add(array[i].double ?: return null)
                                            String::class -> listOfClazzInstances.add(array[i].string ?: return null)
                                            Boolean::class -> listOfClazzInstances.add(array[i].boolean ?: return null)
                                            else -> listOfClazzInstances.add(array[i].unmarshal(listClazz) ?: return null)
                                        }
                                    }
                                    return listOfClazzInstances
                                }
                            }
                            valueToSet = getListOfClazzInstances(listClazz!!, optional)
                            if (valueToSet == null && !isNullable) {
                                return null
                            }
                        }
                    }
                }
            }
            constructorArgs.put(prop, valueToSet)
            if (!isNullable)
                setProperties++
        }
        if (setProperties != totalProperties) return null
        return constructorArgs
    }

    fun <T : Any> marshal(instance: T): JSON {
        val clazz = instance.javaClass.kotlin
        val properties = clazz.declaredMemberProperties
        val instanceJSON = JSON()
        for (prop in properties) {
            var keyName = prop.name
            var marshalNullStrategy = MarshalNullStrategy.NULL
            var nullValue: Any? = null
            var omitNull = false
            clazz.annotations.forEach {
                if (it is SnakeCase) {
                    keyName = prop.name.toSnakeCase()
                }
                if (it is CamelCase) {
                    keyName = prop.name.toCamelCase()
                }
                if (it is MarshalNull) {
                    marshalNullStrategy = it.strategy
                    when (it.strategy) {
                        MarshalNullStrategy.NULL -> nullValue = null
                        MarshalNullStrategy.NULL_STRING -> nullValue = "null"
                        MarshalNullStrategy.OMIT -> omitNull = true
                    }
                }
            }
            var ignored = false
            prop.annotations.forEach {
                if (it is Ignore) {
                    ignored = true
                }
                if (it is JSONKey) {
                    keyName = it.key
                }
                if (it is MarshalNull) {
                    // reset for this property in case there is a different strategy for the class
                    omitNull = false
                    marshalNullStrategy = it.strategy
                    when (it.strategy) {
                        MarshalNullStrategy.NULL -> nullValue = null
                        MarshalNullStrategy.NULL_STRING -> nullValue = "null"
                        MarshalNullStrategy.OMIT -> omitNull = true
                    }
                }
            }
            if (ignored) continue

            val value = prop.get(instance)
            when(prop.returnType) {
                Int::class.defaultType, Long::class.defaultType, Double::class.defaultType, String::class.defaultType, Boolean::class.defaultType -> {
                    instanceJSON[keyName] = value
                }
                optionalMap["Int?"]!!, optionalMap["Long?"]!!, optionalMap["Double?"]!!, optionalMap["String?"]!!, optionalMap["Boolean?"]!! -> {
                    if (value != null) {
                        instanceJSON[keyName] = value
                    } else {
                        if (!omitNull) {
                            instanceJSON[keyName] = nullValue
                        }
                    }
                }
                else -> {
                    if (value is List<*>) {
                        // the value is a List
                        var listClazz: KClass<Any>? = null
                        var optional = false
                        prop.annotations.forEach {
                            if (it is ListClass) {
                                listClazz = it.clazz as? KClass<Any>
                                optional = it.optional
                            }
                        }
                        if (listClazz == null) throw JSONMarshalException("List properties must specify their class generic in @ListClass.")

                        fun getList(listClazz: KClass<Any>, optional: Boolean): List<Any?> {
                            if (optional) {
                                val values = mutableListOf<Any?>()
                                value.forEach {
                                    if (it != null) {
                                        when (it) {
                                            is Int?, is Long?, is Double?, is Boolean?, is String? -> values.add(it)
                                            else -> values.add(marshal(it))
                                        }
                                    } else {
                                        if (!omitNull) {
                                            if (marshalNullStrategy == MarshalNullStrategy.NULL_STRING) {
                                                // TODO: figure out how to handle this
                                            }
                                            // we just put null because we can't put "null" in all cases
                                            values.add(null)
                                        }
                                    }
                                }
                                return values
                            } else {
                                val values = mutableListOf<Any>()
                                value.forEach {
                                    when (it!!) {
                                        is Int, is Long, is Double, is Boolean, is String -> values.add(it)
                                        else -> values.add(marshal(it))
                                    }
                                }
                                return values
                            }
                        }
                        instanceJSON[keyName] = getList(listClazz!!, optional)

                    } else {
                        if (value != null) {
                            instanceJSON[keyName] = marshal(value)
                        } else {
                            if (!omitNull) {
                                instanceJSON[keyName] = nullValue
                            }
                        }
                    }
                }
            }
        }
        return instanceJSON
    }

    override fun toString(): String {
        return rawString() ?: ""
    }

    private fun String.toSnakeCase(): String {
        // paramName -> param_name
        return this.replace(Regex("[A-Z]"), {result ->
            "_" + result.value.toLowerCase()
        })
    }

    private fun String.toCamelCase(): String {
        // paramName -> ParamName
        if (this.isEmpty()) return this
        return this.substring(0, 1).toUpperCase() + this.substring(1)
    }

}

private fun inputStreamToByteArray(inputStream: InputStream, closeWhenDone: Boolean = false): ByteArray? {
    try {
        val buffer = ByteArray(0x1000)
        val outputStream = ByteArrayOutputStream()

        while (true) {
            val length = inputStream.read(buffer)
            if (length < 0) {
                break
            }

            outputStream.write(buffer, 0, length)
        }

        return outputStream.toByteArray()
    } catch (e: IOException) {
        return null
    } finally {
        if (closeWhenDone) {
            try {
                inputStream.close()
            } catch (e: IOException) {
            }
        }
    }
}