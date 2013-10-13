package com.sastraxi.chisel.state;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;

/**
 * Current editor selection on a Brush.
 */
public class Selection implements Pool.Poolable {

    private Array<Integer> selectedFaces;
    private Array<Integer> selectedVertices;

    public Selection() {
        this.selectedFaces = new Array<Integer>();
        this.selectedVertices = new Array<Integer>();
    }

    public Array<Integer> getSelectedFaces() {
        return selectedFaces;
    }

    public Array<Integer> getSelectedVertices() {
        return selectedVertices;
    }

    @Override
    public void reset() {
        this.selectedFaces.clear();
        this.selectedVertices.clear();
    }

    public void selectFace(int face) {
        if (!selectedFaces.contains(face, true)) {
            selectedFaces.add(face);
        }
    }

    public void clearFace(int face) {
        selectedFaces.removeValue(face, true);
    }

    public void selectVertex(int vertex) {
        if (!selectedVertices.contains(vertex, true)) {
            selectedVertices.add(vertex);
        }
    }

    public void clearVertex(int vertex) {
        selectedVertices.removeValue(vertex, true);
    }

}