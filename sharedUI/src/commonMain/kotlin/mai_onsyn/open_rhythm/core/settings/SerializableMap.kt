package mai_onsyn.open_rhythm.core.settings

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.json.Json
import androidx.compose.runtime.*
import com.russhwolf.settings.Settings
import com.russhwolf.settings.string
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

inline fun <reified K, reified V> Settings.map(
    key: String,
    defaultValue: Map<K, V>,
    keySerializer: KSerializer<K>,
    valueSerializer: KSerializer<V>
): ReadWriteProperty<Any?, MutableMap<K, V>> {

    val json = Json { explicitNulls = false }

    val delegate = string(
        key,
        json.encodeToString(
            MapSerializer(keySerializer, valueSerializer),
            defaultValue
        )
    )

    var version by mutableStateOf(0)

    return object : ReadWriteProperty<Any?, MutableMap<K, V>> {

        override fun getValue(
            thisRef: Any?,
            property: KProperty<*>
        ): MutableMap<K, V> {
            val value = json.decodeFromString(
                MapSerializer(keySerializer, valueSerializer),
                delegate.getValue(thisRef, property)
            )

            version++
            return SerializableMap(
                value.toMutableMap()
            ) {
                delegate.setValue(
                    thisRef,
                    property,
                    json.encodeToString(
                        MapSerializer(keySerializer, valueSerializer),
                        it
                    )
                )
            }
        }

        override fun setValue(
            thisRef: Any?,
            property: KProperty<*>,
            value: MutableMap<K, V>
        ) {
            delegate.setValue(
                thisRef,
                property,
                json.encodeToString(
                    MapSerializer(keySerializer, valueSerializer),
                    value
                )
            )
            version++
        }
    }
}

class SerializableMap<K, V>(
    private val map: MutableMap<K, V>,
    private val onChanged: (Map<K, V>) -> Unit
) : MutableMap<K, V> by map {

    private fun changed() {
        onChanged(this)
    }

    override fun put(key: K, value: V): V? {
        val result = map.put(key, value)
        changed()
        return result
    }

    override fun putAll(from: Map<out K, V>) {
        if (from.isNotEmpty()) {
            map.putAll(from)
            changed()
        }
    }

    override fun remove(key: K): V? {
        val exists = map.containsKey(key)
        val result = map.remove(key)
        if (exists) changed()
        return result
    }

    override fun clear() {
        if (map.isNotEmpty()) {
            map.clear()
            changed()
        }
    }
}