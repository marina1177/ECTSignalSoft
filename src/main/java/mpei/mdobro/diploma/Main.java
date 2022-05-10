package mpei.mdobro.diploma;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import mpei.mdobro.diploma.domain.calibrate.Normalization;
import mpei.mdobro.diploma.domain.parse.AlgorithmType;
import mpei.mdobro.diploma.domain.parse.DefectTypes;
import mpei.mdobro.diploma.domain.parse.FileToCollections;
import mpei.mdobro.diploma.domain.parse.HodographObject;
import mpei.mdobro.diploma.domain.print.PaintPlots;

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
        File calibrationDir = new File(CALIBRATION_DIR);

        List<File> files = Arrays.stream(calibrationDir.listFiles()).collect(Collectors.toList());
        File calibration100File = files.stream()
                .filter(f -> f.getName().contains("deep_100"))
                .collect(Collectors.toList()).get(0);
        files.remove(calibration100File);

        if (calibration100File != null) {
            //convertFileToCollection
            Map<Integer, List<HodographObject>> freqHOMap
                    = fileToCollections.convertCalibrationFileToMap(calibration100File);
            norm.normalizeWithMaxAmplitude(freqHOMap);

            PaintPlots painter = new PaintPlots(freqHOMap);

            //painter.plotHodographs();

            //=======================================LIMITS==============================

            File limitsDir = new File(LIMITS_DIR);
            List<File> limitsFiles = Arrays.stream(limitsDir.listFiles()).collect(Collectors.toList());

            List<HodographObject> commonList = new ArrayList<>();
            for (File f : limitsFiles) {
                // в названии файла добавлена длина
                List<HodographObject> HOList = fileToCollections.convertFileToHOList(f);
                // anotherList.addAll(list) will also just add the references
                commonList.addAll(HOList);
            }

            Map<Integer, Map<Integer, List<HodographObject>>> freqToDeepAndLengthAngleList
                    = fileToCollections.convertCommonListToLimitsCurvesMap(commonList, AlgorithmType.MAX_AMPLITUDE);

            painter.setFreqToDeepAndLengthAngleLimitList(freqToDeepAndLengthAngleList);
            //painter.plotPhaseLengthCurves();

            //plot 5 limits curves for 3 frequencies
            // https://stackoverflow.com/questions/38931111/how-to-make-plots-in-java-like-in-matlab-same-syntax


            //=======================================DATA==============================
//
            File dataModelDir = new File(DATA_DIR);
            List<File> dataModelFiles = Arrays.stream(dataModelDir.listFiles()).collect(Collectors.toList());

            List<List<HodographObject>> hodographsDifferentTypes = new ArrayList<>();
            for (File defectTypeDir : dataModelFiles) {
                DefectTypes type = parseDirNameAndGetDefectType(defectTypeDir.getName());

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
            //System.out.println("Saved hodographs!");
            Map<Integer, Map<Integer, List<HodographObject>>> freqToDeepAndLengthAngleModelList
                    = fileToCollections.convertCommonDataListToCommonFreqMap(
                            hodographsDifferentTypes, AlgorithmType.MAX_AMPLITUDE);



            // разместить данные на LIMIT графике, подписать точки (tri/rect/pol_% - deep=%)
            painter.setFreqToDeepAndLengthAngleModelList(freqToDeepAndLengthAngleModelList);
            painter.plotModelDataAmongLimits();
        }
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







