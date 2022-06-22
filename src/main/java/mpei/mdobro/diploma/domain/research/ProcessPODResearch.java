package mpei.mdobro.diploma.domain.research;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mpei.mdobro.diploma.domain.parse.HodographObject;
import mpei.mdobro.diploma.domain.parse.NDTDataObject;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.apache.commons.math3.util.Pair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.lang.Math.PI;

@Data
@Slf4j
@RequiredArgsConstructor
public class ProcessPODResearch {


    private final Map<Integer, Map<Integer, List<HodographObject>>>
            freqToDeepAndLengthAngleLimitList;
    private final Map<Integer, Map<Integer, List<HodographObject>>> freqToDeepAndLengthAngleModelList;
    private final Map<Integer, List<NDTDataObject>> experimentData;

    private Map<Integer, Pair<Double, Double>> rangePerFreq = new HashMap();

    private Map<Integer, Double> limitPerFreq = new HashMap();
    private Map<Integer, Map<Integer, Double>> freqToDeepToPOD = new HashMap<>();
    //POD = Nd/N ->
    // nd - количество обнаруженных трещин размера/глубиной a
    // из n - общего числа трещин размера a в испытании.


    Map<Integer, SimpleRegression> getRegressionPerFreq() {
        Map<Integer, SimpleRegression> freqToRegression = new HashMap<>();

        for (Map.Entry<Integer, Map<Integer, List<HodographObject>>> freqEntry : freqToDeepAndLengthAngleModelList.entrySet()) {

            Map<Integer, List<HodographObject>> deepEntry = freqEntry.getValue();

            SimpleRegression regression = new SimpleRegression();
            for (int i = 0; i < deepEntry.get(60).size(); i++) {

                Double length = deepEntry.get(60).get(i).defectLength;
                Double phase = deepEntry.get(60).get(i).getComplexNumber().getArgument() * 180 / PI;

                regression.addData(length, phase);
            }
            freqToRegression.put(freqEntry.getKey(), regression);
        }
        return freqToRegression;
    }

    public void runSimplePODProcess() {
        determineRangeByFreq();

        for (Map.Entry<Integer, Map<Integer, List<HodographObject>>> freqEntry : freqToDeepAndLengthAngleModelList.entrySet()) {

            applyDetectToExperiment(freqEntry.getKey());

            Map<Integer, Double> deepToPOD = new HashMap<>();
            for (Map.Entry<Integer, List<HodographObject>> deepEntry : freqEntry.getValue().entrySet()) {

                int N = 0;
                int Nd = 0;

                for (HodographObject deepHO : deepEntry.getValue()) {

                    boolean isDetected = modelIsDetected(deepHO);
                    if (isDetected) {
                        Nd = Nd + 1;
                    }
                    N = N + 1;

                    if (deepEntry.getValue().indexOf(deepHO) == deepEntry.getValue().size() - 1) {
                        Double POD = 0.0;
                        if(deepEntry.getKey() == 100){
                            POD = calculatePOD(deepEntry, experimentData.get(freqEntry.getKey()));
                        }
                        else {
                            POD = calculatePOD(deepEntry, null);
                        }
                        deepToPOD.put(deepEntry.getKey(), POD);
                   }
                }
            }

            freqToDeepToPOD.put(freqEntry.getKey(), deepToPOD);
        }
        PODPrinter podPrinter = new PODPrinter();
        podPrinter.print(freqToDeepToPOD);
    }

    private void applyDetectToExperiment(Integer freq){
        List<NDTDataObject> ndtFreqList = experimentData.get(freq);
        for(NDTDataObject experimentObj : ndtFreqList) {
            if (experimentObj.getRotatedPhase() >= rangePerFreq.get(experimentObj.getFreq()).getKey()
                    && experimentObj.getRotatedPhase() <= rangePerFreq.get(experimentObj.getFreq()).getValue()) {
                experimentObj.setDetected(true);
            }
            experimentObj.setDetected(false);
            System.out.println(">>" + experimentObj.toString());
        }
    }

    private Double calculatePOD(Map.Entry<Integer, List<HodographObject>> modelDeepEntry, List<NDTDataObject> experimentTrougthData) {

        int N_exp=0;
        Long Nd_exp = 0L;
        if(experimentTrougthData != null){
            N_exp = experimentTrougthData.size();
            Nd_exp =experimentTrougthData
                    .stream()
                    .filter(e -> e.isDetected())
                    .count();
        }
        Double N = new Double(modelDeepEntry.getValue().size()) + N_exp;
        Long Nd = modelDeepEntry.getValue()
                .stream()
                .filter(ho -> ho.isDetected())
                .count();
        Nd = Nd + Nd_exp;

        Double POD = Nd.doubleValue() / N.doubleValue();

        return POD;
    }

    //условие обнаружения опасного дефекта
    private boolean modelIsDetected(HodographObject hodographObject) {

        Double phase = hodographObject.getComplexNumber().getArgument() * 180 / PI;

        if (phase >= rangePerFreq.get(hodographObject.getFreq()).getKey()
                && phase <= rangePerFreq.get(hodographObject.getFreq()).getValue()){
            hodographObject.setDetected(true);
            return true;
        }
        hodographObject.setDetected(false);
        return false;
    }

    public void determineRangeByFreq() {

        for (Map.Entry<Integer, Map<Integer, List<HodographObject>>> freqEntry : freqToDeepAndLengthAngleLimitList.entrySet()) {

            Map<Integer, List<HodographObject>> deepEntry = freqEntry.getValue();
            List<Double> deep20Phase = deepEntry.get(20).stream()
                    .map(hodographObject -> hodographObject.getComplexNumber().getArgument() * 180 / PI)
                    .collect(Collectors.toList());
            Double lowLimit = deep20Phase.stream()
                    .mapToDouble(Double::doubleValue)
                    .average()
                    .orElse(Double.NaN);


            List<Double> deep100Phase = deepEntry.get(100).stream()
                    .map(hodographObject -> hodographObject.getComplexNumber().getArgument() * 180 / PI)
                    .collect(Collectors.toList());

            Double highLimit = deep100Phase.stream()
                    .mapToDouble(Double::doubleValue)
                    .average()
                    .orElse(Double.NaN);

            rangePerFreq.put(freqEntry.getKey(), new Pair<>(lowLimit, highLimit));
        }
    }

    private void determineLimitByFreq() {

        for (Map.Entry<Integer, Map<Integer, List<HodographObject>>> freqEntry : freqToDeepAndLengthAngleLimitList.entrySet()) {

            Map<Integer, List<HodographObject>> deepEntry = freqEntry.getValue();

            List<Double> deep60Phase = deepEntry.get(60).stream()
                    .map(hodographObject -> hodographObject.getComplexNumber().getArgument() * 180 / PI)
                    .collect(Collectors.toList());

            Double highLimit = deep60Phase.stream()
                    .mapToDouble(Double::doubleValue)
                    .average()
                    .orElse(Double.NaN);

            limitPerFreq.put(freqEntry.getKey(), highLimit);
        }
    }


    //    public void runSimpleRegressionPODProcess() {
//
//        Map<Integer, SimpleRegression> freqToRegression = getRegressionPerFreq();
//
//        for (Map.Entry<Integer, SimpleRegression> regressionEntry : freqToRegression.entrySet()) {
//            regressionEntry.getValue().regress();
//        }
//
//        System.out.println("regression full");
//        for (Map.Entry<Integer, Map<Integer, List<HodographObject>>> freqEntry : freqToDeepAndLengthAngleModelList.entrySet()) {
//
//            Map<Integer, Double> deepToPOD = new HashMap<>();
//            for (Map.Entry<Integer, List<HodographObject>> deepEntry : freqEntry.getValue().entrySet()) {
//
//                int N = 0;
//                int Nd = 0;
//
//                for (HodographObject deepHO : deepEntry.getValue()) {
//
//                    double isDetected = freqToRegression.get(freqEntry.getKey()).predict(deepHO.getComplexNumber().getArgument() * 180 / PI);
//                    if (Math.abs(isDetected)< 5) {
//                        Nd = Nd + 1;
//                        deepHO.setDetected(true);
//                    }else {deepHO.setDetected(false);}
//                    N = N + 1;
//
//                    if (deepEntry.getValue().indexOf(deepHO) == deepEntry.getValue().size() - 1) {
//                        Double POD = calculatePOD(deepEntry);
//                        deepToPOD.put(deepEntry.getKey(), POD);
//                    }
//                }
//
//            }
//
//            freqToDeepToPOD.put(freqEntry.getKey(), deepToPOD);
//        }
//        PODPrinter podPrinter = new PODPrinter();
//        podPrinter.print(freqToDeepToPOD);
//    }

}
