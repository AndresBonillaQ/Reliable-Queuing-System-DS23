package it.polimi.ds.cli;

import it.polimi.ds.exception.CliExitException;
import it.polimi.ds.message.RequestMessage;
import it.polimi.ds.utils.RequestMessageBuilder;

import java.util.Objects;
import java.util.Scanner;

public class CliHandler {

    public static RequestMessage getRequest(String clientId, Scanner in) throws CliExitException {
        String line;

        do{
            System.out.println("1.Create Queue\n2.Append Value\n3.Read Value\n4.Exit");
            line = in.nextLine();

            switch (line) {
                case "1" -> {
                    return RequestMessageBuilder.buildCreateQueueRequestMessage(clientId);
                }
                case "2" -> {
                    System.out.println("Insert QueueId in which append value:");
                    final String queueId = in.nextLine();
                    System.out.println("Insert value to append in queue " + queueId + ":");
                    final int value = Integer.parseInt(in.nextLine());

                    return RequestMessageBuilder.buildAppendValueRequestMessage(clientId, queueId, value);
                }
                case "3" -> {
                    System.out.println("Insert QueueId from which read value:");
                    final String queueId = in.nextLine();

                    return RequestMessageBuilder.buildReadValueRequestMessage(clientId, queueId);
                }
            }
        }while(!Objects.equals(line, "4"));

        throw new CliExitException();
    }

    private CliHandler(){}
}
