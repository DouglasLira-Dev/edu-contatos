package br.educontatos.model;

/**
 * Representa a entidade Setor da Secretaria de Educação.
 * Esta classe é um espelho da tabela 'setores' no banco de dados.
 *
 * @author Seu Nome
 * @version 1.0
 */
public class Setor {
    private int id;
    private String nome;
    private String responsavel;
    private String email;
    private String ramal;
    private String descricao;

    /**
     * Construtor vazio (necessário para instanciar objetos ao buscar do banco).
     */
    public Setor() {}

    /**
     * Construtor completo para facilitar a criação de objetos.
     *
     * @param id O identificador único do setor.
     * @param nome O nome do setor (ex: Recursos Humanos).
     * @param responsavel O nome da pessoa responsável pelo setor.
     * @param email O e-mail institucional do setor.
     * @param ramal O número do ramal telefônico.
     */
    public Setor(int id, String nome, String responsavel, String email, String ramal, String descricao) {
        this.id = id;
        this.nome = nome;
        this.responsavel = responsavel;
        this.email = email;
        this.ramal = ramal;
        this.descricao = descricao;
    }

    // Getters e Setters documentados
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getResponsavel() { return responsavel; }
    public void setResponsavel(String responsavel) { this.responsavel = responsavel; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getRamal() { return ramal; }
    public void setRamal(String ramal) { this.ramal = ramal; }
    public String getDescricao() {return descricao;}
    public void setDescricao(String descricao) {this.descricao = descricao;}
}