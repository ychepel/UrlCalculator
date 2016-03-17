package server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class Calculator {
    private static final String OPERATION_KEY = "operation";
    private static final String FIRST_OPERAND_KEY = "operand1";
    private static final String SECOND_OPERAND_KEY = "operand2";
    private static final String COMMAND_NAME = "/calculate";

    private Map<String, String> currentOpperation;

    public static void main(String[] args) throws Exception {
        new Calculator().run();
    }

    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(6543);) {

            while (true) {
                Socket socket = serverSocket.accept();

                String taskAnswer = "";
                try {
                    currentOpperation = new HashMap<>();
                    String command = getCommand(socket);
                    parseCommand(command);
                    taskAnswer = doMath().toString();
                    if ("Infinity".equals(taskAnswer)) {
                        taskAnswer = "Dividing by zero.";
                    }
                } catch (UnsupportedOperationException e) {
                    taskAnswer = "Wrong command line. Try to use this example: <a href='./calculate?operation=add&operand1=5&operand2=12'>/calculate?operation=add&operand1=5&operand2=12</a>";
                }

                sendAnswer(socket, taskAnswer);
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getCommand(Socket socket) throws IOException {
        InputStream inputStream = socket.getInputStream();
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        BufferedReader input = new BufferedReader(inputStreamReader);

        String line;
        if((line = input.readLine()) == null) {
            return "";
        }
        String[] lineParts = line.split(" ");
        String[] commandParts = lineParts[1].split("\\?");
        if(!COMMAND_NAME.equals(commandParts[0])) {
            throw new UnsupportedOperationException("Wrong URL");
        }
        return commandParts[1];
    }

    private void parseCommand(String command) {
        String[] operationParts = command.split("&");
        for(String part : operationParts ) {
            String[] partValues = part.split("=");
            if(partValues.length == 2) {
                currentOpperation.put(partValues[0], partValues[1]);
            }
        }
    }

    private Number doMath() {
        Double result;
        String operation = currentOpperation.get(OPERATION_KEY);
        if(operation == null) {
            throw new UnsupportedOperationException("Wrong operation command");
        }

        Double operand1;
        Double operand2;
        try {
            operand1 = Double.parseDouble(currentOpperation.get(FIRST_OPERAND_KEY));
            operand2 = Double.parseDouble(currentOpperation.get(SECOND_OPERAND_KEY));
        } catch (NumberFormatException|NullPointerException e) {
            throw new UnsupportedOperationException("Wrong operands.");
        }

        switch (operation) {
            case "add": result = operand1 + operand2; break;
            case "sub": result = operand1 - operand2; break;
            case "mul": result = operand1 * operand2; break;
            case "div": result = operand1 / operand2; break;
            default: throw new UnsupportedOperationException("Wrong operation command");
        }

        if(result == Math.round(result)) {
          return Math.round(result);
        }
        return result;
    }

    private void sendAnswer(Socket socket, String content) throws IOException {
        OutputStream outputStream = socket.getOutputStream();
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
        BufferedWriter output = new BufferedWriter(outputStreamWriter);

        output.write("HTTP/1.0 200 OK\r\n");
        output.write("Server: Apache/0.8.4\r\n");
        output.write("Content-Type: text/html\r\n");
        output.write("Content-Length: " + content.length() + "\r\n");
        output.write("\r\n");
        output.write(content);
        output.flush();
    }
}
