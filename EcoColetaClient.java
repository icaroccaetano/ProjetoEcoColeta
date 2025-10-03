import java.io.*;
import java.net.*;
import java.util.Scanner;

public class EcoColetaClient {
    public static void main(String[] args) {
        String hostname = "localhost";
        int port = 12345;

        try (
            Socket socket = new Socket(hostname, port);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            Scanner scanner = new Scanner(System.in)
        ) {
            System.out.println("Conectado ao servidor EcoColeta.");

            while (true) {
                printMenu();
                System.out.print("> ");
                String choice = scanner.nextLine();

                if ("0".equals(choice)) {
                    System.out.println("Desconectando...");
                    break;
                }

                String request = buildRequest(choice, scanner);
                if (request != null) {
                    out.println(request); // Envia requisição ao servidor
                    String serverResponse = in.readLine(); // Lê a resposta
                    handleResponse(serverResponse);
                } else if (!"0".equals(choice)) {
                    System.out.println("Opção inválida. Tente novamente.");
                }
            }

        } catch (UnknownHostException ex) {
            System.out.println("Servidor não encontrado: " + ex.getMessage());
        } catch (IOException ex) {
            System.out.println("Erro de I/O: " + ex.getMessage());
        }
    }

    private static void printMenu() {
        System.out.println("\n--- EcoColeta Menu ---");
        System.out.println("1. Listar todos os pontos de coleta");
        System.out.println("2. Buscar ponto por tipo de resíduo");
        System.out.println("--- Funções de Administrador ---");
        System.out.println("3. Cadastrar novo ponto de coleta");
        System.out.println("4. Excluir ponto de coleta");
        System.out.println("0. Sair");
    }

    private static String buildRequest(String choice, Scanner scanner) {
        switch (choice) {
            case "1":
                return "LISTAR";
            case "2":
                System.out.print("Digite o tipo de resíduo (ex: Vidro): ");
                String residuo = scanner.nextLine();
                return "BUSCAR;" + residuo;
            case "3":
                System.out.print("Digite o nome do ponto: ");
                String nome = scanner.nextLine();
                System.out.print("Digite o endereço: ");
                String endereco = scanner.nextLine();
                System.out.print("Digite os resíduos aceitos, separados por vírgula (ex: Papel,Metal): ");
                String residuos = scanner.nextLine();
                return "CADASTRAR;" + nome + ";" + endereco + ";" + residuos;
            case "4":
                System.out.print("Digite o nome do ponto a ser excluído: ");
                String nomeExcluir = scanner.nextLine();
                return "EXCLUIR;" + nomeExcluir;
            default:
                return null;
        }
    }

    private static void handleResponse(String response) {
        System.out.println("\n--- Resposta do Servidor ---");
        if (response.startsWith("ERRO:") || response.startsWith("SUCESSO:") || response.startsWith("INFO:")) {
            System.out.println(response);
        } else {
            String[] items = response.split("\\|");
            for (String item : items) {
                System.out.println("- " + item);
            }
        }
        System.out.println("--------------------------");
    }
}