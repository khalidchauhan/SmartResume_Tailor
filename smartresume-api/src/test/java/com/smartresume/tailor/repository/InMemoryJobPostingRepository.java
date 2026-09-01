package com.smartresume.tailor.repository;

import com.smartresume.tailor.domain.entity.JobPosting;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery;

import java.util.*;
import java.util.function.Function;

public class InMemoryJobPostingRepository implements JobPostingRepository {
    private final Map<UUID, JobPosting> storage = new LinkedHashMap<>();

    @Override
    public Optional<JobPosting> findByExternalIdAndSource(String externalId, String source) {
        return storage.values().stream()
                .filter(j -> externalId.equals(j.getExternalId()) && source.equals(j.getSource()))
                .findFirst();
    }

    @Override
    public List<JobPosting> findAllByOrderByCreatedAtDesc() {
        List<JobPosting> list = new ArrayList<>(storage.values());
        Collections.reverse(list);
        return list;
    }

    @Override
    public <S extends JobPosting> S save(S entity) {
        if (entity.getId() == null) {
            entity.setId(UUID.randomUUID());
        }
        storage.put(entity.getId(), entity);
        return entity;
    }

    @Override
    public Optional<JobPosting> findById(UUID uuid) {
        return Optional.ofNullable(storage.get(uuid));
    }

    @Override
    public List<JobPosting> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public boolean existsById(UUID uuid) { return storage.containsKey(uuid); }
    @Override
    public List<JobPosting> findAllById(Iterable<UUID> uuids) { return List.of(); }
    @Override
    public long count() { return storage.size(); }
    @Override
    public void deleteById(UUID uuid) { storage.remove(uuid); }
    @Override
    public void delete(JobPosting entity) { storage.remove(entity.getId()); }
    @Override
    public void deleteAllById(Iterable<? extends UUID> uuids) {}
    @Override
    public void deleteAll(Iterable<? extends JobPosting> entities) {}
    @Override
    public void deleteAll() { storage.clear(); }
    @Override
    public void flush() {}
    @Override
    public <S extends JobPosting> S saveAndFlush(S entity) { return save(entity); }
    @Override
    public <S extends JobPosting> List<S> saveAllAndFlush(Iterable<S> entities) { return List.of(); }
    @Override
    public void deleteAllInBatch(Iterable<JobPosting> entities) {}
    @Override
    public void deleteAllByIdInBatch(Iterable<UUID> uuids) {}
    @Override
    public void deleteAllInBatch() {}
    @Override
    public JobPosting getOne(UUID uuid) { return storage.get(uuid); }
    @Override
    public JobPosting getById(UUID uuid) { return storage.get(uuid); }
    @Override
    public JobPosting getReferenceById(UUID uuid) { return storage.get(uuid); }
    @Override
    public <S extends JobPosting> Optional<S> findOne(Example<S> example) { return Optional.empty(); }
    @Override
    public <S extends JobPosting> List<S> findAll(Example<S> example) { return List.of(); }
    @Override
    public <S extends JobPosting> List<S> findAll(Example<S> example, Sort sort) { return List.of(); }
    @Override
    public <S extends JobPosting> Page<S> findAll(Example<S> example, Pageable pageable) { return null; }
    @Override
    public <S extends JobPosting> long count(Example<S> example) { return 0; }
    @Override
    public <S extends JobPosting> boolean exists(Example<S> example) { return false; }
    @Override
    public <S extends JobPosting, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) { return null; }
    @Override
    public <S extends JobPosting> List<S> saveAll(Iterable<S> entities) {
        List<S> result = new ArrayList<>();
        entities.forEach(e -> result.add(save(e)));
        return result;
    }
    @Override
    public List<JobPosting> findAll(Sort sort) { return findAll(); }
    @Override
    public Page<JobPosting> findAll(Pageable pageable) { return null; }
}
