package org.source.demo.spring.assign;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.source.demo.spring.domain.entity.ClassEntity;
import org.source.demo.spring.domain.entity.StudentEntity;
import org.source.demo.spring.domain.entity.TeacherEntity;
import org.source.demo.spring.facade.AssignFacade;
import org.source.demo.spring.facade.data.StudentData;
import org.source.utility.assign.Assign;
import org.source.utility.assign.InterruptStrategyEnum;
import org.source.utility.utils.Streams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Slf4j
@SpringBootTest
class AssignTest {
    @Autowired
    private AssignFacade assignFacade;

    @Test
    public void assign() {
        List<StudentData> studentInfoList = assignFacade.getStudentDataList();
        Assign.build(studentInfoList)
                // fetch StudentEntity data by ids
                .addAcquire(assignFacade::getStudentList, StudentEntity::getId)
                // get id from studentInfoList
                .addAction(StudentData::getId)
                // assign name、age
                .addAssemble(StudentEntity::getName, StudentData::setName)
                .addAssemble(StudentEntity::getAge, StudentData::setAge)
                .backAcquire()
                .backAssign()
                // final must invoke
                .invoke();
        Map<Long, StudentData> studentInfoMap = Streams.toMap(studentInfoList, StudentData::getId);
        StudentData studentInfo1 = studentInfoMap.get(1L);
        Assertions.assertEquals("Jim", studentInfo1.getName());
    }

    @Test
    public void assignValue() {
        List<StudentData> studentInfoList = assignFacade.getStudentDataList();
        Assign.build(studentInfoList)
                // assignValue 先于 acquire 执行
                .addAssignValue(StudentData::setRemark, "student info")
                .addAssignValueIfAbsent(StudentData::getSchool, StudentData::setSchool, "Shenzhen Second School")
                .invoke();
        Map<Long, StudentData> studentInfoMap = Streams.toMap(studentInfoList, StudentData::getId);
        StudentData studentInfo1 = studentInfoMap.get(1L);
        Assertions.assertEquals("student info", studentInfo1.getRemark());
    }

    @Test
    void testAssign() {
        List<StudentData> studentInfoList = assignFacade.getStudentDataList();
        Assign<StudentData> sourceAssign = Assign.build(studentInfoList)
                // assignValue 先于 acquire 执行
                .addAssignValue(StudentData::setRemark, "student info")
                .addAssignValueIfAbsent(StudentData::getSchool, StudentData::setSchool, "Shenzhen Second School")
                // 启用线程池，并行执行 acquire，也可以指定 ExecutorService
                .parallel()
                // get StudentEntity list by ids
                .addAcquire(assignFacade::getStudentList, StudentEntity::getId)
                // 给 acquire 命名，并且启用本地缓存（启用cache必须设置name值），也可以自定义 Cache<K, T>
                .name(StudentEntity.class.getName()).cache()
                .addAction(StudentData::getId)
                // 赋值
                .addAssemble(StudentEntity::getName, StudentData::setName)
                .addAssemble(StudentEntity::getAge, StudentData::setAge)
                // 分批获取
                .backAcquire().batchSize(2)
                .afterProcessor((studentInfo, idStudentMap) -> {
                    if (!idStudentMap.containsKey(studentInfo.getId())) {
                        log.info("student id:{} 没有获取到对应数据，如果需要可以抛出异常", studentInfo.getId());
                    }
                })
                .exceptionHandler((studentInfo, throwable) -> log.info("如果 acquire fetcher 发生异常，在此处处理"))
                .backAssign()
                // get <teacherId,TeacherEntity> map by teacherIds
                .addAcquire(assignFacade::getTeacherMap)
                .name(TeacherEntity.class.getName()).cache()
                .addAction(StudentData::getTeacherId)
                .addAssemble(TeacherEntity::getName, StudentData::setTeacherName)
                .addAssemble(TeacherEntity::getClassId, StudentData::setClassId)
                .backAcquire().backAssign()
                // must invoke
                .invoke()
                // 添加分支，先通过 TeacherEntity 获取 classId，再获取 class 数据，这里只处理 classId = c2 的班级数据
                .addBranch(k -> "c2".equals(k.getClassId()))
                .addAcquire(assignFacade::getClassMap)
                .addAction(StudentData::getClassId)
                .addAssemble(ClassEntity::getName, StudentData::setClassName)
                .backAcquire().backAssign().invoke()
                // 返回最高层级
                .backSuperlative();
        List<StudentData> mainData = sourceAssign.getMainData2List();
        Map<Long, StudentData> studentInfoMap = Streams.toMap(mainData, StudentData::getId);
        StudentData studentInfo1 = studentInfoMap.get(1L);
        Assertions.assertEquals("Jim", studentInfo1.getName());
        Assertions.assertEquals("student info", studentInfo1.getRemark());
        // addAssignValueIfAbsent 字段无值时设置默认值
        Assertions.assertEquals("Shenzhen First School", studentInfo1.getSchool());
        Assertions.assertNull(studentInfo1.getClassName(), "only get class2 info");
        StudentData studentInfo2 = studentInfoMap.get(2L);
        Assertions.assertEquals("Wolf", studentInfo2.getTeacherName());
        Assertions.assertEquals("class2", studentInfo2.getClassName());
        StudentData studentInfo4 = studentInfoMap.get(4L);
        Assertions.assertNull(studentInfo4.getName());
        Assertions.assertEquals("Shenzhen Second School", studentInfo4.getSchool());
    }

    @Test
    void testAssignThrow() {
        List<StudentData> studentInfoListNo = testEndStrategyIfExcept(InterruptStrategyEnum.NO);
        StudentData studentInfo2No = studentInfoListNo.get(1);
        Assertions.assertNull(studentInfo2No.getName(), "name must be null,because fetch student data throw exception");
        Assertions.assertEquals("Wolf", studentInfo2No.getTeacherName());
        StudentData studentInfo3No = studentInfoListNo.get(2);
        Assertions.assertEquals("subAssign invoked", studentInfo3No.getRemark());
        List<StudentData> studentInfoListAny = testEndStrategyIfExcept(InterruptStrategyEnum.ANY);
        StudentData studentInfo2Any = studentInfoListAny.get(1);
        Assertions.assertNull(studentInfo2Any.getName(), "name must be null");
        Assertions.assertNull(studentInfo2Any.getTeacherName(), "teacherName must be null, branch not invoke");
    }

    public List<StudentData> testEndStrategyIfExcept(InterruptStrategyEnum interruptStrategyEnum) {
        List<StudentData> studentInfoList = assignFacade.getStudentDataList();
        return Assign.build(studentInfoList).name("main")
                .addSub(this::subAssign)
                // throw exception
                .addAcquire(assignFacade::getStudentListThrow, StudentEntity::getId)
                .addAction(StudentData::getId)
                .addAssemble(StudentEntity::getName, StudentData::setName)
                .addAssemble(StudentEntity::getAge, StudentData::setAge)
                .backAcquire()
                .exceptionHandler((studentInfo, throwable) -> log.info("acquire fetcher 发生异常"))
                .backAssign()
                // 发生异常时是否结束流程
                .interruptStrategy(interruptStrategyEnum)
                // not invoke
                .addBranch().name(interruptStrategyEnum.name())
                // 发生任何异常，停止执行分支
                .addAcquire(assignFacade::getTeacherMap)
                .name(TeacherEntity.class.getName()).cache()
                .addAction(StudentData::getTeacherId)
                .addAssemble(TeacherEntity::getName, StudentData::setTeacherName)
                .addAssemble(TeacherEntity::getClassId, StudentData::setClassId)
                .backAcquire().backAssign()
                // sub assign 是否执行不受 InterruptStrategyEnum 的影响
                .invoke()
                .getMainData2List();
    }

    public void subAssign(Collection<StudentData> studentInfoList) {
        Assign.build(studentInfoList).name("subAssign")
                .addBranch(k -> 2L == k.getId())
                .addAssignValue(StudentData::setRemark, "subAssign invoked")
                .invoke();
    }
}
