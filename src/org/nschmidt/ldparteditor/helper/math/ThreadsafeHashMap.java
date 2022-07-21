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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ThreadsafeHashMap<K, V> implements Map<K, V> {

    private final HashMap<K, V> map;
    private final ReadWriteLock rwl = new ReentrantReadWriteLock();
    private final Lock rl = rwl.readLock();
    private final Lock wl = rwl.writeLock();

    public ThreadsafeHashMap() {
        wl.lock();
        try {
            map = new HashMap<>();
        } finally {
            wl.unlock();
        }
    }

    public ThreadsafeHashMap(int initialCapacity) {
        wl.lock();
        try {
            map = new HashMap<>(initialCapacity);
        } finally {
            wl.unlock();
        }
    }

    @Override
    public void clear() {
        wl.lock();
        try {
            map.clear();
        } finally {
            wl.unlock();
        }
    }

    @Override
    public boolean containsKey(Object key) {
        rl.lock();
        try {
            return map.containsKey(key);
        } finally {
            rl.unlock();
        }
    }

    @Override
    public boolean containsValue(Object value) {
        rl.lock();
        try {
            return map.containsValue(value);
        } finally {
            rl.unlock();
        }
    }

    @Override
    public Set<java.util.Map.Entry<K, V>> entrySet() {
        rl.lock();
        try {
            return map.entrySet();
        } finally {
            rl.unlock();
        }
    }

    @Override
    public V get(Object key) {
        rl.lock();
        try {
            return map.get(key);
        } finally {
            rl.unlock();
        }
    }

    @Override
    public boolean isEmpty() {
        rl.lock();
        try {
            return map.isEmpty();
        } finally {
            rl.unlock();
        }
    }

    @Override
    public V putIfAbsent(K key, V value) {
        V entry = null;
        wl.lock();
        try {
            entry = map.putIfAbsent(key, value);

            if (entry == null) {
                entry = value;
            }
        } finally {
            wl.unlock();
        }

        return entry;
    }

    @Override
    public Set<K> keySet() {
        rl.lock();
        try {
            return map.keySet();
        } finally {
            rl.unlock();
        }
    }

    public Set<K> threadsafeKeySet() {
        rl.lock();
        try {
            final Set<K> val = map.keySet();
            final Set<K> result = new HashSet<>();
            for (K key : val) {
                result.add(key);
            }

            return val;
        } finally {
            rl.unlock();
        }
    }

    @Override
    public V put(K key, V value) {
        wl.lock();
        try {
            return map.put(key, value);
        } finally {
            wl.unlock();
        }
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        wl.lock();
        try {
            map.putAll(m);
        } finally {
            wl.unlock();
        }
    }

    @Override
    public V remove(Object key) {
        wl.lock();
        try {
            return map.remove(key);
        } finally {
            wl.unlock();
        }
    }

    @Override
    public int size() {
        rl.lock();
        try {
            return map.size();
        } finally {
            rl.unlock();
        }
    }

    @Override
    public Collection<V> values() {
        rl.lock();
        try {
            return map.values();
        } finally {
            rl.unlock();
        }
    }

    public ThreadsafeHashMap<K, V> copy() {
        rl.lock();
        try {
            ThreadsafeHashMap<K, V> newMap = new ThreadsafeHashMap<>();
            newMap.putAll(map);
            return newMap;
        } finally {
            rl.unlock();
        }
    }
}
