package in.lekhai.common.excel;

import org.apache.poi.ss.usermodel.Workbook;

@FunctionalInterface
public interface WorkbookCustomizer {

    void customize(Workbook workbook);
}