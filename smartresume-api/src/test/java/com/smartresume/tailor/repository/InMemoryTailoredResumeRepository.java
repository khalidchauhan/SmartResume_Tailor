package com.smartresume.tailor.repository;

import com.smartresume.tailor.domain.entity.TailoredResume;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery;

import java.util.*;
import java.util.function.Function;

public class InMemoryTailoredResumeRepository implements TailoredResumeRepository {
    private final Map<UUID, TailoredResume> storage = new LinkedHashMap<>();

    @Override
    public Optional<TailoredResume> findByJobMatchId(UUID matchId) {
        return storage.values().stream()
                .filter(t -> t.getJobMatch() != null && matchId.equals(t.getJobMatch().getId()))
                .findFirst();
    }

    @Override
    public <S extends TailoredResume> S save(S entity) {
        if (entity.getId() == null) entity.setId(UUID.randomUUID());
        storage.put(entity.getId(), entity);
        return entity;
    }

    @Override
    public Optional<TailoredResume> findById(UUID uuid) { return Optional.ofNullable(storage.get(uuid)); }
    @Override
    public List<TailoredResume> findAll() { return new ArrayList<>(storage.values()); }
    @Override
    public boolean existsById(UUID uuid) { return storage.containsKey(uuid); }
    @Override
    public List<TailoredResume> findAllById(Iterable<UUID> uuids) { return List.of(); }
    @Override
    public long count() { return storage.size(); }
    @Override
    public void deleteById(UUID uuid) { storage.remove(uuid); }
    @Override
    public void delete(TailoredResume entity) { storage.remove(entity.getId()); }
    @Override
    public void deleteAllById(Iterable<? extends UUID> uuids) {}
    @Override
    public void deleteAll(Iterable<? extends TailoredResume> entities) {}
    @Override
    public void deleteAll() { storage.clear(); }
    @Override
    public void flush() {}
    @Override
    public <S extends TailoredResume> S saveAndFlush(S entity) { return save(entity); }
    @Override
    public <S extends TailoredResume> List<S> saveAllAndFlush(Iterable<S> entities) { return List.of(); }
    @Override
    public void deleteAllInBatch(Iterable<TailoredResume> entities) {}
    @Override
    public void deleteAllByIdInBatch(Iterable<UUID> uuids) {}
    @Override
    public void deleteAllInBatch() {}
    @Override
    public TailoredResume getOne(UUID uuid) { return storage.get(uuid); }
    @Override
    public TailoredResume getById(UUID uuid) { return storage.get(uuid); }
    @Override
    public TailoredResume getReferenceById(UUID uuid) { return storage.get(uuid); }
    @Override
    public <S extends TailoredResume> Optional<S> findOne(Example<S> example) { return Optional.empty(); }
    @Override
    public <S extends TailoredResume> List<S> findAll(Example<S> example) { return List.of(); }
    @Override
    public <S extends TailoredResume> List<S> findAll(Example<S> example, Sort sort) { return List.of(); }
    @Override
    public <S extends TailoredResume> Page<S> findAll(Example<S> example, Pageable pageable) { return null; }
    @Override
    public <S extends TailoredResume> long count(Example<S> example) { return 0; }
    @Override
    public <S extends TailoredResume> boolean exists(Example<S> example) { return false; }
    @Override
    public <S extends TailoredResume, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) { return null; }
    @Override
    public <S extends TailoredResume> List<S> saveAll(Iterable<S> entities) {
        List<S> result = new ArrayList<>();
        entities.forEach(e -> result.add(save(e)));
        return result;
    }
    @Override
    public List<TailoredResume> findAll(Sort sort) { return findAll(); }
    @Override
    public Page<TailoredResume> findAll(Pageable pageable) { return null; }
}
