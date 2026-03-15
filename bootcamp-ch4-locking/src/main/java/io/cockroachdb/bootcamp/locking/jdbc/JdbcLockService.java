package io.cockroachdb.bootcamp.locking.jdbc;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.PreparedStatement;
import java.sql.Statement;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Service;

import io.cockroachdb.bootcamp.locking.LockContext;
import io.cockroachdb.bootcamp.locking.LockHolder;
import io.cockroachdb.bootcamp.locking.LockService;
import io.cockroachdb.bootcamp.util.AssertUtils;

/**
 * A simple lock service implementation using a single table as mutex
 * by UPSERT:ing a provisional write intent for each lock then
 * released at transaction commit/rollback time.
 *
 * @author Kai Niemi
 */
@Service
@Profile(value = "jdbclock")
public class JdbcLockService implements LockService {
    private final JdbcTemplate jdbcTemplate;

    public JdbcLockService(@Autowired DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public LockHolder acquireLock(LockContext lockContext) {
        AssertUtils.assertReadWriteTransaction();

        GeneratedKeyHolder holder = new GeneratedKeyHolder();

        jdbcTemplate.update(conn -> {
            PreparedStatement ps = conn.prepareStatement("insert into locks  (name, owner) values (?,?) "
                                                         + "on conflict (name) do "
                                                         + "update set owner=?, updated_at=clock_timestamp() "
                                                         + "returning owner", Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, lockContext.getName());
            ps.setString(2, hostName());
            ps.setString(3, lockContext.getName());
            return ps;
        }, holder);

        String owner = holder.getKeyAs(String.class);

        return new LockHolder(owner);
    }

    @Override
    public void releaseLock(LockHolder lock) {
        // No-op since lock is released when txn goes out of scope
    }

    private String hostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return "n/a";
        }
    }
}
