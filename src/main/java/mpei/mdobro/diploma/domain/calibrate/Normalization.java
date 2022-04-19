package mpei.mdobro.diploma.domain.calibrate;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import mpei.mdobro.diploma.domain.parse.AlgorithmType;
import mpei.mdobro.diploma.domain.parse.HodographObject;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.util.FastMath;

import java.util.*;

import static mpei.mdobro.diploma.domain.constants.AppConstants.CALIBRATION_Amplitude;
import static mpei.mdobro.diploma.domain.constants.AppConstants.CALIBRATION_Phase;

@Slf4j
@Data
public class Normalization {

    //    @Value("${calibration.Amp_deep_100}")
    private Integer calibrationAmplitude = CALIBRATION_Amplitude;

    //    @Value("${calibration.Phase_deep_100}")
    private Integer calibrationPhase = CALIBRATION_Phase;

    private Complex etalon = new Complex(calibrationAmplitude * FastMath.cos(FastMath.toRadians(calibrationPhase)),
            calibrationAmplitude * FastMath.sin(FastMath.toRadians(calibrationPhase)));

    private Map<Integer, Complex> freqAndNormCoefficient = new HashMap<>();
    private Map<Integer, List<HodographObject>> freqAndExtremeHO = new HashMap<>();


    //for same deep
    public Map<Integer, Complex> normalizeWithMaxAmplitude(Map<Integer, List<HodographObject>> map) {
        for (Map.Entry<Integer, List<HodographObject>> entry : map.entrySet()) {

            //find HO with max amplitude
            List<HodographObject> value = entry.getValue();
            HodographObject maxHO = Collections.max(value);

            //find first Im max
            HodographObject maxImHO = getFirstImMax(value);

            //maxHO.setFirstAmpMax(true);

            List<HodographObject> extremeHOList = new LinkedList<>();
            extremeHOList.add(maxHO);
            freqAndExtremeHO.put(entry.getKey(), extremeHOList);

            Complex maxComplexNumber = maxHO.getComplexNumber();

            // solveNormEquation
            Complex normCoefficient = etalon.divide(maxComplexNumber);

            freqAndNormCoefficient.put(entry.getKey(), normCoefficient);
            // applyToAllHO
            value.stream().forEach(o -> {
                o.setComplexNumber(o.getComplexNumber().multiply(normCoefficient));
            });
            editDisplacement(entry.getKey(), entry.getValue());
            // A*(Re+i*Im) = 10*exp(40)
            log.debug("max HO:\n {}", maxHO.toString());

        }
        return freqAndNormCoefficient;
    }

    public void normalizeHOListAccordingToAlgorithm(List<HodographObject> HOList) {
        HOList.forEach(ho ->
                ho.setComplexNumber(ho.getComplexNumber()
                        .multiply(freqAndNormCoefficient.get(ho.getFreq())))
        );
    }

    HodographObject applyFirstMaxAlgorithm(List<HodographObject> hodograph, Integer freq) {

        editDisplacement(freq, hodograph);
        normalizeHOListAccordingToAlgorithm(hodograph);
        return getFirstImMax(hodograph);
    }


    private Map<Integer, List<HodographObject>> arrangeByDeep(List<HodographObject> limitsObjectList) {
        Map<Integer, List<HodographObject>> deepToHodographs = new HashMap<>();

        limitsObjectList.sort(Comparator
                .comparing(HodographObject::getDeep));

        List<HodographObject> tmpList = new ArrayList<>();
        Integer tmpDeep = limitsObjectList.get(0).getDeep();

        for (HodographObject ho : limitsObjectList) {

            if ((ho.getDeep() != tmpDeep)) {
                tmpList.sort(Comparator
                        .comparing(HodographObject::getDisplacement));
                deepToHodographs.put(tmpDeep, tmpList);

                tmpList = new ArrayList<>();
                tmpDeep = ho.getDeep();
            }
            tmpList.add(ho);

            if (limitsObjectList.indexOf(ho) == limitsObjectList.size() - 1) {
                tmpList.sort(Comparator
                        .comparing(HodographObject::getDisplacement));
                deepToHodographs.put(tmpDeep, tmpList);
            }
        }
        return deepToHodographs;
    }


    public Map<Integer, Map<Integer, List<HodographObject>>> applyAlgorithmAndGetLimitsCurves(Map<Integer, Map<Double, Map<Integer, List<HodographObject>>>> freqToLengthToDeepAndHodograph,
                                                                                              AlgorithmType algorithmType) {

        Map<Integer, Map<Integer, List<HodographObject>>> limitsCurves = new HashMap<>();//freq ->deep -> [length1, phase1],[l2,p2]...

        for (Map.Entry<Integer, Map<Double, Map<Integer, List<HodographObject>>>> freqEntry : freqToLengthToDeepAndHodograph.entrySet()) {

            List<HodographObject> limitsObjectList = new ArrayList<>();

            for (Map.Entry<Double, Map<Integer, List<HodographObject>>> lenEntry : freqEntry.getValue().entrySet()) {

                for (Map.Entry<Integer, List<HodographObject>> deepEntry : lenEntry.getValue().entrySet()) {

                    deepEntry.getValue().sort(Comparator
                            .comparing(HodographObject::getDisplacement).reversed());
                    switch (algorithmType) {

                        case FIRST_IM_MAX: {
                            System.out.println("FIRST_IM_MAX");
                        }
                        case MAX_AMPLITUDE: {
                            System.out.println("MAX_AMPLITUDE");
                            HodographObject limitObj = applyFirstMaxAlgorithm(deepEntry.getValue(), freqEntry.getKey());
                            limitsObjectList.add(limitObj);
                        }

                    }
                }
            }
            limitsCurves.put(freqEntry.getKey(), arrangeByDeep(limitsObjectList));
        }
        return limitsCurves;
    }

    private boolean numIsOdd(Integer num) {
        if (num % 2 == 0) {
            System.out.println("number is even");
            return false;
        } else {
            System.out.println("number is odd");
            return true;
        }
    }

    public void editDisplacement(Integer freq, List<HodographObject> hoList) {

        Double step = Math.abs(hoList.get(0).getDisplacement() - hoList.get(1).getDisplacement());

        log.info("freq: {} kHz", freq);

        HodographObject HOFirstMax = getFirstMax(hoList);
        HodographObject HOLastMax = getLastMax(hoList);

        Double firstBound = HOFirstMax.getDisplacement();
        Double endBound = HOLastMax.getDisplacement();
        Integer NumOfPoints = new Double((endBound - firstBound) / step).intValue();

        double alignmentDisplacement = 0.0;
        if (numIsOdd(NumOfPoints.intValue())) {//не делится на 2 -> одна средняя точка
            HodographObject middlePoint = hoList.get(hoList.indexOf(HOFirstMax) + ((NumOfPoints - 1) / 2));
            alignmentDisplacement = middlePoint.getDisplacement() - 0;
        } else {
            HodographObject middlePoint_1 = hoList.get(hoList.indexOf(HOFirstMax) + ((NumOfPoints) / 2));
            alignmentDisplacement = middlePoint_1.getDisplacement() / 2;
        }

        log.info("AlignmentDisplacement: {}", alignmentDisplacement);
        double finalAlignmentDisplacement = alignmentDisplacement;

        hoList.stream().forEach(ho ->
        {
            Double newAlignmentDisplacement = ho.getDisplacement() - finalAlignmentDisplacement;
            ho.setDisplacement(newAlignmentDisplacement);
        });
        hoList.sort(Comparator
                .comparing(HodographObject::getDisplacement));
    }

    private HodographObject getFirstMax(List<HodographObject> list) {
        list.sort(Comparator
                .comparing(HodographObject::getDisplacement));

        List<HodographObject> copyList = new ArrayList<>();
        copyList.addAll(list);

        int i = 1;
        while (i < copyList.size()-1) {
            HodographObject ho = copyList.get(i);
            HodographObject ho_prev = copyList.get(i - 1);

            Double delta = Math.abs(ho_prev.complexNumber.abs()*1000) - Math.abs(ho.complexNumber.abs()*1000);
            if (delta > 0) {
                list.get(i - 1).setFirstAmpMax(true);
                System.out.println("FirstMax:\n i = "+i+"| abs = "+ho.complexNumber.abs()*1000+"| disp = "+ho.getDisplacement()*1000);
                System.out.println("\n i-1 = "+(i-1)+"| abs = "+ho_prev.complexNumber.abs()*1000+"| disp = "+ho_prev.getDisplacement()*1000);
                return list.get(i - 1);
            }
            i = i+1;
        }
        return null;
    }

    private HodographObject getLastMax(List<HodographObject> list) {
        list.sort(Comparator
                .comparing(HodographObject::getDisplacement));

        int i = list.size() - 2;
        while (i >= 0) {
            if (list.get(i).complexNumber.abs() - list.get(i+1).complexNumber.abs() > 0) {
                list.get(i).setFirstAmpMax(true);
                System.out.println("LastMax:\n i = "+i+"| abs = "+list.get(i).complexNumber.abs()+"| disp = "+list.get(i).getDisplacement());
                System.out.println("\n i+1 = "+i+"| abs = "+list.get(i+1).complexNumber.abs()+"| disp = "+list.get(i+1).getDisplacement());
                return list.get(i);
            }
            i = i-1;
        }
        return null;
    }

    private HodographObject getFirstImMax(List<HodographObject> list) {
        list.sort(Comparator
                .comparing(HodographObject::getDisplacement));

        for (HodographObject ho : list) {
            if (list.indexOf(ho) == 0) {
                continue;
            } else if (Math.abs(ho.complexNumber.getImaginary()) - Math.abs(list.get(list.indexOf(ho) - 1).complexNumber.getImaginary()) < 0) {
                ho.setFirstImMax(true);
                return list.get(list.indexOf(ho) - 1);
            } else ho.setFirstImMax(false);
        }
        return null;
    }
}