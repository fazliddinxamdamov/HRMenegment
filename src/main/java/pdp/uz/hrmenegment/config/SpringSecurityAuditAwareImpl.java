package pdp.uz.hrmenegment.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import pdp.uz.hrmenegment.entity.Employee;
import pdp.uz.hrmenegment.repository.EmployeeRepository;

import java.util.Optional;
import java.util.UUID;

public class SpringSecurityAuditAwareImpl implements AuditorAware<UUID> {
    @Autowired
    EmployeeRepository employeeRepository;

    @Override
    public Optional<UUID> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null
                && authentication.isAuthenticated()
                && !authentication.getPrincipal().equals("anonymousUser")) {
            Employee employee = (Employee) authentication.getPrincipal();
            return Optional.of(employee.getId());
        }
        return Optional.empty();
    }
}
