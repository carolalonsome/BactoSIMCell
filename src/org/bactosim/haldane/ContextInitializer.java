package org.bactosim.haldane;

import repast.simphony.context.Context;
import repast.simphony.context.space.continuous.ContinuousSpaceFactoryFinder;
import repast.simphony.context.space.grid.GridFactoryFinder;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.parameter.Parameters;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.grid.GridBuilderParameters;
import repast.simphony.space.grid.RandomGridAdder;
import repast.simphony.valueLayer.GridValueLayer;

@SuppressWarnings("rawtypes")
public class ContextInitializer implements ContextBuilder {
	private final static int XMAX = 100;
	private final static int YMAX = 100;
	private final static int GENERATIONS = 10;
	
	
	
	private static final float _CE_THRUST_RELATION = 0.875f;
	
	//Variables that are used in ceInit
	public static int ceCombinedRing;
	public static int cePhysicsIterationsPerCombinedRing;
	public static int cePhysicsIterationsWithoutRingSystem;
	
	public static int ceMaxBodiesPerBox;
	public static int ceMaxAuraContactsPerBody;
	public static short ceBoxLength;
	
	public static float ceBodyWidth;
	public static float ceMiddleBodyWidth;
	public static float ceBodySquareWidth;
	public static float ceParallelForceRectification;
	
	public static float ceThrustRelation;
	
	//este es como mi world y aqui hago la construccion de ese world.
	@SuppressWarnings({"unchecked" })
	public Context build(Context context) {
		int N = (int) ((XMAX * YMAX) * 0.05);
		
		RandomHelper.createNormal(0, 1);
		GridFactoryFinder.createGridFactory(null).createGrid("grid-space", context,
				new GridBuilderParameters<VEColi>(new repast.simphony.space.grid.WrapAroundBorders(),
						new RandomGridAdder<VEColi>(), true, XMAX, YMAX));

		ContinuousSpaceFactoryFinder.createContinuousSpaceFactory(null)
		.createContinuousSpace("continuous-space", context, new AdderCFU<VEColi>(N,4),
				new repast.simphony.space.continuous.WrapAroundBorders(), XMAX, YMAX, 1);
		
		GridValueLayer vl = new GridValueLayer("substrate", true, 
				new repast.simphony.space.grid.WrapAroundBorders(),XMAX,YMAX);
		
		
		// Acquire the instance parameters
		Parameters p = RunEnvironment.getInstance().getParameters();
		
		int doublingTime = (Integer) p.getValue("doublingTime");
		double donorDensity = (Double) p.getValue("donorDensity");
		
		// Create the initial population of R
		/*for (int i = 0; i < N * (1-donorDensity); i++) {
			VEColi vecoli = new VEColi();          
			context.add(vecoli);                  
		}*/
		
		VEColi vecoli = new VEColi(); 
		context.add(vecoli);  
		
		// Create the initial population f D
		/*for (int i = 0; i < N * donorDensity; i++) {
			VEColi vecoli = new VEColi(true);          
			context.add(vecoli);                  
		}*/
		
		// Initialize the substrate for supporting N generations 
		for (int x=0; x< XMAX; x++){
			for (int y=0; y< YMAX; y++){
				vl.set(doublingTime * GENERATIONS,x,y);
			}
		}
		context.addValueLayer(vl);
		
		// Initialize the simple diffusion engine
		EngineDiffusion diffusion = new EngineDiffusion(vl);
		context.add(diffusion);  
		
		// Instantiate the CellEngine implementation
		EnginePhy cellEngine = new EnginePhy();
		context.add(cellEngine);  
		
		ceParam p = new
		//ceInit aqui llamo a ceInit con los parametros
		//ceInit(4,1,MAX_LENGTH,20.0,WIDTH);
		/*ceInit(ceCombinedRing, cePhysicsIterationsPerCombinedRing,
			    float maxBodyLength, float minBodyLength, float bodyWidth);		
		
		*/
		return context;
	}
	
	public void ceInit(int combinedRing, int physicsIterationsPerCombinedRing,
		    float maxBodyLength, float minBodyLength, float bodyWidth, ceParam parametro) {
		
		
		ceCombinedRing = combinedRing;
	    cePhysicsIterationsPerCombinedRing = physicsIterationsPerCombinedRing;
	    cePhysicsIterationsWithoutRingSystem = physicsIterationsPerCombinedRing * ceCombinedRing * ceCombinedRing/4;
	    
	    ceBoxLength = (short) ((maxBodyLength + minBodyLength) * 0.5f);
	    ceMaxBodiesPerBox = (int) (((ceBoxLength / bodyWidth) + 2.0f) * ((ceBoxLength / minBodyLength) + 2.0f) * 4);
	    ceMaxAuraContactsPerBody = (int) ((6 + (int) 2.0f * ((maxBodyLength - (bodyWidth * 2)) / (bodyWidth * 2) + 1.0f)) * 2);

	    ceBodyWidth = bodyWidth;
	    ceMiddleBodyWidth = bodyWidth * 0.5f;
	    ceBodySquareWidth = bodyWidth * bodyWidth;
	    ceParallelForceRectification = (maxBodyLength + minBodyLength) / (bodyWidth * 3);
	    
		ceThrustRelation = _CE_THRUST_RELATION;
	}
	
	public short getCeBoxLength() {
		return ceBoxLength;
	}
	
	public float getCeBodyWidth() {
		return ceBodyWidth;
	}
}

