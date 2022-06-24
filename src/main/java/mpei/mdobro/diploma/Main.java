package mpei.mdobro.diploma;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import mpei.mdobro.diploma.domain.calibrate.Normalization;
import mpei.mdobro.diploma.domain.parse.*;
import mpei.mdobro.diploma.domain.print.PaintPlots;
import mpei.mdobro.diploma.domain.research.ProcessPODResearch;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static mpei.mdobro.diploma.domain.constants.AppConstants.*;


//@SpringBootApplication
@Slf4j
@Data
public class Main {

//    @NotNull
//    @Value("${calibration.path_to_file}")
//    private String excelFileName;

    private static Normalization norm = new Normalization();
    private static FileToCollections fileToCollections = new FileToCollections(norm);

    public static void main(String[] args) throws IOException {
//        SpringApplication.run(Main.class, args);

        //if norm directory is empty
        // => for each file from /calibration ->
        //1) find deep100 -> get normalizeCoeff for each frequency
        //1.2) save normalizeCoefficients to /calibration/coefficients.json ??
        //2) apply normalizeCoeff for other files in /calibration directory
        //3) save
        //else

        //=======================================CALIBRATION==============================

        Map<Integer, List<HodographObject>> freqHOMap = saveCalibrationData(new File(CALIBRATION_DIR));

        PaintPlots painter = new PaintPlots(freqHOMap);
        //painter.plotHodographs();

        //=======================================LIMITS==============================

        List<HodographObject> commonList = saveDataLimits(new File(LIMITS_DIR));
        Map<Integer, Map<Integer, List<HodographObject>>> freqToDeepAndLengthAngleList
                = fileToCollections.convertCommonListToLimitsCurvesMap(commonList, AlgorithmType.MAX_AMPLITUDE);

        painter.setFreqToDeepAndLengthAngleLimitList(freqToDeepAndLengthAngleList);
        //painter.plotPhaseLengthCurves();

        //=======================================DATA==============================

        List<List<HodographObject>> hodographsDifferentTypes = saveData(new File(DATA_DIR));
        Map<Integer, Map<Integer, List<HodographObject>>> freqToDeepAndLengthAngleModelList
                = fileToCollections.convertCommonDataListToCommonFreqMap(
                hodographsDifferentTypes, AlgorithmType.MAX_AMPLITUDE);

        painter.setFreqToDeepAndLengthAngleModelList(freqToDeepAndLengthAngleModelList);
        painter.plotModelDataAmongLimits();

        //======================================= EXPERIMENT ==============================
        File experimentDir = new File(EXPERIMENT_DIR);

      //  Map<Integer, Map<Integer, List<NDTDataObject>>> experimentData = saveExperimentData(experimentDir);


        //======================================= POD ==============================

//        ProcessPODResearch podResearch = new ProcessPODResearch(freqToDeepAndLengthAngleList,
//                freqToDeepAndLengthAngleModelList, experimentData);
//        podResearch.createFilesForMH1823();
//        podResearch.runSimplePODProcess();
    }

    private static Map<Integer, Map<Integer, List<NDTDataObject>>> saveExperimentData(File experimentDir) {
        List<File> files = Arrays.stream(experimentDir.listFiles()).collect(Collectors.toList());

        List<NDTDataObject> commonList = new ArrayList<>();
        for (File f : files) {
            List<NDTDataObject> HOList = fileToCollections.convertFileToExperimentList(f);
            commonList.addAll(HOList);
        }

        Map<Integer, Map<Integer, List<NDTDataObject>>> freqExperimentData
                = fileToCollections.getFreqAndExperimentDataMap(commonList);

        return freqExperimentData;
    }

    private static Map<Integer, List<HodographObject>> saveCalibrationData(File calibrationDir) {

        List<File> files = Arrays.stream(calibrationDir.listFiles()).collect(Collectors.toList());
        File calibration100File = files.stream()
                .filter(f -> f.getName().contains("deep_100"))
                .collect(Collectors.toList()).get(0);
        files.remove(calibration100File);

        if (calibration100File == null) {
            System.exit(0);
        }
        //convertFileToCollection
        Map<Integer, List<HodographObject>> freqHOMap
                = fileToCollections.convertCalibrationFileToMap(calibration100File);
        norm.normalizeWithMaxAmplitude(freqHOMap);

        return freqHOMap;
    }

    private static List<HodographObject> saveDataLimits(File limitsDir) {

        List<File> limitsFiles = Arrays.stream(limitsDir.listFiles()).collect(Collectors.toList());

        List<HodographObject> commonList = new ArrayList<>();
        for (File f : limitsFiles) {
            // в названии файла добавлена длина
            List<HodographObject> HOList = fileToCollections.convertFileToHOList(f);
            // anotherList.addAll(list) will also just add the references
            commonList.addAll(HOList);
        }
        return commonList;
    }

    private static List<List<HodographObject>> saveData(File dataModelDir) {

        List<File> dataModelFiles = Arrays.stream(dataModelDir.listFiles()).collect(Collectors.toList());

        List<List<HodographObject>> hodographsDifferentTypes = new ArrayList<>();

        for (File defectTypeDir : dataModelFiles) {

            if (defectTypeDir.isDirectory()) {
                List<File> dataTypeFiles = Arrays.stream(defectTypeDir.listFiles()).collect(Collectors.toList());

                List<HodographObject> commonDataList = new ArrayList<>();
                for (File f : dataTypeFiles) {
                    List<HodographObject> HOList = fileToCollections.convertFileToHOList(f);
                    commonDataList.addAll(HOList);
                }
                hodographsDifferentTypes.add(commonDataList);
            }
        }
        return hodographsDifferentTypes;
    }

    public static DefectTypes parseDirNameAndGetDefectType(String dirName) {
        switch (dirName) {
            case "poligon_30":
                return DefectTypes.PLGN_W_30;
            case "poligon_60":
                return DefectTypes.PLGN_W_60;
            case "poligon_45":
                return DefectTypes.PLGN_W_45;
            case "tri":
                return DefectTypes.TRI;
            case "rect":
                return DefectTypes.RECT;
        }
        return DefectTypes.UNKNOUWN_TYPE;
    }
}







