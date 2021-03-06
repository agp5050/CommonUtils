    static <K,V> TreeMap.Entry<K,V> successor(Entry<K,V> t) {
        if (t == null)
            return null;
        else if (t.right != null) {
            Entry<K,V> p = t.right;
            while (p.left != null)
                p = p.left;
            return p;
        } else {
            Entry<K,V> p = t.parent;
            Entry<K,V> ch = t;
            while (p != null && ch == p.right) {
                ch = p;
                p = p.parent;
            }
            return p;
        }
    }
    
    
    /**
    *访问规则 else if  先访问微微大的。右下手第一个或者它的最最左节点
    *
    *访问规则第二条，如果没有右下手，返回第一个父节点（右上父节点）或者左左左父节点直到父节点为null或者父节点不是左父节点。
    *需要画图  画出二叉树的访问图。就一目了然。   
    *整体的涵义是，先查右下侧微微大，如果没有微微大，则查左上父节点作为微微大，或者右右右父节点，这时候这个最右边的会查父类节点如果父类为null。则遍历完毕。
    *这个方法遍历所有的比first节点大的值，从小到大排序。
    */

//getHigherEntry--> 比较key， 如果参数key和root比较，小的话 直接查root的左左左节点，知道最左的那个。如果key还是小于最左的，那就以最左作为这个submap中
//的最小起点。
//如果key比root大，依序查看右右右节点如果没有右节点，说明最大的还不如参数key大。则循环回跳，再查看父节点，最后父节点为null，或者右父节点作为返回值。
    final Entry<K,V> getHigherEntry(K key) {
        Entry<K,V> p = root;
        while (p != null) {
            int cmp = compare(key, p.key);
            if (cmp < 0) {
                if (p.left != null)
                    p = p.left;
                else
                    return p;
            } else {
                if (p.right != null) {
                    p = p.right;
                } else {
                    Entry<K,V> parent = p.parent;
                    Entry<K,V> ch = p;
                    while (parent != null && ch == parent.right) {
                        ch = parent;
                        parent = parent.parent;
                    }
                    return parent;
                }
            }
        }
        return null;
    }




    /**
     * Returns the first Entry in the TreeMap (according to the TreeMap's
     * key-sort function).  Returns null if the TreeMap is empty.
     *获取最初值firstkey。 从treemap中获取最小的key。 以后的key根据successor(key)就可以
     *顺序的获取依次增大的序列了。 
     */
    final Entry<K,V> getFirstEntry() {
        Entry<K,V> p = root;
        if (p != null)
            while (p.left != null)
                p = p.left;
        return p;
    }


// 整体这个TreeMap是一个左红右黑的红黑树。  左节点设为红色，右节点设为黑色。



/**
*put方法 决定了数据的存储结构，TreeMap的put逻辑就是红黑树的存储实现逻辑。 和HashMap不同（桶里先分桶，key存在就linked或者tree化）
*TreeMap就直接树形结构，没有桶。
*
*/

    public V put(K key, V value) {
        Entry<K,V> t = root;
        if (t == null) {
            compare(key, key); // type (and possibly null) check

            root = new Entry<>(key, value, null);
            size = 1;
            modCount++;
            return null;
        }
        int cmp;
        Entry<K,V> parent;
        // split comparator and comparable paths
        Comparator<? super K> cpr = comparator;
        if (cpr != null) {
            do {
                parent = t;
                cmp = cpr.compare(key, t.key);
                if (cmp < 0)
                    t = t.left;
                else if (cmp > 0)
                    t = t.right;
                else
                    return t.setValue(value);
            } while (t != null);
        }
        else {
            if (key == null)
                throw new NullPointerException();
            @SuppressWarnings("unchecked")
                Comparable<? super K> k = (Comparable<? super K>) key;
            do {
                parent = t;
                cmp = k.compareTo(t.key);
                if (cmp < 0)
                    t = t.left;
                else if (cmp > 0)
                    t = t.right;
                else
                    return t.setValue(value);
            } while (t != null);
        }
        Entry<K,V> e = new Entry<>(key, value, parent);
        if (cmp < 0)
            parent.left = e;
        else
            parent.right = e;
        fixAfterInsertion(e);
        size++;
        modCount++;
        return null; 
    }

/**
*
*遍历treemap的方式：比较key，小于root左边，大于右边。循环遍历
*
*/

    final Entry<K,V> getEntry(Object key) {
        // Offload comparator-based version for sake of performance
        if (comparator != null)
            return getEntryUsingComparator(key);
        if (key == null)
            throw new NullPointerException();
        @SuppressWarnings("unchecked")
            Comparable<? super K> k = (Comparable<? super K>) key;
        Entry<K,V> p = root;
        while (p != null) {
            int cmp = k.compareTo(p.key);
            if (cmp < 0)
                p = p.left;
            else if (cmp > 0)
                p = p.right;
            else
                return p;
        }
        return null;
    }
