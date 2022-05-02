package mpei.mdobro.diploma.domain.parse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mpei.mdobro.diploma.domain.calibrate.Normalization;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.util.*;

@Slf4j
@RequiredArgsConstructor
public class FileToCollections {

    private static CollectHodographObjects collectHodographObjects;
    public final Normalization norm;

    public Map<Integer, List<HodographObject>> convertCalibrationFileToMap(File f) {

        Map<Integer, List<HodographObject>> freqAndHOList = null;
        List<HodographObject> hodographObjectList = convertFileToHOList(f);

        if (hodographObjectList.get(0).getTypeDefect().equals(DefectTypes.CALIBRATION)) {
            //freq->List<HO>
            freqAndHOList = getFreqAndHOMap(hodographObjectList);
        }
        return freqAndHOList;
    }

    public Map<Integer, Map<Integer, List<HodographObject>>> convertCommonListToLimitsCurvesMap(List<HodographObject> hodographObjectList,
                                                                                                AlgorithmType algorithmType) {

        Map<Integer, Map<Double, Map<Integer, List<HodographObject>>>> freqToLengthToDeepAndHodograph = convertCommonListToCommonFreqMap(hodographObjectList);

        // теперь к каждому годографу - пременить алгоритм нормализации и определить наклон
        //applyAlgorithm for each freq and return HO with Phase
        Map<Integer, Map<Integer, List<HodographObject>>> freqToDeepToLengthAngleList = new HashMap<>();
        Map<Integer, List<HodographObject>> deepAndLengthAngleList = null;
        return norm.applyAlgorithmAndGetLimitsCurves(freqToLengthToDeepAndHodograph, algorithmType);
    }

    public Map<Integer, Map<Double, Map<Integer, List<HodographObject>>>> convertCommonListToCommonFreqMap(List<HodographObject> hodographObjectList) {

        //{freq ->{length -> {deep -> List<HO>}}}
        Map<Integer, List<HodographObject>> freqAndHOList = getFreqAndHOMap(hodographObjectList);
        Map<Integer, Map<Double, List<HodographObject>>> freqToLengthAndHodograph = getFreqToLengthAndHodograph(freqAndHOList);

        Map<Integer, Map<Double, Map<Integer, List<HodographObject>>>> freqToLengthToDeepAndHodograph
                = getFreqToLengthToDeepAndLimitsHO(freqToLengthAndHodograph);

        return freqToLengthToDeepAndHodograph;
    }

    public Map<Double, Map<Integer, Map<Integer, List<HodographObject>>>> convertCommonListToCommonMap(List<HodographObject> hodographObjectList) {
        Map<Double, Map<Integer, Map<Integer, List<HodographObject>>>> lengthToFreqToDeepAndHodograph = null;

        if (hodographObjectList.get(0).getTypeDefect().equals(DefectTypes.LIMIT)) {
            //{length ->{freq -> {deep -> List<HO>}}}
            lengthToFreqToDeepAndHodograph
                    = getLengthToFreqToDeepAndHodograph(hodographObjectList);
        }
        return lengthToFreqToDeepAndHodograph;
    }

    public List<HodographObject> convertFileToHOList(File f) {
        if (!f.exists() && !f.isDirectory()) {
            System.out.println("file [" + f.getName() + "] DON'T exist!");
            System.exit(0);
        }
        log.info("file [{}] exist!", f.getName());

        collectHodographObjects = new CollectHodographObjects(f);
        List<HodographObject> hodographObjectList = fileToList(f);

        //sorting by Freq
        sortByDisplacementAndFreq(hodographObjectList);
        return hodographObjectList;
    }


    //point
    private Map<Integer, Map<Double, Map<Integer, List<HodographObject>>>> getFreqToLengthToDeepAndLimitsHO(
            Map<Integer, Map<Double, List<HodographObject>>> freqToLengthAndHodograph) {

        Map<Integer, Map<Double, Map<Integer, List<HodographObject>>>> freqToLengthToDeepAndLimitsHO = new HashMap<>();

        for (Map.Entry<Integer, Map<Double, List<HodographObject>>> freqEntry : freqToLengthAndHodograph.entrySet()) {
            Map<Double, Map<Integer, List<HodographObject>>> lengthToDeepToHO = new HashMap<>();

            for (Map.Entry<Double, List<HodographObject>> lenEntry : freqEntry.getValue().entrySet()) {
                Map<Integer, List<HodographObject>> deepAndHodograph = new HashMap<>();

                sortByDisplacementAndDeep(lenEntry.getValue());
                List<HodographObject> tmpList = new ArrayList<>();
                Integer tmpDeep = lenEntry.getValue().get(0).getDeep();

                for (HodographObject entity : lenEntry.getValue()) {

                    if (!(entity.getDeep().equals(tmpDeep))) {
                        //sort by deep & put
                        sortByDisplacementAndDeep(tmpList);
                        //----->    apply Algorithm   for tmpDeep
                        deepAndHodograph.put(tmpDeep, tmpList);

                        tmpList = new ArrayList<>();
                        tmpDeep = entity.getDeep();
                    }
                    tmpList.add(entity);

                    if (lenEntry.getValue().indexOf(entity) == lenEntry.getValue().size() - 1) {
                        //sort by deep & put
                        sortByDisplacementAndDeep(tmpList);
                        //----->    apply Algorithm
                        deepAndHodograph.put(tmpDeep, tmpList);
                    }
                }

                lengthToDeepToHO.put(lenEntry.getKey(), deepAndHodograph);
            }
            freqToLengthToDeepAndLimitsHO.put(freqEntry.getKey(), lengthToDeepToHO);
        }

        return freqToLengthToDeepAndLimitsHO;
    }


    private Map<Integer, Map<Double, List<HodographObject>>> getFreqToLengthAndHodograph(Map<Integer, List<HodographObject>> freqAndHOList) {

        Map<Integer, Map<Double, List<HodographObject>>> freqToLengthAndHodograph = new HashMap<>();

        for (Map.Entry<Integer, List<HodographObject>> entry : freqAndHOList.entrySet()) {

            //sort by length
            entry.getValue().sort(Comparator
                    .comparing(HodographObject::getDefectLength).reversed());

            List<HodographObject> tmpList = new ArrayList<>();
            Map<Double, List<HodographObject>> lengthAndHodograph = new HashMap<>();

            Double tmpLength = entry.getValue().get(0).getDefectLength();
            for (HodographObject entity : entry.getValue()) {

                if (!tmpLength.equals(entity.getDefectLength())) {
                    //sort by deep & put
                    sortByDisplacementAndDeep(tmpList);
                    lengthAndHodograph.put(tmpLength, tmpList);

                    tmpList = new ArrayList<>();
                    tmpLength = entity.getDefectLength();
                }
                tmpList.add(entity);
                if (entry.getValue().indexOf(entity) == entry.getValue().size() - 1) {
                    //sort by deep & put
                    sortByDisplacementAndDeep(tmpList);
                    lengthAndHodograph.put(tmpLength, tmpList);
                }
            }
            freqToLengthAndHodograph.put(entry.getKey(), lengthAndHodograph);

        }
        return freqToLengthAndHodograph;
    }


    public Map<Double, Map<Integer, Map<Integer, List<HodographObject>>>> getLengthToFreqToDeepAndHodograph(List<HodographObject> hodographObjectList) {

        Map<Integer, List<HodographObject>> freqAndHOList = getFreqAndHOMap(hodographObjectList);
        //нельзя сдвигать, тк разные длины
//        for (Map.Entry<Integer, List<HodographObject>> entry : freqAndHOList.entrySet()) {
//            norm.editDisplacement(entry);
//        }

        Map<Integer, Map<Integer, List<HodographObject>>> freqToDeepAndHodograph = getFreqWithDeepAndHodograph(freqAndHOList);

        Map<Double, Map<Integer, Map<Integer, List<HodographObject>>>> lengthToFreqToDeepAndHodograph = new HashMap<>();

        //??? уже несколько длин
        //!!!пока длина одна 10мм
        lengthToFreqToDeepAndHodograph.put(hodographObjectList.get(0).defectLength, freqToDeepAndHodograph);

        return lengthToFreqToDeepAndHodograph;
    }


    private Map<Integer, Map<Integer, List<HodographObject>>> getFreqWithDeepAndHodograph
            (Map<Integer, List<HodographObject>> freqAndHOList) {

        Map<Integer, Map<Integer, List<HodographObject>>> mainMap = new HashMap<>();

        for (Map.Entry<Integer, List<HodographObject>> entry : freqAndHOList.entrySet()) {

            //sort by deep
            sortByDisplacementAndDeep(entry.getValue()); //100 80 60 40 20

            List<HodographObject> tmpList = new ArrayList<>();
            Map<Integer, List<HodographObject>> deepAndHodograph = new HashMap<>();

            int tmpDeep = entry.getValue().get(0).getDeep();
            for (HodographObject entity : entry.getValue()) {

                if ((entity.getDeep() != tmpDeep)) {
                    //sort by deep & put
                    sortByDisplacementAndDeep(tmpList);
                    deepAndHodograph.put(tmpDeep, tmpList);

                    tmpList = new ArrayList<>();
                    tmpDeep = entity.getDeep();
                }
                tmpList.add(entity);
                if (entry.getValue().indexOf(entity) == entry.getValue().size() - 1) {
                    //sort by deep & put
                    sortByDisplacementAndDeep(tmpList);
                    deepAndHodograph.put(tmpDeep, tmpList);
                }
            }
            mainMap.put(entry.getKey(), deepAndHodograph);

        }
        return mainMap;
    }


    private Map<Integer, List<HodographObject>> getFreqAndHOMap(List<HodographObject> hodographObjectList) {

        Map<Integer, List<HodographObject>> freqHOMap = new HashMap<>();

        sortByDisplacementAndFreq(hodographObjectList);

        List<HodographObject> tmpList = new ArrayList<>();
        int tmpFreq = hodographObjectList.get(0).getFreq();
        int frequencyCounter = 0;

        for (HodographObject entity : hodographObjectList) {

            if ((entity.getFreq() != tmpFreq)) {
                //sort by deep & put
                sortByDisplacementAndDeep(tmpList);
                norm.editDisplacement(tmpFreq,tmpList);
                freqHOMap.put(tmpFreq, tmpList);

                tmpList = new ArrayList<>();
                tmpFreq = entity.getFreq();
                frequencyCounter += 1;
            }
            tmpList.add(entity);

            if (hodographObjectList.indexOf(entity) == hodographObjectList.size() - 1) {
                //sort by deep & put
                sortByDisplacementAndDeep(tmpList);
                norm.editDisplacement(tmpFreq,tmpList);
                freqHOMap.put(tmpFreq, tmpList);
            }
        }
        return freqHOMap;
    }

    private List<HodographObject> fileToList(File f) {
        List<HodographObject> hodographObjectList = null;
        try {
            FileInputStream fileInputStream = new FileInputStream(f.getPath());
            XSSFWorkbook workbook = new XSSFWorkbook(fileInputStream);
            XSSFSheet sheet = workbook.getSheetAt(0);

            hodographObjectList = collectHodographObjects
                    .getListOfHodographObjects(sheet, workbook);

            fileInputStream.close();
        } catch (Exception e) {
            log.error("file to List process is failed!");
            e.printStackTrace();
        }
        return hodographObjectList;
    }

    private void sortByDisplacementAndDeep(List<HodographObject> list) {
        list.sort(Comparator
                .comparing(HodographObject::getDisplacement));
        list.sort(Comparator
                .comparing(HodographObject::getDeep));
    }

    private void sortByDisplacementAndFreq(List<HodographObject> list) {
        list.sort(Comparator
                .comparing(HodographObject::getDisplacement));
        list.sort(Comparator
                .comparing(HodographObject::getFreq));
    }
}

