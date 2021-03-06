import java.util.ArrayList;
import java.util.List;


public class BTree<Key extends Comparable<Key>, Value>  {
    // max children per B-tree node = M-1
    // (must be even and greater than 2)
    private static final int M = 4;

    private Node root;       // root of the B-tree
    private int height;      // height of the B-tree
    private int N;           // number of key-value pairs in the B-tree

    // helper B-tree node data type
    private static final class Node {
        private int m;                             // number of children
        private Entry[] children = new Entry[M];   // the array of children

        // create a node with k children
        private Node(int k) {
            m = k;
        }
    }

    // internal nodes: only use key and next
    // external nodes: only use key and value
    private static class Entry {
        private Comparable key;
        private Object val;
        private List address;
        private Node next;     // helper field to iterate over array entries
        public Entry(Comparable key, Object val, Node next) {
            this.key  = key;
            this.val  = val;
            this.next = next;
            address = new ArrayList();
            address.add(val);
            
        }
    }

    /**
     * Initializes an empty B-tree.
     */
    public BTree() {
        root = new Node(0);
    }
 
    /**
     * Returns true if this symbol table is empty.
     * @return <tt>true</tt> if this symbol table is empty; <tt>false</tt> otherwise
     */
    public boolean isEmpty() {
        return size() == 0;
    }

    /**
     * Returns the number of key-value pairs in this symbol table.
     * @return the number of key-value pairs in this symbol table
     */
    public int size() {
        return N;
    }

    /**
     * Returns the height of this B-tree (for debugging).
     *
     * @return the height of this B-tree
     */
    public int height() {
        return height;
    }


    /**
     * Returns the value associated with the given key.
     *
     * @param  key the key
     * @return the value associated with the given key if the key is in the symbol table
     *         and <tt>null</tt> if the key is not in the symbol table
     * @throws NullPointerException if <tt>key</tt> is <tt>null</tt>
     */

    // equal
    public List get(Key key) {
        if (key == null) throw new NullPointerException("key must not be null");
        return search(root, key, height);
    }
    
    
    
    
    private List search(Node x, Key key, int ht) {
        Entry[] children = x.children;

        // external node
        if (ht == 0) {
            for (int j = 0; j < x.m; j++) {
                if (eq(key, children[j].key))
                	return (List) children[j].address;
            }
        }

        // internal node
        else {
            for (int j = 0; j < x.m; j++) {
                if (j+1 == x.m || less(key, children[j+1].key))
                    return search(children[j].next, key, ht-1);
            }
        }
        return new ArrayList();
    }
    
    public List get_notequal(Key key) {
        if (key == null) throw new NullPointerException("key must not be null");
        List store = new ArrayList();
        search_notequal(store,root, key, height);
        return store;
    }
    
    private List search_notequal(List store,Node x, Key key, int ht) {
        Entry[] children = x.children;

        // external node
        if (ht == 0) {
            for (int j = 0; j < x.m; j++) {
                if (!eq(key, children[j].key))  {
                	for(Object i : children[j].address){
                		store.add(i);
                	}
                };
            }
        }

        // internal node
        else {
            for (int j = 0; j < x.m; j++) {
                if (j+1 == x.m || less(key, children[j+1].key))
                    search_notequal(store,children[j].next, key, ht-1);
            }
        }
        return null;
    }
    
    
    
    public List get_bigger(Key key) {
        if (key == null) throw new NullPointerException("key must not be null");
        List store = new ArrayList();
        search_bigger(store,root, key, height);
        return store;
    }
    
    private List search_bigger(List store,Node x, Key key, int ht) {
    	//System.out.println("ht = " + ht);
        Entry[] children = x.children;
        
        // external node
        if (ht == 0) {
            for (int j = 0; j < x.m; j++) {
            	
                if (bigger(key, children[j].key))  {
                	for(Object i : children[j].address){
                		store.add(i);
                	}
                };
            }
        }

        // internal node
        else {
            for (int j = 0; j < x.m; j++) {
            	if (j+1 == x.m || bigger(key, children[j].key) ||bigger(key, children[j+1].key))
                    search_bigger(store,children[j].next, key, ht-1);
            }
        }
        return null;
    }
    
    public List get_less(Key key) {
        if (key == null) throw new NullPointerException("key must not be null");
        List store = new ArrayList();
        search_less(store,root, key, height);
        return store;
    }
    
    private List search_less(List store,Node x, Key key, int ht) {
        Entry[] children = x.children;
        

        // external node
        if (ht == 0) {
            for (int j = 0; j < x.m; j++) {
            	
                if (less(key, children[j].key))  {
                	for(Object i : children[j].address){
                		store.add(i);
                	}
                };
            }
        }

        // internal node
        else {
            for (int j = 0; j < x.m; j++) {
                if (j+1 == x.m || less(key, children[j+1].key))
                    search_less(store,children[j].next, key, ht-1);
            }
        }
        return null;
    }
    
    public List get_range(Key key1,Key key2) {
        if (key1 == null) throw new NullPointerException("key must not be null");
        if (key2 == null) throw new NullPointerException("key must not be null");
        List store = new ArrayList();
        search_range(store,root, key1, key2, height);
        return store;
    }
    
    private List search_range(List store,Node x, Key key1, Key key2, int ht) {
        Entry[] children = x.children;

        // external node
        if (ht == 0) {
            for (int j = 0; j < x.m; j++) {
                if (bigger(key1, children[j].key) && less(key2,children[j].key) )  {
                	for(Object i : children[j].address){
                		store.add(i);
                	}
                };
            }
        }

        // internal node
        else {
            for (int j = 0; j < x.m; j++) {
                if (j+1 == x.m || (bigger(key1, children[j].key)||(bigger(key1, children[j+1].key)) && less(key2,children[j+1].key)))
                    search_range(store,children[j].next, key1,key2, ht-1);
            }
        }
        return null;
    }
    
    private Entry search_entry(Node x,Key key,int height){
    	Entry[] children = x.children;

        // external node
        if (height == 0) {
            for (int j = 0; j < x.m; j++) {
                if (eq(key, children[j].key)) return children[j];
            }
        }

        // internal node
        else {
            for (int j = 0; j < x.m; j++) {
                if (j+1 == x.m || less(key, children[j+1].key))
                    return search_entry(children[j].next, key, height-1);
            }
        }
        return null;
    }

    /**
     * Inserts the key-value pair into the symbol table, overwriting the old value
     * with the new value if the key is already in the symbol table.
     * If the value is <tt>null</tt>, this effectively deletes the key from the symbol table.
     *
     * @param  key the key
     * @param  val the value
     * @throws NullPointerException if <tt>key</tt> is <tt>null</tt>
     */
    public void put(Key key, TableList.row_node val) {
    	
        if (key == null) throw new NullPointerException("key must not be null");
        Node u = insert(root, key, val, height); 
        N++;
        if (u == null) return;

        // need to split root
        Node t = new Node(2);
        t.children[0] = new Entry(root.children[0].key, null, root);
        t.children[1] = new Entry(u.children[0].key, null, u);
        root = t;
        height++;
    }

    private Node insert(Node h, Key key, TableList.row_node val, int ht) {
        int j;
        Entry tmp_entry;
        if((tmp_entry = search_entry(root,key,ht)) != null){
        	tmp_entry.address.add(val);
        	return null;
        }else{
	        Entry t = new Entry(key, val, null);
	
	        // external node
	        if (ht == 0) {
	            for (j = 0; j < h.m; j++) {
	                if (less(key, h.children[j].key)) break;
	            }
	        }
	
	        // internal node
	        else {
	            for (j = 0; j < h.m; j++) {
	                if ((j+1 == h.m) || less(key, h.children[j+1].key)) {
	                    Node u = insert(h.children[j++].next, key, val, ht-1);
	                    if (u == null) return null;
	                    t.key = u.children[0].key;
	                    t.next = u;
	                    break;
	                }
	            }
	        }
	
	        for (int i = h.m; i > j; i--)
	            h.children[i] = h.children[i-1];
	        h.children[j] = t;
	        h.m++;
	        if (h.m < M) return null;
	        else         return split(h);
        	}
        }

    // split node in half
    private Node split(Node h) {
        Node t = new Node(M/2);
        h.m = M/2;
        for (int j = 0; j < M/2; j++)
            t.children[j] = h.children[M/2+j]; 
        return t;    
    }

    /**
     * Returns a string representation of this B-tree (for debugging).
     *
     * @return a string representation of this B-tree.
     */
    public String toString() {
        return toString(root, height, "") + "\n";
    }

    private String toString(Node h, int ht, String indent) {
        StringBuilder s = new StringBuilder();
        Entry[] children = h.children;

        if (ht == 0) {
            for (int j = 0; j < h.m; j++) {
                s.append(indent + children[j].key + " " + children[j].val + "\n");
            }
        }
        else {
            for (int j = 0; j < h.m; j++) {
                if (j > 0) s.append(indent + "(" + children[j].key + ")\n");
                s.append(toString(children[j].next, ht-1, indent + "     "));
            }
        }
        return s.toString();
    }


    // comparison functions - make Comparable instead of Key to avoid casts
    private boolean bigger(Comparable k1, Comparable k2) {
        return k1.compareTo(k2) < 0;
        
    }
    
    private boolean less(Comparable k1, Comparable k2) {
        return k1.compareTo(k2) > 0;
    }

    private boolean eq(Comparable k1, Comparable k2) {
        return k1.compareTo(k2) == 0;
    }
    

    /*
    public static void main(String[] args) {
        Main<Integer, Integer> st = new Main<Integer, Integer>();
        
        
        //st.put(500, 1000);
        for(int i =1;i<100;i++){
        	st.put(i,i);
        }


       //List c = st.get_less(62);
        List d = st.get_bigger(39);
        List e = st.get_range(51,99);
        for(Object i:e){
        	System.out.println(i);
        }
        
        

    }*/
}