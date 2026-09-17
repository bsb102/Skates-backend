package cl.duoc.backendskatesapp.service;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import cl.duoc.backendskatesapp.model.Skate;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class SkateService {

    private final Map<Long, Skate> inventario = new ConcurrentHashMap<>();
    private final AtomicLong secuenciaId = new AtomicLong(0);

    @PostConstruct
    public void cargarDatosDeEjemplo() {
        crear(new Skate(null, "Street", "DC", 7.5, 15));
        crear(new Skate(null, "Street", "Maui & Sons", 8.0, 8));
        crear(new Skate(null, "Street", "Polemic", 8.5, 8));
    }

    public List<Skate> listarTodos() {
        return inventario.values().stream()
                .sorted((a, b) -> Long.compare(a.getId(), b.getId()))
                .collect(Collectors.toList());
    }

    public Skate buscarPorId(Long id) {
        Skate skate = inventario.get(id);
        if (skate == null) {
            throw new SkateNoEncontradoException(id);
        }
        return skate;
    }

    public Skate crear(Skate nuevo) {
        long id = secuenciaId.incrementAndGet();
        nuevo.setId(id);
        inventario.put(id, nuevo);
        return nuevo;
    }

    public Skate actualizar(Long id, Skate datos) {
        Skate existente = buscarPorId(id);
        existente.setModelo(datos.getModelo());
        existente.setMarca(datos.getMarca());
        existente.setMedida(datos.getMedida());
        existente.setStock(datos.getStock());
        return existente;
    }

    public void eliminar(Long id) {
        if (!inventario.containsKey(id)) {
            throw new SkateNoEncontradoException(id);
        }
        inventario.remove(id);
    }
}