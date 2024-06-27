package br.com.alura.screenmatch.principal;

import br.com.alura.screenmatch.model.*;
import br.com.alura.screenmatch.repository.SerieRepository;
import br.com.alura.screenmatch.service.ConsumoApi;
import br.com.alura.screenmatch.service.ConverteDados;

import java.util.*;
import java.util.stream.Collectors;

public class Principal {

    private Scanner leitura = new Scanner(System.in);
    private ConsumoApi consumo = new ConsumoApi();
    private ConverteDados conversor = new ConverteDados();
    private final String ENDERECO = "https://www.omdbapi.com/?t=";
    private final String API_KEY = System.getenv("OMDB_API_KEY");
    private SerieRepository repositorio;
    private List<Serie> series = new ArrayList<>();
    private Optional<Serie> serieBusca;

    public Principal(SerieRepository repositorio) {
        this.repositorio = repositorio;
    }

    public void exibeMenu() {
        var opcao = -1;
        while (opcao != 0) {
            var menu = """
                    \n
                    1 - Buscar séries
                    2 - Buscar episódios
                    3 - Listar séries buscadas
                    4 - Buscar série por título
                    5 - Buscar série por ator
                    6 - Buscar top 5 mais bem avaliadas 
                    7 - Buscar série por categoria
                    8 - Buscar série por quantidade de temporadas
                    9 - Buscar um episódio
                    10 - Buscar os 10 melhores episódios de uma série específica
                    11 - Buscar episódios de uma série a partir de uma data 
                                    
                    0 - Sair                                 
                    """;

            System.out.println(menu);
            opcao = leitura.nextInt();
            leitura.nextLine();

            switch (opcao) {
                case 1:
                    buscarSerieWeb();
                    break;
                case 2:
                    buscarEpisodioPorSerie();
                    break;
                case 3:
                    listarSeriesBuscadas();
                    break;
                case 4:
                    buscarSeriePorTitulo();
                    break;
                case 5:
                    buscarPorAtor();
                    break;
                case 6:
                    buscaTopCincoSeries();
                    break;
                case 7:
                    buscarPorCategoria();
                    break;
                case 8:
                    buscarPorQuantTemporadas();
                    break;
                case 9:
                    buscaEpisodioPorTrecho();
                    break;
                case 10:
                    buscaTopEpisodiosSerie();
                    break;
                case 11:
                    buscaEpisodiosPorData();
                    break;
                case 0:
                    System.out.println("Saindo...");
                    break;
                default:
                    System.out.println("Opção inválida");
            }
        }
    }

    private void buscarSerieWeb() {
        DadosSerie dados = getDadosSerie();
        Serie serie = new Serie(dados);
//        dadosSeries.add(dados);
        repositorio.save(serie);
        System.out.println(dados);
    }

    private DadosSerie getDadosSerie() {
        System.out.println("Digite o nome da série para busca");
        var nomeSerie = leitura.nextLine();
        var json = consumo.obterDados(ENDERECO + nomeSerie.replace(" ", "+") + API_KEY);
        DadosSerie dados = conversor.obterDados(json, DadosSerie.class);
        return dados;
    }

    private void buscarEpisodioPorSerie() {
        listarSeriesBuscadas();
        System.out.println("Escolha uma série pelo nome: ");
        var nomeSerie = leitura.nextLine();

        Optional<Serie> serie = repositorio.findByTituloContainingIgnoreCase(nomeSerie);

        if (serie.isPresent()) {
            var serieEncontrada = serie.get(); //facilidade para não precisar fazer serie.get() todas as vezes
            List<DadosTemporada> temporadas = new ArrayList<>();

            for (int i = 1; i <= serieEncontrada.getTotalTemporadas(); i++) {
                var json = consumo.obterDados(ENDERECO + serieEncontrada.getTitulo().replace(" ", "+") + "&season=" + i + API_KEY);
                DadosTemporada dadosTemporada = conversor.obterDados(json, DadosTemporada.class);
                temporadas.add(dadosTemporada);
            }
            temporadas.forEach(System.out::println);

            List<Episodio> episodios = temporadas.stream()
                    .flatMap(d -> d.episodios().stream()
                            .map(e -> new Episodio(d.numero(), e)))
                    .collect(Collectors.toList());

            serieEncontrada.setEpisodios(episodios);
            repositorio.save(serieEncontrada);
        } else {
            System.out.println("Série não encontrada!");
        }
    }

    private void listarSeriesBuscadas() {
        series = repositorio.findAll();
        series.stream()
                .sorted(Comparator.comparing(Serie::getGenero))
                .forEach(System.out::println);

    }

    private void buscarSeriePorTitulo() {
        System.out.println("Escolha uma série pelo nome: ");
        var nomeSerie = leitura.nextLine();

        serieBusca = repositorio.findByTituloContainingIgnoreCase(nomeSerie);

        if (serieBusca.isPresent()) {
            System.out.println("Dados da série: " + serieBusca.get());

        } else {
            System.out.println("Série não encontrada!");
        }
    }

    private void buscarPorAtor() {
        System.out.println("Informe o nome do ator que você deseja pesquisar: ");
        var nomeAtor = leitura.nextLine();

        List<Serie> serieEncontrada = repositorio.findByAtoresContainingIgnoreCase(nomeAtor);

        System.out.println("Séries em que o ator " + nomeAtor + " trabalhou");
        serieEncontrada.forEach(s -> System.out.println("Nome: " + s.getTitulo() + " Avaliação: " + s.getAvaliacao()));
    }

    private void buscaTopCincoSeries() {
        List<Serie> serieTop = repositorio.findTop5ByOrderByAvaliacaoDesc();
        serieTop.forEach(s ->
                System.out.println(s.getTitulo() + " - Avaliação: " + s.getAvaliacao()));
    }

    private void buscarPorCategoria() {
        System.out.println("Deseja buscar séries por qual categoria/gênero");
        var nomeGenero = leitura.nextLine();
        Categoria categoria = Categoria.fromPortugues(nomeGenero);
        List<Serie> seriePorCategoria = repositorio.findByGenero(categoria);
        System.out.println("Series da categoria: " + nomeGenero);
        seriePorCategoria.forEach(System.out::println);
    }

    private void buscarPorQuantTemporadas() {
        System.out.println("Informe por quantas temporadas você deseja buscar: ");
        var quantTemporadas = leitura.nextInt();

        List<Serie> serieTemporadas = repositorio.buscaPorQuantidadeDeTemporadas(quantTemporadas);

        System.out.println("Séries que possuem " + quantTemporadas + " temporadas");

        serieTemporadas.forEach(System.out::println);
    }

    private void buscaEpisodioPorTrecho() {
        System.out.println("Qual o nome do episódio que deseja buscar? ");
        var trechoEpisodio = leitura.nextLine();
        List<Episodio> episodiosEncontrados = repositorio.episodiosPorTrecho(trechoEpisodio);
        episodiosEncontrados.forEach(e ->
                System.out.printf("Serie: %s - Temporada: %s - Episódio: %s - %s\n",
                        e.getSerie().getTitulo(), e.getTemporada(), e.getNumeroEpisodio(), e.getTitulo()));
    }

    private void buscaTopEpisodiosSerie() {
        buscarSeriePorTitulo();
        if (serieBusca.isPresent()) {
            Serie serie = serieBusca.get();
            List<Episodio> topEpisodios = repositorio.topEpisodiosPorSerie(serie);
            topEpisodios.forEach(e ->
                    System.out.printf("Serie: %s - Temporada: %s - Episódio: %s - %s - Nota: %s\n",
                            e.getSerie().getTitulo(), e.getTemporada(), e.getNumeroEpisodio(), e.getTitulo(), e.getAvaliacao()));
        }
    }

    private void buscaEpisodiosPorData() {
        buscarSeriePorTitulo();
        if (serieBusca.isPresent()) {
            Serie serie = serieBusca.get();

            System.out.println("A partir de qual data você deseja buscar?: ");
            var anoLancamento = leitura.nextInt();
            leitura.nextLine();

            List <Episodio> episodiosFiltrados = repositorio.episodiosFiltrados(serie, anoLancamento);
            episodiosFiltrados.forEach(System.out::println);
        }
    }
}