package org.bactosim.haldane;

public class MyMethod implements MyInterface {
	public void _ceCreateForces(ceBody c1, ceBody c2) {
		c1._ceCreateForcesOutward(c2, c1.space.ringsNumber - 1, 0);
	}

	
	public void _ceCreateContact(ceBody b1, ceBody b2) {
		
		if(!b1._ceCollideAABBAuras(b2))
	        return;

	    ceVector2 c1 = null;
	    ceVector2 c2 = null;
	    _ceDistanceBetweenSegments(b1.p1,b1.p2, b2.p1, b2.p2, c1, c2);
	    ceVector2 vDistance = c1.ceGetSubVectors2(c2);
	    float squareDistance = vDistance.ceGetSquareLengthVector2();

	    if(squareDistance <= org.bactosim.haldane.ContextInitializer.ceBodySquareWidth * 4.0f)
	    {
	        if(b1.auraContactsSize < org.bactosim.haldane.ContextInitializer.ceMaxAuraContactsPerBody && b2.auraContactsSize < org.bactosim.haldane.ContextInitializer.ceMaxAuraContactsPerBody)
	        {
	            b1.auraContacts.get(b1.auraContactsSize).body = b2;
	            b1.auraContacts.get(b1.auraContactsSize++).squareDistance = squareDistance;

	            b2.auraContacts.get(b2.auraContactsSize).body = b1;
	            b2.auraContacts.get(b2.auraContactsSize++).squareDistance = squareDistance;
	        }
	    }
	}


	public static void _ceDistanceBetweenSegments(ceVector2 s1p1,ceVector2 s1p2, ceVector2 s2p1, ceVector2 s2p2, ceVector2 c1, ceVector2 c2)
	{
	    ceVector2 d1 = s1p1.ceGetSubVectors2(s1p2);
	    ceVector2 d2 = s2p1.ceGetSubVectors2(s2p2);
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
	        s = org.bactosim.haldane.ceBody._ceClampToUnit((b * f - c * e) / denom);
	    }
	    else
	        s = 0.0f;

	    float tnom = b * s + f;
	    if(tnom < 0.0f)
	    {
	        t = 0.0f;
	        float c = d1.ceGetDotProductVector2(r);
	        s = org.bactosim.haldane.ceBody._ceClampToUnit(-c / a);
	    }
	    else if (tnom > e)
	    {
	        t = 1.0f;
	        float c = d1.ceGetDotProductVector2(r);
	        s = org.bactosim.haldane.ceBody._ceClampToUnit((b - c) / a);
	    }
	    else
	        t = tnom / e;

	    c1 = s1p2.ceGetAddVectors2( d1.ceGetMulVector2ByScalar(s));
	    c2 = s2p2.ceGetAddVectors2( d2.ceGetMulVector2ByScalar(t));
	}
}


