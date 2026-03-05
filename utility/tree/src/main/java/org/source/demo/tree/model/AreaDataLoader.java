package org.source.demo.tree.model;

import com.opencsv.CSVReader;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

/**
* 从CSV.GZ文件加载行政区划数据
*/
public class AreaDataLoader {

    private static final String DATA_FILE = "/area_code_2024.csv.gz";

    /**
     * 加载所有行政区划数据
     */
    public static List<AreaElement> loadAllAreas() {
        List<AreaElement> areas = new ArrayList<>();
        try (InputStream is = AreaDataLoader.class.getResourceAsStream(DATA_FILE);
             GZIPInputStream gis = new GZIPInputStream(is);
             InputStreamReader reader = new InputStreamReader(gis);
             CSVReader csvReader = new CSVReader(reader)) {

            String[] line;
            while ((line = csvReader.readNext()) != null) {
                if (line.length >= 5) {
                    AreaElement area = new AreaElement(
                            // id
                            line[0].trim(),
                            // name
                            line[1].trim(),
                            // level
                            Integer.parseInt(line[2].trim()),
                            // parentId
                            line[3].isEmpty() ? null : line[3].trim(),
                            // reserved
                            Integer.parseInt(line[4].trim())
                    );
                    areas.add(area);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load area data", e);
        }
        return areas;
    }

    /**
     * 加载前N条数据
     */
    public static List<AreaElement> loadAreas(int limit) {
        List<AreaElement> allAreas = loadAllAreas();
        return allAreas.stream().limit(limit).toList();
    }

    /**
     * 获取总数据量
     */
    public static int getTotalCount() {
        return loadAllAreas().size();
    }
}