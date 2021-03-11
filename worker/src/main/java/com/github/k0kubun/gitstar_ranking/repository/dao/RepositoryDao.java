package com.github.k0kubun.gitstar_ranking.repository.dao;

import com.github.k0kubun.gitstar_ranking.model.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.BindBean;
import org.skife.jdbi.v2.sqlobject.SqlBatch;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.BatchChunkSize;
import org.skife.jdbi.v2.sqlobject.customizers.Mapper;
import org.skife.jdbi.v2.sqlobject.stringtemplate.UseStringTemplate3StatementLocator;
import org.skife.jdbi.v2.tweak.ResultSetMapper;
import org.skife.jdbi.v2.unstable.BindIn;

@UseStringTemplate3StatementLocator
public interface RepositoryDao
{
    @SqlQuery("select id, owner_id, name, full_name, description, fork, homepage, stargazers_count, language " +
            "from repositories where id = :id")
    @Mapper(RepositoryMapper.class)
    Repository find(@Bind("id") Integer id);

    @SqlBatch("insert into repositories " +
            "(id, owner_id, name, full_name, description, fork, homepage, stargazers_count, language, created_at, updated_at, fetched_at) " +
            "values (:id, :ownerId, :name, :fullName, :description, :fork, :homepage, :stargazersCount, :language, current_timestamp(0), current_timestamp(0), current_timestamp(0)) " +
            "on conflict (id) do update set " +
            "owner_id=excluded.owner_id, name=excluded.name, full_name=excluded.full_name, description=excluded.description, homepage=excluded.homepage, stargazers_count=excluded.stargazers_count, language=excluded.language, updated_at=excluded.updated_at, fetched_at=excluded.fetched_at")
    @BatchChunkSize(100)
    void bulkInsert(@BindBean List<Repository> repos);

    @SqlQuery("select id, stargazers_count from repositories order by stargazers_count desc, id desc limit :limit")
    @Mapper(RepositoryStarMapper.class)
    List<Repository> starsDescFirstRepos(@Bind("limit") Integer limit);

    @SqlQuery("select id, stargazers_count from repositories where (stargazers_count, id) \\< (:stargazersCount, :id) " +
            "order by stargazers_count desc, id desc limit :limit")
    @Mapper(RepositoryStarMapper.class)
    List<Repository> starsDescReposAfter(@Bind("stargazersCount") Integer stargazersCount, @Bind("id") Long id, @Bind("limit") Integer limit);

    @SqlQuery("select count(1) from repositories")
    int countRepos();

    @SqlQuery("select count(1) from repositories where stargazers_count = :stargazersCount")
    int countReposHavingStars(@Bind("stargazersCount") int stargazersCount);

    @SqlUpdate("delete from repositories where owner_id = :userId")
    long deleteAllOwnedBy(@Bind("userId") Long userId);

    @SqlUpdate("delete from repositories where owner_id = :userId and id not in (<ids>)")
    long deleteAllOwnedByExcept(@Bind("userId") Long userId, @BindIn("ids") List<Long> ids);

    class RepositoryStarMapper
            implements ResultSetMapper<Repository>
    {
        @Override
        public Repository map(int index, ResultSet r, StatementContext ctx)
                throws SQLException
        {
            return new Repository(r.getLong("id"), r.getInt("stargazers_count"));
        }
    }

    class RepositoryMapper
            implements ResultSetMapper<Repository>
    {
        @Override
        public Repository map(int index, ResultSet r, StatementContext ctx)
                throws SQLException
        {
            return new Repository(
                    r.getLong("id"),
                    r.getInt("owner_id"),
                    r.getString("name"),
                    r.getString("full_name"),
                    r.getString("description"),
                    r.getBoolean("fork"),
                    r.getString("homepage"),
                    r.getInt("stargazers_count"),
                    r.getString("language")
            );
        }
    }
}
