package org.source.demo.utility.assign;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.source.demo.utility.assign.facade.AssignFacade;
import org.source.demo.utility.assign.facade.data.Class1;
import org.source.demo.utility.assign.facade.data.Student;
import org.source.demo.utility.assign.facade.data.StudentInfo;
import org.source.demo.utility.assign.facade.data.Teacher;
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
    void testAssign() {
        List<StudentInfo> studentInfoList = assignFacade.getStudentInfoList();
        Assign<StudentInfo> sourceAssign = Assign.build(studentInfoList)
                // assignValue 先于 acquire 执行
                .addAssignValue(StudentInfo::setRemark, "student info")
                .addAssignValueIfAbsent(StudentInfo::getSchool, StudentInfo::setSchool, "New York Second School")
                // 启用线程池，并行执行 acquire，也可以指定 ExecutorService
                .parallel()
                // get Student list by ids
                .addAcquire(assignFacade::getStudentList, Student::getId)
                // 给 acquire 命名，并且启用本地缓存（启用cache必须设置name值），也可以自定义 Cache<K, T>
                .name(Student.class.getName()).cache()
                .addAction(StudentInfo::getId)
                // 赋值
                .addAssemble(Student::getName, StudentInfo::setName)
                .addAssemble(Student::getAge, StudentInfo::setAge)
                .backAcquire()
                .afterProcessor((studentInfo, idStudentMap) -> {
                    if (!idStudentMap.containsKey(studentInfo.getId())) {
                        log.info("student id:{} 没有获取到对应数据，如果需要可以抛出异常", studentInfo.getId());
                    }
                })
                .exceptionHandler((studentInfo, throwable) -> log.info("如果 acquire fetcher 发生异常，在此处处理"))
                .backAssign()
                // get <teacherId,Teacher> map by teacherIds
                .addAcquire(assignFacade::getTeacherMap)
                .name(Teacher.class.getName()).cache()
                .addAction(StudentInfo::getTeacherId)
                .addAssemble(Teacher::getName, StudentInfo::setTeacherName)
                .addAssemble(Teacher::getClassId, StudentInfo::setClassId)
                .backAcquire().backAssign()
                // 添加分支，先通过 Teacher 获取 classId，再获取 class 数据，这里只获取 classId = c2 的班级数据
                .addBranch(k -> "c2".equals(k.getClassId()))
                .addAcquire(assignFacade::getClassMap)
                .addAction(StudentInfo::getClassId)
                .addAssemble(Class1::getName, StudentInfo::setClassName)
                .backAcquire().backAssign().invoke()
                // 返回最高层级
                .backSuperlative();
        List<StudentInfo> mainData = sourceAssign.getMainData2List();
        Map<Long, StudentInfo> studentInfoMap = Streams.toMap(mainData, StudentInfo::getId);
        StudentInfo studentInfo1 = studentInfoMap.get(1L);
        Assertions.assertEquals("Jim", studentInfo1.getName());
        Assertions.assertEquals("student info", studentInfo1.getRemark());
        // addAssignValueIfAbsent 字段无值时设置默认值
        Assertions.assertEquals("New York First School", studentInfo1.getSchool());
        Assertions.assertNull(studentInfo1.getClassName(), "only get class2 info");
        StudentInfo studentInfo2 = studentInfoMap.get(2L);
        Assertions.assertEquals("Wolf", studentInfo2.getTeacherName());
        Assertions.assertEquals("class2", studentInfo2.getClassName());
        StudentInfo studentInfo4 = studentInfoMap.get(4L);
        Assertions.assertNull(studentInfo4.getName());
        Assertions.assertEquals("New York Second School", studentInfo4.getSchool());
    }

    @Test
    void testAssignThrow() {
        List<StudentInfo> studentInfoListNo = testEndStrategyIfExcept(InterruptStrategyEnum.NO);
        StudentInfo studentInfo2No = studentInfoListNo.get(1);
        Assertions.assertNull(studentInfo2No.getName(), "name must be null,because fetch student data throw exception");
        Assertions.assertEquals("Wolf", studentInfo2No.getTeacherName());
        StudentInfo studentInfo3No = studentInfoListNo.get(2);
        Assertions.assertEquals("subAssign invoked", studentInfo3No.getRemark());
        List<StudentInfo> studentInfoListAny = testEndStrategyIfExcept(InterruptStrategyEnum.ANY);
        StudentInfo studentInfo2Any = studentInfoListAny.get(1);
        Assertions.assertNull(studentInfo2Any.getName(), "name must be null");
        Assertions.assertNull(studentInfo2Any.getTeacherName(), "teacherName must be null, branch not invoke");
    }

    public List<StudentInfo> testEndStrategyIfExcept(InterruptStrategyEnum interruptStrategyEnum) {
        List<StudentInfo> studentInfoList = assignFacade.getStudentInfoList();
        return Assign.build(studentInfoList).name("main")
                .addSub(this::subAssign)
                // throw exception
                .addAcquire(assignFacade::getStudentListThrow, Student::getId)
                .addAction(StudentInfo::getId)
                .addAssemble(Student::getName, StudentInfo::setName)
                .addAssemble(Student::getAge, StudentInfo::setAge)
                .backAcquire()
                .exceptionHandler((studentInfo, throwable) -> log.info("acquire fetcher 发生异常"))
                .backAssign()
                // 发生异常时是否执行分支
                .interruptStrategy(interruptStrategyEnum)
                // not invoke
                .addBranch().name(interruptStrategyEnum.name())
                // 发生任何异常，停止执行分支
                .addAcquire(assignFacade::getTeacherMap)
                .name(Teacher.class.getName()).cache()
                .addAction(StudentInfo::getTeacherId)
                .addAssemble(Teacher::getName, StudentInfo::setTeacherName)
                .addAssemble(Teacher::getClassId, StudentInfo::setClassId)
                .backAcquire().backAssign()
                // sub assign 是否执行不受 InterruptStrategyEnum 的影响
                .invoke()
                .getMainData2List();
    }

    public void subAssign(Collection<StudentInfo> studentInfoList) {
        Assign.build(studentInfoList).name("subAssign")
                .addBranch(k -> 2L == k.getId())
                .addAssignValue(StudentInfo::setRemark, "subAssign invoked")
                .invoke();
    }
}
