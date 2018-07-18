
Entry<K,V> extends HashMap.Node<K,V>
Entry<K,V> before, after
TreeNode<K,V> extends LinkedHashMap.Entry<K,V> //TreeNode 继承before，after指针后又增加了parent,left,right,prev等指针。并标注为red,black红黑树。
/**
*static final class TreeNode<K,V> extends LinkedHashMap.Entry<K,V> {
        TreeNode<K,V> parent;  // red-black tree links
        TreeNode<K,V> left;
        TreeNode<K,V> right;
        TreeNode<K,V> prev;    // needed to unlink next upon deletion
        boolean red;
*
*/
//LinkedHashMap 的基本单元Entry结构继承了HashMap的Node基本结构并增加了before，after这两个指针。所有的元素都双向链表链接起来了。
//Override get()
public V get(Object key) {
        Node<K,V> e;
        if ((e = getNode(hash(key), key)) == null)
            return null;
        if (accessOrder)
            afterNodeAccess(e);
        return e.value;
    }



    final Node<K,V> getNode(int hash, Object key) {
        Node<K,V>[] tab; Node<K,V> first, e; int n; K k;
        if ((tab = table) != null && (n = tab.length) > 0 &&
            (first = tab[(n - 1) & hash]) != null) {
            if (first.hash == hash && // always check first node
                ((k = first.key) == key || (key != null && key.equals(k))))
                return first;
            if ((e = first.next) != null) {
                if (first instanceof TreeNode)
                    return ((TreeNode<K,V>)first).getTreeNode(hash, key);
                do {
                    if (e.hash == hash &&
                        ((k = e.key) == key || (key != null && key.equals(k))))
                        return e;
                } while ((e = e.next) != null);
            }
        }
        return null;
    }
    // aterNodeAccess 获取key对应的ENtry后，将这个ENtry移动到链表的最后位置。tail.  累计访问之下，高频访问的对象在队列的尾部。
    //但是getNode()方法说明了，指针变化了，但是实际的Entry对象是没有在数组中变动的。就是before，after变化了。 还是用hash快速访问值。
        void afterNodeAccess(Node<K,V> e) { // move node to last
        LinkedHashMap.Entry<K,V> last;
        if (accessOrder && (last = tail) != e) {
            LinkedHashMap.Entry<K,V> p =
                (LinkedHashMap.Entry<K,V>)e, b = p.before, a = p.after;
            p.after = null;
            if (b == null)
                head = a;
            else
                b.after = a;
            if (a != null)
                a.before = b;
            else
                last = b;
            if (last == null)
                head = p;
            else {
                p.before = last;
                last.after = p;
            }
            tail = p;
            ++modCount;
        }
    }

/**
*LinkedList自带访问频率列表。tail方向的频率最高，head方向的频率低。这样可以用来做为一个cache，
*将“最近最少使用”的节点移除removeEldestEntry()可以写一个子类重写这个方法，将最不频繁访问的几个元素移除cache。
*
*/
