package org.bactosim.haldane;

import java.util.ArrayList;

import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.util.ContextUtils;
import repast.simphony.util.collections.IndexedIterable;

@SuppressWarnings({"rawtypes", "unchecked"})
public class EnginePhy {
	private Class clazz = null;
	public static final float _CE_RESIZE_MULTIPLIER = 1.5f;
	public static final int _CE_PRESSURE_CLEAR = -1;
	public static final int _CE_STAMP_SELECTED = -2;
	public static final int _CE_STAMP_CLEAR = -1;
	MyMethod mine;
	
	public EnginePhy() {
		clazz = (new VEColi()).getClass(); 
	}
	
	@ScheduledMethod(start = 1, interval = 10, shuffle=true)
	public void step(ceSpace space) {
		Context context = (Context) ContextUtils.getContext(this);
		IndexedIterable<VEColi> agents = context.getObjects(clazz);
		
		for (VEColi agent : agents) {
			System.out.println("heading= " + agent.getHeading() + " length= " + agent.getElongation());
		}
		
		
		/*** Prepare step ***/
	    /* Check limit and clear spatial table. */
	    if(space._ceCheckBodiesInSpaceLimit())
	    	space._ceResizeAndClearSpatialTable(_CE_RESIZE_MULTIPLIER);
	    else
	        space._ceClearSpatialTable();

	    /* Per body... */
	    ceBody body = space.getFirstBody();
	    while(body != null)
	    {
	        /* Clear previous contacts. */
	    	body.auraContactsSize = 0 ;

	        /* Reset pressure indicator */
	    	body.pressure = _CE_PRESSURE_CLEAR;

	        /* Reset the stamps */
	       body.stamp =  _CE_STAMP_CLEAR;

	        /* The previous positions are the same. */
	        body.centerPreStep = body.center;

	        //This reset could be avoided. but without it, the program crash by a NaN in totalTorque. Fix it.
	        body._ceResetForces();

	        /* Recalculate AABB Aura. */
	        body._ceComputeAABBAura();

	        /* Insert again the body in spatial table. */
	        space._ceInsertBodyAuraInSpatialTable(body);

	        body = body.next;
	    }

	    
	/* Create all contacts. */
	   space._ceTotalBodiesAuraAction(mine); 

	    /* Create rings. */
	    space._ceResetRingMemory();
	    space._ceFindEdges();
	    space._ceFindRings();

	    /*** Expansion and relaxation process. ***/
	    int externalRing = space.ringsNumber - org.bactosim.haldane.ContextInitializer.ceCombinedRing;
	    if(externalRing > 0)
	    {
	        /* Compute the iterations needed. */
	        int physicsIterations = org.bactosim.haldane.ContextInitializer.cePhysicsIterationsWithoutRingSystem / (space.ringsNumber - org.bactosim.haldane.ContextInitializer.ceCombinedRing + 1);
	        if(physicsIterations < org.bactosim.haldane.ContextInitializer.cePhysicsIterationsPerCombinedRing)
	            physicsIterations = org.bactosim.haldane.ContextInitializer.cePhysicsIterationsPerCombinedRing;

	        /* Create combined rings */
	        for(; externalRing >= 0; externalRing--)
	        {
	            /* Expansion process. */
	            space._ceExpandRing(externalRing);

	            /* Relaxation process. */
	            space._ceRelaxRings(externalRing + org.bactosim.haldane.ContextInitializer.ceCombinedRing - 1, externalRing, physicsIterations);
	        }

	        /* Compute bodies not detected by the rings algorithm. */
	        space._ceRelaxBodiesNotDetected();
	    }
	    else
	    {
	        /* Relax the entire population. */
	        space._ceRelaxEntirePopulation(org.bactosim.haldane.ContextInitializer.cePhysicsIterationsWithoutRingSystem);
	    }
				
	}

}
