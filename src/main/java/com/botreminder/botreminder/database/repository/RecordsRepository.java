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
    List<Records> findAllByNotifiedIsNull();
    List<Records> findAllByNotifiedIsNullAndChatIdIs(long chatId);



    @Query(value = "update  records  set notified ='notified' where chatid =?1 and date=?2 and text =?3", nativeQuery = true)
    void updateNotifiedField(long chatId, Timestamp date, String text);


}
