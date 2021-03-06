package jadex.bdi.planlib.df;

import jadex.base.fipa.IDF;
import jadex.base.fipa.IDFComponentDescription;
import jadex.bdi.runtime.Plan;
import jadex.commons.IFuture;
import jadex.commons.service.SServiceProvider;


/**
 *  Plan to deregister at the df.
 */
public class DFLocalDeregisterPlan extends Plan
{
	/**
	 *  Plan body.
	 */
	public void body()
	{
		IDF	dfservice	= (IDF)SServiceProvider.getService(getScope().getServiceProvider(), IDF.class).get(this);
		
		// In case of a remote request the agent description is already
		// set via the remote deregister plan.
		IDFComponentDescription desc = (IDFComponentDescription)getParameter("description").getValue();
		if(desc==null)
		{
			desc = dfservice.createDFComponentDescription(getScope().getComponentIdentifier(), null);
		}
		else if(desc.getName()==null)
		{
			desc = dfservice.createDFComponentDescription(getScope().getComponentIdentifier(), desc.getServices(), desc.getLanguages(), 
				desc.getOntologies(), desc.getProtocols(), desc.getLeaseTime());
		}

		IFuture ret = dfservice.deregister(desc);
		ret.get(this);
	}
}
