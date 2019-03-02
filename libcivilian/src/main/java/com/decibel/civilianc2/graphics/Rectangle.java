package com.decibel.civilianc2.graphics;

import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.RectF;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by dburnett on 3/20/2018.
 */

public class Rectangle extends RectF implements Serializable
{
    private static final long serialVersionUID = 1L;

    public Rectangle(float left, float top, float right, float bottom) {
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
        // TODO Auto-generated constructor stub
    }

    public Rectangle()
    {

    }

    private void writeObject(java.io.ObjectOutputStream out) throws IOException
    {
        out.writeFloat(this.left);
        out.writeFloat(this.top);
        out.writeFloat(this.right);
        out.writeFloat(bottom);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException,
            ClassNotFoundException
    {
        left = in.readFloat();
        top = in.readFloat();
        right = in.readFloat();
        bottom = in.readFloat();
    }

    public PointF getIntersection(PointF a, PointF b)
    {
        ArrayList<PointF> boundingBox = new ArrayList<PointF>();
        boundingBox.add(new PointF(this.left, this.top));
        boundingBox.add(new PointF(this.right, this.top));
        boundingBox.add(new PointF(this.right, this.bottom));
        boundingBox.add(new PointF(this.left, this.bottom));
        boundingBox.add(new PointF(this.left, this.top));
        PointF intersection = new PointF();
        if(Intersection(boundingBox, a, b, intersection))
        {
            return intersection;
        }
        return null;
    }

    public static boolean Intersection(List<PointF> lineSegments, PointF a, PointF b, PointF intersection)
    {
        for (int i = 0; i < lineSegments.size() - 1; i++)
        {
            if (Intersection(lineSegments.get(i), lineSegments.get(i + 1), a, b, intersection))
            {
                return true;
            }
        }
        return false;
    }


    public static boolean Intersection(PointF p0, PointF p1, PointF p2, PointF p3, PointF intersection)
    {
        PointF s1 = new PointF();
        PointF s2 = new PointF();
        s1.x = p1.x - p0.x; s1.y = p1.y - p0.y;
        s2.x = p3.x - p2.x; s2.y = p3.y - p2.y;

        float s, t;
        s = (-s1.y * (p0.x - p2.x) + s1.x * (p0.y - p2.y)) / (-s2.x * s1.y + s1.x * s2.y);
        t = (s2.x * (p0.y - p2.y) - s2.y * (p0.x - p2.x)) / (-s2.x * s1.y + s1.x * s2.y);

        if (s >= 0.0 && s <= 1.0 && t >= 0.0 && t <= 1.0)
        {
            // Collision detected
            if(intersection != null)
            {
                intersection.x = p0.x + (t * s1.x);
                intersection.y = p0.y + (t * s1.y);
            }
            return true;
        }

        return false; // No collision
    }

    public void scale(float scale)
    {
        this.bottom *= scale;
        this.left *= scale;
        this.right *= scale;
        this.top *= scale;
    }

    public void scale(float x_scale, float y_scale)
    {
        this.bottom *= y_scale;
        this.left *= x_scale;
        this.right *= x_scale;
        this.top *= y_scale;
    }

}
