package in.lekhai.csv.upload.service;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import org.springframework.batch.item.ItemReader;
import org.springframework.core.io.Resource;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Iterator;

public class OpenCsvItemReader<T> implements ItemReader<T> {

    private final Iterator<T> iterator;

    public OpenCsvItemReader(Resource resource, Class<T> type) throws Exception {
        Reader reader = new InputStreamReader(resource.getInputStream());

        CsvToBean<T> csvToBean = new CsvToBeanBuilder<T>(reader)
                .withType(type)
                .withIgnoreLeadingWhiteSpace(true)
                .withIgnoreEmptyLine(true)
                .build();

        //.withThrowExceptions(false)

        this.iterator = csvToBean.iterator();
    }

    @Override
    public T read() {
        return iterator.hasNext() ? iterator.next() : null;
    }
}