package br.com.alura.screenmatch.controller;

import br.com.alura.screenmatch.dto.EpisodioDTO;
import br.com.alura.screenmatch.dto.SerieDTO;
import br.com.alura.screenmatch.service.SerieService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/series") //padroniza o /series para todos
public class SerieController {

    @Autowired
    private SerieService servico;


    @GetMapping()
    public List<SerieDTO> obterSeries(){
        return servico.obterTodasAsSeries();
    }

    @GetMapping("/top5")
    public List<SerieDTO> obterTop5(){
        return servico.obterTop5();
    }

    @GetMapping("/lancamentos")
    public List <SerieDTO> obterLancamentos(){
        return servico.obterLancamentos();
    }

    @GetMapping("/{id}")
    public SerieDTO obterPorId(@PathVariable Long id) { //anotação declara que é uma variavel que vem no caminho do endpoint
        return servico.obterPorId(id);
    }


    @GetMapping ("/{id}/temporadas/todas")
    public List<EpisodioDTO> obterTodasTemporadas(@PathVariable Long id){
        return servico.obterTodasTemporadas(id);
    }

    @GetMapping("/{id}/temporadas/{numero}")
    public List<EpisodioDTO> obterTemporadasPorNumero (@PathVariable Long id, @PathVariable long numero){
        return servico.obterTemporadasPorNumero(id, numero);
    }

    @GetMapping("/categoria/{nomeGenero}")
    public List<SerieDTO> obterSeriePorGenero(@PathVariable String nomeGenero){
        return servico.obterSeriesPorGenero(nomeGenero);
    }

    @GetMapping("/{id}/temporadas/top")
    public List<EpisodioDTO> obterTopEpisodios(@PathVariable Long id){
        return servico.obterTopEpisodios(id);
    }

}
