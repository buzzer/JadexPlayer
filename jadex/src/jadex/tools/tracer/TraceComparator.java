package jadex.tools.tracer;

import jadex.tools.tracer.nodes.TNode;

import java.util.Comparator;

/** 
    * <code>TraceComparator</code>
    * @since Nov 18, 2004
    */
   public class TraceComparator implements Comparator
   {

      /** 
       * @param o1
       * @param o2
       * @return -1 by seq1<seq2, 0 seq1==seq2, 1 else
       * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
       */
      public final int compare(Object o1, Object o2)
      {
         if (o1 == o2) return 0;
         if (!(o1 instanceof TNode)) return -1;
         if (!(o2 instanceof TNode)) return 1;
         long seq1 = ((TNode) o1).getSeq();
         long seq2 = ((TNode) o2).getSeq();
         if (seq1 == seq2) return 0;

         return seq1 < seq2 ? -1 : 1;
      }
   }