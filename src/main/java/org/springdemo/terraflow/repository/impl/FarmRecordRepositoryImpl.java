package org.springdemo.terraflow.repository.impl;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import org.springdemo.terraflow.model.FarmRecord;
import org.springdemo.terraflow.repository.FarmRecordRepository;
import org.springdemo.terraflow.util.FirebaseSync;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class FarmRecordRepositoryImpl implements FarmRecordRepository {

    private static final String NODE = "farmRecords";
    private final DatabaseReference rootRef;

    public FarmRecordRepositoryImpl(FirebaseDatabase firebaseDatabase) {
        this.rootRef = firebaseDatabase.getReference(NODE);
    }

    @Override
    public FarmRecord save(FarmRecord record) {
        boolean isNew = record.getId() == null || record.getId().isBlank();
        DatabaseReference ref = isNew ? rootRef.push() : rootRef.child(record.getId());
        if (isNew) {
            record.setId(ref.getKey());
        }
        FirebaseSync.write(ref, toMap(record));
        return record;
    }

    @Override
    public Optional<FarmRecord> findById(String id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(fromSnapshot(FirebaseSync.read(rootRef.child(id))));
    }

    @Override
    public List<FarmRecord> findByOwnerUsernameOrderByRecordDateDesc(String ownerUsername) {
        DataSnapshot snapshot = FirebaseSync.read(rootRef.orderByChild("ownerUsername").equalTo(ownerUsername));
        List<FarmRecord> records = toList(snapshot);
        records.sort(Comparator.comparing(FarmRecord::getRecordDate, Comparator.nullsLast(Comparator.naturalOrder())).reversed());
        return records;
    }

    @Override
    public void delete(FarmRecord record) {
        if (record == null || record.getId() == null) return;
        FirebaseSync.remove(rootRef.child(record.getId()));
    }

    private List<FarmRecord> toList(DataSnapshot snapshot) {
        List<FarmRecord> records = new ArrayList<>();
        if (snapshot == null || !snapshot.exists()) return records;
        for (DataSnapshot child : snapshot.getChildren()) {
            FarmRecord record = fromSnapshot(child);
            if (record != null) records.add(record);
        }
        return records;
    }

    private Map<String, Object> toMap(FarmRecord record) {
        Map<String, Object> map = new HashMap<>();
        map.put("recordDate", record.getRecordDate() == null ? null : record.getRecordDate().toString());
        map.put("category", record.getCategory());
        map.put("description", record.getDescription());
        map.put("amount", record.getAmount() == null ? null : record.getAmount().toString());
        map.put("ownerUsername", record.getOwnerUsername());
        return map;
    }

    private FarmRecord fromSnapshot(DataSnapshot snapshot) {
        if (snapshot == null || !snapshot.exists()) return null;
        String recordDate = snapshot.child("recordDate").getValue(String.class);
        String amount = snapshot.child("amount").getValue(String.class);
        return FarmRecord.builder()
                .id(snapshot.getKey())
                .recordDate(recordDate == null ? null : LocalDate.parse(recordDate))
                .category(snapshot.child("category").getValue(String.class))
                .description(snapshot.child("description").getValue(String.class))
                .amount(amount == null ? null : new BigDecimal(amount))
                .ownerUsername(snapshot.child("ownerUsername").getValue(String.class))
                .build();
    }
}
