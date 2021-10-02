package pdp.uz.hrmenegment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pdp.uz.hrmenegment.entity.Role;
import pdp.uz.hrmenegment.entity.enums.RoleEnum;


public interface RoleRepository extends JpaRepository<Role, Integer> {
    Role findByRoleEnum(RoleEnum roleEnum);
}
