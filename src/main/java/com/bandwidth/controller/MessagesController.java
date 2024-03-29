package com.bandwidth.controller;

import com.bandwidth.BandwidthClient;
import com.bandwidth.Environment;
import com.bandwidth.model.CreateMessage;
import com.bandwidth.model.MessageReply;
import com.bandwidth.exceptions.ApiException;
import com.bandwidth.http.response.ApiResponse;
import com.bandwidth.messaging.controllers.APIController;
import com.bandwidth.messaging.models.BandwidthMessage;
import com.bandwidth.messaging.models.MessageRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Arrays;

@RestController
@RequestMapping("messages")
public class MessagesController {

    Logger logger = LoggerFactory.getLogger(MessagesController.class);

    private String username = System.getenv("BW_USERNAME");
    private String password = System.getenv("BW_PASSWORD");
    private String accountId = System.getenv("BW_ACCOUNT_ID");
    private String applicationId = System.getenv("BW_MESSAGING_APPLICATION_ID");

    private BandwidthClient client = new BandwidthClient.Builder()
            .messagingBasicAuthCredentials(username, password)
            .environment(Environment.PRODUCTION)
            .build();

    private APIController controller = client.getMessagingClient().getAPIController();

    @PostMapping()
    public MessageReply createMessage(@RequestBody CreateMessage createMessage) throws IOException {

        // Build the body of the message request to the Bandwidth API
        MessageRequest messageRequest = new MessageRequest.Builder()
                .text(createMessage.getMessage())
                .from(createMessage.getFrom())
                .to(Arrays.asList( new String[]{createMessage.getTo()}) )
                .media(Arrays.asList(new String[]{ "https://cdn2.thecatapi.com/images/MTY3ODIyMQ.jpg" })) // Media is added here
                .applicationId(applicationId)
                .build();

        MessageReply messageReply = new MessageReply();
        try {
            ApiResponse<BandwidthMessage> response = controller.createMessage(accountId, messageRequest);
            messageReply.setSuccess(true);
        } catch (ApiException e) { // Bandwidth API response status not 2XX
            messageReply.setSuccess(false);
            messageReply.setError(e.getMessage());
        }

        return messageReply;
    }
}
