/*
 * TNodeListener.java
 * Copyright (c) 2005 by University of Hamburg. All Rights Reserved.
 * Departament of Informatics. 
 * Distributed Systems and Information Systems.
 *
 * Created by 9walczak on Jan 24, 2005.  
 * Last revision $Revision: 3040 $ by:
 * $Author: braubach $ on $Date: 2005-08-02 15:13:37 +0000 (Tue, 02 Aug 2005) $.
 */
package jadex.tools.tracer.nodes;

/** 
 * <code>TNodeListener</code>
 * @since Jan 24, 2005
 */
public interface TNodeListener
{

  /** Called if a  node has been added as child 
   * @param caller
   * @param node
   */
  void childAdded(TNode caller, TNode node);

  /**   Called if a  node has been added as parent
   * @param caller
   * @param node
   */
  void parentAdded(TNode caller, TNode node);

  /** Called if a node has been removed as child
   * @param caller
   * @param node
   * @param index 
   */
  void childRemoved(TNode caller, TNode node, int index);

  /** Called if a parent has been removed 
   * @param caller
   * @param node
   * @param index
   */
  void parentRemoved(TNode caller, TNode node, int index);

  /** Called if a node has changed in any other way
   * @param caller
   */
  void nodeChanged(TNode caller);

  /** Called if the node has been deleted 
   * @param caller
   */
  void nodeDeleted(TNode caller);

}

/*  
 * $Log$
 * Revision 1.1  2005/08/02 15:13:11  braubach
 * alpha version of new platform independent tracer
 *
 * Revision 1.1  2005/04/26 13:45:49  pokahr
 * *** empty log message ***
 *
 * Revision 1.3  2005/03/17 15:08:20  9walczak
 * Tested for multithreading. Major fixes.
 *
 * Revision 1.2  2005/02/05 22:03:42  9walczak
 * Created new Ontology. Removed sync methods.
 * Made all filters positive. User may specify filter from the gui.
 * Many fixes.
 *
 * Revision 1.1  2005/01/26 13:47:29  9walczak
 * Beta release. Many fixes.
 *
 */