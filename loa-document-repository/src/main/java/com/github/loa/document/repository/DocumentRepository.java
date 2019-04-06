package com.github.loa.document.repository;

import com.github.loa.document.repository.domain.DocumentDatabaseEntity;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.cursor.Cursor;

import java.util.List;

@Mapper
public interface DocumentRepository {

    @Insert("INSERT INTO document SET id = #{id}, url = #{url}, crawled_date = NOW(), checksum = #{checksum}, file_size = #{fileSize}, `status` = #{status}, source = #{source}, downloader_version = #{downloaderVersion}")
    void insertDocument(DocumentDatabaseEntity tomeDatabaseEntity);

    @Update("UPDATE document SET `status` = #{status} WHERE id = #{id}")
    void updateStatus(@Param("id") String id, @Param("status") String status);

    @Select("SELECT * FROM document WHERE id = #{id}")
    DocumentDatabaseEntity findById(@Param("id") String id);

    @Select("SELECT * FROM document WHERE `status` = #{status} LIMIT 100")
    List<DocumentDatabaseEntity> findByStatus(@Param("status") String status);

    @Update("UPDATE document SET checksum = #{checksum}, file_size = #{fileSize} WHERE id = #{id}")
    void updateFileSizeAndChecksum(@Param("id") String id, @Param("fileSize") long fileSize, @Param("checksum") String checksum);

    @Select("SELECT * FROM document WHERE file_size = #{fileSize} AND checksum = #{checksum}")
    List<DocumentDatabaseEntity> findByChecksumAndFileSize(@Param("checksum") String checksum, @Param("fileSize") long fileSize);

    @Select("SELECT * FROM document")
    Cursor<DocumentDatabaseEntity> findAll();
}
