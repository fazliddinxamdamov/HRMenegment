package pdp.uz.hrmenegment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pdp.uz.hrmenegment.entity.Company;

public interface CompanyRepository extends JpaRepository<Company, Integer> {
    @Query(nativeQuery = true, value = "select count(*)>0 from employee t\n" +
            "join company c on c.id = t.company_id\n" +
            "join employee_role er on t.id = er.employee_id\n" +
            "where er.role_id=2 and c.id =:id")
    boolean hasDirector(Integer id);
}
