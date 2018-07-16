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
