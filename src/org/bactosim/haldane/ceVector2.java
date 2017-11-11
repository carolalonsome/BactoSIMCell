package org.bactosim.haldane;

public class ceVector2 {

	public static final float CE_EPSILON = 1E-6f;
	
	public float x;
	public float y;
	//private float c2;
	//float c1;

	public ceVector2(float x, float y) {
		x = this.x;
		y = this.y;
	}
	public ceVector2(){

	}

	public float getX() {
		return x;
	}

	public float getY() {
		return y;
	}

	public void setX(float x) {
		this.x = x;
	}

	public void setY(float y) {
		this.y = y;
	}

	
	
	public ceVector2 ceGetVector2(float x, float y) {
		ceVector2 v = new ceVector2(x , y);
		return v;
	}
	
	/* Returns the substraction between the two vectors given. */
	public ceVector2 ceGetSubVectors2(ceVector2 v2){
		return ceGetVector2(x - v2.getX(), y - v2.getY());
	}
	
	public ceVector2 ceGetMulVector2ByScalar(float scalar) {
		return ceGetVector2(this.getX() * scalar, this.getY() * scalar);
	}

	public ceVector2 ceGetAddVectors2(ceVector2 v2) {
		return ceGetVector2(x + v2.getX(), y + v2.getY());
	}
	
	public float ceGetSquareLengthVector2() {
		return x * x + y * y;

	}
	
	public float ceGetDotProductVector2(ceVector2 v2) {

		return x * v2.getX() + y * v2.getY();

	}
	
	public ceVector2 ceGetDivVector2ByScalar(float scalar)
	{
	    return this.ceGetMulVector2ByScalar(1.0f / scalar);
	}

	public ceVector2 ceGetUnitaryVector2()
	{
	    float length = this.ceGetLengthVector2();
	    if(length > CE_EPSILON)
	        return this.ceGetMulVector2ByScalar(1.0f / length);
	    return this;
	}
	
	public float ceGetLengthVector2()
	{
	    return (float) Math.sqrt(this.x * this.x + this.y * this.y);
	}
	
	public float ceGetCrossProductVector2(ceVector2 v2)
	{
	    return this.x * v2.y - this.y * v2.x;
	}
	

}
