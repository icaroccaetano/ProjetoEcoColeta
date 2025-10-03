import java.io.Serializable;
import java.util.List;
import java.util.Objects;

public class PontoColeta implements Serializable {
    private String nome;
    private String endereco;
    private List<String> tiposResiduos;

    public PontoColeta(String nome, String endereco, List<String> tiposResiduos) {
        this.nome = nome;
        this.endereco = endereco;
        this.tiposResiduos = tiposResiduos;
    }

    // Getters e Setters
    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    public List<String> getTiposResiduos() {
        return tiposResiduos;
    }

    public void setTiposResiduos(List<String> tiposResiduos) {
        this.tiposResiduos = tiposResiduos;
    }

    // toString() sobrescrito para uma representação textual melhorada
    @Override
    public String toString() {
        return "Ponto de Coleta: '" + nome + '\'' +
               ", Endereço: '" + endereco + '\'' +
               ", Aceita: " + tiposResiduos;
    }

    // equals() e hashCode() sobrescritos para facilitar a busca e remoção de pontos na lista
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PontoColeta that = (PontoColeta) o;
        return nome.equalsIgnoreCase(that.nome);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nome.toLowerCase());
    }
}