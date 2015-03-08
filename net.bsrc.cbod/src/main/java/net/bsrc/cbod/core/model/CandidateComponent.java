package net.bsrc.cbod.core.model;

import org.opencv.core.Rect;

/**
 * User: bsr
 * Date: 07/03/15
 * Time: 11:19
 */
public class CandidateComponent {

    private Long id;

    private Double decisionFusionResult;

    private EObjectType objectType;

    private Rect rect;

    public CandidateComponent(){

    }

    public Double getDecisionFusionResult() {
        return decisionFusionResult;
    }

    public void setDecisionFusionResult(Double decisionFusionResult) {
        this.decisionFusionResult = decisionFusionResult;
    }

    public EObjectType getObjectType() {
        return objectType;
    }

    public void setObjectType(EObjectType objectType) {
        this.objectType = objectType;
    }

    public Rect getRect() {
        return rect;
    }

    public void setRect(Rect rect) {
        this.rect = rect;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CandidateComponent that = (CandidateComponent) o;

        if (objectType != that.objectType) return false;
        if (rect != null ? !rect.equals(that.rect) : that.rect != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = objectType != null ? objectType.hashCode() : 0;
        result = 31 * result + (rect != null ? rect.hashCode() : 0);
        return result;
    }
}
