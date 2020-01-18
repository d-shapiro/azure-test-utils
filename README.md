# azure-test-utils
Some scripts for testing IoT Hub integrations

## ReadD2CMessages

Reads Device-to-Cloud messages from an Event Hub-compatible endpoint, and prints their contents to standard output.

Takes the Event Hub-compatible endpoint's connection string as a command-line argument. To find this string, go to your IoT Hub in portal.azure.com, then go to the `Built-in endpoints` section in the left sidebar, then copy the string labeled `Event Hub-compatible endpoint`
