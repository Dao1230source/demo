package org.source.spring.doc.domain.object;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.source.spring.doc.SpringExtensionStarterDocApplication;
import org.source.spring.doc.domain.entity.DocEntity;
import org.source.spring.doc.domain.entity.RelationEntity;
import org.source.spring.doc.domain.repository.DocRepository;
import org.source.spring.doc.domain.repository.RelationRepository;
import org.source.spring.doc.domain.value.ClassDocData;
import org.source.spring.doc.domain.value.MethodDocData;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DocObjectProcessor 单元测试
 * <p>
 * 验证 DocEntity 和 RelationEntity 入库结果
 * </p>
 */
@Slf4j
@SpringBootTest(classes = SpringExtensionStarterDocApplication.class)
class DocObjectProcessorTest {

    @Resource
    private DocObjectProcessor processor;

    @Resource
    private DocRepository docRepository;

    @Resource
    private RelationRepository relationRepository;

    private String uniqueSuffix;

    @BeforeEach
    void setUp() {
        uniqueSuffix = UUID.randomUUID().toString().substring(0, 8);
    }

    @Test
    void testSaveClassDocValue() {
        String classId = "com.example.TestClassForSave_" + uniqueSuffix;
        String parentId = "test-module-path_" + uniqueSuffix;

        ClassDocData value = new ClassDocData();
        value.setObjectId(classId);  // 设置 objectId，与 name 保持一致
        value.setName(classId);
        value.setParentObjectId(parentId);  // 设置 parentObjectId，与 parentName 保持一致
        value.setParentName(parentId);
        value.setRelationType(DocObjectTypeEnum.CLASS.getType());
        value.setSorted("0");
        value.setClassName("TestClassForSave");
        value.setDocContent("测试类文档");

        processor.save(List.of(value));

        // 验证 DocEntity
        DocEntity entity = docRepository.findByName(classId);
        assertNotNull(entity, "DocEntity 应该存在");
        assertEquals(classId, entity.getName(), "DocEntity.name 应该等于 classId");
        assertEquals(parentId, entity.getParentName(), "DocEntity.parentName 应该等于 parentId");
        assertNotNull(entity.getValue(), "DocEntity.value 应该有值");

        log.info("DocEntity: objectId={}, name={}, parentName={}",
                entity.getObjectId(), entity.getName(), entity.getParentName());
    }

    @Test
    void testSaveMethodDocValue() {
        String methodId = "com.example.TestClassForMethod_" + uniqueSuffix + "#testMethod(String,int)";
        String parentId = "com.example.TestClassForMethod_" + uniqueSuffix;

        MethodDocData value = new MethodDocData();
        value.setObjectId(methodId);  // 设置 objectId，与 name 保持一致
        value.setName(methodId);
        value.setParentObjectId(parentId);  // 设置 parentObjectId，与 parentName 保持一致
        value.setParentName(parentId);
        value.setRelationType(DocObjectTypeEnum.METHOD.getType());
        value.setSorted("0");
        value.setMethodName("testMethod");
        value.setReturnType("void");
        value.setDocContent("测试方法文档");

        processor.save(List.of(value));

        // 验证 DocEntity
        DocEntity entity = docRepository.findByName(methodId);
        assertNotNull(entity, "DocEntity 应该存在");
        assertEquals(methodId, entity.getName(), "DocEntity.name 应该等于 methodId");
        assertEquals(parentId, entity.getParentName(), "DocEntity.parentName 应该等于 parentId");

        // 注意：单独保存 MethodDocValue 时，父节点不在 tree 中，所以没有 RelationEntity
        // RelationEntity 只在父节点也存在于同一个 tree 时才会生成

        log.info("DocEntity: objectId={}, name={}, parentName={}",
                entity.getObjectId(), entity.getName(), entity.getParentName());
    }

    @Test
    void testSaveClassAndMethodTogether() {
        String classId = "com.example.TestClassWithMethod_" + uniqueSuffix;
        String modulePath = "test-module-combined_" + uniqueSuffix;
        String methodId = classId + "#combinedMethod()";

        ClassDocData classValue = new ClassDocData();
        classValue.setObjectId(classId);  // 设置 objectId，与 name 保持一致
        classValue.setName(classId);
        classValue.setParentObjectId(modulePath);  // 设置 parentObjectId，与 parentName 保持一致
        classValue.setParentName(modulePath);
        classValue.setRelationType(DocObjectTypeEnum.CLASS.getType());
        classValue.setSorted("0");
        classValue.setClassName("TestClassWithMethod");

        MethodDocData methodValue = new MethodDocData();
        methodValue.setObjectId(methodId);  // 设置 objectId，与 name 保持一致
        methodValue.setName(methodId);
        methodValue.setParentObjectId(classId);  // 设置 parentObjectId，与 parentName 保持一致
        methodValue.setParentName(classId);
        methodValue.setRelationType(DocObjectTypeEnum.METHOD.getType());
        methodValue.setSorted("0");
        methodValue.setMethodName("combinedMethod");

        processor.save(List.of(classValue, methodValue));

        // 验证 Class DocEntity
        DocEntity classEntity = docRepository.findByName(classId);
        assertNotNull(classEntity);
        assertEquals(classId, classEntity.getName());
        assertEquals(modulePath, classEntity.getParentName());

        // 验证 Method DocEntity
        DocEntity methodEntity = docRepository.findByName(methodId);
        assertNotNull(methodEntity);
        assertEquals(methodId, methodEntity.getName());
        assertEquals(classId, methodEntity.getParentName());

        // 注意：Class 的父节点 modulePath 不在 tree 中，所以没有 Class -> Module RelationEntity
        // 只有 Method -> Class Relation（因为 classId 在 tree 中存在）

        // 验证 Method -> Class Relation
        List<RelationEntity> methodRelations = relationRepository.findByObjectId(methodId);
        assertFalse(methodRelations.isEmpty());
        assertEquals(classId, methodRelations.get(0).getParentObjectId());

        log.info("Class DocEntity: name={}, parentName={}", classEntity.getName(), classEntity.getParentName());
        log.info("Method DocEntity: name={}, parentName={}", methodEntity.getName(), methodEntity.getParentName());
        log.info("Method Relation: objectId={}, parentObjectId={}",
                methodRelations.get(0).getObjectId(), methodRelations.get(0).getParentObjectId());
    }
}