package org.bactosim.haldane;

import java.util.ArrayList;
import java.lang.Math;
public class ceBody {

	public static final float CE_EPSILON = 1E-6f;
	public static final float _CE_GLOBAL_FORCE_COEFFICIENT = 0.5f;

	ceVector2 center;
	float length;
	float rotation;

	ceVector2 p1;
	ceVector2 p2;
	ceVector2 unitary;

	ceVector2 halfDimensionAABB;
	ceVector2 halfDimensionAABBAura;

	//Contenedor para poder guardar los contactos de al rededor del aura
	ArrayList<ceContact> auraContacts; //???OJO que lo crea con un tamaño especifico 
	int auraContactsSize;

	int pressure;
	ceVector2 centerPreStep;

	ceSpace space;
	ceBody previous;
	ceBody next;

	int id;
	int stamp;

	ceVector2 totalForce;
	float totalTorque;

	//void* data;

	//Constructor
	public ceBody() {
	}

	//Hay que asegurarse que la llamada a ceCreateBody este despues de que se cree el contexto y teng
	public ceBody ( ceSpace space, float length, ceVector2 center, float rotation) {

		this.center = center;
		this.length = length;
		this.rotation = rotation;
		this.unitary = (this.unitary).ceGetVector2((float) Math.cos(rotation),(float) Math.sin(rotation)); 

		ceVector2 centerToPoint = unitary.ceGetMulVector2ByScalar(((length - org.bactosim.haldane.ContextInitializer.ceBodyWidth) * 0.5f));

		p1 = center.ceGetSubVectors2(centerToPoint);
		p2 = center.ceGetAddVectors2(centerToPoint);

		auraContacts = new ArrayList<ceContact>(org.bactosim.haldane.ContextInitializer.ceMaxAuraContactsPerBody); //arrayList de ceContacts.
		auraContactsSize = 0;

		space._ceCheckAndModifySpatialTable(this);
		space._ceAddBodyToSpace(this);


	}

	public void ceDestroyBody() {
		ceBody pre = this.getPrevious();
		if(pre != null){
			changeBody(pre.getNext(),this.getNext());
		}
		ceBody nex = this.getNext();
		if(nex != null) {
			changeBody(next.getPrevious(),this.getPrevious());
		}
		if(this == space.getFirstBody()) {
			changeBody(space.getFirstBody(),this.getNext());
		}
		if(this == space.getLastBody()) {
			changeBody(space.getLastBody(),this.getPrevious());
		}
		space.lessPopulationSize();

	}

	public void ceMoveBodyToSpace(ceSpace newSpace) {
		ceBody pre = this.getPrevious();
		if(pre != null){
			changeBody(pre.getNext(),this.getNext());
		}
		ceBody nex = this.getNext();
		if(nex != null) {
			changeBody(next.getPrevious(),this.getPrevious());
		}
		if(this == space.getFirstBody()) {
			changeBody(space.getFirstBody(),this.getNext());
		}
		if(this == space.getLastBody()) {
			changeBody(space.getLastBody(),this.getPrevious());
		}
		space.lessPopulationSize();

		newSpace._ceCheckAndModifySpatialTable(this);
		newSpace._ceAddBodyToSpace(this);
	}

	public ceBody ceDivideBody(float rotation, float lengthProportion) {

		ceBody child = new ceBody();

		float cLength = child.getLength();
		cLength = this.length * lengthProportion;
		this.length = this.length - cLength;

		/* parent */
		ceVector2 bodyCenterToPoints = (this.unitary).ceGetMulVector2ByScalar((this.length - org.bactosim.haldane.ContextInitializer.ceBodyWidth) * 0.5f);
		this.center = (this.p1).ceGetAddVectors2(bodyCenterToPoints);
		this.p2 = (this.center).ceGetAddVectors2(bodyCenterToPoints);

		/* child */
		float cRotation = child.getRotation();
		cRotation = this.rotation + cRotation;
		ceVector2 cUnitary = child.getUnitary();
		cUnitary = (cUnitary).ceGetVector2((float) Math.cos(cRotation), (float) Math.sin(cRotation));
		ceVector2 bodycenterTochildCenter = cUnitary.ceGetMulVector2ByScalar((cLength + this.length) * 0.5f);
		ceVector2 cCenter = child.getCenter();
		cCenter = (this.center).ceGetAddVectors2(bodycenterTochildCenter);

		ceVector2 childCenterToChildPoint = cUnitary.ceGetMulVector2ByScalar( (cLength - org.bactosim.haldane.ContextInitializer.ceBodyWidth) * 0.5f);
		ceVector2 cP1 = child.getP1();
		cP1 = cCenter.ceGetSubVectors2(childCenterToChildPoint);
		ceVector2 cP2 = child.getP2();
		cP2 = (cCenter).ceGetAddVectors2(childCenterToChildPoint);

		ArrayList<ceContact> cAuraContacts = child.getAuraContacts();
		cAuraContacts = new ArrayList<ceContact>(org.bactosim.haldane.ContextInitializer.ceMaxAuraContactsPerBody);
		int cAuraContactsSize = child.getAuraContactsSize();
		cAuraContactsSize = 0;

		(this.space)._ceAddBodyToSpace(child); 

		return child;
	}


	public void ceMoveBody(ceVector2 displacement) {
		this.center = (this.center).ceGetAddVectors2(displacement);
		this.p1 = (this.p1).ceGetAddVectors2(displacement);
		this.p2 = (this.p2).ceGetAddVectors2(displacement);
		(this.space)._ceCheckAndModifySpatialTable(this); 
	}

	public void ceRotateBody( float rotation) {
		this.rotation += rotation;
		this.unitary = (this.unitary).ceGetVector2((float) Math.cos(this.rotation), (float) Math.sin(this.rotation));
		ceVector2 centerToPoint = (this.unitary).ceGetMulVector2ByScalar((this.length - org.bactosim.haldane.ContextInitializer.ceBodyWidth) * 0.5f);
		this.p1 = (this.center).ceGetSubVectors2(centerToPoint);
		this.p2 = (this.center).ceGetAddVectors2(centerToPoint);
	}

	public boolean ceTestBodyPoint(ceVector2 point) {
		ceVector2 ap = point.ceGetSubVectors2(this.p1);
		ceVector2 bp = point.ceGetSubVectors2(this.p2);
		ceVector2 ab = this.p2.ceGetSubVectors2(this.p1);

		float squareDistance;
		float projection = ap.ceGetDotProductVector2(ab);
		if(projection <= 0.0f)
			squareDistance = ap.ceGetDotProductVector2(ap);
		else
		{
			float totalProjection = ab.ceGetDotProductVector2(ab);
			if(projection >= totalProjection)
				squareDistance = bp.ceGetDotProductVector2(bp);
			else
				squareDistance = ap.ceGetDotProductVector2(ap) - (projection * projection / totalProjection);
		}

		return (squareDistance <= 
				org.bactosim.haldane.ContextInitializer.ceBodySquareWidth * 0.25f); 
	}

	public boolean ceTestBodySegment(ceVector2 segP1, ceVector2 segP2) {

		ceVector2 c1 = new ceVector2();
		ceVector2 c2 = new ceVector2();
		_ceDistanceBetweenSegments(this.p1, this.p2, segP1, segP2, c1, c2);
		ceVector2 distance = c1.ceGetSubVectors2(c2);
		float squareDistance = distance.ceGetSquareLengthVector2();


		return (squareDistance <= 
				org.bactosim.haldane.ContextInitializer.ceBodySquareWidth * 0.25f); 
	}

	public boolean ceTestBodyBody(ceBody otherBody) {

		ceVector2 c1 = new ceVector2();
		ceVector2 c2 = new ceVector2();
		_ceDistanceBetweenSegments(this.p1, this.p2, otherBody.getP1(), otherBody.getP2(), c1, c2);
		ceVector2 distance = c1.ceGetSubVectors2(c2);
		float squareDistance = distance.ceGetSquareLengthVector2();


		return (squareDistance <= org.bactosim.haldane.ContextInitializer.ceBodySquareWidth);
	}

	public ArrayList<ceBody> ceGetNearBodies( float distance, int size){

		ArrayList<ceBody> list = new ArrayList<ceBody>(auraContactsSize);

		for(int i = 0; i < this.auraContactsSize; i++)
		{
			if(Math.sqrt((this.auraContacts.get(i)).squareDistance) < distance + org.bactosim.haldane.ContextInitializer.ceBodyWidth) {

				list.add(this.auraContacts.get(i).body);
			}
		}
		if(size == 0)
		{
			return null;
		}
		return list;
	}

	/*
	 * 
	 * 
	 * Metodos auxiliares
	 * 
	 * 
	 */



	private void _ceDistanceBetweenSegments(ceVector2 s1p1, ceVector2 s1p2, ceVector2 s2p1, ceVector2 s2p2, ceVector2 c1, ceVector2 c2) {

		ceVector2 d1 = s1p1.ceGetSubVectors2(s1p2);
		ceVector2 d2 = s1p1.ceGetSubVectors2(s2p2);
		ceVector2 r = s1p2.ceGetSubVectors2(s2p2);


		float a = d1.ceGetDotProductVector2(d1);
		float b = d1.ceGetDotProductVector2(d2);
		float e = d2.ceGetDotProductVector2(d2);
		float f = d2.ceGetDotProductVector2(r);

		float t;
		float s;

		float denom = a * e - b * b;
		if(denom != 0.0f)
		{
			float c = d1.ceGetDotProductVector2(r);
			s = _ceClampToUnit((b * f - c * e) / denom);
		}
		else
			s = 0.0f;

		float tnom = b * s + f;
		if(tnom < 0.0f)
		{
			t = 0.0f;
			float c = d1.ceGetDotProductVector2(r);
			s = _ceClampToUnit(-c / a);
		}
		else if (tnom > e)
		{
			t = 1.0f;
			float c = d1.ceGetDotProductVector2(r);
			s = _ceClampToUnit((b - c) / a);
		}
		else
			t = tnom / e;

		c1 = s1p2.ceGetAddVectors2(d1.ceGetMulVector2ByScalar(s));
		c2 = s2p2.ceGetAddVectors2(d2.ceGetMulVector2ByScalar(t));

	}



	public static float _ceClampToUnit(float value) {

		if(value < 0.0f)
			return 0.0f;
		if(value > 1.0f)
			return 1.0f;
		return value;

	}




	public void changeBody(ceBody b1, ceBody b2) {
		b1 = b2;
	}

	public void _ceResetForces()
	{
		totalForce.ceGetVector2(0.0f, 0.0f);
		totalTorque = 0.0f;
	}

	public void _ceComputeAABBAura()
	{
		halfDimensionAABBAura.x = org.bactosim.haldane.ContextInitializer.ceMiddleBodyWidth + (Math.abs(p1.getX() - p2.getX()) + org.bactosim.haldane.ContextInitializer.ceBodyWidth) * 0.5f;
		halfDimensionAABBAura.y = org.bactosim.haldane.ContextInitializer.ceMiddleBodyWidth + (Math.abs(p1.getY() - p2.getY()) + org.bactosim.haldane.ContextInitializer.ceBodyWidth) * 0.5f;
	}

	public void _ceCreateContact(ceBody b2)
	{
		if(!this._ceCollideAABBAuras(b2))
			return;

		ceVector2 c1 = null ;
		ceVector2 c2 = null ;
		_ceDistanceBetweenSegments(this.p1, this.p2, b2.p1, b2.p2, c1, c2);
		ceVector2 vDistance = null;
		//compruebo que c1 no sea null para no tener nullPointerException
		if(c1 != null)
			vDistance = c1.ceGetSubVectors2(c2);
		float squareDistance = 0;
		//compruebo que vDistance no sea null para no tener nullPointerException
		if(vDistance != null)
			squareDistance = vDistance.ceGetSquareLengthVector2();

		if(squareDistance <= org.bactosim.haldane.ContextInitializer.ceBodySquareWidth * 4.0f)
		{
			if(this.auraContactsSize < org.bactosim.haldane.ContextInitializer.ceMaxAuraContactsPerBody && b2.auraContactsSize < org.bactosim.haldane.ContextInitializer.ceMaxAuraContactsPerBody)
			{
				this.auraContacts.get(this.auraContactsSize).body = b2;
				this.auraContacts.get(this.auraContactsSize++).squareDistance = squareDistance;

				b2.auraContacts.get(b2.auraContactsSize).body = this;
				b2.auraContacts.get(b2.auraContactsSize++).squareDistance = squareDistance;
			}
		}
	}

	public boolean _ceCollideAABBAuras(ceBody b2)
	{
		if(Math.abs(b2.center.x - this.center.x) < this.halfDimensionAABBAura.x + b2.halfDimensionAABBAura.x)
			if(Math.abs(b2.center.y - this.center.y) < this.halfDimensionAABBAura.y + b2.halfDimensionAABBAura.y)
				return true;
		return false;
	}

	public void _ceMoveBody(ceVector2 displacement)
	{
		center = center.ceGetAddVectors2(displacement);
		p1 = p1.ceGetAddVectors2(displacement);
		p2 = p2.ceGetAddVectors2(displacement);
	}


	public void _ceComputeAABB()
	{
		halfDimensionAABB.x = (Math.abs(p1.x - p2.x) + org.bactosim.haldane.ContextInitializer.ceBodyWidth) * 0.5f;
		halfDimensionAABB.y = (Math.abs(p1.y - p2.y) + org.bactosim.haldane.ContextInitializer.ceBodyWidth) * 0.5f;
	}

	public void _ceCreateForcesOutward(ceBody b2, int internal, int external)
	{
		if(!this._ceCollideAABBs(b2))
			return;

		ceVector2 c1 = null;
		ceVector2 c2 = null;
		_ceDistanceBetweenSegments(this.p1, this.p2, b2.p1, b2.p2, c1, c2);
		ceVector2 vDistance = null;
		if(c1 != null)
			vDistance = c1.ceGetSubVectors2(c2);
		float squareDistance = 0;
		if(vDistance != null)
			squareDistance = vDistance.ceGetSquareLengthVector2();

		if(squareDistance <= org.bactosim.haldane.ContextInitializer.ceBodySquareWidth)
		{
			float distance = (float) Math.sqrt(squareDistance);
			ceVector2 contactPoint = c2.ceGetAddVectors2(vDistance.ceGetMulVector2ByScalar(0.5f));
			ceVector2 vDistanceUnitary = vDistance.ceGetUnitaryVector2();

			float lengthForceToB1 = 0;
			float lengthForceToB2 = 0;

			if(this.pressure > internal)
				lengthForceToB2 = distance - org.bactosim.haldane.ContextInitializer.ceBodyWidth;
			else if(b2.pressure > internal)
				lengthForceToB1 = org.bactosim.haldane.ContextInitializer.ceBodyWidth - distance;
			else if(this.pressure >= external && b2.pressure >= external)
			{
				lengthForceToB1 = (org.bactosim.haldane.ContextInitializer.ceBodyWidth - distance) * 0.5f;
				lengthForceToB2 = -lengthForceToB1;
			}

			if(Math.abs((this.unitary).ceGetDotProductVector2( b2.unitary)) > 0.95f && Math.abs(vDistanceUnitary.ceGetDotProductVector2(b2.unitary)) < 0.05f)
			{
				lengthForceToB1 *= org.bactosim.haldane.ContextInitializer.ceParallelForceRectification;
				lengthForceToB2 *= org.bactosim.haldane.ContextInitializer.ceParallelForceRectification;
			}

			ceVector2 forceToB1 = vDistanceUnitary.ceGetMulVector2ByScalar(lengthForceToB1);
			ceVector2 forceToB2 = vDistanceUnitary.ceGetMulVector2ByScalar(lengthForceToB2);

			this._ceAddForce(forceToB1, contactPoint);
			b2._ceAddForce(forceToB2, contactPoint);
		}
	}

	public void _ceAddForce(ceVector2 force, ceVector2 contactPoint)
	{
		totalForce = totalForce.ceGetAddVectors2(force);
		this._ceAddTorque(force, contactPoint);
	}

	public void _ceAddTorque(ceVector2 force, ceVector2 contactPoint)
	{
		ceVector2 centerToContact = contactPoint.ceGetSubVectors2(this.center);
		float centerToContactSquare = centerToContact.ceGetSquareLengthVector2();
		if(centerToContactSquare >= CE_EPSILON)
			totalTorque += centerToContact.ceGetCrossProductVector2(force) / centerToContactSquare;
	}


	public boolean _ceCollideAABBs(ceBody b2) {

		if(Math.abs(b2.center.x - this.center.x) < this.halfDimensionAABB.x + b2.halfDimensionAABB.x)
			if(Math.abs(b2.center.y - this.center.y) < this.halfDimensionAABB.y + b2.halfDimensionAABB.y)
				return true;
		return false;
	}

	public void _cePushBody()
	{
	   this._ceFilterForcesByDensity();

	    if(pressure == 0)
	        totalTorque *= 10.0f;

	    ceVector2 displacement = totalForce.ceGetMulVector2ByScalar(_CE_GLOBAL_FORCE_COEFFICIENT * org.bactosim.haldane.ContextInitializer.ceThrustRelation);
	    float rotation = totalTorque * _CE_GLOBAL_FORCE_COEFFICIENT * (1.0f - org.bactosim.haldane.ContextInitializer.ceThrustRelation);

	    rotation += rotation;
	    unitary = unitary.ceGetVector2((float)Math.cos(rotation), (float)Math.sin(rotation));
	    ceVector2 centerToPoint = unitary.ceGetMulVector2ByScalar((length - org.bactosim.haldane.ContextInitializer.ceBodyWidth) * 0.5f);
	    center = center.ceGetAddVectors2(displacement);
	    p1 = center.ceGetSubVectors2(centerToPoint);
	    p2 = center.ceGetAddVectors2(centerToPoint);
	}

	public void _ceFilterForcesByDensity() {
		ceSpace space = this.space;
	    int top = (space.middleTableLength + (int)(center.y - halfDimensionAABB.y)) / org.bactosim.haldane.ContextInitializer.ceBoxLength;
	    int right = (space.middleTableLength + (int)(center.x + halfDimensionAABB.x)) / org.bactosim.haldane.ContextInitializer.ceBoxLength;
	    int bottom = (space.middleTableLength + (int)(center.y + halfDimensionAABB.y)) / org.bactosim.haldane.ContextInitializer.ceBoxLength;
	    int left = (space.middleTableLength + (int)(center.x - halfDimensionAABB.x)) / org.bactosim.haldane.ContextInitializer.ceBoxLength;
	    for(int y = top; y <= bottom; y++)
	        for(int x = left; x <= right; x++)
	        {
	            int index = y * space.tableBoxLength + x;
	            /* 6 is a heuristic result. Change this value to modify the global overlaping. */
	            if(space.spatialTable.get(index).size() < 6)
	            {
	                totalForce = totalForce.ceGetMulVector2ByScalar(0.5f);
	                totalTorque *= 0.5f;
	            }
	        }
		
	}
	
	
	
	
	
	

	/*
	 * 
	 **** Getters ceBody ****
	 * 
	 */



	

	public float getLength(){
		return length;
	}

	public float getRotation(){
		return rotation;
	}
	public ceVector2 getUnitary(){
		return unitary;
	}

	public ceVector2 getCenter(){
		return center;
	}

	public ceVector2 getP1(){
		return p1;
	}

	public ceVector2 getP2(){
		return p2;
	}

	public ArrayList<ceContact> getAuraContacts(){
		return auraContacts;
	}

	public int getAuraContactsSize(){
		return auraContactsSize;
	}

	public ceBody getNext() {
		return next;
	}

	public int getStamp(){
		return stamp;
	}

	public int getId() {
		return id;
	}

	public ceBody getPrevious() {
		return previous;
	}

	public int getPressure(){
		return pressure;
	}

}
//fin