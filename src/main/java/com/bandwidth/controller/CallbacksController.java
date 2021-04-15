package com.bandwidth.controller;

import com.bandwidth.model.MessageCallback;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;

@RestController
@RequestMapping("callbacks")
public class CallbacksController {

    private String username = System.getenv("BW_USERNAME");
    private String password = System.getenv("BW_PASSWORD");

    Logger logger = LoggerFactory.getLogger(CallbacksController.class);

    @PostMapping("/messageCallback")
    public void messageCallback(@RequestBody MessageCallback[] callbacks) throws IOException, MalformedURLException {

        for(MessageCallback callback : callbacks) {
            logger.info(callback.getType());
            logger.info(callback.getDescription());
            switch( callback.getType()) {
                case "message-received":
                    logger.info("Message from: " + callback.getMessage().getFrom());
                    logger.info("Message to: " + callback.getMessage().getTo().get(0));
                    logger.info("With media");
                    logger.info(callback.getMessage().getText());

                    if(callback.getMessage().getMedia().size() > 0) {
                        // Save the media locally
                        saveMedia(callback.getMessage().getMedia(), "./");
                    } else  {
                        logger.info("With NO media");
                    }
                    break;
                case "message-sending":
                    logger.info("message-sending type is only for MMS");
                    break;
                case "message-delivered":
                    logger.info("your message has been handed off to the Bandwidth's MMSC network, but has not been confirmed at the downstream carrier");
                    break;
                case "message-failed":
                    logger.info("For MMS and Group Messages, you will only receive this callback if you have enabled delivery receipts on MMS. ");
                    break;
                default:
                    break;
            }
        }
    }

    private void saveMedia(List<String> medias, String rootPath) throws IOException, MalformedURLException {

        //Auth is needed to download the media from the provided media link
        String userPass = username + ":" + password;
        String encoding = Base64.getEncoder().encodeToString(userPass.getBytes());

        for(String media : medias){

            String fileName = media.substring(media.lastIndexOf("/"));
            File file = new File(fileName);
            logger.info("With media, saving to: " + file.getAbsolutePath());

            URLConnection uc = new URL(media).openConnection();
            uc.setRequestProperty("Authorization", "Basic " + encoding);

            try(InputStream in = (InputStream)uc.getInputStream()){
                FileUtils.copyInputStreamToFile(in, file);
            }
        }


    }
}
