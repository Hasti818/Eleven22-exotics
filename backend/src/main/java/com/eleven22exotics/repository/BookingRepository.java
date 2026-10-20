package com.eleven22exotics.repository;

import com.eleven22exotics.entity.Booking;
import com.eleven22exotics.entity.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    /** Two ranges overlap when requestedStart < existingEnd AND requestedEnd > existingStart. */
    @Query("select count(b) > 0 from Booking b where b.car.id = :carId and b.status in :statuses "
         + "and :start < b.endDate and :end > b.startDate")
    boolean existsOverlap(@Param("carId") Long carId, @Param("statuses") Collection<BookingStatus> statuses,
                          @Param("start") LocalDate start, @Param("end") LocalDate end);

    List<Booking> findByCarIdAndStatusInOrderByStartDate(Long carId, Collection<BookingStatus> statuses);
    List<Booking> findByUserIdOrderByStartDateDesc(Long userId);
    List<Booking> findAllByOrderByIdDesc();
    boolean existsByCarId(Long carId);
    boolean existsByCarIdAndStatus(Long carId, BookingStatus status);
    long countByStatus(BookingStatus status);
}
