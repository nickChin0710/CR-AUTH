package com.tcb.authGateway.sg;

import java.util.*;


/**
 * <p>Title: SAGE Generic Library</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: SAGE Information System Corp.</p>
 * @author Roger Chen
 * @version 1.0
 */

public class SGArrayListMap implements Map, Cloneable {

  private ArrayList al;

  private static final Object NULL_KEY = new Object();

  public SGArrayListMap() {
    this(50);
  }



  public SGArrayListMap(int initialCapacity) {
    al = new ArrayList(initialCapacity);
  }



  private int indexOf(Object key) {
    for (int i = 0; i < al.size(); i++) {
      Element e = (Element)al.get(i);
      if (e.getKey().equals(key))
        return i;
    }

    return -1;
  }



  public Object clone() {
    SGArrayListMap result = null;
    try {
      result = (SGArrayListMap)super.clone();
    }
    catch (CloneNotSupportedException e) {
    // assert false;
    }
    if (result != null) {
        result.al = new ArrayList(al.size());
        result.al.addAll(al);
    }

    return result;
  }



  public int size() {
    return al.size();
  }



  public boolean isEmpty() {
    return al.isEmpty();
  }



  public boolean containsKey(Object key) {
    return indexOf(key) > -1;
  }



  public boolean containsValue(Object value) {
    for (int i = 0; i < al.size(); i++) {
      Element e = (Element)al.get(i);
      if (e.getValue().equals(value))
        return true;
    }

    return false;
  }



  public Object get(Object key) {
    int index = indexOf(key);

    if (index > -1)
      return ( (Element)al.get(index)).getValue();
    else
      return null;
  }



  public Object put(Object key, Object value) {
    al.add(new Element(key, value));
    return value;
  }



  public Object remove(Object key) {
    int index;
    Object obj = null;

    while ( (index = indexOf(key)) > -1) {
      obj = al.remove(index);
    }
    return obj;
  }



  public void putAll(Map t) {
    for (Iterator iter = t.keySet().iterator(); iter.hasNext(); ) {
      Object key = (Object)iter.next();
      put(key, t.get(key));
    }
  }



  public void clear() {
    al.clear();
  }



  public Set keySet() {
    Set set = new HashSet(al.size());
    for (int i = 0; i < al.size(); i++) {
      set.add( ( (Element)al.get(i)).getKey());
    }
    return set;
  }



  public Collection values() {
    Collection cl = new ArrayList(al.size());
    for (int i = 0; i < al.size(); i++) {
      cl.add( ( (Element)al.get(i)).getValue());
    }
    return cl;
  }



  public Set entrySet() {
    return new HashSet(al);
  }



  private static class Element implements Map.Entry {
    Object key;

    Object value;

    Element(Object key, Object value) {
      this.key = key;
      this.value = value;
    }



    public void setKey(Object key) {
      this.key = key;
    }



    public Object setValue(Object value) {
      this.value = value;
      return value;
    }



    public Object getKey() {
      return key;
    }



    public Object getValue() {
      return value;
    }



    public boolean equals(Object o) {
      if (! (o instanceof Map.Entry))
        return false;
      Map.Entry e = (Map.Entry)o;
      Object k1 = getKey();
      Object k2 = e.getKey();
      if (k1 == k2 || (k1 != null && k1.equals(k2))) {
        Object v1 = getValue();
        Object v2 = e.getValue();
        if (v1 == v2 || (v1 != null && v1.equals(v2)))
          return true;
      }
      return false;
    }



    public int hashCode() {
      return (key == NULL_KEY ? 0 : key.hashCode()) ^ (value == null ? 0 : value.hashCode());
    }



    public String toString() {
      return getKey() + "=" + getValue();
    }

  }

}