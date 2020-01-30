package com.reactive.dc.projectreactor.app;

import com.reactive.dc.projectreactor.app.models.Comentarios;
import com.reactive.dc.projectreactor.app.models.Usuario;
import com.reactive.dc.projectreactor.app.models.UsuarioComentarios;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Delayed;

@SpringBootApplication
public class ProjectReactorApplication implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(ProjectReactorApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(ProjectReactorApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {

        ejemploIntervaloDesdeCreate();
    }

    public void ejemploFlux() throws Exception {

        List<String> nombresList = Arrays.asList("Daniel", "David", "Santiago");
        Flux<String> nombres = Flux.fromIterable(nombresList);

        //Otra forma
        // Flux<String> nombres = Flux.just("Daniel", "David", "Santiago");

        Flux<Usuario> usuarios = nombres.filter(nombre -> nombre.contains("Da"))
                .map(nombre -> Usuario.builder().nombre(nombre.toUpperCase()).build())
                .doOnNext(usuario -> {
                    if(usuario == null){
                        throw new RuntimeException("Usuario no puede ser null");
                    }
                    System.out.println(usuario);
                })
                .map(usuario -> Usuario.builder().nombre(usuario.getNombre().toLowerCase()).build());

        usuarios.subscribe(usuario -> log.info(usuario.toString()),
                e -> log.error(e.getMessage()),
                () -> log.info("Ha finalizado la ejecución del observable"));
    }

    public void ejemploFilter() throws Exception {

        List<String> nombresList = Arrays.asList("Daniel Cossio", "David Cifuentes", "Santiago Cifuentes");

        Flux<Usuario> usuarios = Flux.fromIterable(nombresList)
                .map(nombre -> Usuario.builder().nombre(nombre.split(" ")[0].toLowerCase())
                        .apellido(nombre.split(" ")[1].toLowerCase())
                        .build())
                .filter(usuario -> usuario.getApellido().equalsIgnoreCase("Cifuentes"))
                .map(usuario -> {
                    usuario.setApellido(usuario.getApellido().toUpperCase());
                    return usuario;
                });

        usuarios.subscribe(usuario -> log.info(usuario.toString()),
                e -> log.error(e.getMessage()),
                () -> log.info("Ha finalizado la ejecución del observable"));
    }


    public void ejemploFlatMap() throws Exception {

        List<String> nombresList = Arrays.asList("Daniel Cossio", "David Cifuentes", "Santiago Cifuentes");

        Flux<Usuario> usuarios = Flux.fromIterable(nombresList)
                .map(nombre -> Usuario.builder().nombre(nombre.split(" ")[0].toLowerCase())
                        .apellido(nombre.split(" ")[1].toLowerCase())
                        .build())
                .flatMap(usuario -> {
                    if(usuario.getNombre().contains("da")){
                        return Mono.just(usuario);
                    }else{
                        return Mono.empty();
                    }
                })
                .filter(usuario -> usuario.getApellido().equalsIgnoreCase("Cifuentes"))
                .map(usuario -> {
                    usuario.setApellido(usuario.getApellido().toUpperCase());
                    return usuario;
                });

        usuarios.subscribe(usuario -> log.info(usuario.toString()),
                e -> log.error(e.getMessage()),
                () -> log.info("Ha finalizado la ejecución del observable"));
    }

    public void ejemploConvertirString() throws Exception {

        List<Usuario> nombresList = Arrays.asList(
                Usuario.builder().nombre("Daniel").apellido("Cossio").build(),
                Usuario.builder().nombre("David").apellido("Cifuentes").build(),
                Usuario.builder().nombre("Santiago").apellido("Cifuentes").build());

        Flux<String> usuarios = Flux.fromIterable(nombresList)
                .map(usuario -> usuario.getNombre().toUpperCase()
                        .concat(" ")
                        .concat(usuario.getApellido().toUpperCase()))
                .flatMap(nombre -> {
                    if(nombre.contains("CIFU")){
                        return Mono.just(nombre);
                    }else{
                        return Mono.empty();
                    }
                })
                .map(nombre -> nombre.toLowerCase());

        usuarios.subscribe(usuario -> log.info(usuario),
                e -> log.error(e.getMessage()),
                () -> log.info("Ha finalizado la ejecución del observable"));
    }

    public void ejemploCollectList(){
        List<Usuario> nombresList = Arrays.asList(
                Usuario.builder().nombre("Daniel").apellido("Cossio").build(),
                Usuario.builder().nombre("David").apellido("Cifuentes").build(),
                Usuario.builder().nombre("Santiago").apellido("Cifuentes").build());

        Flux.fromIterable(nombresList)
                .collectList()
                .subscribe(usuario -> usuario.forEach(System.out::println));
    }

    public void ejemploUsuarioComentariosFlapMap(){
        Mono<Usuario> usuarioMono = Mono.fromCallable(() -> Usuario.builder().nombre("Daniel").apellido("cifuentes").build());
        Mono<Comentarios> comentariosMono = Mono.fromCallable(()->{
            Comentarios comentarios = new Comentarios();
            comentarios.addComentario("Comentario 1");
            comentarios.addComentario("Comentario 2");
            comentarios.addComentario("Comentario 3");
            return comentarios;
        });

        usuarioMono.flatMap(usuario ->
           comentariosMono.map(comentarios -> UsuarioComentarios.builder().usuario(usuario).comentarios(comentarios).build())
        )
        .subscribe(System.out::println);
    }

    public void ejemploUsuarioComentariosZipWith(){
        Mono<Usuario> usuarioMono = Mono.fromCallable(() -> Usuario.builder().nombre("Daniel").apellido("cifuentes").build());
        Mono<Comentarios> comentariosMono = Mono.fromCallable(()->{
            Comentarios comentarios = new Comentarios();
            comentarios.addComentario("Comentario 1");
            comentarios.addComentario("Comentario 2");
            comentarios.addComentario("Comentario 3");
            return comentarios;
        });

        Mono<UsuarioComentarios> usuarioComentariosMono = usuarioMono
                .zipWith(comentariosMono, (usuario, comentarios) -> UsuarioComentarios.builder().usuario(usuario).comentarios(comentarios).build());

        usuarioComentariosMono.subscribe(System.out::println);

        Mono<UsuarioComentarios> usuarioComentariosMonoTuple = usuarioMono
                .zipWith(comentariosMono)
                .map(tuple -> UsuarioComentarios.builder().usuario(tuple.getT1()).comentarios(tuple.getT2()).build());
        usuarioComentariosMonoTuple.subscribe(System.err::println);
    }

    public void ejemploRange(){
        Flux.just(1, 2, 3, 4)
                .map(number -> number * 2)
                .zipWith(Flux.range(0, 4), (fluxOne, fluxTwo) -> String.format("FLujo uno:%d Flujo dos:%d", fluxOne, fluxTwo))
                .subscribe(System.err::println);
    }

    public void ejemploInterval(){
        Flux<Integer> rango = Flux.range(1, 12);
        Flux<Long> retraso = Flux.interval(Duration.ofSeconds(1));

        rango.zipWith(retraso, (ran, ret) -> ran)
                .doOnNext(System.out::println)
            .blockLast();
    }

    public void ejemploDelay(){
        Flux<Integer> rango = Flux.range(1, 12)
                .delayElements(Duration.ofSeconds(1))
                .doOnNext(System.out::println);

        rango.blockLast();
    }

    public void ejemploIntervaloInfinito() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        Flux.interval(Duration.ofSeconds(1))
                .doOnTerminate(() -> {
                    latch.countDown();
                })
                .flatMap(i ->{
                    if(i>=5){
                        return Flux.error(new InterruptedException("Es mayor o igual a 5"));
                    }
                    return Flux.just(i);
                })
                .retry(2)
                .subscribe(i -> log.info("hola " + i), e -> {
                    log.error("Error", e);
                    //latch.countDown();
                });

        latch.await();
    }

    public void ejemploIntervaloDesdeCreate(){
        Flux.create(emitter -> {
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                private int contandor = 0;
                @Override
                public void run() {
                    emitter.next(++contandor);
                    if (contandor ==5){
                        timer.cancel();
                        //emitter.complete();
                        emitter.error(new InterruptedException("emitter cancelado por ser igual a 5"));
                    }
                }
            }, 1000, 1000);
        }).subscribe(System.out::println,
                e -> System.err.println(e.getMessage()),
                () -> System.out.println("Completado"));
    }

}
