package mpei.mdobro.diploma.domain.research;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mpei.mdobro.diploma.domain.parse.HodographObject;

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

    private Map<Integer, Double> rangePerFreq = new HashMap();
    private Map<Integer, Map<Integer, Double>> freqToDeepToPOD = new HashMap<>();
    //POD = Nd/N ->
    // nd - количество обнаруженных трещин размера/глубиной a
    // из n - общего числа трещин размера a в испытании.


    public void findPODProcess() {
        determineRangeByFreq();

        for (Map.Entry<Integer, Map<Integer, List<HodographObject>>> freqEntry : freqToDeepAndLengthAngleModelList.entrySet()) {

//            System.out.println("------------------------------------------------------------");
//            System.out.println("|                   FREQ = " + freqEntry.getKey() + "                        |");
//            System.out.println("-------------------------------------------------------------");

            Map<Integer, Double> deepToPOD = new HashMap<>();
            for (Map.Entry<Integer, List<HodographObject>> deepEntry : freqEntry.getValue().entrySet()) {
//                System.out.println("\n|     DEEP = " + deepEntry.getKey() + "                                                              |\n");

                int N = 0;
                int Nd = 0;

                for (HodographObject deepHO : deepEntry.getValue()) {

                    boolean isDetected = isDetected(deepHO);
                    if (isDetected) {
                        Nd = Nd + 1;
                    }
                    N = N + 1;

//                    System.out.println("| LNGTH = " + deepHO.getDefectLength()
//                            + " | TYPE: " + deepHO.getTypeDefect()
//                            + "\n| PHASE: " + deepHO.getComplexNumber().getArgument() * 180 / PI
//                            + " | DETECTED:" + deepHO.isDetected());
//                    System.out.println("|------------------------------------------------------------------------|\n" +
//                            "|                                                                        |");

                    if (deepEntry.getValue().indexOf(deepHO) == deepEntry.getValue().size() - 1) {
                        Double POD = calculatePOD(deepEntry);
                        deepToPOD.put(deepEntry.getKey(), POD);
//                        System.out.println("|                   POD = " + POD + " | DEEP = " + deepEntry.getKey() + "                        |");
                    }
                }

            }

            freqToDeepToPOD.put(freqEntry.getKey(), deepToPOD);
        }
        PODPrinter podPrinter = new PODPrinter();
        podPrinter.print(freqToDeepToPOD);
    }

    private Double calculatePOD(Map.Entry<Integer, List<HodographObject>> deepEntry) {

        Double N = new Double(deepEntry.getValue().size());
        Long Nd = deepEntry.getValue()
                .stream()
                .filter(ho -> ho.isDetected())
                .count();
        Double POD = Nd.doubleValue() / N.doubleValue();

        return POD;
    }

    //условие обнаружения опасного дефекта
    private boolean isDetected(HodographObject hodographObject) {

        Double phase = hodographObject.getComplexNumber().getArgument() * 180 / PI;
        if (phase >= rangePerFreq.get(hodographObject.getFreq())) {
            hodographObject.setDetected(true);
            return true;
        }
        hodographObject.setDetected(false);
        return false;
    }

    private void determineRangeByFreq() {

        for (Map.Entry<Integer, Map<Integer, List<HodographObject>>> freqEntry : freqToDeepAndLengthAngleLimitList.entrySet()) {

            Map<Integer, List<HodographObject>> deepEntry = freqEntry.getValue();

            List<Double> deep60Phase = deepEntry.get(60).stream()
                    .map(hodographObject -> hodographObject.getComplexNumber().getArgument() * 180 / PI)
                    .collect(Collectors.toList());

            Double highLimit = deep60Phase.stream()
                    .mapToDouble(Double::doubleValue)
                    .average()
                    .orElse(Double.NaN);

            rangePerFreq.put(freqEntry.getKey(), highLimit);
        }
    }
}
