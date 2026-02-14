package in.lekhai.csv.upload.factory;

import in.lekhai.csv.upload.model.CsvUploadTypes;
import in.lekhai.csv.upload.service.CsvUploadService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class CsvUploadFactory {

    Map<CsvUploadTypes, CsvUploadService<?,?>> serviceMap;

    public CsvUploadFactory(List<CsvUploadService<?,?>> services) {
        this.serviceMap = services
                .stream()
                .collect(Collectors.toUnmodifiableMap(CsvUploadService::getType, Function.identity()));
    }

    public CsvUploadService<?,?> get(CsvUploadTypes uploadTypes) {
        return serviceMap.get(uploadTypes);
    }
}
