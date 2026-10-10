import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;
import org.bson.Document;
import org.bson.json.JsonMode;
import org.bson.json.JsonWriterSettings;
import com.mongodb.*;
import com.mongodb.client.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import com.tcc.rj_system.model.*;
import com.tcc.rj_system.enums.*;
import com.tcc.rj_system.repository.*;
import com.tcc.rj_system.service.*;
import com.tcc.rj_system.security.*;
import com.tcc.rj_system.dto.LoginRequest;
import org.springframework.data.mongodb.repository.support.MongoRepositoryFactory;

/** Explicit maintenance tool. Never runs with the application's startup. */
public class PresentationData {
    static final List<String> COLLECTIONS = List.of("usuarios", "processos", "credores", "documentos_processos");
    static final Path ROOT = Path.of("").toAbsolutePath().normalize();
    static final String EMAIL = System.getenv("PRESENTATION_ADMIN_EMAIL");

    public static void main(String[] args) {
        try { run(args); }
        catch (Exception e) {
            if (e instanceof org.bson.codecs.configuration.CodecConfigurationException) {
                System.err.println("Diagnostico de conversao: " + e.getMessage());
            }
            // Connection exceptions can include connection details. Do not dump them.
            System.err.println("Operacao interrompida: " + e.getClass().getSimpleName() + ". Consulte o backup local; nenhuma credencial foi registrada.");
            System.exit(1);
        }
    }

    static void run(String[] args) throws Exception {
        if (args.length != 1 || !(args[0].equals("--inspect") || args[0].equals("--apply") || args[0].equals("--verify")))
            throw new IllegalArgumentException("Modo obrigatorio");
        ((ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME))
                .setLevel(ch.qos.logback.classic.Level.WARN);
        Properties properties = new Properties();
        try (var input = Files.newInputStream(ROOT.resolve("src/main/resources/application.properties"))) { properties.load(input); }
        if (Files.exists(ROOT.resolve("application-local.properties"))) {
            try (var input = Files.newInputStream(ROOT.resolve("application-local.properties"))) { properties.load(input); }
        }
        String uri = System.getenv().getOrDefault("SPRING_MONGODB_URI", properties.getProperty("spring.mongodb.uri"));
        ConnectionString connection = new ConnectionString(uri);
        if (!"rj_system".equals(connection.getDatabase())) throw new IllegalArgumentException("Banco inesperado");
        Path uploads = ROOT.resolve(properties.getProperty("app.upload.dir", "uploads")).normalize().toAbsolutePath();
        if (!uploads.startsWith(ROOT) || uploads.equals(ROOT) || Files.isSymbolicLink(uploads))
            throw new IllegalArgumentException("Diretorio de upload fora do projeto");
        var settings = MongoClientSettings.builder().applyConnectionString(connection)
                .applyToClusterSettings(b -> b.serverSelectionTimeout(15, TimeUnit.SECONDS))
                .applyToSocketSettings(b -> b.connectTimeout(15, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS)).build();
        try (MongoClient client = MongoClients.create(settings)) {
            var db = client.getDatabase(connection.getDatabase());
            db.runCommand(new Document("ping", 1));
            System.out.println("Banco confirmado: " + connection.getDatabase());
            for (String name : COLLECTIONS) System.out.println(name + ": " + db.getCollection(name).countDocuments());
            if (args[0].equals("--inspect")) return;
            String password = System.getenv("PRESENTATION_ADMIN_PASSWORD");
            if (password == null || password.isBlank() || EMAIL == null || EMAIL.isBlank()) throw new IllegalArgumentException("E-mail e senha obrigatorios por ambiente");
            if (args[0].equals("--verify")) {
                var template = new MongoTemplate(client, connection.getDatabase());
                var factory = new MongoRepositoryFactory(template);
                var usuarios = factory.getRepository(UsuarioRepository.class);
                var processos = factory.getRepository(ProcessoRepository.class);
                var credores = factory.getRepository(CredorRepository.class);
                var docs = factory.getRepository(DocumentoProcessoRepository.class);
                var jwt = new JwtService();
                var secretField = JwtService.class.getDeclaredField("jwtSecret");
                secretField.setAccessible(true);
                secretField.set(jwt, System.getenv().getOrDefault("APP_SECURITY_JWT_SECRET", properties.getProperty("app.security.jwt.secret")));
                var expirationField = JwtService.class.getDeclaredField("jwtExpirationMs");
                expirationField.setAccessible(true);
                expirationField.setLong(jwt, Long.parseLong(properties.getProperty("app.security.jwt.expiration-ms", "86400000")));
                var auth = new AuthService(usuarios, new BCryptPasswordEncoder(), jwt).login(new LoginRequest(EMAIL, password));
                var user = usuarios.findByEmail(EMAIL).orElseThrow();
                if (!"ADMIN".equals(auth.usuario().role()) || !jwt.isTokenValid(auth.accessToken(), new CustomUserDetails(user)))
                    throw new IllegalStateException("Autenticacao invalida");
                var documentService = new DocumentoProcessoService(docs, processos);
                var processResponses = new ProcessoService(processos, credores, documentService).listar();
                var creditorResponses = new CredorService(credores, processos).listar();
                if (processResponses.size() != 5 || creditorResponses.size() != 20) throw new IllegalStateException("Listagens invalidas");
                BigDecimal total = processResponses.stream().map(p -> p.valorTotal()).reduce(BigDecimal.ZERO, BigDecimal::add);
                for (var doc : docs.findAll()) documentService.buscarParaDownload(doc.getId());
                System.out.println("Login validado pelo AuthService; JWT valido; perfil ADMIN.");
                System.out.println("Listagens verificadas: 5 processos, 20 credores; downloads: 10 arquivos legiveis.");
                System.out.println("Passivo total: " + total.toPlainString());
                return;
            }
            Document manifest = Document.parse(Files.readString(ROOT.resolve("tools/presentation/manifest.json"), StandardCharsets.UTF_8));
            var encoder = new BCryptPasswordEncoder();
            Usuario admin = Usuario.builder().id("demo-admin-joao").nome("Administrador").email(EMAIL)
                    .senha(encoder.encode(password)).role("ADMIN").build();
            if (!encoder.matches(password, admin.getSenha())) throw new IllegalStateException("Hash invalido");
            var template = new MongoTemplate(client, connection.getDatabase());
            Map<String, Processo> processes = new LinkedHashMap<>();
            List<Credor> creditors = new ArrayList<>();
            List<DocumentoProcesso> documents = new ArrayList<>();
            for (Document row : manifest.getList("processos", Document.class)) {
                var timeline = row.getList("timeline", Document.class).stream().map(event -> EventoTimeline.builder()
                        .id(event.getString("id")).titulo(event.getString("titulo")).descricao(event.getString("descricao"))
                        .data(LocalDate.parse(event.getString("data"))).status(StatusEventoTimeline.valueOf(event.getString("status"))).build()).toList();
                var process = Processo.builder().id(row.getString("id")).numeroProcesso(row.getString("numeroProcesso"))
                        .empresa(row.getString("empresa")).cnpj(row.getString("cnpj")).vara(row.getString("vara"))
                        .comarca(row.getString("comarca")).juizResponsavel(row.getString("juizResponsavel"))
                        .administradorJudicial(row.getString("administradorJudicial")).abertura(LocalDate.parse(row.getString("abertura")))
                        .status(StatusProcesso.valueOf(row.getString("status"))).valorTotal(BigDecimal.ZERO).timeline(timeline).build();
                processes.put(process.getId(), process);
            }
            for (Document row : manifest.getList("credores", Document.class)) {
                Processo process = Objects.requireNonNull(processes.get(row.getString("processoId")));
                var creditor = Credor.builder().id(row.getString("id")).nome(row.getString("nome")).documento(row.getString("documento"))
                        .processo(process).tipo(TipoCredor.valueOf(row.getString("tipo")))
                        .valor(new BigDecimal(row.get("valor").toString())).status(StatusCredor.valueOf(row.getString("status")))
                        .dataCadastro(Instant.parse("2026-09-01T12:00:00Z")).build();
                creditors.add(creditor);
                process.getCredores().add(creditor);
                process.setValorTotal(process.getValorTotal().add(creditor.getValor()));
            }
            for (Document row : manifest.getList("documentos", Document.class)) {
                String name = row.getString("nomeArquivo");
                Path source = ROOT.resolve("tools/presentation").resolve(name).normalize();
                Path destination = uploads.resolve("processos").resolve(row.getString("processoId")).resolve(name).normalize();
                if (!source.startsWith(ROOT.resolve("tools/presentation")) || !destination.startsWith(uploads)) throw new IllegalArgumentException("Caminho invalido");
                documents.add(DocumentoProcesso.builder().id(row.getString("id")).processoId(row.getString("processoId"))
                        .nome(row.getString("nome")).nomeArquivo(name).tipo(row.getString("tipo")).contentType("application/pdf")
                        .tamanhoBytes(Files.size(source)).caminhoArquivo(destination.toString()).dataUpload(Instant.parse("2026-09-20T12:00:00Z")).build());
            }
            Map<String, List<Document>> replacement = new LinkedHashMap<>();
            replacement.put("usuarios", bson(template, List.of(admin)));
            replacement.put("processos", bson(template, new ArrayList<>(processes.values())));
            replacement.put("credores", bson(template, creditors));
            replacement.put("documentos_processos", bson(template, documents));
            Map<String, List<Document>> originals = new LinkedHashMap<>();
            Path backup = ROOT.resolve("backups").resolve("antes-apresentacao-" + ZonedDateTime.now(ZoneId.of("America/Sao_Paulo"))
                    .format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")));
            Files.createDirectories(backup);
            var jsonSettings = JsonWriterSettings.builder().outputMode(JsonMode.EXTENDED).build();
            for (String name : COLLECTIONS) {
                var snapshot = db.getCollection(name).find().into(new ArrayList<Document>());
                originals.put(name, snapshot);
                try (var writer = Files.newBufferedWriter(backup.resolve(name + ".jsonl"), StandardCharsets.UTF_8)) {
                    for (Document doc : snapshot) { writer.write(doc.toJson(jsonSettings, db.getCodecRegistry().get(Document.class))); writer.newLine(); }
                }
            }
            Files.writeString(backup.resolve("LEIA-ME.txt"), "Backup anterior à substituição dos dados de testes. JSONL em Extended JSON preserva IDs, datas e referências. uploads contém os arquivos originais. Banco: rj_system. Não publicar este backup.\n", StandardCharsets.UTF_8);
            boolean moved = false;
            boolean committed = false;
            try {
                if (Files.exists(uploads)) {
                    // Validate every entry before moving the directory; links are not accepted.
                    try (var paths = Files.walk(uploads)) {
                        if (paths.anyMatch(Files::isSymbolicLink)) throw new IllegalArgumentException("Upload com link simbolico");
                    }
                    Files.move(uploads, backup.resolve("uploads"));
                    moved = true;
                }
                for (DocumentoProcesso document : documents) {
                    Path destination = Path.of(document.getCaminhoArquivo());
                    Files.createDirectories(destination.getParent());
                    Files.copy(ROOT.resolve("tools/presentation").resolve(document.getNomeArquivo()), destination);
                }
                try (var session = client.startSession()) {
                    session.withTransaction(() -> {
                        for (String name : COLLECTIONS) {
                            var current = db.getCollection(name).find(session).into(new ArrayList<Document>());
                            if (!byId(current).equals(byId(originals.get(name)))) throw new IllegalStateException("Dados alterados durante a preparacao");
                        }
                        for (String name : COLLECTIONS) {
                            db.getCollection(name).deleteMany(session, new Document());
                            db.getCollection(name).insertMany(session, replacement.get(name));
                        }
                        return true;
                    }, TransactionOptions.builder().readConcern(ReadConcern.SNAPSHOT).writeConcern(WriteConcern.MAJORITY).build());
                }
                committed = true;
            } finally {
                if (!committed) {
                    // Remove only the newly staged upload tree, confined to this project's upload directory.
                    if (Files.exists(uploads)) try (var paths = Files.walk(uploads)) {
                        for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) {
                            if (!path.toAbsolutePath().normalize().startsWith(uploads)) throw new IllegalStateException("Caminho de rollback invalido");
                            Files.delete(path);
                        }
                    }
                    if (moved) Files.move(backup.resolve("uploads"), uploads);
                }
            }
            for (String name : COLLECTIONS) {
                long count = db.getCollection(name).countDocuments();
                if (count != replacement.get(name).size()) throw new IllegalStateException("Contagem final divergente");
                System.out.println("Final " + name + ": " + count);
            }
            for (Processo process : processes.values()) {
                Processo saved = template.findById(process.getId(), Processo.class);
                if (saved == null || saved.getCredores().size() != 4) throw new IllegalStateException("Vinculos invalidos");
            }
            Usuario savedAdmin = template.findById(admin.getId(), Usuario.class);
            if (savedAdmin == null || !encoder.matches(password, savedAdmin.getSenha())) throw new IllegalStateException("Login invalido");
            System.out.println("Backup: " + backup);
            System.out.println("Credores vinculados, documentos existentes e senha BCrypt verificados.");
        }
    }

    static List<Document> bson(MongoTemplate template, List<?> models) {
        return models.stream().map(model -> { Document doc = new Document(); template.getConverter().write(model, doc); return doc; }).toList();
    }

    static Map<Object, Document> byId(List<Document> docs) {
        Map<Object, Document> result = new HashMap<>();
        docs.forEach(doc -> result.put(doc.get("_id"), doc));
        return result;
    }
}
