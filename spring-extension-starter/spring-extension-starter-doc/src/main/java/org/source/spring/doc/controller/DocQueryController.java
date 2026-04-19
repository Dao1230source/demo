package org.source.spring.doc.controller;

import lombok.RequiredArgsConstructor;
import org.source.spring.doc.domain.repository.DocObjectRepository;
import org.source.spring.doc.domain.repository.DocObjectBodyRepository;
import org.source.spring.doc.domain.entity.DocObjectEntity;
import org.source.spring.doc.domain.entity.DocObjectBodyEntity;
import org.source.spring.io.Output;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

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

    private final DocObjectRepository objectRepository;
    private final DocObjectBodyRepository objectBodyRepository;

    /**
     * 获取所有文档对象列表
     *
     * @return 文档对象列表
     */
    @GetMapping("/objects")
    public Output<List<DocObjectEntity>> getAllObjects() {
        return Output.success(objectRepository.findAll());
    }

    /**
     * 按 ID 获取文档对象
     *
     * @param objectId 对象 ID
     * @return 文档对象
     */
    @GetMapping("/element/{objectId}")
    public Output<DocObjectEntity> getElementById(@PathVariable String objectId) {
        Optional<DocObjectEntity> object = objectRepository.findByObjectId(objectId);
        if (object.isPresent()) {
            return Output.success(object.get());
        }
        return Output.success(null);
    }

    /**
     * 按类型获取文档对象列表
     *
     * @param objectType 对象类型编码
     * @return 文档对象列表
     */
    @GetMapping("/elements/type/{objectType}")
    public Output<List<DocObjectEntity>> getElementsByType(@PathVariable Integer objectType) {
        List<DocObjectEntity> objects = objectRepository.findByObjectType(objectType);
        return Output.success(objects);
    }

    /**
     * 搜索文档对象
     *
     * @param keyword 关键词
     * @return 匹配的文档对象列表
     */
    @GetMapping("/search")
    public Output<List<DocObjectEntity>> searchElements(@RequestParam String keyword) {
        List<DocObjectEntity> objects = objectRepository.findByObjectIdContaining(keyword);
        return Output.success(objects);
    }

    /**
     * 获取文档对象的详细内容
     *
     * @param objectId 对象 ID
     * @return 文档对象详细内容
     */
    @GetMapping("/element/{objectId}/body")
    public Output<DocObjectBodyEntity> getElementBody(@PathVariable String objectId) {
        DocObjectBodyEntity body = objectBodyRepository.findByObjectId(objectId);
        return Output.success(body);
    }

    /**
     * 获取文档对象的子元素
     *
     * @param objectId 父对象 ID
     * @return 子元素列表
     */
    @GetMapping("/element/{objectId}/children")
    public Output<List<DocObjectEntity>> getChildren(@PathVariable String objectId) {
        List<DocObjectEntity> children = objectRepository.findByParentId(objectId);
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
        stats.classCount = objectRepository.findByObjectType(1).size();
        stats.methodCount = objectRepository.findByObjectType(2).size();
        stats.variableCount = objectRepository.findByObjectType(3).size() +
                objectRepository.findByObjectType(4).size();
        stats.endpointCount = objectRepository.findByObjectType(7).size();
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