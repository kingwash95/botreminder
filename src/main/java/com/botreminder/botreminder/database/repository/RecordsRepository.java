package com.botreminder.botreminder.database.repository;

import com.botreminder.botreminder.database.entity.Records;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

@Repository
public interface RecordsRepository extends JpaRepository<Records, Long> {

    List<Records> findAllByDate(Timestamp date);


    @Query(value = "select * from records where chatid =?1 and date>current_timestamp", nativeQuery = true)
    List<Records> findRecordsForNotifications(long chatId);


}
