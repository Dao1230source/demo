package org.source.spring.log.controller;

import lombok.AllArgsConstructor;
import org.source.spring.log.annotation.Log;
import org.source.spring.log.facade.LogFacade;
import org.source.spring.log.facade.param.UserParam;
import org.source.spring.log.facade.view.UserView;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * {@literal @RestController}或{@literal @Controller} 自动设置 {@code
 * systemType = LogSystemTypeEnum.WEB
 * }
 */
@AllArgsConstructor
@RequestMapping("/log")
@RestController
public class LogController {
    private final LogFacade facade;

    @RequestMapping("/select")
    @Log(logId = "#param.userId", title = "select", desc = "LogController log")
    public UserView select(UserParam param) {
        return facade.select(param);
    }

    @RequestMapping("/manualSetSomething")
    public UserView manualSetSomething(UserParam param) {
        return facade.manualSetSomething(param);
    }

    @RequestMapping("/manualSetAll")
    public UserView manualSetAll(UserParam param) {
        return facade.manualSetAll(param);
    }

    @RequestMapping("/selectBatch")
    public List<UserView> selectBatch(List<UserParam> params) {
        return facade.selectBatch(params);
    }

    @RequestMapping("/selectBatchLogFailed")
    public List<UserView> selectBatchLogFailed(List<UserParam> params) {
        return facade.selectBatchLogFailed(params);
    }
}
