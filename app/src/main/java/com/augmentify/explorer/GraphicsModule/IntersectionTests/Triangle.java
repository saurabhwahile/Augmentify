package com.augmentify.explorer.GraphicsModule.IntersectionTests;

import java.util.Arrays;

public class Triangle
{
	public float[] V0;
	public float[] V1;
	public float[] V2;

	public Triangle(float[] V0, float[] V1, float[] V2)
	{
		this.V0 = V0;
		this.V1 = V1;
		this.V2 = V2;
	}
	
	public Triangle newInstance(Triangle triangle)
	{
		return new Triangle(triangle.V0, triangle.V1, triangle.V2);
	}

	private static final float SMALL_NUM = 0.00000001f; // anything that avoids
														// division overflow

	// intersectRayAndTriangle(): intersect a ray with a 3D triangle
	// Input: a ray R, and a triangle T
	// Output: *I = intersection point (when it exists)
	// Return: -1 = triangle is degenerate (a segment or point)
	// 0 = disjoint (no intersect)
	// 1 = intersect in unique point I1
	// 2 = are in the same plane
	public static int intersectRayAndTriangle(float rayNearCoOrds[],
			float rayFarCoOrds[], Triangle T, float[] I)
	{
		float[] u, v, n; // triangle vectors
		float[] dir, w0, w; // ray vectors
		float r, a, b; // params to calc ray-plane intersect

		// get triangle edge vectors and plane normal
		u = Vector.minus(T.V1, T.V0);
		v = Vector.minus(T.V2, T.V0);
		n = Vector.crossProduct(u, v); // cross product

		if (Arrays.equals(n, new float[] { 0.0f, 0.0f, 0.0f }))
		{ // triangle is degenerate
			return -1; // do not deal with this case
		}
		dir = Vector.minus(rayNearCoOrds, rayFarCoOrds); // ray direction vector
		w0 = Vector.minus(rayFarCoOrds, T.V0);
		a = -Vector.dot(n, w0);
		b = Vector.dot(n, dir);
		if (Math.abs(b) < SMALL_NUM)
		{ // ray is parallel to triangle plane
			if (a == 0)
			{ // ray lies in triangle plane
				return 2;
			}
			else
			{
				return 0; // ray disjoint from plane
			}
		}

		// get intersect point of ray with triangle plane
		r = a / b;
		if (r < 0.0f)
		{ // ray goes away from triangle
			return 0; // => no intersect
		}
		// for a segment, also test if (r > 1.0) => no intersect

		float[] tempI = Vector.addition(rayFarCoOrds,
				Vector.scalarProduct(r, dir)); 
		// intersect point of ray and plane
		I[0] = tempI[0];
		I[1] = tempI[1];
		I[2] = tempI[2];

		// is I inside T?
		float uu, uv, vv, wu, wv, D;
		uu = Vector.dot(u, u);
		uv = Vector.dot(u, v);
		vv = Vector.dot(v, v);
		w = Vector.minus(I, T.V0);
		wu = Vector.dot(w, u);
		wv = Vector.dot(w, v);
		D = (uv * uv) - (uu * vv);

		// get and test parametric coords
		float s, t;
		s = ((uv * wv) - (vv * wu)) / D;
		if (s < 0.0f || s > 1.0f) // I is outside T
			return 0;
		t = (uv * wu - uu * wv) / D;
		if (t < 0.0f || (s + t) > 1.0f) // I is outside T
			return 0;

		return 1; // I is in T
	}
}

class Vector
{
	public static float[] minus(float[] u, float[] v)
	{
		return new float[] { u[X] - v[X], u[Y] - v[Y], u[Z] - v[Z] };
	}

	public static float[] addition(float[] u, float[] v)
	{
		return new float[] { u[X] + v[X], u[Y] + v[Y], u[Z] + v[Z] };
	}

	// dot product (3D) which allows vector operations in arguments
	public static float dot(float[] u, float[] v)
	{
		return ((u[X] * v[X]) + (u[Y] * v[Y]) + (u[Z] * v[Z]));
	}
	
	// scalar product
	public static float[] scalarProduct(float r, float[] u)
	{
		return new float[] { u[X] * r, u[Y] * r, u[Z] * r };
	}

	// (cross product)
	public static float[] crossProduct(float[] u, float[] v)
	{
		return new float[] { (u[Y] * v[Z]) - (u[Z] * v[Y]),
				(u[Z] * v[X]) - (u[X] * v[Z]), (u[X] * v[Y]) - (u[Y] * v[X]) };
	}

	// magnitude or length
	public static float length(float[] u)
	{
		return (float) Math.abs(Math.sqrt((u[X] * u[X]) + (u[Y] * u[Y])
				+ (u[Z] * u[Z])));
	}

	public static final int X = 0;
	public static final int Y = 1;
	public static final int Z = 2;
}