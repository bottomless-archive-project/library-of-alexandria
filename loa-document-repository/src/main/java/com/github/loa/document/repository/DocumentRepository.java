package com.github.loa.document.repository;

import com.github.loa.document.repository.domain.DocumentDatabaseEntity;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface DocumentRepository {

    @Insert("INSERT INTO document SET id = #{id}, url = #{url}, crawled_date = NOW(), crc = #{crc}, file_size = #{fileSize}, `status` = #{status}, source = #{source}, downloader_version = #{downloaderVersion}")
    void insertDocument(DocumentDatabaseEntity tomeDatabaseEntity);

    @Update("UPDATE document SET `status` = #{status} WHERE id = #{id}")
    void updateStatus(@Param("id") String id, @Param("status") String status);

    @Update("UPDATE document SET crc = #{crc}, file_size = #{fileSize} WHERE id = #{id}")
    void updateFileSizeAndCrc(@Param("id") String id, @Param("fileSize") long fileSize, @Param("crc") String crc);

    @Select("SELECT * FROM document WHERE id = #{id}")
    DocumentDatabaseEntity findById(@Param("id") String id);

    @Select("SELECT * FROM document WHERE file_size = #{fileSize} AND crc = #{crc}")
    List<DocumentDatabaseEntity> findByCrcAndFileSize(@Param("crc") String crc, @Param("fileSize") long fileSize);
}
