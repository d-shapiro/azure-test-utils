package azure.test.utils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.time.Instant;
import java.util.Map.Entry;
import java.util.function.Consumer;

import com.microsoft.azure.eventhubs.EventData;
import com.microsoft.azure.eventhubs.EventHubClient;
import com.microsoft.azure.eventhubs.PartitionReceiver;
import com.microsoft.azure.servicebus.ServiceBusException;

public class ReadD2CMessages {
	
	public ReadD2CMessages(String connStr) {
		this.connStr = connStr;
	}

	public ReadD2CMessages(String iothubKey, String eventhubCompatibleEndpoint, String eventhubCompatibleName) {
		this("Endpoint=" + eventhubCompatibleEndpoint + ";EntityPath=" + eventhubCompatibleName +
				";SharedAccessKeyName=iothubowner;SharedAccessKey=" + iothubKey);
	}
	
	private String connStr;

	private EventHubClient receiveMessages(final String partitionId)
	{
	  EventHubClient client = null;
	  try {
	    client = EventHubClient.createFromConnectionStringSync(connStr);
	  }
	  catch(Exception e) {
	    System.out.println("Failed to create client: " + e.getMessage());
	    System.exit(1);
	  }
	  try {
	    client.createReceiver( 
	      EventHubClient.DEFAULT_CONSUMER_GROUP_NAME,  
	      partitionId,  
	      Instant.now()).thenAccept(new Consumer<PartitionReceiver>()
	    {
	      public void accept(PartitionReceiver receiver)
	      {
	        System.out.println("** Created receiver on partition " + partitionId);
	        try {
	          while (true) {
	            Iterable<EventData> receivedEvents = receiver.receive(100).get();
	            int batchSize = 0;
	            if (receivedEvents != null)
	            {
	              for(EventData receivedEvent: receivedEvents)
	              {
	                System.out.println(String.format("Offset: %s, SeqNo: %s, EnqueueTime: %s", 
	                  receivedEvent.getSystemProperties().getOffset(), 
	                  receivedEvent.getSystemProperties().getSequenceNumber(), 
	                  receivedEvent.getSystemProperties().getEnqueuedTime()));
	                System.out.println(String.format("| Device ID: %s", receivedEvent.getSystemProperties().get("iothub-connection-device-id")));
	                System.out.println(String.format("| Message Payload: %s", new String(receivedEvent.getBytes(),
	                  Charset.defaultCharset())));
	                System.out.println("| Properties:");
	                for (Entry<String, Object> entry: receivedEvent.getProperties().entrySet()) {
	                    System.out.println(String.format("|\t %s : %s", entry.getKey(), entry.getValue().toString()));
	                }
	                batchSize++;
	              }
	            }
	            System.out.println(String.format("Partition: %s, ReceivedBatch Size: %s", partitionId,batchSize));
	          }
	        }
	        catch (Exception e)
	        {
	          System.out.println("Failed to receive messages: " + e.getMessage());
	        }
	      }
	    });
	  }
	  catch (Exception e)
	  {
	    System.out.println("Failed to create receiver: " + e.getMessage());
	  }
	  return client;
	}
	
	public static void main(String[] args) throws IOException {
		ReadD2CMessages obj;
		if (args.length == 1) {
			obj = new ReadD2CMessages(args[0]);
		} else if (args.length >= 3) {
			obj = new ReadD2CMessages(args[0], args[1], args[2]);
		} else {
			return;
		}
		
		EventHubClient client0 = obj.receiveMessages("0");
		EventHubClient client1 = obj.receiveMessages("1");
		System.out.println("Press ENTER to exit.");
		System.in.read();
		try
		{
		  client0.closeSync();
		  client1.closeSync();
		  System.exit(0);
		}
		catch (ServiceBusException sbe)
		{
		  System.exit(1);
		}

	}

}
