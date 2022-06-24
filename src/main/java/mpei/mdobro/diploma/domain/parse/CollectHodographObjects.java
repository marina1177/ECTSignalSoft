package mpei.mdobro.diploma.domain.parse;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.complex.Complex;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.*;

@Slf4j
@Data
@RequiredArgsConstructor
public class CollectHodographObjects {

    private final File fileName;
    private FileNameParser fileNameParser;

    public List<NDTDataObject> getListOfDataObjects(XSSFSheet sheet, XSSFWorkbook workbook) throws ParseException {

        fileNameParser = new FileNameParser(fileName);
        DataFormatter formatter = new DataFormatter();
        FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
        DataFormatter dataFormatter = new DataFormatter(new java.util.Locale("en", "US"));
        //dataFormatter.setDefaultNumberFormat();
        String cellValue = "";

        List<NDTDataObject> ndtDataObjects = new ArrayList<>();
        Map<Object, Integer> titleIndex = new HashMap<>();

        for (Row row : sheet) {
            //check if row is empty
            Cell c = row.getCell(0);
            if (c == null || c.getCellType() == CellType.BLANK) {
                break;
            }
            int cellCount = 0;

            List<Double> cellsValues = new ArrayList<>();
            for (Cell cell : row) {
                cellValue = formatter.formatCellValue(cell, evaluator);
                if (row.getRowNum() == 0) {
                    Map<Object, Integer> title = saveNDTDataTiteles(cellValue, cellCount);
                    titleIndex.putAll(title);
                } else if (cell.getCellType() == CellType.FORMULA) {
                        switch (cell.getCachedFormulaResultType()) {
                            case NUMERIC: { // from apache poi 5.2.0 on
                                cellsValues.add(Double.valueOf(cell.getNumericCellValue()));
                                break;
                            }
                        }
                    } else if (cell.getCellType() == CellType.NUMERIC) {
                        String value = dataFormatter.formatCellValue(cell); // from apache poi 5.2.0 on
                        cellsValues.add(Double.parseDouble(value));
                    }
                cellCount++;
            }
            if (!cellsValues.isEmpty()) {

                Integer deep = 0;
                if (titleIndex.containsKey("deep[%]")) {
                    Double deepDouble = cellsValues.get(titleIndex.get("deep[%]")).doubleValue() * 100;
                    deep = deepDouble.intValue();
                } else {
                    deep = fileNameParser.getDefectDeep();
                }

                NDTDataObject ndtDataObject = new NDTDataObject(
                        fileNameParser.getDefectType(),
                        //cellsValues.get(titleIndex.get("description")).toString(),
                        deep,
                        cellsValues.get(titleIndex.get("frequency[kHz]")).intValue(),
                        cellsValues.get(titleIndex.get("Amplitude[V]")).doubleValue(),
                        cellsValues.get(titleIndex.get("Phase [deg]")).doubleValue(),
                        cellsValues.get(titleIndex.get("rotated phase [deg]")).doubleValue(),
                        cellsValues.get(titleIndex.get("number of experiment")).intValue()
                );

                ndtDataObjects.add(ndtDataObject);
            }
        }
        return ndtDataObjects;
    }

    public static boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false;
        }
        boolean numeric = true;

        numeric = strNum.matches("-?\\d+(\\.\\d+)?");
        return  numeric;

//        try {
//            double d = Double.parseDouble(strNum);
//        } catch (NumberFormatException nfe) {
//            return false;
//        }
//        return true;
    }

    public static boolean isNumeric2(String str) {
        return str.matches("-?\\d+(\\.\\d+)?");  //match a number with optional '-' and decimal.
    }

    public List<HodographObject> getListOfHodographObjects(XSSFSheet sheet, XSSFWorkbook workbook) throws ParseException {

        fileNameParser = new FileNameParser(fileName);
        DataFormatter formatter = new DataFormatter();
        FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
        String cellValue = "";

        List<HodographObject> HOList = new ArrayList<>();
        Map<Object, Integer> titleIndex = new HashMap<>();

        for (Row row : sheet) {
            //check if row is empty
            Cell c = row.getCell(0);
            if (c == null || c.getCellType() == CellType.BLANK) {
                break;
            }
//            log.debug("rowNumber:{}", row.getRowNum());
            int cellCount = 0;

            List<Double> cellsValues = new ArrayList<>();
            for (Cell cell : row) {
                // вернуть значение ячейки так, как оно показано в документе Excel.
                cellValue = formatter.formatCellValue(cell, evaluator);
                if (row.getRowNum() == 0) {
                    //save column Name
                    if (cellValue.contains("freq"))
                        titleIndex.put("frequency[kHz]", cellCount);
                    else if (cellValue.contains("disp"))
                        titleIndex.put("defect_disp[mm]", cellCount);
                    else if (cellValue.contains("deep"))
                        titleIndex.put("deep[%]", cellCount);
                    else if (cellValue.contains("Im"))
                        titleIndex.put("Im[V]", cellCount);
                    else if (cellValue.contains("Re"))
                        titleIndex.put("Re[V]", cellCount);
                    else if (cellValue.contains("Amp"))
                        titleIndex.put("Amplitude[V]", cellCount);
                    else if (cellValue.contains("length"))
                        titleIndex.put("defect_length[mm]", cellCount);
                } else {
                    NumberFormat format = NumberFormat.getInstance(Locale.FRANCE);
                    Number number = format.parse(cellValue);
                    cellsValues.add(number.doubleValue());
                }
//                log.debug("cellValue: [{}] {}", cell.getAddress(), cellValue);
                cellCount++;
            }

            if (!cellsValues.isEmpty()) {

                Integer deep = 0;
                if (titleIndex.containsKey("deep[%]")) {
                    Double deepDouble = cellsValues.get(titleIndex.get("deep[%]")).doubleValue() * 100;
                    deep = deepDouble.intValue();
                } else {
                    deep = fileNameParser.getDefectDeep();
                }
                //если дефект не калибровочный типа - должна быть указана длина в названии
                Double length = fileNameParser.getDefectLength();
                if (length != 0.0 && titleIndex.containsKey("defect_length[mm]")) {
                    length = cellsValues.get(titleIndex.get("defect_length[mm]")).doubleValue();
                    if (length < 1) {
                        length = length * 1000;
                    }
                }

                HodographObject hodographObject = new HodographObject(
                        fileNameParser.getDefectType(),
                        deep,
                        cellsValues.get(titleIndex.get("frequency[kHz]")).intValue());

                hodographObject.setDisplacement(cellsValues.get(titleIndex.get("defect_disp[mm]")));

                Complex newComplex = new Complex(cellsValues.get(titleIndex.get("Re[V]")),
                        cellsValues.get(titleIndex.get("Im[V]")));
                hodographObject.setComplexNumber(newComplex);

                if (!hodographObject.getTypeDefect().equals(DefectTypes.CALIBRATION)) {
                    hodographObject.setDefectLength(length);
                }

                HOList.add(hodographObject);
            }
        }
        return HOList;
    }

    private Map<Object, Integer> saveNDTDataTiteles(String cellValue, int cellCount) {

        Map<Object, Integer> titleIndex = new HashMap<>();
        if (cellValue.contains("freq"))
            titleIndex.put("frequency[kHz]", cellCount);
        else if (cellValue.contains("deep"))
            titleIndex.put("deep[%]", cellCount);
        else if (cellValue.contains("Amp"))
            titleIndex.put("Amplitude[V]", cellCount);
        else if (cellValue.contains("Rotate"))
            titleIndex.put("rotated phase [deg]", cellCount);
        else if (cellValue.contains("Phase"))
            titleIndex.put("Phase [deg]", cellCount);
        else if (cellValue.contains("Type"))
            titleIndex.put("Type", cellCount);
        else if (cellValue.contains("description"))
            titleIndex.put("description", cellCount);
        else if (cellValue.contains("Number"))
            titleIndex.put("number of experiment", cellCount);

        return titleIndex;
    }

}
