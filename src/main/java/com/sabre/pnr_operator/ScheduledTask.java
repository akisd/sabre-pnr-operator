package com.sabre.pnr_operator;

import com.sabre.pnr_operator.config.properties.HeaderProperties;
import com.sabre.pnr_operator.handlers.SabreCommandHandler;
import com.sabre.pnr_operator.handlers.SessionCloseHandler;
import com.sabre.pnr_operator.handlers.SessionCreateHandler;
import com.sabre.pnr_operator.responses.Response;
import com.sabre.pnr_operator.rest.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ScheduledTask {

    @Autowired
    private SessionCreateHandler sessionCreateHandler;

    @Autowired
    private SabreCommandHandler sabreCommandHandler;

    @Autowired
    private SessionCloseHandler sessionCloseHandler;

    @Autowired
    private HeaderProperties headerProperties;

    @Autowired
    private ApiResponse apiResponse;

    private Response sessionCloseResponse;
    private Response dxStatusResponse;
    private Response dxTransmitResponse;

    @Scheduled(cron = "${execute.time}")
    private void runTask() {
        log.info("Start running task...");

        try {
            Response sessionCreateResponse = sessionCreateHandler.processRequest();

            if (sessionCreateResponse.isSuccess()) {
                sabreCommandHandler.setCommand("DX STATUS");
                dxStatusResponse = sabreCommandHandler.processRequest();

                sabreCommandHandler.setCommand("DX TRANSMIT");
                dxTransmitResponse = sabreCommandHandler.processRequest();

                sessionCloseResponse = sessionCloseHandler.processRequest();
            }

            apiResponse.getScheduledTask().put("session-create", sessionCreateResponse);
            apiResponse.getScheduledTask().put("dx-status", dxStatusResponse);
            apiResponse.getScheduledTask().put("dx-transmit", dxTransmitResponse);
            apiResponse.getScheduledTask().put("session-close", sessionCloseResponse);

        } catch (Exception e) {
            log.error("Exception while executing scheduled task: " + e);
        }

        log.info("Scheduled task is finished.");
    }

}