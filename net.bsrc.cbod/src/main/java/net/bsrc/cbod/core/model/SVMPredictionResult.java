package net.bsrc.cbod.core.model;

import net.bsrc.cbod.core.model.ImageModel;

import java.util.ArrayList;
import java.util.List;

/**
 * User: bsr
 * Date: 07/03/15
 * Time: 10:48
 */
public class SVMPredictionResult {

    private ESVMType svmType;

    private ImageModel inputImageModel;

    private String segmentedImagePath;

    private List<ImageModel> segmentedInputImageModels = new ArrayList<ImageModel>();

    private List<CandidateComponent> candidateComponents = new ArrayList<CandidateComponent>();


    public ImageModel getInputImageModel() {
        return inputImageModel;
    }

    public void setInputImageModel(ImageModel inputImageModel) {
        this.inputImageModel = inputImageModel;
    }


    public String getSegmentedImagePath() {
        return segmentedImagePath;
    }

    public void setSegmentedImagePath(String segmentedImagePath) {
        this.segmentedImagePath = segmentedImagePath;
    }

    public List<ImageModel> getSegmentedInputImageModels() {
        return segmentedInputImageModels;
    }

    public void setSegmentedInputImageModels(List<ImageModel> segmentedInputImageModels) {
        this.segmentedInputImageModels = segmentedInputImageModels;
    }

    public List<CandidateComponent> getCandidateComponents() {
        return candidateComponents;
    }

    public void setCandidateComponents(List<CandidateComponent> candidateComponents) {
        this.candidateComponents = candidateComponents;
    }

    public ESVMType getSvmType() {
        return svmType;
    }

    public void setSvmType(ESVMType svmType) {
        this.svmType = svmType;
    }

    public void addCandidateComponent(CandidateComponent candidateComponent){
        if(candidateComponent!=null){
            candidateComponents.add(candidateComponent);
        }
    }

    public void addSegmentedInputImageModel(ImageModel imageModel){
        if(imageModel!=null){
            segmentedInputImageModels.add(imageModel);
        }
    }



}
