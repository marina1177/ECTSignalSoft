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

//    public Map<Integer, Map<Integer, List<HodographObject>>> convertCommonDataListToLimitsCurvesMap(List<HodographObject> hodographObjectList,
//                                                                                                AlgorithmType algorithmType) {
//
//        Map<Integer, Map<Double, Map<Integer, List<HodographObject>>>> freqToLengthToDeepAndHodograph
//                = convertCommonDataListToCommonFreqMap(hodographObjectList, algorithmType);
//
//        // теперь к кажgetFreqAndHOMapдому годографу - пременить алгоритм нормализации и определить наклон
//        //applyAlgorithm for each freq and return HO with Phase
//        return norm.applyAlgorithmAndGetLimitsCurves(freqToLengthToDeepAndHodograph, algorithmType);
//    }

    public Map<Integer, Map<Integer, List<HodographObject>>> convertCommonDataListToCommonFreqMap(
            List<List<HodographObject>> hodographsDifferentTypes, AlgorithmType algorithmType) {

        Map<Integer, Map<Integer, List<List<HodographObject>>>> limitPointsForCurrentDefectType = new HashMap<>();
        Map<Integer, Map<Double, Map<Integer, List<HodographObject>>>> commonBuffer = new HashMap<>();

        for (List<HodographObject> hodographObjectList : hodographsDifferentTypes) {

            //еще не отдельные годографы, а много их
            Map<Integer, List<HodographObject>> freqAndManyHodoraphsList
                    = getFreqAndManyHodographsMap(hodographObjectList);
            Map<Integer, Map<Double, List<HodographObject>>> freqToLengthAndHodograph
                    = getFreqToLengthAndHodograph(freqAndManyHodoraphsList);

            //тут должен быть лист с одним годографом для дефекта текущего типа
            Map<Integer, Map<Double, Map<Integer, List<HodographObject>>>> freqToLengthToDeepAndHodograph
                    = getFreqToLengthToDeepAndLimitsHO(freqToLengthAndHodograph);

            // разбить нормализацию годографов и вернуть мапу
            // Map<Integer, Map<Double, Map<Integer, List<HodographObject>>>>
            // где в списке будет одна точка для этого дефекта
            norm.returnMapWitNormalizedPointsForDifferentDefectTypes(freqToLengthToDeepAndHodograph, algorithmType);

            // проверить внешний буффер на ключи и
            // либо добавить новые ключи с точкой либо доложить точку в список
            putPointsToCommonBuffer(commonBuffer, freqToLengthToDeepAndHodograph);
        }
        //- преобразовать в мапу
        Map<Integer, Map<Integer, List<HodographObject>>> allPointsForResearch =
                convertCommonBufferToFreqToLengthToPointsList(commonBuffer);

//        Map<Integer, Map<DefectTypes, Map<Integer, List<HodographObject>>>> pointsForResearch =
//                convertCommonBufferToFreqToDefectTypeLengthToPointsList(commonBuffer);

        // ??? усреднить все точки в списке - преобразовать в мапу

        //Map<Integer, Map<Integer, List<HodographObject>>>

        // нарисовать
        System.out.println("!");

        return allPointsForResearch;
    }

    Map<Integer, Map<Integer, List<HodographObject>>> convertCommonBufferToFreqToLengthToPointsList(Map<Integer, Map<Double, Map<Integer, List<HodographObject>>>> commonBuffer) {

        Map<Integer, Map<Integer, List<HodographObject>>> limitsCurves = new HashMap<>();//freq ->deep -> [length1, phase1],[l2,p2]...

        for (Map.Entry<Integer, Map<Double, Map<Integer, List<HodographObject>>>> freqEntry : commonBuffer.entrySet()) {

            List<HodographObject> limitsObjectList = new ArrayList<>();

            for (Map.Entry<Double, Map<Integer, List<HodographObject>>> lenEntry : freqEntry.getValue().entrySet()) {

                for (Map.Entry<Integer, List<HodographObject>> deepEntry : lenEntry.getValue().entrySet()) {

                    limitsObjectList.addAll(deepEntry.getValue());
                }
            }
            limitsCurves.put(freqEntry.getKey(), norm.arrangeByDeep(limitsObjectList));
        }
        return limitsCurves;
    }

    private void putPointsToCommonBuffer(Map<Integer, Map<Double, Map<Integer, List<HodographObject>>>> commonBuffer,
                                         Map<Integer, Map<Double, Map<Integer, List<HodographObject>>>> freqToLengthToDeepAndHodograph) {

        for (Map.Entry<Integer, Map<Double, Map<Integer, List<HodographObject>>>> freqEntry : freqToLengthToDeepAndHodograph.entrySet()) {

            if (!commonBuffer.containsKey(freqEntry.getKey())) {
                commonBuffer.put(freqEntry.getKey(), freqEntry.getValue());
                continue;
            }
            for (Map.Entry<Double, Map<Integer, List<HodographObject>>> lenEntry : freqEntry.getValue().entrySet()) {
                Map<Double, Map<Integer, List<HodographObject>>> tmpLen = commonBuffer.get(freqEntry.getKey());
                if (!tmpLen.containsKey(lenEntry.getKey())) {
                    commonBuffer.get(freqEntry.getKey()).put(lenEntry.getKey(), lenEntry.getValue());
                    continue;
                }
                for (Map.Entry<Integer, List<HodographObject>> deepEntry : lenEntry.getValue().entrySet()) {
                    Map<Integer, List<HodographObject>> tmpDeep = commonBuffer.get(freqEntry.getKey()).get(lenEntry.getKey());
                    if (!tmpDeep.containsKey(deepEntry.getKey())) {
                        commonBuffer.get(freqEntry.getKey()).get(lenEntry.getKey()).put(deepEntry.getKey(), deepEntry.getValue());
                        continue;
                    } else {

                        commonBuffer.get(freqEntry.getKey())
                                .get(lenEntry.getKey())
                                .get(deepEntry.getKey())
                                .add(deepEntry.getValue().get(0));
                    }

                }
            }
        }
    }

    public Map<Integer, Map<Integer, List<HodographObject>>> convertCommonListToLimitsCurvesMap(List<HodographObject> hodographObjectList,
                                                                                                AlgorithmType algorithmType) {

        Map<Integer, Map<Double, Map<Integer, List<HodographObject>>>> freqToLengthToDeepAndHodograph
                = convertCommonListToCommonFreqMap(hodographObjectList);

        // теперь к каждому годографу - пременить алгоритм нормализации и определить наклон
        //applyAlgorithm for each freq and return HO with Phase
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

    public List<NDTDataObject> convertFileToExperimentList(File f) {
        if (!f.exists() && !f.isDirectory()) {
            System.out.println("file [" + f.getName() + "] DON'T exist!");
            System.exit(0);
        }
        log.info("file [{}] exist!", f.getName());

        collectHodographObjects = new CollectHodographObjects(f);
        List<NDTDataObject> hodographObjectList = fileToExperimentList(f);

        hodographObjectList.sort(Comparator
                .comparing(NDTDataObject::getFreq));

        return hodographObjectList;
    }

    public Map<Integer, Map<Integer, List<NDTDataObject>>> getFreqAndExperimentDataMap(List<NDTDataObject> ndtDataObjectList) {

        //Map<Integer, List<NDTDataObject>> freqHOMap = new HashMap<>();


        Map<Integer, List<NDTDataObject>> freqToNDTList = getFreqToExperimentObjects(ndtDataObjectList);
        Map<Integer, Map<Integer, List<NDTDataObject>>> freqToDeepToNDT = getFreqToDeepAndExperimentObjects(freqToNDTList);

        return freqToDeepToNDT;
    }

    private Map<Integer, Map<Integer, List<NDTDataObject>>> getFreqToDeepAndExperimentObjects(
            Map<Integer, List<NDTDataObject>> freqToNDTMap) {

        Map<Integer, Map<Integer, List<NDTDataObject>>> freqToDeepAndExperimentObjects = new HashMap<>();

        for (Map.Entry<Integer, List<NDTDataObject>> entry : freqToNDTMap.entrySet()) {

            //sort by length
            entry.getValue().sort(Comparator
                    .comparing(NDTDataObject::getDeep).reversed());

            List<NDTDataObject> tmpList = new ArrayList<>();
            Map<Integer, List<NDTDataObject>> lengthAndHodograph = new HashMap<>();

            Integer tmpDeep = entry.getValue().get(0).getDeep();
            for (NDTDataObject entity : entry.getValue()) {

                if (!tmpDeep.equals(entity.getDeep())) {

                    lengthAndHodograph.put(tmpDeep, tmpList);

                    tmpList = new ArrayList<>();
                    tmpDeep = entity.getDeep();
                }
                tmpList.add(entity);
                if (entry.getValue().indexOf(entity) == entry.getValue().size() - 1) {
                    lengthAndHodograph.put(tmpDeep, tmpList);
                }
            }
            freqToDeepAndExperimentObjects.put(entry.getKey(), lengthAndHodograph);
        }
        return freqToDeepAndExperimentObjects;
    }

    private Map<Integer, List<NDTDataObject>> getFreqToExperimentObjects(List<NDTDataObject> ndtDataObjectList) {

        ndtDataObjectList.sort(Comparator
                .comparing(NDTDataObject::getFreq));

        Map<Integer, List<NDTDataObject>> freqToNDTMap = new HashMap<>();

        List<NDTDataObject> tmpList = new ArrayList<>();
        int tmpFreq = ndtDataObjectList.get(0).getFreq();

        for (NDTDataObject entity : ndtDataObjectList) {

            if ((entity.getFreq() != tmpFreq)) {

                freqToNDTMap.put(tmpFreq, tmpList);

                tmpList = new ArrayList<>();
                tmpFreq = entity.getFreq();
            }
            tmpList.add(entity);

            System.out.println("entityIndex: " + ndtDataObjectList.indexOf(entity) + " vs " + (ndtDataObjectList.size() - 1));
            if (ndtDataObjectList.indexOf(entity) == ndtDataObjectList.size() - 1) {
                freqToNDTMap.put(tmpFreq, tmpList);
            }
        }


        return freqToNDTMap;
    }

    private Map<Integer, Map<Double, Map<Integer, List<HodographObject>>>> getFreqToLengthToDeepAndLimitsHO(
            Map<Integer, Map<Double, List<HodographObject>>> freqToLengthAndHodograph) {

        Map<Integer, Map<Double, Map<Integer, List<HodographObject>>>> freqToLengthToDeepAndLimitsHO = new HashMap<>();

        for (Map.Entry<Integer, Map<Double, List<HodographObject>>> freqEntry : freqToLengthAndHodograph.entrySet()) {
            Map<Double, Map<Integer, List<HodographObject>>> lengthToDeepToHO = new HashMap<>();

            for (Map.Entry<Double, List<HodographObject>> lenEntry : freqEntry.getValue().entrySet()) {
                Map<Integer, List<HodographObject>> deepAndHodograph = new HashMap<>();

                sortByDisplacementAndDeep(lenEntry.getValue());
                List<HodographObject> tmpList = new LinkedList<>();
                Integer tmpDeep = lenEntry.getValue().get(0).getDeep();

                for (HodographObject entity : lenEntry.getValue()) {

                    if (!(entity.getDeep().equals(tmpDeep))) {
                        //sort by deep & put
                        sortByDisplacementAndDeep(tmpList);
                        //----->    apply Algorithm   for tmpDeep
                        deepAndHodograph.put(tmpDeep, tmpList);

                        tmpList = new LinkedList<>();
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


    private Map<Integer, List<HodographObject>> getFreqAndManyHodographsMap(List<HodographObject> hodographObjectList) {

        Map<Integer, List<HodographObject>> freqHOMap = new HashMap<>();

        sortByDisplacementAndFreq(hodographObjectList);

        List<HodographObject> tmpList = new ArrayList<>();
        int tmpFreq = hodographObjectList.get(0).getFreq();

        for (HodographObject entity : hodographObjectList) {

            if ((entity.getFreq() != tmpFreq)) {
                //sort by deep & put
                sortByDisplacementAndDeep(tmpList);
                freqHOMap.put(tmpFreq, tmpList);

                tmpList = new ArrayList<>();
                tmpFreq = entity.getFreq();
            }
            tmpList.add(entity);

            if (hodographObjectList.indexOf(entity) == hodographObjectList.size() - 1) {
                //sort by deep & put
                sortByDisplacementAndDeep(tmpList);
                freqHOMap.put(tmpFreq, tmpList);
            }
        }
        return freqHOMap;
    }

    private Map<Integer, List<HodographObject>> getFreqAndHOMap(List<HodographObject> hodographObjectList) {

        Map<Integer, List<HodographObject>> freqHOMap = new HashMap<>();

        sortByDisplacementAndFreq(hodographObjectList);

        List<HodographObject> tmpList = new ArrayList<>();
        int tmpFreq = hodographObjectList.get(0).getFreq();
        for (HodographObject entity : hodographObjectList) {

            if ((entity.getFreq() != tmpFreq)) {
                //sort by deep & put
                sortByDisplacementAndDeep(tmpList);
                norm.moveHodographAccordingZero(tmpList);
                norm.editDisplacement(tmpFreq, tmpList);
                freqHOMap.put(tmpFreq, tmpList);

                tmpList = new ArrayList<>();
                tmpFreq = entity.getFreq();
            }
            tmpList.add(entity);

            if (hodographObjectList.indexOf(entity) == hodographObjectList.size() - 1) {
                //sort by deep & put
                sortByDisplacementAndDeep(tmpList);
                norm.moveHodographAccordingZero(tmpList);
                norm.editDisplacement(tmpFreq, tmpList);
                freqHOMap.put(tmpFreq, tmpList);
            }
        }
        return freqHOMap;
    }

    private List<NDTDataObject> fileToExperimentList(File f) {
        List<NDTDataObject> ndtDataObjectList = null;
        try {
            FileInputStream fileInputStream = new FileInputStream(f.getPath());
            XSSFWorkbook workbook = new XSSFWorkbook(fileInputStream);
            XSSFSheet sheet = workbook.getSheetAt(0);

            ndtDataObjectList = collectHodographObjects
                    .getListOfDataObjects(sheet, workbook);
            fileInputStream.close();
        } catch (Exception e) {
            log.error("file to List process is failed!");
            e.printStackTrace();
        }
        return ndtDataObjectList;
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

