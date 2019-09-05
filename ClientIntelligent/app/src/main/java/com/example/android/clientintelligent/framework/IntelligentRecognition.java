package com.example.android.clientintelligent.framework;

import android.graphics.RectF;

/** An immutable result returned by a BaseClassifier describing what was recognized. */
public class IntelligentRecognition {
    /**
     * A unique identifier for what has been recognized. Specific to the class, not the instance of
     * the object.
     */
    private int id;

    /** Display name for the recognition. */
    private String title;

    /**
     * A sortable score for how good the recognition is relative to others. Higher should be better.
     */
    private Float confidence;

    /** Optional location within the source image for the location of the recognized object. */
    private RectF location;

    public IntelligentRecognition(final int id, final String title, final Float confidence,
                                  final RectF location) {
        this.id = id;
        this.title = title;
        this.confidence = confidence;
        this.location = location;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
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

    @Override
    public String toString() {
        String resultString = "";
        resultString += "[" + id + "] ";

        if (title != null) {
            resultString += title + " ";
        }

        if (confidence != null) {
            resultString += String.format("(%.1f%%) ", confidence * 100.0f);
        }

        if (location != null) {
            resultString += location + " ";
        }

        return resultString.trim();
    }
}