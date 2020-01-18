package azure.test.utils;

import java.io.IOException;

import com.microsoft.azure.sdk.iot.service.FileUploadNotification;
import com.microsoft.azure.sdk.iot.service.FileUploadNotificationReceiver;
import com.microsoft.azure.sdk.iot.service.IotHubServiceClientProtocol;
import com.microsoft.azure.sdk.iot.service.ServiceClient;

public class ReadFileUploadNotifs {
        
    private static void readFileNotifs(String connStr, IotHubServiceClientProtocol protocol) throws IOException {
        ServiceClient serviceClient;
        final FileUploadNotificationReceiver receiver;
        try {
            serviceClient = ServiceClient.createFromConnectionString(connStr, protocol);
            serviceClient.open();
            receiver = serviceClient.getFileUploadNotificationReceiver();
            receiver.open();
        } catch (Exception e) {
            System.out.println("Failed to create receiver: " + e.getMessage());
            return;
        }
        
        Thread t = new Thread(new Runnable() {
            
            @Override
            public void run() {
                try {
                    while (true) {
                        System.out.println("Recieve file upload notifications...");
                        FileUploadNotification notification = receiver.receive();
                        if (notification != null) {
                            String enqTime = notification.getEnqueuedTimeUtcDate().toInstant().toString();
                            String devId = notification.getDeviceId();
                            String blobUri = notification.getBlobUri();
                            String blobName = notification.getBlobName();
                            String lastUpdate =
                                    notification.getLastUpdatedTimeDate().toInstant().toString();
                            Long blobBytes = notification.getBlobSizeInBytes();
                            System.out.println("Recieved notification:");
                            System.out.println(String.format("| Enqueue Time: %s", enqTime));
                            System.out.println(String.format("| Device ID: %s", devId));
                            System.out.println(String.format("| Blob Name: %s", blobName));
                            System.out.println(String.format("| Blob URI: %s", blobUri));
                            System.out.println(String.format("| Blob Size in Bytes: %s", blobBytes));
                            System.out.println(String.format("| Last Update: %s", lastUpdate));
                        }
                    }
                } catch (Exception e) {
                    System.out.println("error recieving notification: " + e.getMessage());
                }
                
            }
        });
        
        t.start();
        System.out.println("Press ENTER to exit.");
        System.in.read();
        return;
    }
    
    
    public static void main(String[] args) throws IOException {
        String connStr;
        IotHubServiceClientProtocol protocol = IotHubServiceClientProtocol.AMQPS_WS;
        if (args.length > 0) {
            connStr = args[0];
        } else {
            return;
        }
        if (args.length > 1 && args[1].toLowerCase().equals("amqps")) {
            protocol = IotHubServiceClientProtocol.AMQPS;
        }
        readFileNotifs(connStr, protocol);
        System.exit(0);
    }

}
