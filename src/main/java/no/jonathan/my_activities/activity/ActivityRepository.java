package no.jonathan.my_activities.activity;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface ActivityRepository extends JpaRepository<Activity, Long> {

    Page<ActivityDto> findByUser_EmailAndDateGreaterThanEqualOrderByDateAsc(String email, LocalDate date, Pageable pageable);
}
