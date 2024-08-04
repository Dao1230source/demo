package org.source.demo.spring.log.facade;

import org.source.demo.spring.facade.mapper.StudentMapper;
import org.source.demo.spring.facade.param.StudentParam;
import org.source.demo.spring.facade.view.StudentView;
import org.source.spring.expression.VariableConstants;
import org.source.spring.log.LogData;
import org.source.spring.log.Logs;
import org.source.spring.log.annotation.Log;
import org.source.spring.log.annotation.LogContext;
import org.source.spring.log.enums.LogBizTypeEnum;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LogFacade {
    /**
     * <pre>
     *  可手动设置 LogContext
     *  {@literal @Log}也可以设置 systemType和bizType，并且优先级比{@literal @LogContext}高
     * </pre>
     *
     * @param param param
     * @return view
     */
    @LogContext(bizType = LogBizTypeEnum.USER)
    @Log(logId = "#param.name", title = "select student info", desc = "LogFacade select")
    public StudentView select(StudentParam param) {
        return StudentMapper.INSTANCE.x2y(param);
    }

    @Log(logId = VariableConstants.PARAM_SP_EL + ".name", title = "manualSetSomething")
    public StudentView manualSetSomething(StudentParam param) {
        Logs.setDesc("manual set something");
        return StudentMapper.INSTANCE.x2y(param);
    }

    public StudentView manualSetAll(StudentParam param) {
        LogData logData = LogData.builder().logId(param.getName()).title("manualSetAll").desc("logManual").build();
        Logs.save(logData);
        return StudentMapper.INSTANCE.x2y(param);
    }

    /**
     * 批量时，可以自动将集合展开，迭代处理，此时 spEl 的符号使用 #P
     *
     * @param params params
     * @return list
     */
    @Log(logId = VariableConstants.PARAM_SP_EL + ".name",
            title = "batch select", desc = "LogFacade selectBatch")
    public List<StudentView> selectBatch(List<StudentParam> params) {
        return StudentMapper.INSTANCE.x2yList(params);
    }

    /**
     * log的spring表达式中设计结果数据，但参数和结果的集合大小不想等，日志处理异常
     *
     * @param params params
     * @return list
     */
    @Log(logId = VariableConstants.PARAM_SP_EL + ".name",
            title = "'batch select' #R.name", desc = "LogFacade selectBatch")
    public List<StudentView> selectBatchLogFailed(List<StudentParam> params) {
        return List.of();
    }
}
