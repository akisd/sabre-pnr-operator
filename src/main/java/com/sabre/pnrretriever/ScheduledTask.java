package com.sabre.pnrretriever;

import com.sabre.pnrretriever.config.properties.HeaderProperties;
import com.sabre.pnrretriever.handlers.SabreCommandHandler;
import com.sabre.pnrretriever.handlers.SessionCloseHandler;
import com.sabre.pnrretriever.handlers.SessionCreateHandler;
import com.sabre.pnrretriever.headers.message_header.Action;
import com.sabre.pnrretriever.responses.Response;
import com.sabre.pnrretriever.rest.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
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

    private Response sessionCreateResponse;
    private Response sessionCloseResponse;
    private Response dxStatusResponse;
    private Response dxTransmitResponse;

    @Scheduled(cron = "${execute.time}")
    private void runTask() {
        System.out.println("Running Task...");

        try {
            System.out.println(Action.SESSION_CREATE.getValue());
            sessionCreateResponse = sessionCreateHandler.processRequest();

            if (sessionCreateResponse.isSuccess()) {
                sabreCommandHandler.setCommand("DX STATUS");
                dxStatusResponse = sabreCommandHandler.processRequest();

                sabreCommandHandler.setCommand("DX TRANSMIT");
                dxTransmitResponse = sabreCommandHandler.processRequest();

                sessionCloseResponse = sessionCloseHandler.processRequest();
            }

            apiResponse.getApiResponse().put("session-create", sessionCreateResponse);
            apiResponse.getApiResponse().put("dx-status", dxStatusResponse);
            apiResponse.getApiResponse().put("dx-transmit", dxTransmitResponse);
            apiResponse.getApiResponse().put("session-close", sessionCloseResponse);

        } catch (Exception e) {
            System.out.println(e);
        }

        System.out.println("Task finished");
    }

}