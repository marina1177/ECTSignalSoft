package mpei.mdobro.diploma;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import mpei.mdobro.diploma.domain.calibrate.Normalization;
import mpei.mdobro.diploma.domain.parse.AlgorithmType;
import mpei.mdobro.diploma.domain.parse.FileToCollections;
import mpei.mdobro.diploma.domain.parse.HodographObject;
import mpei.mdobro.diploma.domain.print.Drawer;
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

    private static FileToCollections fileToCollections = new FileToCollections();
    private static Normalization norm = new Normalization();

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
            painter.plotHodographs();
        }

//        for (File f : files) {
//            HashMap<Integer, List<HodographObject>> freqHOMap
//                    = fileToCollections.convertFileToCollection(f);
//
//            norm.normalizeHodographsAcordingToThroughSignal(freqHOMap);
////                printPlots
////            PaintPlots painter = new PaintPlots(freqHOMap);
////            painter.plotHodographs();
//        }


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

        //norm.normalizeHOListAccordingToAlgorithm(commonList, AlgorithmType.FIRST_IM_MAX);

        // общая карта для отрисовки и анализа
        // каждый отдельный годограф внутри нужно сдвинуть относительно нуля -> опять norm. ???
//        Map<Double, Map<Integer, Map<Integer, List<HodographObject>>>> lengthToFreqToDeepAndHodograph
//                = fileToCollections.convertCommonListToCommonMap(commonList);



        Drawer drawer = new Drawer();
        drawer.simpleChart2();
        //drawLimitsCurves(lengthToFreqToDeepAndHodograph);
//        for (var entryLength : lengthToFreqToDeepAndHodograph.entrySet()) {
//            System.out.println("LIMITS\nLength: " + entryLength.getKey() + "[mm] ->");
//            for (var freqToDeepAndHodograph : entryLength.getValue().entrySet()) {
//                System.out.println("\t\tfreq: " + freqToDeepAndHodograph.getKey() + " [kHz] ->");
//                for (var deepAndHodograph : freqToDeepAndHodograph.getValue().entrySet()) {
//                    System.out.println("\t\t\tdeep: " + deepAndHodograph.getKey() + "[%]"
//                    + " -> " + deepAndHodograph.getValue().size() + " [entities]");
//
//                    //везде ли есть точка максимума?
////                    HodographObject ob = deepAndHodograph.getValue()
////                            .stream().filter(ho -> ho.firstImMax == true)
////                            .collect(toSingleton());
////                    System.out.println("\t\t\tArg: " + ob.getComplexNumber().getArgument()
////                            + "\n"
////                            + "\t\t\tAbs:" + ob.getComplexNumber().abs());
//                    //print
//                }
//            }
//
//        }

        //plot 5 limits curves for 3 frequencies
        // https://stackoverflow.com/questions/38931111/how-to-make-plots-in-java-like-in-matlab-same-syntax


        //=======================================DATA==============================

//        File dataDir = new File(DATA_DIR);
//        List<File> dataFiles = Arrays.stream(dataDir.listFiles()).collect(Collectors.toList());

    }
}







