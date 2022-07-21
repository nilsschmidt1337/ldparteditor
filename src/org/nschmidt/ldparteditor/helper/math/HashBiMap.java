/* MIT - License

Copyright (c) 2012 - this year, Nils Schmidt

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"),
to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE. */
package org.nschmidt.ldparteditor.helper.math;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class HashBiMap<K, V> implements Map<K, V> {

    private final Map<K, V> keyToValue = new HashMap<>();
    private final Map<V, K> valueToKey = new HashMap<>();

    public HashBiMap() {
        super();
    }

    private HashBiMap(Map<K, V> keyToValue2, Map<V, K> valueToKey2) {
        super();
        this.keyToValue.putAll(keyToValue2);
        this.valueToKey.putAll(valueToKey2);
    }

    @Override
    public void clear() {
        keyToValue.clear();
        valueToKey.clear();
    }

    @Override
    public boolean containsKey(Object key) {
        return keyToValue.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return valueToKey.containsKey(value);
    }

    @Override
    public Set<java.util.Map.Entry<K, V>> entrySet() {
        return keyToValue.entrySet();
    }

    @Override
    public V get(Object key) {
        return keyToValue.get(key);
    }

    public V getValue(K key) {
        return keyToValue.get(key);
    }

    public K getKey(V value) {
        return valueToKey.get(value);
    }

    @Override
    public boolean isEmpty() {
        return keyToValue.isEmpty();
    }

    @Override
    public Set<K> keySet() {
        return keyToValue.keySet();
    }

    @Override
    public V remove(Object key) {
        V value = keyToValue.get(key);
        valueToKey.remove(value);
        keyToValue.remove(key);
        return value;
    }

    public V removeByKey(K key) {
        V value = keyToValue.get(key);
        valueToKey.remove(value);
        keyToValue.remove(key);
        return value;
    }

    public K removeByValue(V value) {
        K key = valueToKey.get(value);
        keyToValue.remove(key);
        valueToKey.remove(value);
        return key;
    }

    @Override
    public int size() {
        return keyToValue.size();
    }

    @Override
    public Collection<V> values() {
        return valueToKey.keySet();
    }

    @Override
    public V put(K key, V value) {
        if (value == null)
            throw new AssertionError();
        valueToKey.remove(keyToValue.get(key));
        valueToKey.put(value, key);
        return keyToValue.put(key, value);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        keyToValue.putAll(m);
        for (Entry<? extends K, ? extends V> entry : m.entrySet()) {
            valueToKey.put(entry.getValue(), entry.getKey());
        }
    }

    public HashBiMap<K, V> copy() {
        return new HashBiMap<>(keyToValue, valueToKey);
    }
}
