package com.smartresume.tailor.repository;

import com.smartresume.tailor.domain.entity.JobMatch;
import com.smartresume.tailor.domain.enums.MatchStatus;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery;

import java.util.*;
import java.util.function.Function;

public class InMemoryJobMatchRepository implements JobMatchRepository {
    private final Map<UUID, JobMatch> storage = new LinkedHashMap<>();

    @Override
    public List<JobMatch> findByResumeIdOrderByOverallScoreDesc(UUID resumeId) {
        return storage.values().stream()
                .filter(m -> m.getResume() != null && resumeId.equals(m.getResume().getId()))
                .sorted(Comparator.comparingInt(JobMatch::getOverallScore).reversed())
                .toList();
    }

    @Override
    public List<JobMatch> findByStatusOrderByOverallScoreDesc(MatchStatus status) {
        return storage.values().stream()
                .filter(m -> m.getStatus() == status)
                .sorted(Comparator.comparingInt(JobMatch::getOverallScore).reversed())
                .toList();
    }

    @Override
    public Optional<JobMatch> findByResumeIdAndJobId(UUID resumeId, UUID jobId) {
        return storage.values().stream()
                .filter(m -> m.getResume() != null && resumeId.equals(m.getResume().getId()) &&
                             m.getJob() != null && jobId.equals(m.getJob().getId()))
                .findFirst();
    }

    @Override
    public long countByStatus(MatchStatus status) {
        return storage.values().stream().filter(m -> m.getStatus() == status).count();
    }

    @Override
    public <S extends JobMatch> S save(S entity) {
        if (entity.getId() == null) entity.setId(UUID.randomUUID());
        storage.put(entity.getId(), entity);
        return entity;
    }

    @Override
    public Optional<JobMatch> findById(UUID uuid) { return Optional.ofNullable(storage.get(uuid)); }
    @Override
    public List<JobMatch> findAll() { return new ArrayList<>(storage.values()); }
    @Override
    public long count() { return storage.size(); }
    @Override
    public boolean existsById(UUID uuid) { return storage.containsKey(uuid); }
    @Override
    public List<JobMatch> findAllById(Iterable<UUID> uuids) { return List.of(); }
    @Override
    public void deleteById(UUID uuid) { storage.remove(uuid); }
    @Override
    public void delete(JobMatch entity) { storage.remove(entity.getId()); }
    @Override
    public void deleteAllById(Iterable<? extends UUID> uuids) {}
    @Override
    public void deleteAll(Iterable<? extends JobMatch> entities) {}
    @Override
    public void deleteAll() { storage.clear(); }
    @Override
    public void flush() {}
    @Override
    public <S extends JobMatch> S saveAndFlush(S entity) { return save(entity); }
    @Override
    public <S extends JobMatch> List<S> saveAllAndFlush(Iterable<S> entities) { return List.of(); }
    @Override
    public void deleteAllInBatch(Iterable<JobMatch> entities) {}
    @Override
    public void deleteAllByIdInBatch(Iterable<UUID> uuids) {}
    @Override
    public void deleteAllInBatch() {}
    @Override
    public JobMatch getOne(UUID uuid) { return storage.get(uuid); }
    @Override
    public JobMatch getById(UUID uuid) { return storage.get(uuid); }
    @Override
    public JobMatch getReferenceById(UUID uuid) { return storage.get(uuid); }
    @Override
    public <S extends JobMatch> Optional<S> findOne(Example<S> example) { return Optional.empty(); }
    @Override
    public <S extends JobMatch> List<S> findAll(Example<S> example) { return List.of(); }
    @Override
    public <S extends JobMatch> List<S> findAll(Example<S> example, Sort sort) { return List.of(); }
    @Override
    public <S extends JobMatch> Page<S> findAll(Example<S> example, Pageable pageable) { return null; }
    @Override
    public <S extends JobMatch> long count(Example<S> example) { return 0; }
    @Override
    public <S extends JobMatch> boolean exists(Example<S> example) { return false; }
    @Override
    public <S extends JobMatch, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) { return null; }
    @Override
    public <S extends JobMatch> List<S> saveAll(Iterable<S> entities) {
        List<S> result = new ArrayList<>();
        entities.forEach(e -> result.add(save(e)));
        return result;
    }
    @Override
    public List<JobMatch> findAll(Sort sort) { return findAll(); }
    @Override
    public Page<JobMatch> findAll(Pageable pageable) { return null; }
}
