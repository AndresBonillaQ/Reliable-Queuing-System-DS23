package it.polimi.ds.cli;

import it.polimi.ds.exception.CliExitException;
import it.polimi.ds.message.RequestMessage;
import it.polimi.ds.utils.RequestMessageBuilder;

import java.util.Scanner;

public class CliHandler {

    private static final Scanner in = new Scanner(System.in);

    public static RequestMessage getRequest(String clientId) throws CliExitException {
        String line;

        do{
           // System.out.println("1.Create Queue\n2.Append Value\n3.Read Value\n4.Exit");
            printMenu();
            line = in.nextLine();

            switch (line) {
                case "1" -> {
                    return RequestMessageBuilder.buildCreateQueueRequestMessage(clientId);
                }
                case "2" -> {
                    System.out.println("Insert QueueId in which append value:");
                    final String queueId = in.nextLine();
                    System.out.println("Insert value to append in queue " + queueId + ":");
                    try{
                        final int value = Integer.parseInt(in.nextLine());
                        return RequestMessageBuilder.buildAppendValueRequestMessage(clientId, queueId, value);
                    } catch (NumberFormatException e){
                        System.out.println("It's allowed to insert only integer values..");
                    }
                }
                case "3" -> {
                    System.out.println("Insert QueueId from which read value:");
                    final String queueId = in.nextLine();

                    return RequestMessageBuilder.buildReadValueRequestMessage(clientId, queueId);
                }
            }
        }while(!"4".equals(line));

        throw new CliExitException();
    }
    private static void printMenu() {
        System.out.println("╔════════════════════════════════════════╗");
        System.out.println("║             ☆ Main Menu ☆             ║");
        System.out.println("╠════════════════════════════════════════╣");
        System.out.println("║   1. Create Queue                      ║");
        System.out.println("║   2. Append Value                      ║");
        System.out.println("║   3. Read Value                        ║");
        System.out.println("║   4. Exit                              ║");
        System.out.println("╚════════════════════════════════════════╝");
        System.out.print("➤ Choose an option: ");
    }


    private CliHandler(){}
}
