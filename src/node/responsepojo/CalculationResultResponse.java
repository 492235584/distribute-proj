package node.responsepojo;

import java.util.HashMap;

public class CalculationResultResponse {
    private HashMap<String,Integer> result1;
    private int[][] result2;
    private HashMap<String,HashMap<String,Double>> result3;

    public CalculationResultResponse(HashMap<String,Integer> result1, int[][] result2, HashMap<String,HashMap<String,Double>> result3){
        this.result1=result1;
        this.result2=result2;
        this.result3=result3;
    }

    public void setResult1(HashMap<String, Integer> result) {
        this.result1 = result;
    }

    public HashMap<String,Integer> getResult1(){
        return result1;
    }

    public void setResult2(int[][] result) {
        this.result2 = result;
    }

    public int[][] getResult2(){
        return result2;
    }

    public void setResult3(HashMap<String, HashMap<String,Double>> result) {
        this.result3 = result;
    }

    public HashMap<String,HashMap<String,Double>> getResult3(){
        return result3;
    }
}
