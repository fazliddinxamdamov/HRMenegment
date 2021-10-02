package pdp.uz.hrmenegment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pdp.uz.hrmenegment.entity.EmployeeSalary;


import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

public interface SalaryRepository extends JpaRepository<EmployeeSalary, Long> {
    List<EmployeeSalary> findAllByVerifyingCode(String verifyingCode);

    List<EmployeeSalary> findAllByUpdatedAtBetweenAndEmployeeId(Timestamp updatedAt, Timestamp updatedAt2, UUID employee_id);


}
