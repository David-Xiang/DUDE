package com.example.android.clientintelligent.framework.pojo;

import android.graphics.RectF;

/** An immutable result returned by a BaseClassifier describing what was recognized. */
public class Recognition {
    /**
     * A unique identifier for what has been recognized. Specific to the class, not the instance of
     * the object.
     */
    private int id;
    private String name;

    /**
     * A sortable score for how good the recognition is relative to others. Higher should be better.
     */
    private Float confidence;

    /** Optional location within the source image for the location of the recognized object. */
    private RectF location;

    public Recognition(final int id, final String name, final Float confidence,
                       final RectF location) {
        this.id = id;
        this.name = name;
        this.confidence = confidence;
        this.location = location;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Float getConfidence() {
        return confidence;
    }

    public RectF getLocation() {
        return new RectF(location);
    }

    public void setLocation(RectF location) {
        this.location = location;
    }
}