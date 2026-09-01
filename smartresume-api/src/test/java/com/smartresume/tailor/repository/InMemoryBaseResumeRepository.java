package com.smartresume.tailor.repository;

import com.smartresume.tailor.domain.entity.BaseResume;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery;

import java.util.*;
import java.util.function.Function;

public class InMemoryBaseResumeRepository implements BaseResumeRepository {
    private final Map<UUID, BaseResume> storage = new LinkedHashMap<>();

    @Override
    public List<BaseResume> findByUserIdOrderByCreatedAtDesc(UUID userId) {
        return new ArrayList<>(storage.values());
    }

    @Override
    public Optional<BaseResume> findFirstByOrderByCreatedAtDesc() {
        List<BaseResume> list = new ArrayList<>(storage.values());
        if (list.isEmpty()) return Optional.empty();
        return Optional.of(list.get(list.size() - 1));
    }

    @Override
    public <S extends BaseResume> S save(S entity) {
        if (entity.getId() == null) entity.setId(UUID.randomUUID());
        storage.put(entity.getId(), entity);
        return entity;
    }

    @Override
    public Optional<BaseResume> findById(UUID uuid) { return Optional.ofNullable(storage.get(uuid)); }
    @Override
    public List<BaseResume> findAll() { return new ArrayList<>(storage.values()); }
    @Override
    public boolean existsById(UUID uuid) { return storage.containsKey(uuid); }
    @Override
    public List<BaseResume> findAllById(Iterable<UUID> uuids) { return List.of(); }
    @Override
    public long count() { return storage.size(); }
    @Override
    public void deleteById(UUID uuid) { storage.remove(uuid); }
    @Override
    public void delete(BaseResume entity) { storage.remove(entity.getId()); }
    @Override
    public void deleteAllById(Iterable<? extends UUID> uuids) {}
    @Override
    public void deleteAll(Iterable<? extends BaseResume> entities) {}
    @Override
    public void deleteAll() { storage.clear(); }
    @Override
    public void flush() {}
    @Override
    public <S extends BaseResume> S saveAndFlush(S entity) { return save(entity); }
    @Override
    public <S extends BaseResume> List<S> saveAllAndFlush(Iterable<S> entities) { return List.of(); }
    @Override
    public void deleteAllInBatch(Iterable<BaseResume> entities) {}
    @Override
    public void deleteAllByIdInBatch(Iterable<UUID> uuids) {}
    @Override
    public void deleteAllInBatch() {}
    @Override
    public BaseResume getOne(UUID uuid) { return storage.get(uuid); }
    @Override
    public BaseResume getById(UUID uuid) { return storage.get(uuid); }
    @Override
    public BaseResume getReferenceById(UUID uuid) { return storage.get(uuid); }
    @Override
    public <S extends BaseResume> Optional<S> findOne(Example<S> example) { return Optional.empty(); }
    @Override
    public <S extends BaseResume> List<S> findAll(Example<S> example) { return List.of(); }
    @Override
    public <S extends BaseResume> List<S> findAll(Example<S> example, Sort sort) { return List.of(); }
    @Override
    public <S extends BaseResume> Page<S> findAll(Example<S> example, Pageable pageable) { return null; }
    @Override
    public <S extends BaseResume> long count(Example<S> example) { return 0; }
    @Override
    public <S extends BaseResume> boolean exists(Example<S> example) { return false; }
    @Override
    public <S extends BaseResume, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) { return null; }
    @Override
    public <S extends BaseResume> List<S> saveAll(Iterable<S> entities) {
        List<S> result = new ArrayList<>();
        entities.forEach(e -> result.add(save(e)));
        return result;
    }
    @Override
    public List<BaseResume> findAll(Sort sort) { return findAll(); }
    @Override
    public Page<BaseResume> findAll(Pageable pageable) { return null; }
}
