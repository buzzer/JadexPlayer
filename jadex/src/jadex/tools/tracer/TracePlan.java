/*
 * TracePlan.java
 * Copyright (c) 2005 by University of Hamburg. All Rights Reserved.
 * Departament of Informatics. 
 * Distributed Systems and Information Systems.
 *
 * Created by walczak on Aug 2, 2005.  
 * Last revision $Revision: 3040 $ by:
 * $Author: braubach $ on $Date: 2005-08-02 15:13:37 +0000 (Tue, 02 Aug 2005) $.
 */
package jadex.tools.tracer;

import jadex.adapter.fipa.AgentIdentifier;
import jadex.adapter.fipa.SFipa;
import jadex.runtime.IMessageEvent;
import jadex.runtime.Plan;
import jadex.tools.ontology.OTrace;

/** TracePlan 
 * @author walczak
 * @since  Aug 2, 2005
 */
public class TracePlan extends Plan
{
    /**
     *  Plan body.
     */
    public void body()
    {
        IMessageEvent me=(IMessageEvent)getInitialEvent();
        TracerController ctrl = (TracerController)getBeliefbase().getBelief("ctrl").getFact();
        if (ctrl!=null) {
            Object cnt = me.getContent();
            if (cnt instanceof OTrace) {
                AgentIdentifier sendr = (AgentIdentifier)me.getParameter(SFipa.SENDER).getValue();
                ctrl.add(sendr, (OTrace)cnt);
            }
        }
    }

}


/* $Log$
 * Revision 1.1  2005/08/02 15:13:11  braubach
 * alpha version of new platform independent tracer
 *
 */