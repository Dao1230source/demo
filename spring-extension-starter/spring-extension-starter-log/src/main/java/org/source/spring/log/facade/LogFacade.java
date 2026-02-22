package org.source.spring.log.facade;

import org.source.spring.common.spel.VariableConstants;
import org.source.spring.log.LogData;
import org.source.spring.log.Logs;
import org.source.spring.log.annotation.Log;
import org.source.spring.log.annotation.LogContext;
import org.source.spring.log.enums.LogBizTypeEnum;
import org.source.spring.log.enums.LogScopeEnum;
import org.source.spring.log.facade.mapper.UserMapper;
import org.source.spring.log.facade.param.UserParam;
import org.source.spring.log.facade.view.UserView;
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
    @Log(bizId = "#param.userId", title = "select student info", desc = "LogFacade select")
    public UserView select(UserParam param) {
        return UserMapper.INSTANCE.x2y(param);
    }

    @Log(bizId = VariableConstants.PARAM_SP_EL + ".userId", title = "manualSetSomething")
    public UserView manualSetSomething(UserParam param) {
        Logs.setDesc(LogScopeEnum.LOG, "manual set something");
        return UserMapper.INSTANCE.x2y(param);
    }

    public UserView manualSetAll(UserParam param) {
        LogData logData = LogData.builder().bizId(param.getUserId()).title("manualSetAll").desc("logManual").build();
        Logs.save(logData);
        return UserMapper.INSTANCE.x2y(param);
    }

    /**
     * 批量时，可以自动将集合展开，迭代处理，此时 spEl 的符号使用 #P
     *
     * @param params params
     * @return list
     */
    @Log(bizId = VariableConstants.PARAM_SP_EL + ".userId",
            title = "batch select", desc = "LogFacade selectBatch")
    public List<UserView> selectBatch(List<UserParam> params) {
        return UserMapper.INSTANCE.x2yList(params);
    }

    /**
     * log的spring表达式中设计结果数据，但参数和结果的集合大小不想等，日志处理异常
     *
     * @param params params
     * @return list
     */
    @Log(bizId = VariableConstants.PARAM_SP_EL + ".userId",
            title = "'batch select' #R.userId", desc = "LogFacade selectBatch")
    public List<UserView> selectBatchLogFailed(List<UserParam> params) {
        return List.of();
    }
}
