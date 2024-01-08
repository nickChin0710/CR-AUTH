package com.tcb.authGateway.queue;

import java.io.Serializable;
import java.util.Vector;


/**
 * <p>Title: Taipei Bank XML Transforming System</p>
 * <p>Description: A middle system between IBM S/390 and R6, is responsible to transform data between fixed format data and XML</p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: Sage Information Corp.</p>
 * @author Roger Chen
 * @version 1.0
 */

public class Queue implements Serializable {

  public String queueName = "";

  private Vector ctn;

  public Queue() {
    this("NoName");
  }



  public Queue(int initCapacity) {
    ctn = new Vector(initCapacity);
  }



  public Queue(String name) {
    this.queueName = name;
    ctn = new Vector();
  }



  public Object get() {
    Object rtn = ctn.firstElement();
    ctn.remove(rtn);
    return rtn;
  }



  public void put(Object obj) {
    ctn.add(obj);
  }



  public int getSize() {
    return ctn.size();
  }



  public boolean isEmpty() {
    return ctn.isEmpty();
  }



  public Object first() {
    return ctn.firstElement();
  }



  public Object last() {
    return ctn.lastElement();
  }



  public Object[] getList() {
    return ctn.toArray();
  }



  public void clear() {
    ctn.clear();
  }



  public Object get(int index) {
    if (ctn.size() > 0 && index >= 0 && index < ctn.size())
      return ctn.get(index);
    else
      return null;
  }



  public void insert(int index, Object obj) {
    ctn.add(index, obj);
  }

}
