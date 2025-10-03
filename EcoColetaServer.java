import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class EcoColetaServer {
    private static final Map<String, PontoColeta> pontosDeColeta = new ConcurrentHashMap<>();

    public static void main(String[] args) throws IOException {
        pontosDeColeta.put("Ecoponto Central", new PontoColeta("Ecoponto Central", "Av. Anhanguera, 500", Arrays.asList("Plástico", "Vidro", "Metal")));
        pontosDeColeta.put("Coleta Sul", new PontoColeta("Coleta Sul", "Rua 8, Setor Sul", Arrays.asList("Papel", "Pilhas")));

        int port = 12345;
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Servidor EcoColeta iniciado na porta " + port);
            System.out.println("Aguardando conexões de clientes...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Cliente conectado: " + clientSocket.getInetAddress().getHostAddress());
                // Cria uma nova thread para cada cliente, permitindo atender vários simultaneamente
                new ClientHandler(clientSocket).start();
            }
        }
    }

    // Classe interna para lidar com cada cliente em uma thread separada
    private static class ClientHandler extends Thread {
        private Socket clientSocket;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        @Override
        public void run() {
            try (
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)
            ) {
                String request;
                while ((request = in.readLine()) != null) {
                    System.out.println("Recebido do cliente: " + request);
                    String response = processRequest(request);
                    out.println(response);
                }
            } catch (IOException e) {
                System.out.println("Erro ao comunicar com o cliente: " + e.getMessage());
            } finally {
                try {
                    clientSocket.close();
                    System.out.println("Cliente desconectado.");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private String processRequest(String request) {
            String[] parts = request.split(";", 2);
            String command = parts[0].toUpperCase();

            try {
                switch (command) {
                    case "CADASTRAR": // Formato: CADASTRAR;Nome;Endereço;Residuo1,Residuo2
                        String[] dataCadastro = parts[1].split(";");
                        String nomeCadastro = dataCadastro[0];
                        String enderecoCadastro = dataCadastro[1];
                        List<String> residuosCadastro = Arrays.asList(dataCadastro[2].split(","));
                        if (pontosDeColeta.containsKey(nomeCadastro)) {
                            return "ERRO: Ponto de coleta com este nome já existe.";
                        }
                        PontoColeta novoPonto = new PontoColeta(nomeCadastro, enderecoCadastro, residuosCadastro);
                        pontosDeColeta.put(nomeCadastro, novoPonto);
                        return "SUCESSO: Ponto de coleta cadastrado.";

                    case "LISTAR": // Formato: LISTAR
                        if (pontosDeColeta.isEmpty()) {
                            return "INFO: Nenhum ponto de coleta cadastrado.";
                        }
                        return pontosDeColeta.values().stream()
                                .map(PontoColeta::toString)
                                .collect(Collectors.joining("|")); // | como separador de pontos

                    case "BUSCAR": // Formato: BUSCAR;TipoDeResiduo
                        String tipoResiduoBusca = parts[1];
                        List<PontoColeta> encontrados = pontosDeColeta.values().stream()
                                .filter(p -> p.getTiposResiduos().stream()
                                        .anyMatch(r -> r.equalsIgnoreCase(tipoResiduoBusca)))
                                .collect(Collectors.toList());
                        if (encontrados.isEmpty()) {
                            return "INFO: Nenhum ponto encontrado para o resíduo: " + tipoResiduoBusca;
                        }
                        return encontrados.stream()
                                .map(PontoColeta::toString)
                                .collect(Collectors.joining("|"));

                    case "EXCLUIR": // Formato: EXCLUIR;NomeDoPonto
                        String nomeExcluir = parts[1];
                        if (pontosDeColeta.remove(nomeExcluir) != null) {
                            return "SUCESSO: Ponto de coleta removido.";
                        }
                        return "ERRO: Ponto de coleta não encontrado.";

                    default:
                        return "ERRO: Comando desconhecido.";
                }
            } catch (Exception e) {
                return "ERRO: Formato da requisição inválido. " + e.getMessage();
            }
        }
    }
}