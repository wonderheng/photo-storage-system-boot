package top.wonderheng.dao.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import top.wonderheng.common.Binary;
import top.wonderheng.common.Page;
import top.wonderheng.dao.SmallFileDao;
import top.wonderheng.domain.SmallFile;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @BelongsProject: servlet-photo-storage-system
 * @BelongsPackage: top.wonderheng.dao.impl
 * @Author: WonderHeng
 * @CreateTime: 2018-11-14 12:16
 */
@Repository
public class DatabaseSmallDaoImpl implements SmallFileDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Logger logger = LoggerFactory.getLogger(DatabaseSmallDaoImpl.class);

    @Override
    public void save(SmallFile smallFile) {
        String sql = "insert into file_info (id, name, content_type, size, upload_date, md5, content) value (?,?,?,?,?,?,?)";
        try {

            this.jdbcTemplate.update(sql,
                    smallFile.getId(),
                    smallFile.getName(),
                    smallFile.getContentType(),
                    smallFile.getSize(),
                    smallFile.getUploadDate(),
                    smallFile.getMd5(),
                    smallFile.getContent().getData());
        } catch (DataAccessException e) {
            logger.error("Save smallfile occur {} .", e.getMessage());
        }
    }

    @Override
    public void delete(String id) {
        String sql = "delete  from file_info where id = ?";
        try {
            jdbcTemplate.update(sql, id);

        } catch (DataAccessException e) {
            logger.error("Delete smallfile id={} occur {} .", id, e.getMessage());
        }
    }

    @Override
    public SmallFile find(String id) {
        String sql = "select id, name, content_type, size, md5, upload_date, content from file_info where id=?";
        List<SmallFile> smallFiles = this.jdbcTemplate.query(sql, new SmallFileRowMapper(), id);
        if (smallFiles.isEmpty()) {
            return null;
        } else {
            return smallFiles.get(0);
        }
    }

    @Override
    public List<SmallFile> query(Page page) {
        String sql = "select id, name, content_type, size, md5, upload_date, content from file_info limit ? offset ?";
        List<SmallFile> smallFiles = new ArrayList<>();
        try {
            smallFiles = this.jdbcTemplate.query(sql, new SmallFileRowMapper(), page.getPageSize(), page.getSkip());

        } catch (DataAccessException e) {
            logger.error("Query by page {} occur {} .", page, e.getMessage());
        }
        return smallFiles;
    }


    private static class SmallFileRowMapper implements RowMapper<SmallFile> {

        @Override
        public SmallFile mapRow(ResultSet rs, int rowNum) throws SQLException {
            SmallFile smallFile = new SmallFile();
            smallFile.setId(rs.getString("id"));
            smallFile.setName(rs.getString("name"));
            smallFile.setContentType(rs.getString("content_type"));
            smallFile.setMd5(rs.getString("md5"));
            smallFile.setSize(rs.getLong("size"));
            smallFile.setUploadDate(rs.getTimestamp("upload_date").toLocalDateTime());
            smallFile.setContent(new Binary(rs.getBytes("content")));
            return smallFile;
        }
    }
}