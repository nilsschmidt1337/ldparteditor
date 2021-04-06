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
package org.nschmidt.ldparteditor.helpers.math;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author nils
 *
 */
public class ThreadsafeTreeMap<K, V> implements Map<K, V> {

    private final TreeMap<K, V> map;
    private final ReadWriteLock rwl = new ReentrantReadWriteLock();
    private final Lock rl = rwl.readLock();
    private final Lock wl = rwl.writeLock();

    public ThreadsafeTreeMap() {
        try {
            wl.lock();
            map = new TreeMap<>();
        } finally {
            wl.unlock();
        }
    }

    @Override
    public void clear() {
        try {
            wl.lock();
            map.clear();
        } finally {
            wl.unlock();
        }
    }

    @Override
    public Object clone() {
        try {
            rl.lock();
            return map.clone();
        } finally {
            rl.unlock();
        }
    }

    @Override
    public boolean containsKey(Object key) {
        try {
            rl.lock();
            return map.containsKey(key);
        } finally {
            rl.unlock();
        }
    }

    @Override
    public boolean containsValue(Object value) {
        try {
            rl.lock();
            return map.containsValue(value);
        } finally {
            rl.unlock();
        }
    }

    @Override
    public Set<java.util.Map.Entry<K, V>> entrySet() {
        try {
            rl.lock();
            return map.entrySet();
        } finally {
            rl.unlock();
        }
    }

    public Set<java.util.Map.Entry<K, V>> threadSafeEntrySet() {
        try {
            rl.lock();
            final Set<java.util.Map.Entry<K, V>> val = map.entrySet();
            final Set<java.util.Map.Entry<K, V>> result = new HashSet<>();
            for (Entry<K, V> entry : val) {
                result.add(entry);
            }

            return result;
        } finally {
            rl.unlock();
        }
    }

    @Override
    public V get(Object key) {
        try {
            rl.lock();
            return map.get(key);
        } finally {
            rl.unlock();
        }
    }

    @Override
    public boolean isEmpty() {
        try {
            rl.lock();
            return map.isEmpty();
        } finally {
            rl.unlock();
        }
    }

    @Override
    public Set<K> keySet() {
        try {
            rl.lock();
            return map.keySet();
        } finally {
            rl.unlock();
        }
    }

    @Override
    public V put(K key, V value) {
        try {
            wl.lock();
            return map.put(key, value);
        } finally {
            wl.unlock();
        }
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        try {
            wl.lock();
            map.putAll(m);
        } finally {
            wl.unlock();
        }
    }

    @Override
    public V remove(Object key) {
        try {
            wl.lock();
            return map.remove(key);
    } finally {
        wl.unlock();
    }
    }

    @Override
    public int size() {
        try {
            rl.lock();
            return map.size();
        } finally {
            rl.unlock();
        }
    }

    public K firstKey() {
        try {
            rl.lock();
            return map.firstKey();
        } finally {
            rl.unlock();
        }
    }

    public K lastKey() {
        try {
            rl.lock();
            return map.lastKey();
        } finally {
            rl.unlock();
        }
    }

    @Override
    public Collection<V> values() {
        try {
            rl.lock();
            return map.values();
        } finally {
            rl.unlock();
        }
    }
}
