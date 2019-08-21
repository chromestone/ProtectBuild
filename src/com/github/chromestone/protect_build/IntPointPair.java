package com.github.chromestone.protect_build;

import java.io.Serializable;

/**
 * The IntLocationPair class
 * Created by Derek Zhang on 8/20/19.
 */
public class IntPointPair implements Serializable {

    private static final long serialVersionUID = -710012265099808024L;

    public final int integer;
    public My3DPoint point;

    public IntPointPair(int integer) {

        this.integer = integer;
    }
}
