package com.tcb.authGateway.agent;

import java.util.Iterator;


/**
 * This ConnectionTable contains a correlation table between an identifier object and a connection object.
 * An identifier will be a key to find a connection object.
 */

public interface IConnectionTable {

  public void putConnection(Object identifier, Object connection);



  public Object getConnection(Object identifier);



  public void removeConnection(Object identifier);



  public Iterator getIdentifiers();



  public Iterator getConnections();



  public void clear();



  public int size();



  public boolean isConnectionExists(String identifier);

}