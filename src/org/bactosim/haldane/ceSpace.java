package org.bactosim.haldane;

import java.util.ArrayList;

public class ceSpace{

	//Declara _CE_INITIAL_TABLE_BOX_LENGTH en CellEngine_p.h
	public static final int _CE_INITIAL_TABLE_BOX_LENGTH = 10;
	public static final float _CE_DEFAULT_LIMIT_VALUE = 0.02f;
	public static final int _CE_INITIAL_RINGS_INDICATOR_SIZE = 10000;
	public static final int _CE_STAMP_SELECTED  = -2;
	public static final int _CE_PRESSURE_CLEAR = -1;
	public static final int _CE_ACCURACY_EDGE = 20;
	public static final int _CE_STAMP_CLEAR = -1;



	public int tableBoxLength;
	public int middleTableLength;
	public int limit;
	public ArrayList<ArrayList<ceBody>> spatialTable;
	//spatialTable es un arrayList de arrayList de cebodies.

	public int populationSize;
	public ceBody firstBody;
	public ceBody lastBody;
	public int currentID;

	public ArrayList<ceBody> ringsBuffer; 
	public int ringsBufferMemory;
	public int ringsBufferSize;

	public int ringsNumber;
	public int ringsIndicatorMemory;
	public int [] ringsIndicator;
	
	public MyMethod mine;


	public ceSpace () {
		tableBoxLength = _CE_INITIAL_TABLE_BOX_LENGTH;
		middleTableLength = (tableBoxLength * 
				org.bactosim.haldane.ContextInitializer.ceBoxLength) / 2;
		limit = (int) (tableBoxLength * _CE_DEFAULT_LIMIT_VALUE);

		//spatialTable = (ceBox*) malloc(sizeof(ceBox) * space->tableBoxLength * space->tableBoxLength);
		//Me creo un contenedor de ceBodies
		spatialTable = new ArrayList<ArrayList<ceBody>>(ceBox.size * this.tableBoxLength * this.tableBoxLength);
		for(int i = 0; i < tableBoxLength * tableBoxLength; i++)
		{
			ArrayList<ceBody> bodies = new ArrayList<ceBody>(org.bactosim.haldane.ContextInitializer.ceMaxBodiesPerBox);
			spatialTable.add(bodies);

		}

		populationSize = 0;
		firstBody = null; 
		lastBody = null;
		currentID = 0;

		ringsBuffer = null; 
		ringsBufferMemory = 0;

		ringsNumber = 0;
		ringsIndicatorMemory = _CE_INITIAL_RINGS_INDICATOR_SIZE;
		ringsIndicator = new int[ringsIndicatorMemory];
		//me creo un array normal con el tamaño de ringsIndicatorMemory

	}

	void ceDestroySpace() {
		ceBody body = this.firstBody;
		while(body != null) {
			ceBody nextAux = body.getNext();
			body = nextAux;
		}
	}

	public ceBody ceGetBody(ceVector2 position){
		int middleTableLength = this.middleTableLength;
		if((-1 * middleTableLength) <= position.getX() 
				&& position.getX() < middleTableLength 
				&& (-1 * middleTableLength) <= position.getY() 
				&& position.getY() < middleTableLength) {


			int x = (middleTableLength + (int)position.getX()) / org.bactosim.haldane.ContextInitializer.ceBoxLength;
			int y = (middleTableLength + (int)position.getY()) / org.bactosim.haldane.ContextInitializer.ceBoxLength;
			int index = y * this.tableBoxLength + x;
			for(int i = 0; i < this.spatialTable.get(index).size(); i++) 
			{
				ceBody body = this.spatialTable.get(index).get(i); //ceBody* body = space->spatialTable[index].bodies[i];
				if(body.ceTestBodyPoint(position))
					return body;
			}
		}
		return null;
	}

	public ArrayList<ceBody> ceGetBodies(int size, ceVector2 position, ceVector2 dimension) {
		/* Find a square belong to the spatial table */
		size = 0;
		int middleTableLength = this.middleTableLength;
		ceVector2 position2 = position.ceGetAddVectors2(dimension);

		if(position.getX() > position2.getX())
		{
			float aux = position.getX();
			position.setX(position2.getX());
			position2.setX(aux);
		}
		if(position.getY() > position2.getY())
		{
			float aux = position.getY();
			position.setY(position2.getY());
			position2.setY(aux);
		}

		float top; 
		if(position.getY() > (-1 * middleTableLength)) {
			top = position.getY();
		} else {
			top = (-1 * middleTableLength);
		}

		float buttom;
		if(position2.getY() < middleTableLength) {
			buttom = position2.getY();
		} else {
			buttom = middleTableLength - 1;
		}


		float left ;
		if(position.getX() > (-1 * middleTableLength)) {
			left = position.getX();
		} else {
			left = (-1 * middleTableLength);
		}

		float right;
		if(position2.getX() < middleTableLength){
			right = position2.getX();
		} else {
			right = middleTableLength - 1;
		}


		if(buttom - top <= 0 || right - left <= 0)
			return null;

		/* Find the ceBoxes of the selection */
		int topIndex = (middleTableLength + (int)top) / org.bactosim.haldane.ContextInitializer.ceBoxLength;
		int rightIndex = (middleTableLength + (int)right) / org.bactosim.haldane.ContextInitializer.ceBoxLength;
		int buttomIndex = (middleTableLength + (int)buttom) / org.bactosim.haldane.ContextInitializer.ceBoxLength;
		int leftIndex = (middleTableLength + (int)left) / org.bactosim.haldane.ContextInitializer.ceBoxLength;
		ArrayList<ceBody> bodies = new ArrayList<ceBody>(org.bactosim.haldane.ContextInitializer.ceMaxBodiesPerBox * (buttomIndex - topIndex + 1) * (rightIndex - leftIndex + 1));


		/* Choose the correct Bodies */
		for(int y = topIndex; y <= buttomIndex; y++)
			for(int x = leftIndex; x <= rightIndex; x++)
			{
				int index = y * this.tableBoxLength + x;
				for(int i = 0; i < this.spatialTable.get(index).size(); i++)
				{
					spatialTable.add(bodies);
					ceBody body = this.spatialTable.get(index).get(i);
					int bStamp = body.getStamp();
					if(bStamp != _CE_STAMP_SELECTED)
					{
						if(y == topIndex || y == buttomIndex || x == leftIndex || x == rightIndex)
						{
							if(_cePointInsideAABB(body.getP1(), top, buttom, left, right) || _cePointInsideAABB(body.getP2(), top, buttom, left, right))
							{
								bodies.set(size++, body);
								bStamp = _CE_STAMP_SELECTED;
							}
							else
							{
								if(y == topIndex && body.ceTestBodySegment(new ceVector2(left,top), new ceVector2(right, top)))
								{
									bodies.set(size++, body);
									bStamp = _CE_STAMP_SELECTED;
								}
								if(bStamp != _CE_STAMP_SELECTED && y == buttomIndex && body.ceTestBodySegment(new ceVector2(left, buttom), new ceVector2(right, buttom)))
								{
									bodies.set(size++, body);
									bStamp = _CE_STAMP_SELECTED;
								}
								if(bStamp != _CE_STAMP_SELECTED && x == leftIndex && body.ceTestBodySegment(new ceVector2(left, top), new ceVector2(left, buttom)))
								{
									bodies.set(size++, body);
									bStamp = _CE_STAMP_SELECTED;
								}
								if(bStamp != _CE_STAMP_SELECTED && x == rightIndex && body.ceTestBodySegment(new ceVector2(right, top), new ceVector2(right, buttom)))
								{
									bodies.set(size++, body);
									bStamp = _CE_STAMP_SELECTED;
								}
							}
						}
						else
						{
							bodies.set(size++, body);
							bStamp = _CE_STAMP_SELECTED;;
						}
					}
				}
			}

		if(size == 0)
		{
			return null;
		}
		return bodies;
	}


	/*
	 * 
	 * Metodos auxiliares
	 * 
	 * 
	 */

	public boolean _cePointInsideAABB(ceVector2 point, float top, float buttom, float left, float right)
	{
		return top <= point.getY() &&
				point.getY() <= buttom &&
				left <= point.getX() &&
				point.getX() <= right;
	}

	public void _ceAddBodyToSpace(ceBody body) {
		ceBody next = body.getNext();
		next = null;
		int id = body.getId();
		id = this.getCurrentID(); 

		ceBody lastBody = this.getLastBody();
		ceBody firstBody = this.getFirstBody();
		if(lastBody != null)
		{
			ceBody nextLB = lastBody.getNext();
			nextLB = body; 
			ceBody previousB = body.getPrevious();
			previousB = lastBody;
		}
		else
		{
			firstBody = body;
			ceBody previousB = body.getPrevious();
			previousB = null;
		}
		lastBody = body;
		populationSize++;
	}

	public void _ceCheckAndModifySpatialTable(ceBody body) {
		ceVector2 center = body.getCenter();
		int x = Math.abs(((int) center.getX()) / org.bactosim.haldane.ContextInitializer.ceBoxLength);
		int y = Math.abs(((int) center.getY()) / org.bactosim.haldane.ContextInitializer.ceBoxLength);

		int middleTableBoxLength = this.getTableBoxLength() / 2;

		int max;
		if(x > y) {
			max = x;
		} else {
			max = y;
		}


		if((max + body.getLength()) >= middleTableBoxLength)
			this._ceResizeAndClearSpatialTable((max + body.getLength()) / middleTableBoxLength + 1);

	}

	public void _ceResizeAndClearSpatialTable( float multiplier ) {

		/* After resize, tableBoxLength have to be a pair number. */
		tableBoxLength = (int) (tableBoxLength * multiplier);
		if(tableBoxLength != 0) //Aqui lo que quiero es que mi tableBoxLength no sea vacia no???
			tableBoxLength++;

		middleTableLength = (org.bactosim.haldane.ContextInitializer.ceBoxLength * tableBoxLength) / 2;
		limit = (int) (tableBoxLength * _CE_DEFAULT_LIMIT_VALUE);

		spatialTable = new ArrayList<ArrayList<ceBody>>(ceBox.size * this.tableBoxLength * this.tableBoxLength);
		for(int i = 0; i < tableBoxLength * tableBoxLength; i++)
		{
			ArrayList<ceBody> bodies = new ArrayList<ceBody>(org.bactosim.haldane.ContextInitializer.ceMaxBodiesPerBox); 
			

		}
	}

	public boolean _ceCheckBodiesInSpaceLimit()
	{
		int tableBoxLength = this.tableBoxLength;
		int limit = this.limit;
		int limit2 = tableBoxLength - limit;
		int increment = tableBoxLength - 2 * limit - 1;

		int i = 0;

		/* Top limit line. */
		for(i = tableBoxLength * limit + limit; i < tableBoxLength * limit + limit2; i++)
			if(this.spatialTable.get(i).size() > 0)
				return true;

		/* Left and right limit lines. */
		for(i = i + limit * 2; i < tableBoxLength * (limit2 - 1) + limit; i += tableBoxLength)
		{
			if(this.spatialTable.get(i).size() > 0)
				return true;
			if(this.spatialTable.get(i+increment).size() > 0)
				return true;
		}

		/* Bottom limit line. */
		for(i = 0 ; i < tableBoxLength * (limit2 - 1) + limit2; i++)
			if(this.spatialTable.get(i).size() > 0)
				return true;

		return false;
	}

	public void _ceClearSpatialTable()
	{
		for(int i = 0; i < this.tableBoxLength * this.tableBoxLength; i++)
			this.spatialTable.get(i).clear();
	}
	
	//Improve the logic around this function to increase its performance. It is necessary avoid the extra lineal dependence.
	public void _ceClearSpatialTableByPressure(int pressure)
	{
	    for(int index = 0; index < tableBoxLength * tableBoxLength; index++)
	    {
	        for(int i = 0; i < spatialTable.get(index).size(); i++)
	        {
	            if(spatialTable.get(index).get(i).pressure <= pressure){
	            	for(int j = index + 1 ; j < spatialTable.get(index).size() ; j++)
	            		this.spatialTable.get(j).clear();
	            }
	                
	        }
	    }
	}

	public void _ceInsertBodyAuraInSpatialTable(ceBody body) {
		/* It could alloc a body outside of the spatial table, in the positive X axis,
	    but this function is NEVER called with a body outside of the spatial table. */
		int top = (middleTableLength + (int)(body.center.getY() - body.halfDimensionAABBAura.getY())) / org.bactosim.haldane.ContextInitializer.ceBoxLength;
		int right = (middleTableLength + (int)(body.center.getX() + body.halfDimensionAABBAura.getX())) / org.bactosim.haldane.ContextInitializer.ceBoxLength;
		int bottom = (middleTableLength + (int)(body.center.getY() + body.halfDimensionAABBAura.getY())) / org.bactosim.haldane.ContextInitializer.ceBoxLength;
		int left = (middleTableLength + (int)(body.center.getX() - body.halfDimensionAABBAura.getX())) / org.bactosim.haldane.ContextInitializer.ceBoxLength;

		for(int y = top; y <= bottom; y++)
			for(int x = left; x <= right; x++)
			{
				int index = y * tableBoxLength + x;
				//if(index >= space->tableBoxLength * space->tableBoxLength) //DEBUG
				//    printf("**** SpatialTableError (Aura) ****\n");
				if(spatialTable.get(index).size() < org.bactosim.haldane.ContextInitializer.ceMaxBodiesPerBox)
					spatialTable.get(index).add(body); //ES IGUAL?? space->spatialTable[index].bodies[space->spatialTable[index].size++] = body; no necesito ponerle posicion porque en size++ se agregaria directamente.
				//ojo con el cast de object a arraylist. cuando hago el get.
				//else //DEBUG
				//    printf("**** SpatialTableLimit (Aura) ****\n");
			}
	}

	public void _ceInsertBodyInSpatialTable(ceBody body) {
	    /* It could alloc a body outside of spatial table, in the positive axis X,
	    but this function is NEVER called with a body outside of the spatial table. */
	    int top = (middleTableLength + (int)(body.center.y - body.halfDimensionAABB.y)) / org.bactosim.haldane.ContextInitializer.ceBoxLength;
	    int right = (middleTableLength + (int)(body.center.x + body.halfDimensionAABB.x)) / org.bactosim.haldane.ContextInitializer.ceBoxLength;
	    int bottom = (middleTableLength + (int)(body.center.y + body.halfDimensionAABB.y)) / org.bactosim.haldane.ContextInitializer.ceBoxLength;
	    int left = (middleTableLength + (int)(body.center.x - body.halfDimensionAABB.x)) / org.bactosim.haldane.ContextInitializer.ceBoxLength;
	    for(int y = top; y <= bottom; y++)
	        for(int x = left; x <= right; x++)
	        {
	            int index = y * tableBoxLength + x;
	            //if(index >= space->tableBoxLength * space->tableBoxLength) //DEBUG
	            //    printf("**** SpatialTableError ****\n");
	            if(spatialTable.get(index).size() < org.bactosim.haldane.ContextInitializer.ceMaxBodiesPerBox)
	            	spatialTable.get(index).add(index, body); //es igual?? space->spatialTable[index].bodies[space->spatialTable[index].size++] = body;
	            //else //DEBUG
	            //    printf("**** SpatialTableLimit ****\n");
	        }
	}
	
	
	public void _ceTotalBodiesAuraAction(MyMethod my ) {
		ceBody b1 = firstBody;
		while(b1 != null)
		{
			int top = (middleTableLength + (int)(b1.center.getY() - b1.halfDimensionAABBAura.getY() ) ) / org.bactosim.haldane.ContextInitializer.ceBoxLength;
			int right = (middleTableLength + (int)(b1.center.getX() + b1.halfDimensionAABBAura.getX() ) ) / org.bactosim.haldane.ContextInitializer.ceBoxLength;
			int bottom = (middleTableLength + (int)(b1.center.getY() + b1.halfDimensionAABBAura.getY() ) ) / org.bactosim.haldane.ContextInitializer.ceBoxLength;
			int left = (middleTableLength + (int)(b1.center.getX() - b1.halfDimensionAABBAura.getX() ) ) / org.bactosim.haldane.ContextInitializer.ceBoxLength;
			for(int y = top; y <= bottom; y++)
				for(int x = left; x <= right; x++)
				{
					int index = y * tableBoxLength + x;
					for(int i = 0; i < spatialTable.get(index).size(); i++)
					{
						ceBody b2 = spatialTable.get(index).get(i);
						if(b1.id < b2.id)
						{
							if(b2.stamp != b1.id)
							{
								my._ceCreateContact(b1, b2);
								
								b2.stamp = b1.id;
							}
						}
					}
				}
			b1 = b1.next;
		}
	}
	
	public void _ceTotalBodiesAction(MyMethod my){
	    ceBody b1 = firstBody;
	    while(b1 != null)
	    {
	    	int top = (middleTableLength + (int)(b1.center.getY() - b1.halfDimensionAABBAura.getY() ) ) / org.bactosim.haldane.ContextInitializer.ceBoxLength;
			int right = (middleTableLength + (int)(b1.center.getX() + b1.halfDimensionAABBAura.getX() ) ) / org.bactosim.haldane.ContextInitializer.ceBoxLength;
			int bottom = (middleTableLength + (int)(b1.center.getY() + b1.halfDimensionAABBAura.getY() ) ) / org.bactosim.haldane.ContextInitializer.ceBoxLength;
			int left = (middleTableLength + (int)(b1.center.getX() - b1.halfDimensionAABBAura.getX() ) ) / org.bactosim.haldane.ContextInitializer.ceBoxLength;

	        for(int y = top; y <= bottom; y++)
	            for(int x = left; x <= right; x++)
	            {
	                int index = y * tableBoxLength + x;
	                for(int i = 0; i < spatialTable.get(index).size(); i++)
	                {
	                    ceBody b2 = spatialTable.get(index).get(i);
	                    if(b1.id < b2.id)
	                    {
	                        if(b2.stamp != b1.id)
	                        {
	                            my._ceCreateForces(b1, b2);
	                            b2.stamp = b1.id;
	                        }
	                    }
	                }
	            }
	        b1 = b1.next;
	    }
	}

	public void _ceFindEdges() {
		for(int i = 0; i < tableBoxLength * tableBoxLength; i++)
		{
			int size = spatialTable.get(i).size();
			if(size <= (org.bactosim.haldane.ContextInitializer.ceMaxBodiesPerBox) / _CE_ACCURACY_EDGE)
			{
				for(int b = 0; b < size; b++)
				{
					ceBody body = spatialTable.get(i).get(b);
					if(body.pressure == _CE_PRESSURE_CLEAR)
					{
						body.pressure = 0;
						ringsBuffer.set(ringsBufferSize++, body);
					}
				}
			}
		}
	}

	public void _ceFindRings() {
		if(ringsBufferSize > 0)
		{
			ringsNumber = 1;
			ringsIndicator[0] = 0;

			for(int i = 0; i < ringsBufferSize; i++)
			{
				ceBody body = ringsBuffer.get(i);

				if(body.pressure == ringsNumber - 1)
					ringsIndicator[ringsNumber++] = ringsBufferSize;

				for(int c = 0; c < body.auraContactsSize; c++)
				{
					ceBody nextBody = body.auraContacts.get(c).body;
					if(nextBody.pressure == _CE_PRESSURE_CLEAR)
					{
						nextBody.pressure = body.pressure + 1;
						ringsBuffer.set(ringsBufferSize++, nextBody);
					}
				}
			}

			ringsNumber = ringsNumber - 1;
		}

		/* Bodies not detected. */
		ceBody body = firstBody;
		while(body != null)
		{
			if(body.pressure == _CE_PRESSURE_CLEAR)
				ringsBuffer.set(ringsBufferSize++, body);

				body = body.next;
		}

		ringsIndicator[ringsNumber + 1] = populationSize;
	}

	public void _ceResetRingMemory()
	{
	    ringsBufferSize = 0;
	    if(ringsBufferMemory < populationSize)
	    {
	        ringsBufferMemory = populationSize * 2;
	         
	        ringsBuffer = new ArrayList<ceBody>(ringsBufferMemory);
	        //tengo un array de ceBody con el tamaño de ringsBufferMemory
	    }

	    if(ringsIndicatorMemory < ringsNumber * 2)
	    {
	        ringsIndicatorMemory = ringsNumber * 2;
	        ringsIndicator = new int[ringsIndicatorMemory];
	    }
	    ringsNumber = 0;
	}
	
	public void _ceExpandRing(int ring)
	{
	    for(int i = ringsIndicator[ring + 1] - 1; i >= (int)ringsIndicator[ring]; i--)
	    {
	        ceBody body = ringsBuffer.get(i);
	        ceVector2 totalDisplacement = newCeGetVector2(0, 0);
	        int accumulator = 0;
	        for(int c = 0; c < body.auraContactsSize; c++)
	        {
	            ceBody closeBody = body.auraContacts.get(c).body;
	            if (closeBody.pressure >= body.pressure)
	            {
	                ceVector2 displacement = (closeBody.center).ceGetSubVectors2(closeBody.centerPreStep);
	                if(displacement.x != 0 || displacement.y != 0)
	                {
	                    totalDisplacement = totalDisplacement.ceGetAddVectors2(displacement);
	                    accumulator++;
	                }
	            }
	            for(int c2 = 0; c2 < closeBody.auraContactsSize; c2++)
	            {
	                ceBody closeBody2 = closeBody.auraContacts.get(c2).body;
	                if (closeBody2.pressure >= body.pressure)
	                {
	                    ceVector2 displacement2 = (closeBody2.center).ceGetSubVectors2(closeBody2.centerPreStep);
	                    if(displacement2.x != 0 || displacement2.y != 0)
	                    {
	                        totalDisplacement = totalDisplacement.ceGetAddVectors2(displacement2);
	                        accumulator++;
	                    }
	                }
	            }
	        }
	        if(accumulator > 0)
	            body._ceMoveBody(totalDisplacement.ceGetDivVector2ByScalar(accumulator));
	    }
	}
	
	
	private ceVector2 newCeGetVector2(float i, float j) {
		ceVector2 v = new ceVector2(i , j);
		return v;
	}

	public void _ceRelaxRings(int internalRing, int externalRing, int physicsIterations)
	{
	    int internalBody = ringsIndicator[internalRing + 1] - 1;
	    int externalBody = ringsIndicator[externalRing];

	    for(int it = 0; it < physicsIterations; it++)
	    {

	        this._ceClearSpatialTableByPressure(internalRing);

	        for(int i = internalBody; i >= externalBody; i--)
	        {
	            ceBody body = ringsBuffer.get(i);
	            body.stamp = _CE_STAMP_CLEAR;
	            body._ceResetForces();
	            body._ceComputeAABB();
	            this._ceInsertBodyInSpatialTable(body);
	        }

	        this._ceCollideBodiesAtRing(internalRing, externalRing);

	        for(int i = internalBody; i >= externalBody; i--)
	            (ringsBuffer.get(i))._cePushBody();
	    }

	    /* only bodies at ring with value "internalRing". */
	    int internalBodyOut = ringsIndicator[internalRing] - 1;
	    for(int i = internalBody; i >= internalBodyOut; i--)
	    {
	        ceBody body = ringsBuffer.get(i);

	        body.stamp = _CE_STAMP_CLEAR;

	        body._ceComputeAABB();
	        this._ceInsertBodyInSpatialTable(body);
	    }
	}
	
	

	public void _ceCollideBodiesAtRing(int internalRing, int externalRing)
	{
	    int internalRingExpand;
	    if(internalRing + 2 < (int)ringsNumber)
	    	internalRingExpand = internalRing + 2;
	    else
	    	internalRingExpand = internalRing + 1;
	    
	    int externalRingExpand = externalRing;

	    int internalBody = ringsIndicator[internalRingExpand] - 1;
	    int externalBody = ringsIndicator[externalRingExpand];
	    for(int i = internalBody; i >= externalBody; i--)
	    {
	        ceBody b1 = ringsBuffer.get(i);
	        int top = (middleTableLength + (int)(b1.center.y - b1.halfDimensionAABB.y)) / org.bactosim.haldane.ContextInitializer.ceBoxLength;
	        int right = (middleTableLength + (int)(b1.center.x + b1.halfDimensionAABB.x)) / org.bactosim.haldane.ContextInitializer.ceBoxLength;
	        int bottom = (middleTableLength + (int)(b1.center.y + b1.halfDimensionAABB.y)) / org.bactosim.haldane.ContextInitializer.ceBoxLength;
	        int left = (middleTableLength + (int)(b1.center.x - b1.halfDimensionAABB.x)) / org.bactosim.haldane.ContextInitializer.ceBoxLength;
	        for(int y = top; y <= bottom; y++)
	            for(int x = left; x <= right; x++)
	            {
	                int index = y * tableBoxLength + x;
	                for(int i1 = 0; i1 < spatialTable.get(index).size(); i1++)
	                {
	                    ceBody b2 = spatialTable.get(index).get(i1);
	                    if(b1.id < b2.id)
	                    {
	                        if(b2.stamp != b1.id)
	                        {
	                            if(b2.pressure >= externalRing)
	                                b1._ceCreateForcesOutward(b2, internalRing, externalRing);
	                            b2.stamp = b1.id;
	                        }
	                    }
	                }
	            }
	    }
	}
	
	public void _ceRelaxBodiesNotDetected()
	{
	    for(int i = ringsIndicator[ringsNumber]; i < populationSize; i++)
	    {
	        ceBody body = ringsBuffer.get(i);
	        body.stamp = _CE_STAMP_CLEAR;
	        body._ceResetForces();
	        body._ceComputeAABB();
	        this._ceInsertBodyInSpatialTable(body);
	    }

	    for(int i = ringsIndicator[ringsNumber]; i < populationSize; i++)
	    {
	        ceBody b1 = ringsBuffer.get(i);
	        int top = (middleTableLength + (int)(b1.center.y - b1.halfDimensionAABB.y)) / org.bactosim.haldane.ContextInitializer.ceBoxLength;
	        int right = (middleTableLength + (int)(b1.center.x + b1.halfDimensionAABB.x)) / org.bactosim.haldane.ContextInitializer.ceBoxLength;
	        int bottom = (middleTableLength + (int)(b1.center.y + b1.halfDimensionAABB.y)) / org.bactosim.haldane.ContextInitializer.ceBoxLength;
	        int left = (middleTableLength + (int)(b1.center.x - b1.halfDimensionAABB.x)) / org.bactosim.haldane.ContextInitializer.ceBoxLength;
	        for(int y = top; y <= bottom; y++)
	            for(int x = left; x <= right; x++)
	            {
	                int index = y * tableBoxLength + x;
	                for(int i1 = 0; i1 < spatialTable.get(index).size(); i1++)
	                {
	                    ceBody b2 = spatialTable.get(index).get(i1);
	                    if(b1.id < b2.id)
	                    {
	                        if(b2.stamp != b1.id)
	                        {
	                            b1._ceCreateForcesOutward(b2, _CE_PRESSURE_CLEAR, _CE_PRESSURE_CLEAR);
	                            b2.stamp = b1.id;
	                        }
	                    }
	                }
	            }
	    }

	    for(int i = ringsIndicator[ringsNumber]; i < populationSize; i++)
	    	(ringsBuffer.get(i))._cePushBody();
	}

	
	public void _ceRelaxEntirePopulation(int physicsIterations){
	   
		
		for(int it = 0; it < physicsIterations; it++)
	    {
	        this._ceClearSpatialTable();

	        ceBody body = firstBody;
	        while(body != null)
	        {
	            body.stamp = _CE_STAMP_CLEAR;
	            body._ceResetForces();

	            body._ceComputeAABB();
	            this._ceInsertBodyInSpatialTable(body);

	            body = body.next;
	        }

	        /* Calculate all colony forces. */
	        
			this._ceTotalBodiesAction(mine);

	        /* Move all bodies by the forces previously calculated. */
	        body = firstBody;
	        while(body != null)
	        {
	            /* Move body and rotate Body. */
	        	body._cePushBody();

	            body = body.next;
	        }
	    }
	}
	
	
	

	/*
	 *
	 *
	 ***** Getters ceSpace *****
	 *
	 */
	public int getTableBoxLength(){
		return  tableBoxLength;
	}


	public int getMiddleTableLength(){
		return middleTableLength;
	}
	public int getLimit() {
		return limit;
	}

	public float getCE_DEFAULT_LIMIT_VALUE(){
		return _CE_DEFAULT_LIMIT_VALUE;
	}
	public ArrayList<ArrayList<ceBody>> getSpatialTable(){
		return spatialTable;
	}
	public int getCurrentID(){
		return currentID;
	}
	public ceBody getLastBody() {
		return lastBody;
	}

	public ceBody getFirstBody() {
		return firstBody;
	}

	public int getPopulationSize() {
		return populationSize;
	}
	public void lessPopulationSize() {
		populationSize--;
	}


}





	
	
	

