package fr.cnes.sonar.report.exporters.xlsx;

import fr.cnes.sonar.report.exceptions.BadExportationDataTypeException;
import fr.cnes.sonar.report.exporters.IExporter;
import fr.cnes.sonar.report.model.Report;
import fr.cnes.sonar.report.utils.StringManager;

import org.apache.poi.ss.usermodel.DataConsolidateFunction;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFTable;
import org.apache.poi.xssf.usermodel.XSSFPivotTable;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.ss.SpreadsheetVersion;


import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Exports the report in .docx format
 */
public class XlsXExporter implements IExporter {

    /** Logger for XlsXExporter. */
    private static final Logger LOGGER = Logger.getLogger(StringManager.class.getCanonicalName());
    /**
     * Name of the tab containing formatted issues
     */
    private static final String ISSUES_SHEET_NAME = "Issues";
    /**
     * Name of the tab containing unconfirmed issues
     */
    private static final String UNCONFIRMED_SHEET_NAME = "Unconfirmed";
    /**
     *  Name of the tab containing all detailed issues
     */
    private static final String ALL_DETAILS_SHEET_NAME = "ISSUES";
    /**
     * Name for the table containing selected resources
     */
    private static final String SELECTED_TABLE_NAME = "selected";
    /**
     * Name for the table containing unconfirmed resources
     */
    private static final String UNCONFIRMED_TABLE_NAME = "unconfirmed";
    /**
     * Name for the table containing all raw resources
     */
    private static final String ALL_TABLE_NAME = "issues";  
    /**
     * Name for the tab containing metrics
     */
    private static final String METRICS_SHEET_NAME = "Metrics";
    /**
     * Name for the table containing metrics
     */
    private static final String METRICS_TABLE_NAME = "metrics";
    /**
     * Name for the sheet containing all hotspots
     */
    private static final String ALL_HOTSPOTS_SHEET_NAME = "HOTSPOTS";
    /**
     * Name for the table containing all raw resources
     */
    private static final String HOTSPOT_TABLE_NAME = "hotspots";

      /**
     * Name for the sheet containing the summary
     */
    private static final String SUMMARY_SHEET_NAME = "SUMMARY";

    /**
     * Overridden export for XlsX
     * @param data Data to export as Report
     * @param path Path where to export the file
     * @param filename Name of the template file
     * @return Generated file.
     * @throws BadExportationDataTypeException ...
     * @throws IOException when reading a file
     */
    @Override
    public File export(Object data, String path, String filename)
            throws BadExportationDataTypeException, IOException {
        // check resources type
        if(!(data instanceof Report)) {
            throw new BadExportationDataTypeException();
        }
        // resources casting
        final Report report = (Report) data;

        // set output filename
        final String outputFilePath = path;

        // open excel file from the path given in the parameters
        final File file = new File(filename);

        // Check if template file exists
        if(!file.exists() && !filename.isEmpty()) {
            LOGGER.log(Level.WARNING, "Unable to find provided XLSX template file (using default one instead) : " + file.getAbsolutePath());
        }

        // open the template
        try(
            InputStream excelFile = file.exists() ?
                    new FileInputStream(file) : getClass().getResourceAsStream("/template/issues-template.xlsx");
            Workbook workbook = new XSSFWorkbook(excelFile);
            FileOutputStream fileOut = new FileOutputStream(outputFilePath)
        ) {

            // retrieve the sheet aiming to contain selected resources
            //final XSSFSheet selectedSheet = (XSSFSheet) workbook.getSheet(ISSUES_SHEET_NAME);

            // retrieve the sheet aiming to contain selected resources
            //final XSSFSheet unconfirmedSheet = (XSSFSheet) workbook.getSheet(UNCONFIRMED_SHEET_NAME);

            // retrieve the sheet aiming to contain selected resources
            final XSSFSheet allDataSheet = (XSSFSheet) workbook.getSheet(ALL_DETAILS_SHEET_NAME);

            // retrieve the sheet aiming to contain hotspots resources
            final XSSFSheet allHotSheet = (XSSFSheet) workbook.getSheet(ALL_HOTSPOTS_SHEET_NAME);

            // retrieve the sheet with metrics
            //final XSSFSheet metricsSheet = (XSSFSheet) workbook.getSheet(METRICS_SHEET_NAME);

            // write selected resources in the file
            //XlsXTools.addSelectedData(report.getIssues(), selectedSheet, SELECTED_TABLE_NAME);

            // write selected resources in the file
            //XlsXTools.addSelectedData(report.getUnconfirmed(), unconfirmedSheet, UNCONFIRMED_TABLE_NAME);

            // write all raw resources in the third sheet
            XlsXTools.addListOfMap(allDataSheet, report.getRawIssues(), ALL_TABLE_NAME);

            // write all metrics in the metric sheet
            //XlsXTools.addListOfMap(metricsSheet, report.getComponents(), METRICS_TABLE_NAME);

            // write all raw resources in the hotspot sheet
            XlsXTools.addListOfMap(allHotSheet, report.getRawHotspots(), HOTSPOT_TABLE_NAME);

            // write summary sheet
            XSSFSheet summarySheet = (XSSFSheet) workbook.createSheet(SUMMARY_SHEET_NAME);
            
            // Label Pivot Tables 
            XSSFRow row  = summarySheet.createRow(0);
            row.createCell(0).setCellValue("ISSUES");
            row.createCell(3).setCellValue("HOTSPOTS");

            // Get the reference for Pivot Data 
            XSSFTable table = XlsXTools.findTableByName(allDataSheet, ALL_TABLE_NAME);

            // Create Pivot Table only if there are findings 
            if (report.getRawIssues().size() > 0) {
                XSSFPivotTable pivotTableIssues = summarySheet.createPivotTable(table,  new CellReference("A2"));

                pivotTableIssues.addColumnLabel(DataConsolidateFunction.COUNT, 0);

                pivotTableIssues.addRowLabel(20);
                //pivotTableIssues.addRowLabel(10);
                //pivotTableIssues.getCTPivotTableDefinition().getPivotFields().getPivotFieldArray(10).setDataField(true);
                pivotTableIssues.addRowLabel(0);
                pivotTableIssues.getCTPivotTableDefinition().getPivotFields().getPivotFieldArray(0).setDataField(true);
            } else {
                row.createCell(1).setCellValue("NO FINDINGS!!!");
            }

            // Get the reference for Pivot Data 
            table = XlsXTools.findTableByName(allHotSheet, HOTSPOT_TABLE_NAME);
        
            // Create Pivot Table only if there are findings
            if (report.getRawHotspots().size() > 0) {       
                XSSFPivotTable pivotTableHot = summarySheet.createPivotTable(table,  new CellReference("D2"));

                pivotTableHot.addColumnLabel(DataConsolidateFunction.COUNT, 1);
                pivotTableHot.addRowLabel(1);
                pivotTableHot.addRowLabel(7);

                pivotTableHot.getCTPivotTableDefinition().getPivotFields().getPivotFieldArray(1).setDataField(true);
            } else {
                row.createCell(4).setCellValue("NO FINDINGS!!!");
            }

            // write output as file
            workbook.write(fileOut);
        }

        return new File(outputFilePath);
    }
}
