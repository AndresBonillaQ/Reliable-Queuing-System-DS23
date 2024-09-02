package it.polimi.ds.cli;

import it.polimi.ds.exception.CliExitException;
import it.polimi.ds.message.RequestMessage;
import it.polimi.ds.utils.RequestMessageBuilder;

import java.util.Scanner;

public class CliHandler {
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_BOLD = "\u001B[1m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_RED = "\u001B[31m";

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
        System.out.println(ANSI_BLUE + "╔═══════════════════════════════════════════╗" + ANSI_RESET);
        System.out.println(ANSI_BLUE + "║" + ANSI_RESET + ANSI_YELLOW + "       ★    " + ANSI_BOLD + "    MAIN MENU   " + ANSI_RESET + ANSI_YELLOW + "      ★       " + ANSI_RESET + ANSI_BLUE + "║" + ANSI_RESET);
        System.out.println(ANSI_BLUE + "╠═══════════════════════════════════════════╣" + ANSI_RESET);
        System.out.println(ANSI_BLUE + "║" + ANSI_RESET + " " + ANSI_GREEN + "[1]" + ANSI_RESET + " " + "Create Queue                          " + ANSI_BLUE + "║" + ANSI_RESET);
        System.out.println(ANSI_BLUE + "║" + ANSI_RESET + " " + ANSI_GREEN + "[2]" + ANSI_RESET + " " + "Append Value                          " + ANSI_BLUE + "║" + ANSI_RESET);
        System.out.println(ANSI_BLUE + "║" + ANSI_RESET + " " + ANSI_GREEN + "[3]" + ANSI_RESET + " " + "Read Value                            " + ANSI_BLUE + "║" + ANSI_RESET);
        System.out.println(ANSI_BLUE + "║" + ANSI_RESET + " " + ANSI_RED + "[4]" + ANSI_RESET + " " + "Exit                                  " + ANSI_BLUE + "║" + ANSI_RESET);
        System.out.println(ANSI_BLUE + "╚═══════════════════════════════════════════╝" + ANSI_RESET);
        System.out.print(ANSI_YELLOW + "➤ Choose an option by writing the corresponding number: " + ANSI_RESET);
    }
    private CliHandler(){}
}
