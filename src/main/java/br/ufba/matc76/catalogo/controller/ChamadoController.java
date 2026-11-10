package br.ufba.matc76.catalogo.controller;

import java.net.URI;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import br.ufba.matc76.catalogo.model.Chamado;
import br.ufba.matc76.catalogo.model.NovoChamado;
import br.ufba.matc76.catalogo.model.Prioridade;
import br.ufba.matc76.catalogo.model.ResumoChamados;
import br.ufba.matc76.catalogo.model.Status;

@RestController
@RequestMapping("/chamados")
public class ChamadoController {

    private final List<Chamado> chamados = new CopyOnWriteArrayList<>();
    private final AtomicLong proximoId = new AtomicLong(1);

    public ChamadoController() {
        adicionar("Impressora do financeiro nao imprime", "Ana Souza", "Financeiro", Prioridade.ALTA, Status.ABERTO);
        adicionar("Computador nao liga apos queda de energia", "Bruno Lima", "RH", Prioridade.ALTA, Status.EM_ANDAMENTO);
        adicionar("Solicitar acesso ao sistema de vendas", "Carla Dias", "Comercial", Prioridade.MEDIA, Status.ABERTO);
        adicionar("Wi-Fi instavel na sala de reunioes", "Daniel Rocha", "Marketing", Prioridade.MEDIA, Status.EM_ANDAMENTO);
        adicionar("Redefinir senha da conta de e-mail", "Elisa Prado", "Financeiro", Prioridade.BAIXA, Status.RESOLVIDO);
        adicionar("Servidor de arquivos fora do ar", "Fabio Nunes", "TI", Prioridade.ALTA, Status.ABERTO);
        adicionar("Trocar toner da impressora do RH", "Gabriela Melo", "RH", Prioridade.BAIXA, Status.RESOLVIDO);
        adicionar("Instalar planilhas no notebook novo", "Heitor Alves", "Logistica", Prioridade.BAIXA, Status.ABERTO);
        adicionar("VPN nao conecta para trabalho remoto", "Isabela Costa", "Comercial", Prioridade.ALTA, Status.RESOLVIDO);
        adicionar("Monitor com tela piscando", "Joao Pereira", "Logistica", Prioridade.MEDIA, Status.RESOLVIDO);
        adicionar("Impressora de etiquetas sem conexao de rede", "Karen Martins", "Logistica", Prioridade.ALTA, Status.EM_ANDAMENTO);
        adicionar("Criar conta de acesso para estagiario", "Lucas Barros", "Marketing", Prioridade.BAIXA, Status.ABERTO);
    }

    @GetMapping
    public List<Chamado> listar() {
        return chamados;
    }

    @GetMapping("/{id}")
    public Chamado buscarPorId(@PathVariable Long id) {
        return chamados.stream()
                .filter(c -> c.id().equals(id))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Chamado " + id + " nao encontrado"));
    }

    @GetMapping("/setor")
    public List<Chamado> filtrarPorSetor(@RequestParam String nome) {
        return chamados.stream()
                .filter(c -> c.setor().equalsIgnoreCase(nome.trim()))
                .toList();
    }

    @GetMapping("/prioridade")
    public List<Chamado> filtrarPorPrioridade(@RequestParam String nivel) {
        Prioridade prioridade = lerPrioridade(nivel);
        return chamados.stream()
                .filter(c -> c.prioridade() == prioridade)
                .toList();
    }

    @GetMapping("/busca")
    public List<Chamado> buscarPorTitulo(@RequestParam String texto) {
        String termo = texto.trim().toLowerCase(Locale.ROOT);
        return chamados.stream()
                .filter(c -> c.titulo().toLowerCase(Locale.ROOT).contains(termo))
                .toList();
    }

    @GetMapping("/criticos")
    public List<Chamado> listarCriticos() {
        return chamados.stream()
                .filter(c -> c.prioridade() == Prioridade.ALTA && c.status() != Status.RESOLVIDO)
                .toList();
    }

    @GetMapping("/resumo")
    public ResumoChamados resumo() {
        long abertos = contar(Status.ABERTO);
        long emAndamento = contar(Status.EM_ANDAMENTO);
        long resolvidos = contar(Status.RESOLVIDO);
        return new ResumoChamados(abertos, emAndamento, resolvidos, chamados.size());
    }

    @PostMapping
    public ResponseEntity<Chamado> cadastrar(@RequestBody NovoChamado novo) {
        exigir(novo.titulo(), "titulo");
        exigir(novo.solicitante(), "solicitante");
        exigir(novo.setor(), "setor");
        exigir(novo.prioridade(), "prioridade");

        Status status = (novo.status() == null || novo.status().isBlank())
                ? Status.ABERTO
                : lerStatus(novo.status());

        Chamado criado = adicionar(novo.titulo().trim(), novo.solicitante().trim(),
                novo.setor().trim(), lerPrioridade(novo.prioridade()), status);

        return ResponseEntity.created(URI.create("/chamados/" + criado.id())).body(criado);
    }

    private Chamado adicionar(String titulo, String solicitante, String setor,
                              Prioridade prioridade, Status status) {
        Chamado chamado = new Chamado(proximoId.getAndIncrement(), titulo, solicitante,
                setor, prioridade, status);
        chamados.add(chamado);
        return chamado;
    }

    private long contar(Status status) {
        return chamados.stream().filter(c -> c.status() == status).count();
    }

    private static void exigir(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Campo obrigatorio ausente: " + campo);
        }
    }

    private static Prioridade lerPrioridade(String valor) {
        try {
            return Prioridade.valueOf(normalizar(valor));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Prioridade invalida: use BAIXA, MEDIA ou ALTA");
        }
    }

    private static Status lerStatus(String valor) {
        try {
            return Status.valueOf(normalizar(valor));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Status invalido: use ABERTO, EM_ANDAMENTO ou RESOLVIDO");
        }
    }

    private static String normalizar(String valor) {
        return valor.trim().toUpperCase(Locale.ROOT).replace(' ', '_').replace('-', '_');
    }
}
