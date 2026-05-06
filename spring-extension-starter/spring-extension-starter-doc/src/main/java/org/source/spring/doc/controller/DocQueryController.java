package org.source.spring.doc.controller;

import lombok.RequiredArgsConstructor;
import org.source.spring.doc.domain.repository.ObjectRepository;
import org.source.spring.doc.domain.repository.DocRepository;
import org.source.spring.doc.domain.repository.RelationRepository;
import org.source.spring.doc.domain.entity.ObjectEntity;
import org.source.spring.doc.domain.entity.DocEntity;
import org.source.spring.doc.domain.entity.RelationEntity;
import org.source.spring.io.Output;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 文档查询控制器
 * <p>
 * 提供文档解析结果的 REST API 查询接口：
 * <ul>
 *     <li>GET /doc/objects - 获取所有文档对象</li>
 *     <li>GET /doc/element/{id} - 按 ID 获取节点</li>
 *     <li>GET /doc/elements/type/{type} - 按类型查询</li>
 *     <li>GET /doc/search - 关键词搜索</li>
 * </ul>
 * </p>
 *
 * @author dao1230source
 * @since 1.0.0
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/doc")
public class DocQueryController {

    private final ObjectRepository objectRepository;
    private final DocRepository objectBodyRepository;
    private final RelationRepository relationRepository;

    /**
     * 获取所有文档对象列表
     *
     * @return 文档对象列表
     */
    @GetMapping("/objects")
    public Output<List<ObjectEntity>> getAllObjects() {
        return Output.success(objectRepository.findAll());
    }

    /**
     * 按 ID 获取文档对象
     *
     * @param objectId 对象 ID
     * @return 文档对象
     */
    @GetMapping("/element/{objectId}")
    public Output<ObjectEntity> getElementById(@PathVariable String objectId) {
        Optional<ObjectEntity> object = objectRepository.findByObjectId(objectId);
        if (object.isPresent()) {
            return Output.success(object.get());
        }
        return Output.success(null);
    }

    /**
     * 按类型获取文档对象列表
     *
     * @param type 对象类型编码
     * @return 文档对象列表
     */
    @GetMapping("/elements/type/{type}")
    public Output<List<ObjectEntity>> getElementsByType(@PathVariable Integer type) {
        List<ObjectEntity> objects = objectRepository.findByType(type);
        return Output.success(objects);
    }

    /**
     * 搜索文档对象
     *
     * @param keyword 关键词
     * @return 匹配的文档对象列表
     */
    @GetMapping("/search")
    public Output<List<ObjectEntity>> searchElements(@RequestParam String keyword) {
        List<ObjectEntity> objects = objectRepository.findByObjectIdContaining(keyword);
        return Output.success(objects);
    }

    /**
     * 获取文档对象的详细内容
     *
     * @param objectId 对象 ID
     * @return 文档对象详细内容
     */
    @GetMapping("/element/{objectId}/body")
    public Output<DocEntity> getElementBody(@PathVariable String objectId) {
        DocEntity body = objectBodyRepository.findByObjectId(objectId);
        return Output.success(body);
    }

    /**
     * 获取文档对象的子元素
     *
     * @param objectId 父对象 ID
     * @return 子元素列表
     */
    @GetMapping("/element/{objectId}/children")
    public Output<List<ObjectEntity>> getChildren(@PathVariable String objectId) {
        List<RelationEntity> relations = relationRepository.findByParentObjectId(objectId);
        List<String> childIds = relations.stream()
                .map(RelationEntity::getObjectId)
                .collect(Collectors.toList());
        List<ObjectEntity> children = objectRepository.findAll().stream()
                .filter(obj -> childIds.contains(obj.getObjectId()))
                .collect(Collectors.toList());
        return Output.success(children);
    }

    /**
     * 获取统计数据
     *
     * @return 统计数据
     */
    @GetMapping("/stats")
    public Output<DocStats> getStats() {
        long totalCount = objectRepository.count();
        DocStats stats = new DocStats();
        stats.totalCount = totalCount;
        stats.classCount = objectRepository.findByType(1).size();
        stats.methodCount = objectRepository.findByType(2).size();
        stats.variableCount = objectRepository.findByType(3).size() +
                objectRepository.findByType(4).size();
        stats.endpointCount = objectRepository.findByType(7).size();
        return Output.success(stats);
    }

    /**
     * 统计数据结构
     */
    @lombok.Data
    public static class DocStats {
        private long totalCount;
        private long classCount;
        private long methodCount;
        private long variableCount;
        private long endpointCount;
    }
}