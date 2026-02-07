package com.tdfanta.game.util.container

import android.content.res.Resources
import com.tdfanta.game.util.math.Vector2
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.nio.charset.StandardCharsets
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Date
import java.util.Locale

class KeyValueStore {
    private val mJsonObject: JSONObject

    constructor() {
        mJsonObject = JSONObject()
    }

    private constructor(jsonObject: JSONObject) {
        mJsonObject = jsonObject
    }

    fun toStream(output: OutputStream) {
        try {
            output.write(mJsonObject.toString().toByteArray(StandardCharsets.UTF_8))
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    fun hasKey(key: String): Boolean = mJsonObject.has(key)

    fun getKeys(): Collection<String> {
        val collection = ArrayList<String>()
        val iterator = mJsonObject.keys()

        while (iterator.hasNext()) {
            collection.add(iterator.next())
        }

        return collection
    }

    fun putDate(key: String, value: Date) {
        putString(key, dateFormat.format(value))
    }

    fun getDate(key: String): Date {
        try {
            return checkNotNull(dateFormat.parse(getString(key))) { "Could not parse date for key '$key'." }
        } catch (e: ParseException) {
            throw RuntimeException(e)
        }
    }

    fun putString(key: String, value: String) {
        try {
            mJsonObject.put(key, value)
        } catch (e: JSONException) {
            throw RuntimeException(e)
        }
    }

    fun getString(key: String): String {
        try {
            return mJsonObject.getString(key)
        } catch (e: JSONException) {
            throw RuntimeException(e)
        }
    }

    fun putStringList(key: String, strings: List<String>) {
        try {
            val jsonArray = JSONArray()
            for (string in strings) {
                jsonArray.put(string)
            }
            mJsonObject.put(key, jsonArray)
        } catch (e: JSONException) {
            throw RuntimeException(e)
        }
    }

    fun getStringList(key: String): List<String> {
        try {
            val jsonArray = mJsonObject.getJSONArray(key)
            val strings = ArrayList<String>()

            for (i in 0 until jsonArray.length()) {
                strings.add(jsonArray.getString(i))
            }

            return strings
        } catch (e: JSONException) {
            throw RuntimeException(e)
        }
    }

    fun putInt(key: String, value: Int) {
        try {
            mJsonObject.put(key, value)
        } catch (e: JSONException) {
            throw RuntimeException(e)
        }
    }

    fun getInt(key: String): Int {
        try {
            return mJsonObject.getInt(key)
        } catch (e: JSONException) {
            throw RuntimeException(e)
        }
    }

    fun putFloat(key: String, value: Float) {
        try {
            mJsonObject.put(key, value)
        } catch (e: JSONException) {
            throw RuntimeException(e)
        }
    }

    fun getFloat(key: String): Float {
        try {
            return mJsonObject.getDouble(key).toFloat()
        } catch (e: JSONException) {
            throw RuntimeException(e)
        }
    }

    fun putBoolean(key: String, value: Boolean) {
        try {
            mJsonObject.put(key, value)
        } catch (e: JSONException) {
            throw RuntimeException(e)
        }
    }

    fun getBoolean(key: String): Boolean {
        try {
            return mJsonObject.getBoolean(key)
        } catch (e: JSONException) {
            throw RuntimeException(e)
        }
    }

    fun putVector(key: String, vector: Vector2) {
        try {
            mJsonObject.put("$key.x", vector.x())
            mJsonObject.put("$key.y", vector.y())
        } catch (e: JSONException) {
            throw RuntimeException(e)
        }
    }

    fun getVector(key: String): Vector2 {
        try {
            return Vector2(
                mJsonObject.getDouble("$key.x").toFloat(),
                mJsonObject.getDouble("$key.y").toFloat(),
            )
        } catch (e: JSONException) {
            throw RuntimeException(e)
        }
    }

    fun putVectorList(key: String, vectors: List<Vector2>) {
        try {
            val jsonArray = JSONArray()
            for (vector in vectors) {
                val jsonObject = JSONObject()
                jsonObject.put("x", vector.x())
                jsonObject.put("y", vector.y())
                jsonArray.put(jsonObject)
            }
            mJsonObject.put(key, jsonArray)
        } catch (e: JSONException) {
            throw RuntimeException(e)
        }
    }

    fun getVectorList(key: String): List<Vector2> {
        try {
            val jsonArray = mJsonObject.getJSONArray(key)
            val vectors = ArrayList<Vector2>()

            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                vectors.add(
                    Vector2(
                        jsonObject.getDouble("x").toFloat(),
                        jsonObject.getDouble("y").toFloat(),
                    ),
                )
            }

            return vectors
        } catch (e: JSONException) {
            throw RuntimeException(e)
        }
    }

    fun putStore(key: String, store: KeyValueStore) {
        try {
            mJsonObject.put(key, store.mJsonObject)
        } catch (e: JSONException) {
            throw RuntimeException(e)
        }
    }

    fun getStore(key: String): KeyValueStore {
        try {
            return KeyValueStore(mJsonObject.getJSONObject(key))
        } catch (e: JSONException) {
            throw RuntimeException(e)
        }
    }

    fun appendStore(key: String, store: KeyValueStore) {
        try {
            var jsonArray = mJsonObject.optJSONArray(key)
            if (jsonArray == null) {
                jsonArray = JSONArray()
                mJsonObject.put(key, jsonArray)
            }
            jsonArray.put(store.mJsonObject)
        } catch (e: JSONException) {
            throw RuntimeException(e)
        }
    }

    fun getStoreList(key: String): List<KeyValueStore> {
        try {
            val jsonArray = mJsonObject.optJSONArray(key)
            val stores = ArrayList<KeyValueStore>()

            if (jsonArray == null) {
                return stores
            }

            for (i in 0 until jsonArray.length()) {
                stores.add(KeyValueStore(jsonArray.getJSONObject(i)))
            }

            return stores
        } catch (e: JSONException) {
            throw RuntimeException(e)
        }
    }

    fun extend(other: KeyValueStore) {
        try {
            extendObject(mJsonObject, other.mJsonObject)
        } catch (e: JSONException) {
            throw RuntimeException(e)
        }
    }

    companion object {
        private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US)

        @JvmStatic
        fun fromResources(resources: Resources, resourceId: Int): KeyValueStore {
            try {
                resources.openRawResource(resourceId).use { stream ->
                    return fromStream(stream)
                }
            } catch (e: IOException) {
                throw RuntimeException(e)
            }
        }

        @JvmStatic
        fun fromStream(input: InputStream): KeyValueStore {
            try {
                val buffer = CharArray(1024)
                val stringBuilder = StringBuilder()
                val reader = InputStreamReader(input, StandardCharsets.UTF_8)

                while (true) {
                    val count = reader.read(buffer, 0, buffer.size)
                    if (count < 0) {
                        break
                    }
                    stringBuilder.append(buffer, 0, count)
                }

                return KeyValueStore(JSONObject(stringBuilder.toString()))
            } catch (e: IOException) {
                throw RuntimeException(e)
            } catch (e: JSONException) {
                throw RuntimeException(e)
            }
        }

        private fun extendObject(`object`: JSONObject, other: JSONObject) {
            val iterator = other.keys()
            while (iterator.hasNext()) {
                val key = iterator.next()

                val objectValue = `object`.opt(key)
                val otherValue = other.get(key)

                if (objectValue is JSONObject && otherValue is JSONObject) {
                    extendObject(objectValue, otherValue)
                    continue
                }

                if (objectValue is JSONArray && otherValue is JSONArray) {
                    extendArray(objectValue, otherValue)
                    continue
                }

                `object`.put(key, otherValue)
            }
        }

        private fun extendArray(array: JSONArray, other: JSONArray) {
            for (i in 0 until other.length()) {
                array.put(other.get(i))
            }
        }
    }
}
