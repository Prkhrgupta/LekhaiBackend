package in.lekhai.common.excel;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.util.Comparator;
import java.util.List;

@Service
public class ExcelExporter {

    public <T> byte[] export(List<T> data, Class<T> clazz) {

        return export(data, clazz, null);
    }

    public <T> byte[] export(
            List<T> data,
            Class<T> clazz,
            WorkbookCustomizer customizer
    ) {

        try (
                Workbook workbook = new XSSFWorkbook();
                ByteArrayOutputStream out = new ByteArrayOutputStream()
        ) {

            Sheet sheet = workbook.createSheet("Data");

            List<Field> fields = List.of(clazz.getDeclaredFields())
                    .stream()
                    .filter(field ->
                            field.isAnnotationPresent(ExcelColumn.class)
                    )
                    .sorted(Comparator.comparingInt(field ->
                            field.getAnnotation(ExcelColumn.class).order()
                    ))
                    .toList();

            // ============================
            // Header Style
            // ============================

            Font headerFont = workbook.createFont();
            headerFont.setBold(true);

            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFont(headerFont);

            // ============================
            // Header Row
            // ============================

            Row headerRow = sheet.createRow(0);

            for (int i = 0; i < fields.size(); i++) {

                Field field = fields.get(i);

                ExcelColumn column =
                        field.getAnnotation(ExcelColumn.class);

                Cell cell = headerRow.createCell(i);

                cell.setCellValue(column.name());
                cell.setCellStyle(headerStyle);

                sheet.setColumnWidth(i, column.width());
            }

            // ============================
            // Data Rows
            // ============================

            int rowNum = 1;

            for (T item : data) {

                Row row = sheet.createRow(rowNum++);

                for (int col = 0; col < fields.size(); col++) {

                    Field field = fields.get(col);

                    field.setAccessible(true);

                    Object value = field.get(item);

                    Cell cell = row.createCell(col);

                    if (value == null) {
                        continue;
                    }

                    if (value instanceof Number number) {

                        cell.setCellValue(number.doubleValue());

                    } else if (value instanceof Boolean bool) {

                        cell.setCellValue(bool);

                    } else {

                        cell.setCellValue(value.toString());
                    }
                }
            }

            // ============================
            // Optional Customization
            // ============================

            if (customizer != null) {
                customizer.customize(workbook);
            }

            workbook.write(out);

            return out.toByteArray();

        } catch (Exception e) {

            throw new RuntimeException("Failed to export excel", e);
        }
    }

    public ResponseEntity<Resource> convertByteArrayToApiResponse(String fileName, byte[] bytes) {
        ByteArrayResource resource = new ByteArrayResource(bytes);

        return ResponseEntity.ok()
                .contentType(
                        MediaType.parseMediaType(
                                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                        )
                )
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + fileName + "\""
                )
                .contentLength(bytes.length)
                .body(resource);
    }
}