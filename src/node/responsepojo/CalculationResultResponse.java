package node.responsepojo;


import java.util.Map;

public class CalculationResultResponse {
    private Map<String,Integer> result1;
    private int[][] result2;
    private Map<String,Map<String,Double>> result3;

    public CalculationResultResponse(Map<String,Integer> result1, int[][] result2, Map<String,Map<String,Double>> result3){
        this.result1=result1;
        this.result2=result2;
        this.result3=result3;
    }

    public void setResult1(Map<String, Integer> result) {
        this.result1 = result;
    }

    public Map<String,Integer> getResult1(){
        return result1;
    }

    public void setResult2(int[][] result) {
        this.result2 = result;
    }

    public int[][] getResult2(){
        return result2;
    }

    public void setResult3(Map<String, Map<String,Double>> result) {
        this.result3 = result;
    }

    public Map<String,Map<String,Double>> getResult3(){
        return result3;
    }
}
